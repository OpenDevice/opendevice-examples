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

package io.opendevice;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 21/01/16
 */
public class TestSendResponse extends LocalDeviceManager {

    private AtomicInteger send = new AtomicInteger();
    private AtomicInteger response = new AtomicInteger();
    private AtomicBoolean toggle = new AtomicBoolean(false);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {
        addOutput(Connections.out.usb());
        addConnectionListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {
//                System.out.println("Listener: " + message);
                response.incrementAndGet();
            }
        });

        try {
            connect();
            System.out.println("Connected...");

            while (isConnected()) {

                send.incrementAndGet();

                DeviceCommand command;
                if (toggle.get()) {
                    command = DeviceCommand.ON(1);
                    toggle.set(false);
                } else {
                    command = DeviceCommand.OFF(1);
                    toggle.set(true);
                }

                System.out.println("Send..");
                send(command);
                Thread.sleep(10);
                ResponseCommand response = command.getResponse();

                System.out.println("Stats : [" + this.response.intValue() + "/" + send.intValue() + "] = " + response );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

