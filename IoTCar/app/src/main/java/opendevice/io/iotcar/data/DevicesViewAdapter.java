package opendevice.io.iotcar.data;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.OBDSensor;
import opendevice.io.iotcar.R;

/**
 * Created by ricardo on 14/06/17.
 */

public class DevicesViewAdapter  extends RecyclerView.Adapter<DevicesViewAdapter.ViewHolder> implements OnDeviceChangeListener {
    private List<OBDSensor> sensors;
    private Handler mainThread ;


    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tfLine1;
        public TextView tfLine2;
        public Switch cbState;
        public ImageView icon;
        public ViewHolder(View v) {
            super(v);
            tfLine1 = (TextView) v.findViewById(R.id.firstLine);
            tfLine2 = (TextView) v.findViewById(R.id.secondLine);
            cbState = (Switch) v.findViewById(R.id.cbState);
            icon = (ImageView) v.findViewById(R.id.icon);
        }
    }

    public DevicesViewAdapter(List<OBDSensor> sensors) {
        this.sensors = sensors;
        for (OBDSensor sensor : sensors) {
            sensor.addListener(this);
        }
        mainThread = new Handler(Looper.getMainLooper());
    }



    // Create new views (invoked by the layout manager)
    @Override
    public DevicesViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        DevicesViewAdapter.ViewHolder vh = new DevicesViewAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        OBDSensor obdSensor = sensors.get(position);

        holder.tfLine1.setText(obdSensor.getTitle());
        holder.tfLine2.setText(obdSensor.getValue() + " - Active : " + obdSensor.isEnabled() );

        holder.cbState.setOnCheckedChangeListener(null); // avoid fire previous listener
        holder.cbState.setChecked(obdSensor.isEnabled());

        if(obdSensor.isEnabled()){
            setUnlocked(holder.icon);
        }else{
            setLocked(holder.icon);
        }

        holder.cbState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sensors.get(holder.getAdapterPosition()).setEnabled(isChecked);
                notifyItemChanged(holder.getAdapterPosition());
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sensors.size();
    }


    @Override
    public void onDeviceChanged(final Device device) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                final int i = sensors.indexOf(device);
                 notifyItemChanged(i);
            }
        });
    }

    public static void setLocked(ImageView v) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
        v.setImageAlpha(128);   // 128 = 0.5
    }

    public static void setUnlocked(ImageView v) {
        v.setColorFilter(null);
        v.setImageAlpha(255);
    }
}



