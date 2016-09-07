
// config = Class: br.com.criativasoft.opendevice.core.model.OpenDeviceConfig
function initConfig(){
	config.setDatabaseEnabled(false);
    // config.setCertificateFile("/media/ricardo/Dados/Codidos/Java/Projetos/opendevice-project/ssl/lets/fullchain.crt");
    // config.setCertificateKey("/media/ricardo/Dados/Codidos/Java/Projetos/opendevice-project/ssl/lets/privkey.pem");

    addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-web-view/src/main/webapp");
    addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-clients/opendevice-js/dist");
    addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-examples/benchmark/JavaScript");

//            webscoket.addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-examples/opendevice-access-control-v2/src/main/resources/webapp");
}


function initDevices(){
    addDevice(new Device(1, "Ar-Condicionado JS", Device.DIGITAL));
    addDevice(new Device(2, "Luz Quarto JS", Device.DIGITAL));
}

function initConnecions(){
    // OutputConnections
    // ===============================ar
    addOutput(out.usb()); // Connect to first USB port available
       // addOutput(Connections.out.bluetooth("00:11:09:25:01:42"));
//        addOutput(Connections.out.bluetooth("00:11:06:14:04:57"));
//        addOutput(out.tcp("Controlador-Quarto.local.opendevice"));
//        addOutput(out.tcp("GEDAI_Lock1.local.opendevice"));
//        addOutput(out.tcp("arduino.local:5555"));
//        addOutput(out.tcp("192.168.3.100:8182"));
//        addOutput(out.bluetooth("20:13:01:24:01:93"));
//        addOutput(out.tcp("192.168.4.1:8182"));
}

// Register for Event
onConnected(function(conn){
    print(" >>>> [JS] onConnect:"+conn);
});


// Listener for All Devices
// addListener(new DeviceListener({
//     onDeviceRegistred : function(device){
//         print("new device: " + device);
//     },
//     onDeviceChanged : function(device){
//         print("device change: " + device);
//     }
// }));


// init()
// ====================
initConfig();
initDevices();
initConnecions();