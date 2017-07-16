package opendevice.io.iotcar;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.connection.ConnectionType;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.OBDConnection;
import io.opendevice.ext.obd.OBDSensor;
import io.opendevice.ext.obd.OBDSensorPID;

public class FakeOBDConnection extends OBDConnection {

    private Thread anim;

    public FakeOBDConnection(String name, String urlOrPath, ConnectionType type) {
        super(name, urlOrPath, type);
    }

    @Override
    public void connect() throws ConnectionException {

        setStatus(ConnectionStatus.CONNECTING);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(isAutoScanSensors()){
            attach(OBDSensorPID.ENGINE_LOAD);
            attach(OBDSensorPID.ENGINE_RPM).setEnabled(true);
            attach(OBDSensorPID.SPEED).setEnabled(true);
            attach(OBDSensorPID.THROTTLE_POSITION);
        }

        setStatus(ConnectionStatus.CONNECTED);

        final Random random = new Random();

        anim = new Thread(){
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<? extends Device> devices = new LinkedList<>(getBoardInfo().getDevices());

                while(!isInterrupted()){
                    if(!devices.isEmpty()){
                        int nextInt = random.nextInt(devices.size());
                        if(nextInt > -1 && nextInt < devices.size()){
                            OBDSensor obdSensor = (OBDSensor) devices.get(nextInt);
                            if(obdSensor != null && obdSensor.isEnabled()) {
                                //obdSensor.setActivated(!obdSensor.isActivated());

                                if(obdSensor.getPid() == OBDSensorPID.ENGINE_RPM){
                                    obdSensor.setValue(random.nextInt(4000));
                                }else if(obdSensor.getPid() == OBDSensorPID.SPEED){
                                    obdSensor.setValue(random.nextInt(250));
                                } else{
                                    obdSensor.setValue(random.nextInt(100));
                                }


                                System.out.println("Running devices changes - " + obdSensor.getName() + " = " + obdSensor.getValue());
                            }
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

//        anim.start();

    }

    @Override
    public void disconnect() throws ConnectionException {
        setStatus(ConnectionStatus.DISCONNECTED);
        anim.interrupt();
    }

    @Override
    public void send(Message message) throws IOException {
        //
    }
}
