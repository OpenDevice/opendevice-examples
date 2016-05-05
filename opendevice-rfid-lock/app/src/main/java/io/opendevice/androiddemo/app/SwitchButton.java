package io.opendevice.androiddemo.app;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import io.opendevice.androiddemo.R;


public class SwitchButton extends ImageButton implements DeviceListener, View.OnClickListener {

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
        device.addListener(this);
        this.device = device;
        updateViewState();
    }

    private void updateViewState(){
        if(this.device.getValue() == Device.ON){
            setImageResource(R.drawable.btn_switch_on);
        }else{
            setImageResource(R.drawable.btn_switch_off);
        }
    }

    @Override
    public void onDeviceChanged(final Device changed) {

        Activity context = (Activity) getContext();

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(changed.getUid() == device.getUid()){
                    updateViewState();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        if(this.device.getValue() == Device.OFF){
            device.on();
        }else{
            device.off();
        }

    }
}
