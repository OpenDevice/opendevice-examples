package com.shashi;

import java.io.IOException;
import java.util.HashMap;

import org.parse4j.Parse;
import org.parse4j.ParseCloud;

import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;

/**
 * 
 * @author Sharath MK
 * @date 03/11/2015
 */
public class BulbDemo extends LocalDeviceManager implements DeviceListener {

	public static void main(String[] args) {
		Parse.initialize("c1SNsHTKE682vSrHA4ftzNSnev393Czqr0nvwwgB",
				"QrVkQhLxxKZLzEfR3iaSXDImKcbrwugHOX8tNtPi");
		launch(args);
	}

	public void start() throws IOException {
		Device bulb = new Device(9, Device.DIGITAL);
		connect(out.usb());
		bulb.onChange(this);
	}

	public void onDeviceChanged(Device bulbStateChanged) {
		try {
			// Check bulb status
			if (bulbStateChanged.isOFF()) {
				System.out
						.println("++++++++++++++++++++++++ Bulb is OFF+++++++++++++++++++++");
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("data", "Blub event changed to OFF");
				String result = ParseCloud.callFunction("sendNotification",
						params);
				System.out.println(result);
				// Now you can send notifications to mobile this will be done in
				// next phase
			} else if (bulbStateChanged.isON()) {
				System.out
						.println("++++++++++++++++++++++++ Bulb is ON+++++++++++++++++++++");
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("data", "Blub event changed to ON");
				String result = ParseCloud.callFunction("sendNotification",
						params);
				System.out.println(result);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
