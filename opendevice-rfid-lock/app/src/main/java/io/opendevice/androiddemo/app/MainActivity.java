package io.opendevice.androiddemo.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.androiddemo.R;

import java.io.IOException;

public class MainActivity extends Activity implements ConnectionListener, DeviceListener {

    private LocalDeviceManager manager = new LocalDeviceManager();

    // private static final String DEFAULT_BLUETOOTH = "00:11:06:14:04:57"; // "00:11:09:25:01:42"

    enum ConnectionType { Wifi_Direct, Wifi_LocalServer, Bluetooth, Internet }

    private Snackbar snackbar;
    private SwitchButton btnSwitch;
    private FloatingActionButton btnConnect;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Load defaults
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        btnSwitch = (SwitchButton) findViewById(R.id.btnSwitch1);
        btnConnect = (FloatingActionButton) findViewById(R.id.btnConnect);

        btnSwitch.setEnabled(false);
        btnSwitch.setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);


        findViewById(R.id.btnConfig).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                btnConfigOnClick(view);
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                btnConnectOnClick(view);
            }
        });

        manager.addListener(this);

        String server = preferences.getString("server", null);
        ConnectionType connectionType = ConnectionType.valueOf(preferences.getString("connectionType", null));

        if(!TextUtils.isEmpty(server)){
            connect(server, connectionType);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    private void connect(String server, ConnectionType connectionType) {

        snackbar = Snackbar.make(btnConnect, "Connecting ...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        if (!manager.hasConnections()) {

            if (connectionType == ConnectionType.Wifi_Direct) {
                manager.addOutput(Connections.out.tcp(server));
            }else if (connectionType == ConnectionType.Wifi_LocalServer) {
                manager.addOutput(Connections.out.websocket(server));
            } else if (connectionType == ConnectionType.Bluetooth) {
                manager.addOutput(Connections.out.bluetooth(server));
            }

            manager.addConnectionListener(this);
        }

        Thread connect = new Thread() {
            @Override
            public void run() {
                if (!manager.isConnected()) {
                    try {
                        manager.connect();
                    } catch (final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                snackbar.dismiss();
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Connection Error")
                                        .setMessage(e.getCause().getMessage()).create().show();

                            }
                        });

                    }

                }
            }
        };
        connect.start();

    }

    @Override
    public void connectionStateChanged(final DeviceConnection deviceConnection, final ConnectionStatus status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status == ConnectionStatus.CONNECTED) {
                    if(snackbar != null) snackbar.dismiss();
                    btnSwitch.setEnabled(true);
                    btnSwitch.clearColorFilter();
                    btnConnect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorON)));
                    btnConnect.setImageResource(R.drawable.ic_cloud_done);
                    btnConnect.setRippleColor(Color.RED);

                    try {
                        manager.send(new GetDevicesRequest());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (status == ConnectionStatus.DISCONNECTED || status == ConnectionStatus.FAIL) {
                    if(snackbar != null) snackbar.dismiss();
                    btnSwitch.setEnabled(false);
                    btnSwitch.setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                    btnConnect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorOFF)));
                    btnConnect.setImageResource(R.drawable.ic_cloud_off);
                    btnConnect.setRippleColor(Color.GREEN);
                }
            }
        });

    }
    @Override
    public void onMessageReceived(Message message, DeviceConnection deviceConnection) {

    }

    @Override
    public void onDeviceChanged(Device device) {

    }

    @Override
    public void onDeviceRegistred(Device device) {

        // Automatic discovery device
        // User can also configure DeviceID in settings.

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int deviceID = Integer.parseInt(preferences.getString("deviceID", "0"));

        if(deviceID > 0){
            if(device.getUid() == deviceID) setLockDevice(device);
        }else if(device.getType() == Device.DIGITAL && Device.class.equals(device.getClass())){
            setLockDevice(device);
        }

    }

    private void setLockDevice(Device device){
        btnSwitch.setDevice(device);
        preferences.edit().putString("deviceID", "" + device.getUid()).apply();
    }

    public void btnConnectOnClick(View view)  {

        if ( ! manager.isConnected()) {

            String server = preferences.getString("server", null);
            ConnectionType connectionType = ConnectionType.valueOf(preferences.getString("connectionType", null));

            if(!TextUtils.isEmpty(server)){
                connect(server, connectionType);
            }else{
                btnConfigOnClick(view);
            }

        } else {
            try {
                manager.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public void btnConfigOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String server = intent.getStringExtra(OpenDeviceIntent.DATA_SERVER);
//            boolean status = intent.getBooleanExtra(OpenDeviceIntent.DATA_DISCOVERY_STATUS, false);
//
//            System.out.println("received : " + server);
//            if (status) {
//                connect(server, connectionType);
//            } else {
//                Snackbar.make(btnConnect, "Server not found !", Snackbar.LENGTH_LONG).show();
//            }
//
//        }
//    };

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
