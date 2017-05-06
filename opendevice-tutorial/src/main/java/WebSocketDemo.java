import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.dao.memory.DeviceDaoMemory;
import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.File;


/**
 * <<<<< WARN >>>>> Run using Maven: mvn compile exec:java -Dexec.mainClass=WebSocketDemo
 *
 * Access the URL in the browser: http://localhost:8181<br/>

 * @author Ricardo JL Rufino
 * @date 17/08/2014
 */
public class WebSocketDemo extends LocalDeviceManager {

    public static void main(String[] args) throws Exception {
        new WebSocketDemo();
    }


    public WebSocketDemo() throws Exception {

//        addDevice(new Device(1, "BLUE", DeviceType.DIGITAL));
//        addDevice(new Device(2, "YELLOW", DeviceType.DIGITAL));
//        addDevice(new Device(3, "RED", DeviceType.DIGITAL));
//        addDevice(new Sensor(4, "SW1", DeviceType.DIGITAL));
//        addDevice(new Device(5,"SW2", DeviceType.DIGITAL));

        // setup connection with arduino/hardware
//        addOutput(out.bluetooth("00:11:06:14:04:57"));
        addOutput(out.usb()); // Connect to first USB port available

        String path = getClass().getResource("/").getPath();
        String current = new File(path).getParentFile().getParent();

        // Set IoC/DI Config
        Injector injector = Guice.createInjector(new DependencyConfig());
        GuiceInjectProvider.setInjector(injector);

        // Configure a Websocket interface for receiving commands over HTTP
        IWSServerConnection server = in.websocket(8181);
        server.addWebResource(current + "/src/main/resources/webapp");
        server.addWebResource(current + "/target/classes/webapp"); //  running exec:java

        addInput(server);

        connect(); // Connects all configured connections

        // Enable discovery service  (Allows clients automatically find this server
        getDiscoveryService().listen();
    }

    public static class DependencyConfig extends GuiceModule {

        public void configure(Binder binder) {
            super.configure(binder);
            binder.bind(DeviceDao.class).to(DeviceDaoMemory.class);
//            binder.bind(EntityManager.class).toProvider(HibernateProvider.class);

        }
    }

    }