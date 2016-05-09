package io.opendevice.rfidremote;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Connection;

import br.com.criativasoft.opendevice.android.stream.BluetoothConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.IBluetoothConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sensor;

/**
 * Ricardo JL Rufino on 23/10/15.
 */
public class SoftKeyboard extends InputMethodService implements DeviceListener {

    private static String TAG = "SoftKeyboard";

    private LocalDeviceManager manager;
    private Sensor sensor;
    private static String BLUETOOTH_ID = "00:11:09:25:01:42";
    private static int SENSOR_ID = 1;

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        manager = new LocalDeviceManager();
        sensor = new Sensor(SENSOR_ID, Device.NUMERIC);

        manager.addDevice(sensor);
        manager.addListener(this);
        manager.addConnectionListener(connectionListener);

        if(manager != null && ! manager.isConnected()){
            try {
                BluetoothConnection bluetooth = (BluetoothConnection) Connections.out.bluetooth(BLUETOOTH_ID);
                bluetooth.setContext(getApplicationContext());

                manager.addOutput(bluetooth);
                manager.connect();

                Toast.makeText(this, "Connecting...", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            manager.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connectionStateChanged(DeviceConnection deviceConnection, ConnectionStatus connectionStatus) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if(connectionStatus == ConnectionStatus.CONNECTED){
                Notification mNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText("Connected to: " + BLUETOOTH_ID)
                        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                        .setAutoCancel(true).build();
                notificationManager.notify(0, mNotification);
            }else if(connectionStatus == ConnectionStatus.DISCONNECTED){
                notificationManager.cancelAll();
            }else if(connectionStatus == ConnectionStatus.FAIL){
                Notification mNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText("Connection Fail")
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setAutoCancel(true).build();
                notificationManager.notify(0, mNotification);
            }

        }

        @Override
        public void onMessageReceived(Message message, DeviceConnection deviceConnection) {

            if(message instanceof DeviceCommand){
                DeviceCommand command = (DeviceCommand) message;
                if(command.getType() == CommandType.NUMERIC) {
                    sendKeyboard(command.getValue());
                }
            }

        }
    };

    @Override
    public void onDeviceChanged(Device device) {

//        sendKeyboard(device.getValue());

    }

    private void sendKeyboard(long value){
        InputConnection keyboard = getCurrentInputConnection();
        if (keyboard == null) {
            Toast.makeText(SoftKeyboard.this, "no input !", Toast.LENGTH_SHORT).show();
            return;
        }

        String val = Long.toString(value);

        Log.d(TAG, "Send keys: " + val);

        char[] chars = val.toCharArray();

        KeyCharacterMap CharMap;
        if(Build.VERSION.SDK_INT >= 11) // My soft runs until API 5
            CharMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        else
            CharMap = KeyCharacterMap.load(KeyCharacterMap.ALPHA);

        KeyEvent[] events = CharMap.getEvents(chars);

        for(int i=0; i<events.length; i++) {
            keyboard.sendKeyEvent(events[i]);
        }

        keyboard.performEditorAction(EditorInfo.IME_ACTION_SEND);
//        keyboard.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0));
    }
}
