package opendevice.io.iotcar.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.OBDConnection;
import io.opendevice.ext.obd.OBDSensor;
import opendevice.io.iotcar.R;
import opendevice.io.iotcar.data.DevicesViewAdapter;

public class FragmentDevices extends Fragment implements DeviceListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<OBDSensor> sensors = new ArrayList<>();

    private  BaseDeviceManager deviceManager;
    private OBDConnection connection;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        deviceManager = LocalDeviceManager.getInstance();
        deviceManager.addListener(this);

        connection = deviceManager.getOutputConnections().getConnection(OBDConnection.class);
        if(connection != null && connection.isConnected()) {
            Collection<? extends  Device> devices = connection.getBoardInfo().getDevices();
            for (Device device : devices) {
                sensors.add((OBDSensor) device);
            }
        }

        // specify an adapter (see also next example)
        mAdapter = new DevicesViewAdapter(sensors);
        mRecyclerView.setAdapter(mAdapter);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deviceManager.removeListener(this);
    }


    @Override
    public void onDeviceRegistred(Device device) {
        System.out.println("onDeviceRegistred !! = " + device);
    }

    @Override
    public void onDeviceChanged(Device device) {

        if(device instanceof OBDSensor){
            System.out.println("Device changed !! = " + device);
        }

    }
}
