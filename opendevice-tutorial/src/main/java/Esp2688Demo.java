/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;

import java.io.IOException;

/**
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/WiFi+using+ESP8266
 *
 * On arduino/similar use: (git)/opendevice-hardware-libraries/examples/ESP8266WiFi_AT
 * On Esp2688 use: (git)/opendevice-lib-arduino/examples/ESP8266WiFi
 *
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class Esp2688Demo extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        Device led = new Device(1, Device.DIGITAL);

        connect(out.tcp("ODev-Thing1.local.opendevice")); // local.opendevice is a 'magic' domain

        while(true){
            led.on();
            delay(500);
            led.off();
            delay(500);
        }

    }

}
