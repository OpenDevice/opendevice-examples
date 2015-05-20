package br.com.criativasoft.opendevice.android.btkeyboard;

import android.bluetooth.BluetoothDevice;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import br.com.criativasoft.opendevice.android.stream.BluetoothConnection;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sensor;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 25/10/14.
 */
public class SoftKeyboard extends InputMethodService implements DeviceListener {

    private BaseDeviceManager manager;
    private SnessController controller;

//    @Override
//    public View onCreateInputView() {
//        requestHideSelf(0);
//        View view = super.onCreateInputView();
//        return view;
//    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        System.out.println("onStartInput !!! ");

    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("onCreate !!! ");

        manager = new SimpleDeviceManager();
        controller = new SnessController(1);
        manager.addDevice(controller);
        manager.addListener(this);

        if(manager != null && ! manager.isConnected()){
            try {
                manager.connect(new BluetoothConnection("00:13:03:14:19:07"));

                Toast.makeText(this, "Connected !", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            manager.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void onDeviceChanged(Device device) {

        System.out.println("=== Changed === ");
        InputConnection conn = getCurrentInputConnection();
        if (conn == null) {
            Toast.makeText(this, "no input !", Toast.LENGTH_SHORT).show();
            return;
        }


        List<SnessController.Button> pressed = controller.getPressed(SnessController.Pad1.values());

        // return last pressed buttons back !
        List<SnessController.Button> lastPressed = controller.getLastPressed();
        if(lastPressed != null){
            lastPressed.removeAll(pressed);
            for (SnessController.Button button : lastPressed) {
                System.out.println("Return button: " + button.toString());
                conn.sendKeyEvent(new KeyEvent(android.os.SystemClock.uptimeMillis(),
                        android.os.SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP, button.getButtonCode(), 0));
            }
        }

        for (SnessController.Button button : pressed) {
            System.out.println("Pressed button: " + button.toString());

            conn.sendKeyEvent(new KeyEvent(
                        android.os.SystemClock.uptimeMillis(),
                        android.os.SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_DOWN, button.getButtonCode(), 0
            ));
        }

        controller.setLastPressed(pressed);

    }
}
