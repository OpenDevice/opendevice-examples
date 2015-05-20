package io.opendevice.androiddemo.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import io.opendevice.androiddemo.R;
import io.opendevice.androiddemo.android.OpenDeviceIntent;
import io.opendevice.androiddemo.android.services.DiscoveryService;

public class MainActivity extends Activity implements ConnectionListener {

    private LocalDeviceManager manager = new LocalDeviceManager();
    private Device lamp = new Device(1, DeviceType.DIGITAL);

    SwitchButton btnSwitch;
    Button btnConnect;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    Uri soundON, soundOFF = null;
    Drawable iconOFF, iconON;

    private String server;

    private String AppID = OpenDeviceConfig.LOCAL_APP_ID;

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

		btnSwitch = (SwitchButton) findViewById(R.id.btnSwitch);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        manager.addDevice(lamp);
        btnSwitch.setDevice(lamp);
        lamp.addListener(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                playSound();
            }
        });

        btnSwitch.setEnabled(false);
        btnSwitch.setAlpha(150);


    }



    private void connect(String server){

        this.server = server;

        if(!manager.hasConnections()){
            manager.addOutput(Connections.out.websocket(server));
            manager.addConnectionListener(this);
        }

        Thread connect =  new Thread(){
            @Override
            public void run() {
                if(!manager.isConnected()){
                    try {
                        manager.connect();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Connection error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
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
                    lamp.off();
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
            if (lamp.getValue() == Device.ON) {
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
            // Start discovery process
            IntentFilter discoveryIntent = new IntentFilter(OpenDeviceIntent.SERVER_DISCOVERY_ACTION);
            registerReceiver(broadcastReceiver, discoveryIntent);
            startService(new Intent(this, DiscoveryService.class));

            btnConnect.setText("Connecting ...");
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
        tfDeviceID.setText(""+lamp.getId());

        // if button is clicked, close the custom dialog
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lamp.setId(Integer.parseInt(tfDeviceID.getText().toString()));
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
