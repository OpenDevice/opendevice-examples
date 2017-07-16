package opendevice.io.iotcar.views;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import opendevice.io.iotcar.R;

/**
 * Created by ricardo on 10/07/17.
 */
public class FragmentParking extends Fragment {

    private static final int MAX_RANGE = 200; // 2M

    private TextView tfRight;
    private TextView tfLeft;

    private ProgressBar progRight;
    private ProgressBar progLeft;

    private BaseDeviceManager deviceManager;
    private Device sensorSA;
    private Device sensorSD;

    private static final String SENSOR_SA = "Reverse_SA";
    private static final String SENSOR_SD = "Reverse_SD";

    private Handler mainThread ;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking, container, false);
        tfLeft = (TextView) view.findViewById(R.id.left);
        tfRight = (TextView) view.findViewById(R.id.right);
        progLeft = (ProgressBar) view.findViewById(R.id.progressLeft);
        progRight = (ProgressBar) view.findViewById(R.id.progressRight);

        deviceManager = BaseDeviceManager.getInstance();

        // May be null (try on addListener)
        sensorSA = deviceManager.findDeviceByName(SENSOR_SA);
        sensorSD = deviceManager.findDeviceByName(SENSOR_SD);

        if(sensorSA == null) tfLeft.setText("Offline");

        deviceManager.addListener(deviceListener);
        mainThread = new Handler(Looper.getMainLooper()); // update in ui-thread
        mainThread.post(updateView);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deviceManager.removeListener(deviceListener);
    }

    DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceRegistred(Device device) {

        }

        @Override
        public void onDeviceChanged(Device device) {

            if(device.getName().equals(SENSOR_SA)){
                sensorSA = device;
                mainThread.post(updateView);
            }

            if(device.getName().equals(SENSOR_SD)){
                sensorSD = device;
                mainThread.post(updateView);
            }

        }
    };


    private Runnable updateView = new Runnable() {
        @Override
        public void run() {
            if(sensorSA == null){
                tfLeft.setText("---");
            }else{
                tfLeft.setText(fixRange(sensorSA.getValue()));
                progLeft.setProgress(percentage(sensorSA.getValue()));
                tfLeft.setBackgroundColor(Color.BLACK);
            }

            if(sensorSD == null){
                tfRight.setText("---");
            }else{
                tfRight.setText(fixRange(sensorSD.getValue()));
                progRight.setProgress(percentage(sensorSD.getValue()));
            }
        }
    };

    private String fixRange(long val){
        val = val * 10; // increments of 10

        if(val > MAX_RANGE) return "---";
        if(val < 0) return "---";
        return String.format(String.format("%.2fm", val / 100f));
    }

    private int percentage(long val){
        if(val > MAX_RANGE) return 0;
        if(val <= 0) return 100;

        val = val * 10; // increments of 10cm

        int percent = (int) (((val / (float) MAX_RANGE)) * 100);

        return 100 - percent;
    }

}
