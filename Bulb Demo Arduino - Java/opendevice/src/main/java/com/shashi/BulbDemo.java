package com.shashi;
import java.io.IOException;

import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;

/**
 * 
 * @author Sharath MK
 * @date 03/11/2015
 */
public class BulbDemo extends LocalDeviceManager implements
		DeviceListener {

	public static void main(String[] args) {
		launch(args);
	}

	public void start() throws IOException {
		Device bulb = new Device(9, Device.DIGITAL);
		connect(out.usb());
		bulb.onChange(this);
	}

	public void onDeviceChanged(Device bulbStateChanged) {
		// TODO Auto-generated method stub
		// Check bulb status
		if (bulbStateChanged.isOFF()) {
			System.out.println("++++++++++++++++++++++++ Bulb is OFF+++++++++++++++++++++");
			// Now you can send notifications to mobile this will be done in next phase
		} else if (bulbStateChanged.isON()) {
			System.out.println("++++++++++++++++++++++++ Bulb is ON+++++++++++++++++++++");
		}
	}
}
