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

import io.opendevice.androiddemo.android.OpenDeviceIntent;

/**
 * Service to find the "OpenDevice" servers running on the local network.
 * The search is done using a UDP broadcast
 * @author Ricardo JL Rufino
 */
public class DiscoveryService extends IntentService {

    private static final String TAG = "Discovery";
    private static final String REMOTE_KEY = "DiscKeyValidator";
    private static final int DISCOVERY_PORT = 2562;
    private static final int TIMEOUT_MS = 5000;

    private static final String DISCOVER_SERVER_REQUEST = "DISCOVER_SERVER_REQUEST";
    private static final String DISCOVER_SERVER_RESPONSE = "DISCOVER_SERVER_RESPONSE";


    public DiscoveryService() {
        super("LocalDiscoveryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            sendDiscoveryRequest(socket);
            listenForResponses(socket);
        } catch (IOException e) {
            Log.e(TAG, "Could not send discovery request", e);
        }

    }

    /**
     * Send a broadcast UDP packet containing a request for boxee services to
     * announce themselves.
     *
     * @throws java.io.IOException
     */
    private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
        String data = DISCOVER_SERVER_REQUEST;
        Log.d(TAG, "Send discover request... ");
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),getBroadcastAddress(), DISCOVERY_PORT);
        socket.send(packet);
    }

    /**
     * Listen on socket for responses, timing out after TIMEOUT_MS
     *
     * @param socket
     *          socket on which the announcement request was sent
     * @throws java.io.IOException
     */
    private void listenForResponses(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received response from <<"+packet.getAddress().getHostAddress()+">> " + response);

                if(response.length() != 0 && response.startsWith(DISCOVER_SERVER_RESPONSE)){
                    JSONObject json = (JSONObject) new JSONObject(response.split("=")[1]); // a segunta parte é no formato json.
                    int porta = json.getInt("port");
                    notifyListeners(packet.getAddress().getHostAddress() + ":" + porta, true);
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
            notifyListeners(null, false);

        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        socket.close();
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
