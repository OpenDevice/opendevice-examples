package io.opendevice.androiddemo.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Set;

import br.com.criativasoft.opendevice.connection.discovery.DiscoveryListener;
import br.com.criativasoft.opendevice.connection.discovery.DiscoveryService;
import br.com.criativasoft.opendevice.connection.discovery.NetworkDeviceInfo;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import io.opendevice.androiddemo.android.OpenDeviceIntent;

/**
 * Service to find the "OpenDevice" servers running on the local network.
 * The search is done using a UDP broadcast
 * @author Ricardo JL Rufino
 */
public class DiscoveryIntentService extends IntentService {

    private static final String TAG = "DiscoveryIntentService";

    public DiscoveryIntentService() {
        super("LocalDiscoveryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        DiscoveryService discoveryService = LocalDeviceManager.getInstance().getDiscoveryService();

        try {
            Set<NetworkDeviceInfo> founds = discoveryService.scan(5000, null);

            if(!founds.isEmpty()){
                for (NetworkDeviceInfo device : founds) {
                    if(device.getIp() != null){
                        notifyListeners(device.getIp()+":"+device.getPort(), true);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            notifyListeners(null, false);
        }

    }


    /**
     * Calculate the broadcast IP we need to send the packet along. If we send it
     * to 255.255.255.255, it never gets sent. I guess this has something to do
     * with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager mWifi = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo dhcp = mWifi.getDhcpInfo();
        if (dhcp == null) {
            Log.d(TAG, "Could not get dhcp info");
            return null;
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private void notifyListeners(String server, boolean success){
        Intent broadcastServer = new Intent(OpenDeviceIntent.SERVER_DISCOVERY_ACTION);
        broadcastServer.putExtra(OpenDeviceIntent.DATA_SERVER, server);
        broadcastServer.putExtra(OpenDeviceIntent.DATA_DISCOVERY_STATUS, success);
        sendBroadcast(broadcastServer);
    }
}
