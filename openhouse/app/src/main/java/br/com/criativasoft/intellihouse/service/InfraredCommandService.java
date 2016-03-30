package br.com.criativasoft.intellihouse.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import br.com.criativasoft.intellihouse.IntelliHouseIntent;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.util.DataUtils;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandMapping;
import br.com.criativasoft.opendevice.core.command.CommandMappingStore;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Device;

public class InfraredCommandService extends Service implements ConnectionListener{
	
	private static final String TAG = "InfraredCommandService";
	
	private static final String INFRARED_MAPPING_KEY = "device_infrared_mapping";
	// private static final String INFRARED_MAPPING_KEY = "multimidia_infrared_mapping";
	
	private CommandMapping mapping = new CommandMapping();
	
	private boolean commandActive = true;
	
	private void initTestMapping(){

		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0Xa25d807f), new Device(1));
		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0Xa25d40bf), new Device(2));
		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0Xa25dc03f), new Device(3));
		
		// 	RELOGIO
		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0XF4BA2988), new Device(1)); // BTN:power
		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0XE13DDA28), new Device(1)); // BTN:1
		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0XAD586662), new Device(2)); // BTN:2
		mapping.add( new DeviceCommand(CommandType.ANALOG, -1, 0X273009C4), new Device(3)); // BTN:3
		
	}
	
	@Override
	public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
		
	}

    @Override
	public void onMessageReceived(Message message, DeviceConnection connection) {

        Command command = (Command) message;
		
		if(mapping.isEmpty()){
			initTestMapping();
		}
		
		if(command.getType().equals(CommandType.ANALOG)){
			
			// FIXME: talvez nao precise do CommandMappingStore , (pois o Command, já pode ter o ID do Device real.)
			CommandMappingStore mappingStore = mapping.findMapping(command);
			
			if(mappingStore != null){
				
				Device device = mappingStore.getDevice();
				device = DeviceManagerService.getDeviceByID(device.getUid());
				
				if(device != null){
					
					DeviceCommand targetCommand = new DeviceCommand(CommandType.DIGITAL, device.getUid(), (device.getValue() == Device.VALUE_HIGH ? Device.VALUE_LOW : Device.VALUE_HIGH));
					
				  	Intent intent = new Intent(IntelliHouseIntent.ACTION_SEND);
					intent.putExtra(IntelliHouseIntent.EXTRA_COMMAND, targetCommand);
					intent.putExtra(IntelliHouseIntent.EXTRA_DEVICE_ID, targetCommand.getDeviceID());
					
					sendBroadcast(intent);
					
				}
				
			}else{
				
				long eventtime = SystemClock.uptimeMillis();
				
				DeviceCommand deviceCommand = (DeviceCommand) command;
				
				long cmdCode = deviceCommand.getValue();
				int playerEvent = -1;
				
				switch (DataUtils.longToInt(cmdCode)) {
				case 0xa25d847b: // Meio
					playerEvent = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
					break;
				case 0xa25d8a75: // esqueda
					playerEvent = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
					break;
				case 0xa25db24d:// direita
					playerEvent = KeyEvent.KEYCODE_MEDIA_NEXT;
					break;
				case 0xa25d9867:// voltar rapido.
					playerEvent = KeyEvent.KEYCODE_MEDIA_REWIND;
					break;
				case 0xa25dc837:// avancar rapido.
					playerEvent = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
					break;
				case 0xa25d28d7:
					playerEvent = KeyEvent.KEYCODE_MEDIA_STOP;
					break;
				case 0xa25d01fe:
					playerEvent = KeyEvent.KEYCODE_VOLUME_UP;
					break;
				case 0xa25d817e:
					playerEvent = KeyEvent.KEYCODE_VOLUME_DOWN;
					break;
				default:
					break;
				}
				
				
				// RELOGIO
				// ==========================================
				switch (DataUtils.longToInt(cmdCode)) {
				case 0x68733A46: // BTN: =
					playerEvent = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
					break;
				case 0x189D7928: // ch-
					playerEvent = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
					break;
				case 0x5F12E8C4:// ch+
					playerEvent = KeyEvent.KEYCODE_MEDIA_NEXT;
					break;
				default:
					break;
				}				

				if(playerEvent > 0){
					Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime,KeyEvent.ACTION_DOWN,playerEvent, 0);
					downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
					sendOrderedBroadcast(downIntent, null);

					Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
					KeyEvent upEvent = new KeyEvent(eventtime, eventtime,KeyEvent.ACTION_UP,playerEvent, 0);
					upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
					sendOrderedBroadcast(upIntent, null);
								
				}else{
					if(cmdCode != -1 )
						Toast.makeText(getApplicationContext(),  "Infrared: CMD="+deviceCommand.getValue() + " not registred !", Toast.LENGTH_SHORT).show();
				}
				
				
			}
			
			// Notify Others
//			Intent intent = new Intent(IntelliHouseIntent.EVENT_INFRARED_RECEIVED);
//			intent.putExtra(IntelliHouseIntent.EXTRA_COMMAND, command);
//			intent.putExtra(IntelliHouseIntent.EXTRA_DEVICE_ID, command.getDeviceID());
//			sendBroadcast(intent);		
					
		}
	}
	
	
	 public int onStartCommand(Intent intent, int flags, int startId) {
	        Log.i(TAG, "Received start id " + startId + ": " + intent);
	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	        return START_STICKY;
	    }

	// This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    
	 @Override
	 public IBinder onBind(Intent intent) {
		 return mBinder;
	 }
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	InfraredCommandService getService() {
            return InfraredCommandService.this;
        }
    }
	
	
	public void setCommandActive(boolean commandActive) {
		this.commandActive = commandActive;
	}
	
	public boolean isCommandActive() {
		return commandActive;
	}



}

//