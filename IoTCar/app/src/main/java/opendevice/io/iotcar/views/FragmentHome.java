package opendevice.io.iotcar.views;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.anastr.speedviewlib.base.Speedometer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.TCPConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.OBDConnection;
import io.opendevice.ext.obd.OBDSensor;
import io.opendevice.ext.obd.OBDSensorPID;
import opendevice.io.iotcar.R;
import opendevice.io.iotcar.ui.SensorView;

public class FragmentHome extends Fragment implements ConnectionListener, DeviceListener {

    private FloatingActionButton btnConnect;
    private BaseDeviceManager deviceManager;
    private OBDConnection connection;
//    private TCPConnection connection;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceManager = LocalDeviceManager.getInstance();
        deviceManager.addConnectionListener(this);
        deviceManager.addListener(this);

        connection = deviceManager.getOutputConnections().getConnection(OBDConnection.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(new SensorViewPagerAdapter());
        viewPager.addOnPageChangeListener((ViewPager.OnPageChangeListener) viewPager.getAdapter());

        btnConnect = (FloatingActionButton) v.findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(connection.isConnected()){
                    Toast.makeText(getContext(), "Disconnection ...", Toast.LENGTH_SHORT).show();
                    try {
                        connection.disconnect();
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getContext(), "Connecting ...", Toast.LENGTH_SHORT).show();
                    new ConnectionTask().execute();
                }
            }
        });

        updateConnectButton(connection.getStatus());

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deviceManager.removeConnectionListener(this);
        deviceManager.removeListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // if(!connection.isConnected())  new ConnectionTask().execute();
    }

    private void updateConnectButton(ConnectionStatus status){
        int offLineColor = Color.parseColor("#FF4081");
        int onLineColor = Color.parseColor("#8BC34A");

        switch(status){
            case CONNECTING:
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEF40")));
                btnConnect.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_connection_on));
                break;
            case CONNECTED:
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(onLineColor));
                btnConnect.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_connection_on));
                break;
            case FAIL:
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(offLineColor));
                btnConnect.setImageResource(R.drawable.ic_connection_off);
                break;
            case DISCONNECTED:
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(offLineColor));
                btnConnect.setImageResource(R.drawable.ic_connection_off);
                break;
        }
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, final ConnectionStatus status) {
        if(connection instanceof OBDConnection){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectButton(status);
                }
            });
        }
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

    }

    @Override
    public void onDeviceRegistred(Device device) {

    }


    public class UpdateChartsRunnable implements Runnable {

        public Device device;

        public synchronized void  setDevice(Device device) {
            this.device = device;
        }

        @Override
        public void run() {
            int currentItem = viewPager.getCurrentItem();
            SensorViewPagerAdapter adapter = (SensorViewPagerAdapter) viewPager.getAdapter();
            OBDSensorPID pid = adapter.getItemAt(currentItem);
            OBDSensor sensor = (OBDSensor) device;
            if(sensor.getPid() == pid){
                adapter.updateSensor(currentItem, device);
            }
        }
    }

    // Use single runnable to avoid garbage
    private UpdateChartsRunnable updateChartsRunnable = new UpdateChartsRunnable();

    @Override
    public void onDeviceChanged(final Device device) {
        if(device instanceof OBDSensor){
            updateChartsRunnable.setDevice(device);
            getActivity().runOnUiThread(updateChartsRunnable);
        }
    }


    private class SensorViewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

        private OBDSensorPID[] sensors = new OBDSensorPID[]{
                OBDSensorPID.SPEED,
                OBDSensorPID.ENGINE_RPM,
                OBDSensorPID.ENGINE_LOAD,
                OBDSensorPID.THROTTLE_POSITION
        };

        // Current views in ViewPager (only 2 or 3)
        private Map<Integer, SensorView> currentGauges = new HashMap<>();

        @Override
        public int getCount() {
            return sensors.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            OBDSensorPID sensor = sensors[position];
            SensorView sensorView = new SensorView(getContext(), sensor);
            container.addView(sensorView, 0);
            currentGauges.put(position, sensorView);
            return sensorView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            currentGauges.remove(position);
        }

        public OBDSensorPID getItemAt(int position) {
            return sensors[position];
        }

        public void updateSensor(int currentItem, Device device) {
            SensorView sensorView = currentGauges.get(currentItem);
            if(sensorView != null){
                sensorView.updateData(device);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Set<Integer> pages = currentGauges.keySet();
            for (Integer page : pages) {
                SensorView sensorView = currentGauges.get(page);
                if(position == page){
                    sensorView.enableAnimation(true);
                }else{
                    sensorView.enableAnimation(false);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }



    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... params) {
            try {
                deviceManager.connect();
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
            Toast.makeText(getContext(), "Conected  = " + result, Toast.LENGTH_SHORT).show();
        }
    }
}
