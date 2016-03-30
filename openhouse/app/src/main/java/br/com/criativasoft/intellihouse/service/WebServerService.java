package br.com.criativasoft.intellihouse.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;

import br.com.criativasoft.intellihouse.IntelliHouseIntent;
import br.com.criativasoft.intellihouse.R;
import br.com.criativasoft.intellihouse.view.SectorsActivity;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.nanohttp.server.ControlServer;

public class WebServerService extends Service implements ConnectionListener {
	
	private static final String TAG = "WebServerService";
	private static final int NOTIFICATION_ID = 1000;
	
	private int port = 8081;
	private ControlServer server;
	

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Background service created");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		handleStart(intent);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		stopServer();
		super.onDestroy();
	}
	
	
	private void handleStart(Intent intent){
		
		String action = intent.getAction();
		
		if(action == null) return;
		
		try{
			if(IntelliHouseIntent.ACTION_START_WEBSERVER.equals(action)){
				Log.d(TAG, "ACTION_START_WEBSERVER");
				if(hasWifi()){
					startServer();
					showNotification();
				}else{
					Log.i(TAG, "Wifi is not enabled. Stoping...");
					stopSelf();
				}
			}
			
			if(IntelliHouseIntent.ACTION_STOP_WEBSERVER.equals(action)){
				Log.d(TAG, "ACTION_STOP_WEBSERVER");
				stopServer();
				stopSelf();
			}
		}catch(IOException exception){
			// TODO: como seria a notificação desses erros ?
			Log.e(TAG, exception.getMessage());
		}
		
	}
	
	private void startServer() throws IOException {
		if(! isStartedServer()){
			server = new ControlServer(port);
			server.addListener(this); // Handle in commandReceived
		}
	}
	
	private void stopServer(){
		
		// TODO: nothing to say...
		
	}
	
	public boolean hasWifi(){
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if(mWifi == null) return false;
	    return mWifi.isConnected();
	}
	
	public boolean isStartedServer(){
		return server != null;
	}
	
	private void showNotification(){
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		String ip = BigInteger.valueOf(wifiMgr.getDhcpInfo().netmask).toString();

		Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.server_started_text));
		builder.setContentText("Conectado. IP:" + ip);
		builder.setTicker("IP:" + ip);
		builder.setSmallIcon(R.drawable.icon);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setDefaults(Notification.DEFAULT_VIBRATE);
		builder.setDefaults(Notification.DEFAULT_LIGHTS);
		
//		builder.setVibrate(Notification.DEFAULT_VIBRATE);
		
		
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		PendingIntent intent = PendingIntent.getActivity(this, NOTIFICATION_ID, new Intent(this, SectorsActivity.class), flags);
		builder.setContentIntent(intent);

		notificationManager.notify(NOTIFICATION_ID, builder.getNotification());
		
	}


	public void commandReceived(Command command, DeviceConnection connection) {
    	Log.d(TAG, "Http Command Received: " + command);
		Intent intent = new Intent(IntelliHouseIntent.ACTION_SEND);
		intent.putExtra(IntelliHouseIntent.EXTRA_COMMAND, command);
		
		if(command instanceof DeviceCommand){
			intent.putExtra(IntelliHouseIntent.EXTRA_DEVICE_ID, ((DeviceCommand) command).getDeviceID());
		}
		
		sendBroadcast(intent);
	}

    @Override

    public void onMessageReceived(Message message, DeviceConnection deviceConnection) {
        commandReceived((Command) message, deviceConnection);
    }

    @Override
	public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {

	}


}
