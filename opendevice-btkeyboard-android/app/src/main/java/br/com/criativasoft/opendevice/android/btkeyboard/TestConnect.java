package br.com.criativasoft.opendevice.android.btkeyboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.Set;

import br.com.criativasoft.opendevice.android.stream.BluetoothConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sensor;

public class TestConnect extends ActionBarActivity {

    private DeviceManager manager = new SimpleDeviceManager();
    private SnessController controller = new SnessController(1);

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_connect);

        editText = (EditText) findViewById(R.id.editText);

        // KeyEvent.KEYCODE_BUTTON_B

        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        System.out.println("Paired Devices");
        for (BluetoothDevice device : devices) {
            System.out.println(" - " + device.getAddress());
        }

        manager.addListener(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                System.out.println("!!! Changed !!! ");

                final SnessController.Pad1[] buttons = SnessController.Pad1.values();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (SnessController.Pad1 button : buttons) {
                            if(controller.isPressed(button)){
                                System.out.println("Pressed : " + button);
                                editText.append(button.toString());
                            }else{
                                // conn.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, button.getButtonCode()));
                            }
                        }
                    }
                });

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void connect(View btn){

        try {

            manager.connect(new BluetoothConnection("00:13:03:14:19:07"));

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(View btn){
        try {
            if(manager != null)
                manager.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void on(View btn) throws IOException {

        if(manager.isConnected()){
            System.out.println("send on");
            // led.on();
        }

    }

    public void off(View btn) throws IOException {

        if (manager.isConnected()) {
            System.out.println("send off");
            // led.off();
        }
    }

}
