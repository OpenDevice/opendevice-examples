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

import br.com.criativasoft.opendevice.engine.js.OpenDeviceJSEngine;

/**
 * This example demonstrates how to use javascript to build your application.
 * NOTE: The opendevice-js-engine can be compiled to generate a .exe or jar executable.
 * @author Ricardo JL Rufino
 */
public class JavaScriptDemo {

    public static void main(String[] args)  {

        // To run as application you need call main method.
        // Use scripts form /opendevice-samples/src/main/resources/jsengine

        String path = JavaScriptDemo.class.getResource("/jsengine/BlinkButtonDemo.js").getPath();

        OpenDeviceJSEngine.main(new String[]{path});

        // For JavaFX APP

        OpenDeviceJSEngine.main(new String[]{"-fx", path});

    }
}
