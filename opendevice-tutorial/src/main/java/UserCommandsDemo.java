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

/**
 * Demo using {@link br.com.criativasoft.opendevice.core.command.UserCommand}
 * @author Ricardo JL Rufino
 */
public class UserCommandsDemo extends LocalDeviceManager {

    public static void main(String[] args) throws Exception {
        new UserCommandsDemo();
    }

    public UserCommandsDemo() throws Exception {

        connect(out.usb());

        sendCommand("alertMode","Your String", 5);

        delay(1000);
    }
}