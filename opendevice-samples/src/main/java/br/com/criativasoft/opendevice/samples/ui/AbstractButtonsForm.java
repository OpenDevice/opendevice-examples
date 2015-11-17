/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.samples.ui;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.model.Device;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collection;


public class AbstractButtonsForm extends JFrame implements ConnectionListener , KeyEventDispatcher {

    private DeviceConnection connection;
    private DeviceManager manager = new LocalDeviceManager();
    private boolean sync = false;

	public AbstractButtonsForm(DeviceConnection connection) throws ConnectionException {
		this.init();
        this.setTitle("Controller (JavaSE)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.connection = connection;
        manager.addOutput(connection);
        connection.addListener(this);
    }


    public void connect(){
        try {
            manager.connect();
        } catch (ConnectionException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDevices(Collection<Device> devices){
        for (Device device : devices){
            SwitchButton switchButton = new SwitchButton(device);
            add(switchButton);
        }
        pack(); // force resize
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {
        System.out.println(">>> DeviceConnection = " + status);
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {


		if(message instanceof DeviceCommand){

			DeviceCommand deviceCommand = (DeviceCommand) message;

            // do... something

        }

        if(message instanceof GetDevicesResponse){
            GetDevicesResponse response = (GetDevicesResponse) message;
            if(!sync) {
                sync = true;
                addDevices(response.getDevices());
            }
        }

	}

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {

            int id =  e.getKeyCode() - 48;
            Device device = manager.findDeviceByUID(id);

            if(device != null){
                device.toggle();
            }

        }

        return false;
    }

    public void init(){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Collection<Device> devices = manager.getDevices();

		final JButton btnConnect = new JButton("Disconnect");
        final JButton btnSync = new JButton("Sync");
        super.add(btnConnect);
        super.add(btnSync);
        this.setLocation(150, 150);
        
        this.setLayout(new FlowLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addKeyEventDispatcher(this);

        
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = btnConnect.getText();
                try {
                    if (text.equals("Disconnect")) {
                        connection.disconnect();

                        for (Device device : devices) {
                            device.setValue(0);
                        }

                        btnConnect.setText("Connect");
                    } else {
                        connection.connect();
                        btnConnect.setText("Disconnect");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AbstractButtonsForm.this, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        btnSync.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(manager.isConnected()){
                    try {
                        manager.send(new GetDevicesRequest());
                    } catch (IOException e1) {}
                }
            }
        });
        
        
	}

    public DeviceConnection getConnection() {
        return connection;
    }
}
