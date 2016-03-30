package br.com.criativasoft.intellihouse.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import br.com.criativasoft.intellihouse.Constants;
import br.com.criativasoft.intellihouse.IntelliHouseIntent;
import br.com.criativasoft.intellihouse.R;
import br.com.criativasoft.intellihouse.io.AssertFileHandler;
import br.com.criativasoft.intellihouse.io.IOUtils;
import br.com.criativasoft.intellihouse.view.SectorsActivity;
import br.com.criativasoft.intellihouse.view.SettingsActivity;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.IWSConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sector;
import br.com.criativasoft.opendevice.nanohttp.handlers.DeviceCommandHandler;
import br.com.criativasoft.opendevice.nanohttp.server.WebServerConnection;


public class DeviceManagerService extends Service implements ConnectionListener {
	 
	private static final String TAG = "DeviceManagerService";
	private static final int NOTIFICATION_ID = 1000;
	private static final int NOTIFICATION_STARTED = 1;
	private static final int NOTIFICATION_WEBSERVER = 2;
	
	// TODO: deve ser removido quanto tiver o crud de conecao
	public static String DEFAULT_BLUETOOTH_DEVICE = "00:11:09:25:04:75"; // "00:11:06:14:04:57";
	 
	private LocalDeviceManager manager;

	private String deviceURI = DEFAULT_BLUETOOTH_DEVICE; 
	private int webServerPort = WebServerConnection.DEFAULT_PORT; // TODO: pegar das configurações...
	private boolean webServerEnabled = true;
	
	private static List<Device> devices = new LinkedList<Device>();
	private static List<Sector> sectors = new LinkedList<Sector>();
	
	public SharedPreferences preferences;
	
	private InfraredCommandService infraredCommandService;

	@Override
	public IBinder onBind(Intent ctx) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Starting DeviceManagerService...");
		
		// Monitora comandos recebidos pelo arduino(módulo) ou wifi
		IntentFilter filter = new IntentFilter();
		filter.addAction(IntelliHouseIntent.ACTION_SEND);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(RECEIVER, filter);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener);
        
        String portStr = preferences.getString(SettingsActivity.WEB_SERVICE_PORT, Integer.toString(WebServerConnection.DEFAULT_PORT));
        webServerPort = Integer.parseInt(portStr);
        
        deviceURI = preferences.getString(Constants.Settings.BLUETOOTH_DEFAULT, DEFAULT_BLUETOOTH_DEVICE);
		
        bindDependentServices();
        
		initConnection();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		
		Log.d(TAG, "onDestroy DeviceManagerService...");
		
		unregisterReceiver(RECEIVER);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		
		if(manager != null && manager.isConnected()){
			try {
				manager.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener);
		
		doUnbindDependentServices();
		
		super.onDestroy();
	}
	

	
	private void initConnection(){
		if(manager == null){
			// NOTA: Talvez seja mais apropriado um ConnectionManager mais complexo.
			manager = new LocalDeviceManager();

            manager.addListener(new DeviceListener() {
                @Override
                public void onDeviceChanged(Device device) {
                    // Enviar notificação para os compoentens externos (Ex: Widgets)
                    DeviceCommand command = new DeviceCommand(CommandType.DIGITAL, device.getUid(), device.getValue());
                    Intent intent = new Intent(IntelliHouseIntent.ACTION_SEND);
                    intent.putExtra(IntelliHouseIntent.EXTRA_COMMAND, command);
                    intent.putExtra(IntelliHouseIntent.EXTRA_DEVICE_ID, command.getDeviceID());
                    sendBroadcast(intent);
                }
            });


			
//			ConnectionProperties bluetoothProperties = new ConnectionProperties();
//			bluetoothProperties.put(AmarinoBluetoohConnection.PROP_CONTEXT, getApplicationContext());
//			AmarinoBluetoohConnection bluetoohConnection = (AmarinoBluetoohConnection) connectionFactory.getBluetoothConnection(bluetoothProperties);
//			bluetoohConnection.updateBluetoothState();
//			manager.addConnection(bluetoohConnection);

//			WebServerConnection webServerConnection = new WebServerConnection(webServerPort, getCacheDir());
//			setupWebServerUrls(webServerConnection);
//			
//			manager.addConnection(webServerConnection);
			
			// Connect to remote Server...
			// Se o WIfi estiver habilitado, ele será inicializado pelo EVENTO:NETWORK_STATE_CHANGED_ACTION
			// Isso evita tentar conectar duas vesses.
//			if(IOUtils.hasMobileConnection(this) && !hasWifi()){
				setupRemoteConnection();
//			}
			
			manager.addConnectionListener(this);
			
			BluetoothAdapter.getDefaultAdapter(); // Hack for evict RuntimeException in *BluetoohConnection (http://stackoverflow.com/a/15036421/955857)

		}
		


            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        manager.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),  "ERROR:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            };

        thread.start();
		
		if(hasWifi() && isWebServerEnabled()){
			showNotification(NOTIFICATION_WEBSERVER);
		}else{
			showNotification(NOTIFICATION_STARTED);
		}
	}
	
	private void setupWebServerUrls(WebServerConnection webServerConnection){
        webServerConnection.getServer().addRequestHandler("/view", new AssertFileHandler(this));
        webServerConnection.getServer().addRequestHandler("/device", new DeviceCommandHandler());
        // webServerConnection.getServer().addRequestHandler("/listDevices", new DeviceListRequetHandler(this));
    }

	
	private boolean isWebServerEnabled() {
		return webServerEnabled;
	}


    @Override
    public void onMessageReceived(Message message, DeviceConnection deviceConnection) {
        commandReceived((Command) message, deviceConnection);
    }

    /**
	 * Método chamado quando algum comando for recebido pela Connection.
	 */
	public void commandReceived(Command command, DeviceConnection connection) {
		
		Log.d(TAG, "commandReceived = " + command + ", from = " + connection.getClass().getSimpleName());
		
//		// FIXME: Verificar a possibilidade de implementar um CommandHandler.
//
//		CommandType type = command.getType();
//
//		if(DeviceCommand.isCompatible(type)){
//
//			DeviceCommand deviceCommand = (DeviceCommand) command;
//
//
//			int deviceID = deviceCommand.getDeviceID();
//			Device device = findDevice(deviceID);
//
////            if(device.getCategory() == DeviceCategory.IR_SENSOR){
////                fireInfraRedCommand(command, manager);
////            }
//
//			if(device != null){
//
//				device.setValue(deviceCommand.getValue());
//
//				// TODO: pensar numa maneira de u um BIND para não ter o delay na tela.
//				fireDeviceUpdate(deviceCommand);
//
//			}else{
//				Log.w(TAG, "Device not found. (deviceID = " + deviceID + ")");
//			}
//
//
//			// Envia para as outra conexões
//
//		}else{
//
//			Log.w(TAG, "Command not recognized. (type = " + type + ")");
//
//		}
		

		// broadcastCommand(command, manager);
		
	}
	
	@Override
	public void connectionStateChanged(final DeviceConnection connection, final ConnectionStatus status) {
		// TODO: fire intent !
		
		new Handler(Looper.getMainLooper()).post(new Runnable() {
		    @Override
		    public void run() {
		    	Toast.makeText(DeviceManagerService.this, connection.getClass().getSimpleName() + " - " + status, Toast.LENGTH_SHORT).show();
		    }
		});
	}

	
	private void fireDeviceUpdate(DeviceCommand command){
		Intent intent = new Intent(IntelliHouseIntent.EVENT_DEVICE_UPDATED);
		intent.putExtra(IntelliHouseIntent.EXTRA_COMMAND, command);
		intent.putExtra(IntelliHouseIntent.EXTRA_DEVICE_ID, command.getDeviceID());
		sendBroadcast(intent);	
	}
	
	private void fireInfraRedCommand(Command command, DeviceConnection connection){
		if(infraredBind){
			
			Log.d(TAG, "FireInfraRedCommand :: calling service. ");
			
			if(infraredCommandService != null){
				infraredCommandService.onMessageReceived(command, connection);
			}else{
				Toast.makeText(getApplicationContext(),  "ERROR: InfraredService not Found !", Toast.LENGTH_SHORT).show();
			}
		}
	
	}
	

	/**
	 * Enviar comando para o módulo seje ele Físico ou Remoto
	 * @param command
	 */
	public void send(final Command command) {
		
		AsyncTask<Void, Void, Void> sendTask = new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {

                try {
                    manager.send(command);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

				if(DeviceCommand.isCompatible(command.getType())){
					
					DeviceCommand deviceCommand = (DeviceCommand) command;
					
					int deviceID = deviceCommand.getDeviceID();
					long value = deviceCommand.getValue();
					Device device = findDevice(deviceID);
					
					// Update current value.
					if(device.getValue() != value){
						device.setValue(value);
					}
					
					// TODO: O ideal seria somente notificar se recebesse uma reposta de confirmacao de uma das conexoes.
					fireDeviceUpdate(deviceCommand);
					
					// TODO: Colocar aqui a funcao para atualizar na base de dados
					// saveOrUpdate(device); -- need implement !!
				}
				return null;
			}
			
		};

		
		sendTask.execute(new Void[0]);

	}
	
	public boolean hasWifi(){
	    return IOUtils.hasWifi(this);
	}
	
	public List<Device> getDevices() {
		return DeviceManagerService.getDevicesLoaded();
	}
	
    public static List<Device> getDevicesLoaded() {
    	if(devices == null || devices.isEmpty()) loadData();
		return devices;
	}
    
    public static List<Sector> getSectors() {
    	if(sectors == null || sectors.isEmpty()) loadData();
		return sectors;
	}
    
    public static void stopService(Context context){
		Intent service = new Intent(context, DeviceManagerService.class);
		context.stopService(service);
    }
    
	private void showNotification(int code){
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setSmallIcon(R.drawable.icon);
		builder.setAutoCancel(false);
		builder.setWhen(System.currentTimeMillis());
		builder.setDefaults(Notification.DEFAULT_VIBRATE);
		builder.setDefaults(Notification.DEFAULT_LIGHTS);
		builder.setOngoing(true);
		
		switch (code) {
		case NOTIFICATION_WEBSERVER:
			String ip = IOUtils.getWifiIP(this) + ":" + webServerPort;
			builder.setContentText("Webserver - IP:" + ip);
			builder.setTicker("IP:" + ip);		
			break;
		case NOTIFICATION_STARTED:
			builder.setContentText("Servico Iniciado.");
			break;
		default:
			break;
		}

//		builder.setVibrate(Notification.DEFAULT_VIBRATE);
//
//		int flags = Notification.FLAG_ONGOING_EVENT;
//		flags |= Notification.FLAG_NO_CLEAR;

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		
		PendingIntent intent = PendingIntent.getActivity(this, NOTIFICATION_ID, new Intent(this, SectorsActivity.class), flags);
		builder.setContentIntent(intent);

		notificationManager.notify(NOTIFICATION_ID, builder.getNotification());
		
	}
	
    private BroadcastReceiver RECEIVER = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

//			// Ação recebida dos Componentes da aplicação como Activity e Widgets
//    		if(intent.getAction().equals(IntelliHouseIntent.ACTION_SEND)){
//    			Command command = (Command) intent.getSerializableExtra(IntelliHouseIntent.EXTRA_COMMAND);
//    			send(command);
//    		}
    		
    		// Notificar que o Servidor pode recever chamadas
    		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
    			
    			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
    			
    			if(info != null){
	    			if (info.getDetailedState() == DetailedState.CONNECTED) {
	    				
	    				if(isWebServerEnabled()) showNotification(NOTIFICATION_WEBSERVER);
	    				
	    				updateRemoteServer();
	    				
	    			}else if (info.getDetailedState() == DetailedState.DISCONNECTED) {
	    				showNotification(NOTIFICATION_STARTED);
	    			}
    			}
    			
    		}
    		
    		
    	}
    };
    
    // Listener defined by anonymous inner class.
    public OnSharedPreferenceChangeListener sharedPreferencesListener = new OnSharedPreferenceChangeListener() {        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        	if(SettingsActivity.WEB_SERVICE_PORT.equals(key)){
//                String portStr = preferences.getString(SettingsActivity.WEB_SERVICE_PORT, Integer.toString(WebServerConnection.DEFAULT_PORT));
//                webServerPort = Integer.parseInt(portStr);
//        		WebServerConnection webServerConnection = manager.getConnections(WebServerConnection.class);
//
//        		Log.i(TAG, "Updating WebServerConnection = " + webServerConnection);
//
//        		if(webServerConnection != null){
//        			try {
//						webServerConnection.updatePort(webServerPort);
//						setupWebServerUrls(webServerConnection);
//					} catch (IOException e) {
//						Log.d(TAG, e.getMessage(), e);
//					}
//        		}
        		
        	}
        	
        	if(Constants.Settings.BLUETOOTH_DEFAULT.equals(key)){
//                deviceURI = preferences.getString(Constants.Settings.BLUETOOTH_DEFAULT, DEFAULT_BLUETOOTH_DEVICE);
//                IBluetoothConnection current = manager.getConnection(IBluetoothConnection.class);
//
//        		Log.i(TAG, "Updating AmarinoBluetoohConnection = " + manager);
//
//				// FIXME: codigo não testado....
//				try{
//					if(current != null){
//						if(current.isConnected()) current.disconnect();
//						current.setConnectionURI(deviceURI);
//						current.connect();
//					}
//				}catch (ConnectionException ex){
//					Log.e(TAG, ex.getMessage());
//				}
        		
        	}
        	
        	if(Constants.Settings.ENABLE_REMOTE_SERVER.equals(key) || Constants.Settings.REMOTE_SERVER.equals(key) ){
        		updateRemoteServer();
        	}
        	
            Log.d("debug", "A preference has been changed = " + key);            
        }
    };
    
    private void setupRemoteConnection(){
		boolean enableRemote = preferences.getBoolean(Constants.Settings.ENABLE_REMOTE_SERVER, false);
		
		boolean isEmulator = "goldfish".equals(Build.HARDWARE);
        org.slf4j.Logger log = LoggerFactory.getLogger("MEU");
        log.debug(" ################################################");

        // FIXME: move this
        manager.addOutput(Connections.out.websocket("192.168.3.104:8181"));

//		if(enableRemote /*&& !isEmulator*/){
//			String url = preferences.getString(Constants.Settings.REMOTE_SERVER, null);
//			if(url != null && url.trim().length() != 0){
//				if(! url.startsWith("http")) url = "http://" + url;
//                System.setProperty("java.net.preferIPv6Addresses", "false");
//				manager.addOutput(Connections.out.websocket(url));
//
//			}
//		}
    }
    
    private void updateRemoteServer(){
//		IWSConnection targetConnection = manager.get(IWSConnection.class);
//
//  		boolean enabled = preferences.getBoolean(Constants.Settings.ENABLE_REMOTE_SERVER, false);
//
//  		try {
//	  		if(enabled){
//
//	  			if(targetConnection != null){
//
//	  				String url = preferences.getString(Constants.Settings.REMOTE_SERVER, null);
//
//	  				if(url != null && url.trim().length() != 0){
//
//	  					if(! url.startsWith("http")) url = "http://" + url;
//
//                        // FIXME: CODIGO NÃO TESTADO
//	  					if( ! targetConnection.isConnected()  ){
//	  						Log.d(TAG, "Reconnecting..");
//                            targetConnection.setConnectionURI(url);
//	  						targetConnection.connect();
//	  					}
//
//	  				}
//
//
//	  			}else{ // Enabled, but not Initialized !.
//	  				setupRemoteConnection(); // Setup new manager
//	  			}
//
//	  		}else{ // If NOT ENABLED will disconect !
//
//	  			if(targetConnection != null){
//	  				targetConnection.disconnect();
//	  			}
//
//	  		}
//		} catch (ConnectionException e) {
//			Log.e(TAG, e.getMessage(), e);
//		}
    	
    }
    
    // =====================================================================================
    // START: Service Binding
    // =====================================================================================
    
    void bindDependentServices() {
    	
    	Log.d(TAG, "bindDependentServices...");            
    	 
    	Intent intent = new Intent(this, InfraredCommandService.class);
        bindService(intent, infraredBindConnection, Context.BIND_AUTO_CREATE);
        
        infraredBind = true;
    }

    void doUnbindDependentServices() {
        if (infraredBind) {
            // Detach our existing manager.
            unbindService(infraredBindConnection);
            infraredBind = false;
        }
    }
    
    private boolean infraredBind = false;
    private ServiceConnection infraredBindConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the manager with the service has been established
        	infraredCommandService = ((InfraredCommandService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the manager with the service has been unexpectedly disconnected
        	infraredCommandService = null;
        }
    };

    
    
 // load stub data 
    public static void loadData() {

    	Sector sector = new Sector("Meu Quarto");
    	addDevice(sector, "Ar-Condicionado", 1, DeviceCategory.POWER_SOURCE);
    	addDevice(sector, "Luz Quarto", 2, DeviceCategory.LAMP);
    	addDevice(sector, "Tomada 1", 3, DeviceCategory.POWER_SOURCE);
    	addDevice(sector, "Ventilador", 4, DeviceCategory.FAN);
    	sectors.add(sector);
    	
    	sector = new Sector("Sala");
    	addDevice(sector, "Device 2.1", 4, DeviceCategory.FAN);
    	addDevice(sector, "Device 2.2", 5, DeviceCategory.LAMP);
    	addDevice(sector, "Device 2.3", 6, DeviceCategory.POWER_SOURCE);
    	sectors.add(sector);
    	
    	sector = new Sector("Cozinha");
    	addDevice(sector, "Device 3.1", 7, DeviceCategory.POWER_SOURCE);
    	addDevice(sector, "Device 3.2", 8, DeviceCategory.LAMP);
    	addDevice(sector, "Device 3.3", 9, DeviceCategory.FAN);
    	sectors.add(sector);
    }
    
    
    // load stub data
    private static Device addDevice(Sector sector, String name, int id, DeviceCategory category){
    	
    	Device device = new Device(id);
    	device.setName(name);
//    	if(DeviceCategory.IR_SENSOR.getCode() == category.getCode()){
//    		device.setType(DeviceType.ANALOG);
//    	}else{
    		device.setType(DeviceType.DIGITAL);
//    	}
    	
    	device.setCategory(category);
    	
    	sector.getDevices().add(device);
    	
    	devices.add(device);


    	return device;
    	
    }


	public Device findDevice(int deviceID) {
		return BaseDeviceManager.getInstance().findDeviceByUID(deviceID);
	}


}
