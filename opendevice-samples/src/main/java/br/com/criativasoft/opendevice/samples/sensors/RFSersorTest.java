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

package br.com.criativasoft.opendevice.samples.sensors;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.command.SimpleCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.listener.impl.DeviceMappingListener;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 03/11/15
 */
public class RFSersorTest extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    private AtomicInteger responses = new AtomicInteger();

    public void start() throws IOException {

        Device rf = new Sensor(1, Device.NUMERIC);
        rf.setName("RF Sensor");

        Sensor sensor1 = new Sensor(10, Device.DIGITAL);
        sensor1.setName("Switch1 - Value");
        sensor1.setValue(22015);

        // Manual.
        Sensor sensor2 = new Sensor(22015, Device.DIGITAL);
        sensor2.setName("Switch2 - ID");

        rf.addListener(new DeviceMappingListener(DeviceMappingListener.MAP_VALUE_TO_ID));

        addListener(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                System.out.println("DEVICE CHANGE :" + device.getName() + ", value: " + device.getValue());
            }

            @Override
            public void onDeviceRegistred(Device device) {

            }
        });

        connect(Connections.out.usb());

//        addFilter(new DeviceMappingListener(RFReadCommand.class));

        addConnectionListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {

                if (message instanceof ResponseCommand) {
                    int i = responses.incrementAndGet();
                    System.out.println("Responses: " + i + " of " + getCommandDelivery().getCmdCount());
                } else if (message instanceof SimpleCommand) {
                    System.out.println("Command: " + message.getClass() + ", value: " + ((SimpleCommand) message).getValue());
                }else{
                    System.out.println("Command: " + message);
                }
            }
        });

        while(isConnected()){
//            led.on();
            delay(1000);
//            led.off();
        }


    }
}
