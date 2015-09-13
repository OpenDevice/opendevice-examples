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

import br.com.criativasoft.opendevice.connection.server.TCPServerConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Run using IntelliJ or mvn:exec
 * @author Ricardo JL Rufino
 * @date 05/09/15
 */
public class SlidersForm {
    private JPanel rootPanel;
    private List<JSlider> sliders;
    private List<JLabel> labels;

    private JSlider slider4;
    private JSlider slider5;
    private JSlider slider3;
    private JSlider slider2;
    private JSlider slider1;
    private JLabel lab1;
    private JLabel lab2;
    private JLabel lab3;
    private JLabel lab4;
    private JLabel lab5;

    private LocalDeviceManager manager= new LocalDeviceManager();

    List<Device> devices;
    Device finger1 = new Device(10, Device.ANALOG);
    Device finger2 = new Device(11, Device.ANALOG);
    Device finger3 = new Device(12, Device.ANALOG);
    Device finger4 = new Device(13, Device.ANALOG);
    Device finger5 = new Device(14, Device.ANALOG);

    public SlidersForm() {

        manager.addInput(new TCPServerConnection(8182));

        sliders = Arrays.asList(slider1, slider2, slider3, slider4, slider5);
        labels = Arrays.asList(lab1, lab2, lab3, lab4, lab5);
        devices = Arrays.asList(finger1, finger2,finger3, finger4, finger5);

        for (JSlider slider : sliders) {
            slider.setValue(0);
            slider.setMinimum(0);
            slider.setMaximum(360);
            slider.addChangeListener(changeListener);
        }

        try {
            manager.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            int indexOf = sliders.indexOf(source);

            Device device = devices.get(indexOf);
            JLabel label = labels.get(indexOf);
            device.setValue(source.getValue());
            label.setText(Integer.toString(source.getValue()));
        }
    };


    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        JFrame frame = new JFrame("Sliders - OpenDevice (Java)");
        frame.setContentPane(new SlidersForm().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


}
