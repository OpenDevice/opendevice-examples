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

package br.com.criativasoft.opendevice.samples;

import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.event.EventException;
import br.com.criativasoft.opendevice.core.event.EventHookManager;
import br.com.criativasoft.opendevice.engine.js.JavaScriptEventHandler;

import java.io.File;
import java.io.IOException;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 30/08/15.
 */
public class JavaScriptHooksDemo extends LocalDeviceManager {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

        // addInput(in.rest(8181));

        connect(out.usb());

        String path = JavaScriptDemo.class.getResource("/jsengine/hooks").getPath();
        try {
            getEventManager().scanHooks(new File(path), ".js");
        } catch (EventException e) {
            e.printStackTrace();
        }


    }
}
