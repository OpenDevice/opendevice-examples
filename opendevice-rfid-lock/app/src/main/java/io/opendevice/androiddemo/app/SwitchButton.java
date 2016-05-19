package io.opendevice.androiddemo.app;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.androiddemo.R;


public class SwitchButton extends ImageButton implements OnDeviceChangeListener, View.OnClickListener {

    private Device device;

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setImageResource(R.drawable.btn_switch_off);
    }

    public void setDevice(Device device) {
        this.device = device;
        device.addListener(this);
        updateViewState();
    }

    public Device getDevice() {
        return device;
    }

    private void updateViewState(){

        if(device == null) return;

        Activity context = (Activity) getContext();

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(getDevice().isON()){
                    setImageResource(R.drawable.btn_switch_on);
                }else{
                    setImageResource(R.drawable.btn_switch_off);
                }
            }
        });
    }

    @Override
    public void onDeviceChanged(final Device changed) {
        updateViewState();
    }

    @Override
    public void onClick(View v) {

        if(device == null){
            System.err.println("Device not configured !");
            return;
        }

        if(this.device.isOFF()){
            device.on();
        }else{
            device.off();
        }

    }
}
