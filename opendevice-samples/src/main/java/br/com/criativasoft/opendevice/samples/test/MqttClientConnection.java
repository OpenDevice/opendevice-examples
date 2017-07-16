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

package br.com.criativasoft.opendevice.samples.test;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.URIBasedConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.CommandException;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.webclient.WebSocketClientConnection;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 19/10/16
 */
public class MqttClientDev extends AbstractConnection implements DeviceConnection, URIBasedConnection /*implements IWSConnection*/ {

    private String url;
    private MqttClient client;
    private MqttConnectOptions connectOptions;
    int qos = 2;
    private String topic;
    private String moduleName = "FakeDevice";


    private static final Logger log = LoggerFactory.getLogger(WebSocketClientConnection.class);

    public MqttClientDev(){

    }

    public MqttClientDev(String url) {
        setConnectionURI(url);
    }

    @Override
    public void setConnectionURI(String uri) {
        this.url = uri;
    }


    @Override
    public String getConnectionURI() {
        return  this.url;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("connecting...");

            if(!isConnected()){

                initConnection(); // Setup

                client.connect(connectOptions);

                String apiKey = TenantProvider.getCurrentID();
                client.subscribe(apiKey + "/in/" + moduleName);

                setStatus(ConnectionStatus.CONNECTED);

            }
        } catch (Exception e) {
            setStatus(ConnectionStatus.FAIL);
            throw new ConnectionException(e);
        }

    }

    public void reconnectTo(String url) throws ConnectionException{
        log.debug("re-connect to: " + this.url);

        if(isConnected()) disconnect();

        this.url = url;

        connect();

    }

    @Override
    public void disconnect() throws ConnectionException {
        log.debug("disconnecting... (isConnected: "+isConnected()+")");

        if(isConnected()){
            // Send CLOSE request..
            try {
                client.disconnect();
                setStatus(ConnectionStatus.DISCONNECTED);
            } catch (MqttException e) {
                log.error(e.getMessage(), e);
            }
        }else{ // set 'disconnected' in case of previous connection fail.
            setStatus(ConnectionStatus.DISCONNECTED);
        }
        client = null;
    }

    @Override
    public boolean isConnected() {
        return client != null && (client.isConnected());
    }

    @Override
    public void send(Message message) throws IOException {
        if(isConnected()){
            byte[] bytes = getSerializer().serialize(message);
            MqttMessage msg = new MqttMessage(bytes);
            msg.setQos(qos);
            try {
                client.publish(topic, msg);
            } catch (MqttException e) {
                throw new CommandException(e);
            }
        }else{
            log.warn("Can't send command, not Connected !");
        }
    }

    private void initConnection() throws IOException{
        if(client == null){

            try {

                // Execute autentication using Http Request

                String apiKey = TenantProvider.getCurrentID();
                String clientID = apiKey + "/" + moduleName;

                client = new MqttClient(getConnectionURI(), clientID, new MemoryPersistence());
                connectOptions = new MqttConnectOptions();
                connectOptions.setCleanSession(true);
                connectOptions.setUserName(apiKey);
                connectOptions.setPassword("x".toCharArray());

                MessageSerializer serializer = getSerializer();

                if(serializer instanceof CommandStreamSerializer){
                    ((CommandStreamSerializer) serializer).setSendTerminator(false);
                }

                topic = apiKey + "/out/" + moduleName;

            } catch (MqttException e) {
                e.printStackTrace();
            }


        }
    }

}
