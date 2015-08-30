import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;

import java.io.IOException;

/**
 *
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/A.+First+Steps+with+OpenDevice
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 *
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkDeviceDemo extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        Device led = new Device(1, Device.DIGITAL);

        connect(out.usb()); // Connect to first USB port available

        while(true){
            led.on();
            delay(500);
            led.off();
            delay(500);
        }
    }
}


