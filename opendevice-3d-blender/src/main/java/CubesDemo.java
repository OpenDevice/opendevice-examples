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
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.io.IOException;

public class CubesDemo extends LocalDeviceManager {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

//
        addDevice(new Device(1, DeviceType.DIGITAL));
        addDevice(new Device(2, DeviceType.DIGITAL));
        addDevice(new Device(3, DeviceType.DIGITAL));
        addDevice(new Sensor(4, DeviceType.DIGITAL));
        addDevice(new Sensor(5, DeviceType.DIGITAL));


        addListener(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                System.out.println("Listener - Device changed : " + device.getId() + " - " + device.getValue());
            }
        });


        addInput(Connections.in.tcp(8182));

        connect(Connections.out.bluetooth("00:11:06:14:04:57"));
        // connect(Connections.out.usb());
    }

}
