package io.opendevice.androiddemo.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import br.com.criativasoft.opendevice.android.stream.BluetoothConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import io.opendevice.androiddemo.R;
import io.opendevice.androiddemo.android.OpenDeviceIntent;

public class MainActivity extends Activity implements ConnectionListener {

    private LocalDeviceManager manager = new LocalDeviceManager();
    private Device lock = new Device(2, DeviceType.DIGITAL);

    private static final String DEFAULT_BLUETOOTH = "00:11:06:14:04:57"; // "00:11:09:25:01:42"

    enum ConnectionType {USB, BLUETOOTH, WIFI, CLOUD}

    SwitchButton btnSwitch;
    Button btnConnect;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    Uri soundON, soundOFF = null;
    Drawable iconOFF, iconON;

    private String server;

    private ConnectionType connectionType = ConnectionType.WIFI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        soundON = Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.light_switch_on);
        soundOFF = Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.light_switch_off);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        iconOFF = getResources().getDrawable(android.R.drawable.button_onoff_indicator_off);
        iconON = getResources().getDrawable(android.R.drawable.button_onoff_indicator_on);

		btnSwitch = (SwitchButton) findViewById(R.id.btnSwitch1);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        ((SwitchButton) findViewById(R.id.btnSwitch1)).setDevice(lock);

        lock.addListener(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                // playSound();
            }
        });

        btnSwitch.setEnabled(false);
        btnSwitch.setAlpha(150);

    }



    private void connect(String server){

        this.server = server;

        if(!manager.hasConnections()){

            if(connectionType == ConnectionType.WIFI){

                if(server.contains("8182") || server.contains("local.opendevice")){ // Hardware
                    manager.addOutput(Connections.out.tcp(server));
                }else{ // LocalServer
                    manager.addOutput(Connections.out.websocket(server));
                }


            }else if(connectionType == ConnectionType.BLUETOOTH){
                BluetoothConnection bluetooth = Connections.out.bluetooth(server);
                bluetooth.setContext(this);
                manager.addOutput(bluetooth);
            }

            manager.addConnectionListener(this);
        }

        Thread connect =  new Thread(){
            @Override
            public void run() {
                if(!manager.isConnected()){
                    try {
                        manager.connect();
                    } catch (final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Connection error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                }
            }
        };
        connect.start();

    }

    @Override
    public void connectionStateChanged(DeviceConnection deviceConnection, final ConnectionStatus status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(status == ConnectionStatus.CONNECTED){
                    btnConnect.setText("Connected :" + server);
                    btnConnect.setCompoundDrawablesWithIntrinsicBounds(iconON, null, null, null);
                    btnSwitch.setEnabled(true);
                    btnSwitch.setAlpha(255);
                }else if(status == ConnectionStatus.DISCONNECTED || status == ConnectionStatus.FAIL){
                    btnSwitch.setEnabled(false);
                    lock.off();
                    btnConnect.setText("Connect");
                    btnConnect.setCompoundDrawablesWithIntrinsicBounds(iconOFF, null, null, null);
                }
            }
        });

    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection deviceConnection) {

    }

	/*
	 * Playing sound
	 * will play button toggle sound on flash on / off
	 * */
	private void playSound()  {
        try {

            mMediaPlayer.reset();
            if (lock.getValue() == Device.ON) {
                mMediaPlayer.setDataSource(this, soundOFF);
            } else {
                mMediaPlayer.setDataSource(this, soundON);
            }
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

	}


    public void btnConnectOnClick(View view) throws IOException {

        if(!manager.isConnected()){

            if(connectionType == ConnectionType.WIFI){

                // Start discovery process
//                IntentFilter discoveryIntent = new IntentFilter(OpenDeviceIntent.SERVER_DISCOVERY_ACTION);
//                registerReceiver(broadcastReceiver, discoveryIntent);
//                startService(new Intent(this, DiscoveryIntentService.class));

//                connect("192.168.3.105:8181");
                connect("192.168.4.1:8182");
            }else if(connectionType == ConnectionType.BLUETOOTH){
                connect(DEFAULT_BLUETOOTH); // FIXME: USE FROM PAIRED DEVICES.
                btnConnect.setText("Connecting ...");
            }

        }else{
            manager.disconnect();
        }


    }

    public void btnConfigOnClick(View view){

        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.preferences);

        dialog.setTitle("Config");

        // set the custom dialog components
        final EditText tfServer = (EditText) dialog.findViewById(R.id.tfServer);
        final EditText tfDeviceID = (EditText) dialog.findViewById(R.id.tfDeviceID);

        Button btnSave = (Button) dialog.findViewById(R.id.btnSaveConfig);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        tfServer.setText("192.168.");
        tfDeviceID.setText("" + lock.getId());

        // if button is clicked, close the custom dialog
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock.setId(Integer.parseInt(tfDeviceID.getText().toString()));
                if (tfServer.getText().length() > 0) {
                    if (!manager.isConnected()) {
                        connect(tfServer.getText().toString());
                    }
                }
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String server = intent.getStringExtra(OpenDeviceIntent.DATA_SERVER);
            boolean status = intent.getBooleanExtra(OpenDeviceIntent.DATA_DISCOVERY_STATUS, false);

            System.out.println("received : " + server);
            if(status){
                connect(server);
            }else{
                btnConnect.setText("Server not found !");
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            manager.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
