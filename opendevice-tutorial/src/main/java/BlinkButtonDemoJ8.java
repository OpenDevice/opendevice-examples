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
import br.com.criativasoft.opendevice.core.model.Sensor;

/**
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class BlinkButtonDemoJ8 extends LocalDeviceManager{

    public static void main(String[] args) throws Exception {
        new BlinkDeviceDemo();
    }

    public BlinkButtonDemoJ8() throws Exception {

        final Device led = new Device(1, Device.DIGITAL);
        final Device btn = new Sensor(2, Device.DIGITAL);

        connect(out.bluetooth("00:13:03:14:19:07"));

        btn.onChange(device -> {
            if(btn.isON()){
                led.on();
            }else{
                led.off();
            }
        });
    }
}
