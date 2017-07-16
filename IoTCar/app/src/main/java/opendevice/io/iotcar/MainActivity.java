package opendevice.io.iotcar;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.connection.ConnectionType;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.OBDConnection;
import opendevice.io.iotcar.utils.Constansts;
import opendevice.io.iotcar.utils.WifiBroadcastReceiver;
import opendevice.io.iotcar.views.FragmentDevices;
import opendevice.io.iotcar.views.FragmentHome;
import opendevice.io.iotcar.views.FragmentParking;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private OBDConnection obd;

    private DeviceManager deviceManager;

//    private final String BT_ADDR = "48:5A:B6:F4:D9:00"; // PC
    private final String BT_ADDR = "00:1D:A5:68:98:8D"; // OBD ADAPTER

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        deviceManager = new LocalDeviceManager();
        ODev.getConfig().setAutoRegisterLocalDevice(false); // ignore register local devices/variables
        ODev.getConfig().setRemoteIDGeneration(false); // not generate local IDs

        obd = new FakeOBDConnection("IotCar", BT_ADDR, ConnectionType.BLUETOOTH);
//        obd = new OBDConnection("IotCar", BT_ADDR, ConnectionType.BLUETOOTH);
        obd.setAutoScanSensors(true);
        obd.setEnableAllSensors(true);
        deviceManager.addOutput(obd);
        deviceManager.addOutput(Connections.out.tcp("192.168.4.1:8182"));
        deviceManager.addListener(deviceListener);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new FragmentHome());
        transaction.commit();


        if(getIntent().getAction().equals(Constansts.ACTION_CAR_DETECTED)){
            new ConnectionTask().execute();
        }

        // Monitor Wifi
//        registerReceiver(new WifiBroadcastReceiver(), new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home :
                    selectedFragment = new FragmentHome();
                    break;
                case R.id.navigation_sensors:
                    selectedFragment = new FragmentDevices();
                    break;
                case R.id.navigation_settings:
                    // selectedFragment = new FragmentHome();
                    break;
                default:
                    return false;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.commit();

            return true;
        }

    };


    private DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceRegistred(Device device) {
            System.err.println("onDeviceRegistred = "  + device);
        }

        @Override
        public void onDeviceChanged(Device device) {
            if(device.getName().equals("Reverse")){
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(device.isON()){
                    transaction.replace(R.id.frame_layout, new FragmentParking());
                }else{
                    transaction.replace(R.id.frame_layout, new FragmentHome());
                }
                transaction.commit();

            }
        }
    };

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... params) {
            try {
                if(deviceManager.isConnected()) deviceManager.connect();
            } catch (ConnectionException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return deviceManager.isConnected();
        }

        protected void onPostExecute(Boolean result) {

        }
    }

}
