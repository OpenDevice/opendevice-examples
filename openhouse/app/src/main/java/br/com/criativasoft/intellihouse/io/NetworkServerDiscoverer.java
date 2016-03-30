package br.com.criativasoft.intellihouse.io;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


/**
 * This class tries to send a broadcast UDP packet over your wifi network to discover the Servers. 
 */
public class NetworkServerDiscoverer extends Thread {
  private static final String TAG = "Discovery";
  private static final String REMOTE_KEY = "DiscKeyValidator";
  private static final int DISCOVERY_PORT = 2562;
  private static final int TIMEOUT_MS = 3000;
  
  private static final String DISCOVER_SERVER_REQUEST = "DISCOVER_SERVER_REQUEST";
  private static final String DISCOVER_SERVER_RESPONSE = "DISCOVER_SERVER_RESPONSE";
  
  private WifiManager mWifi;
  private DiscoveryListener listener;

  public static interface DiscoveryListener {
    void onDiscovererServer(String server);
    void onDiscovererTimeout();
  }

  public NetworkServerDiscoverer(WifiManager wifi) {
    mWifi = wifi;
  }

  public void run() {
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
   * @throws IOException
   */
  private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
    // String data = String.format("<cmd type=\"discover\" client=\"android\" challenge=\"%s\" signature=\"%s\"/>",mChallenge, getSignature(mChallenge));
	String data = DISCOVER_SERVER_REQUEST;  
    Log.d(TAG, "Send discover request... ");
    DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),getBroadcastAddress(), DISCOVERY_PORT);
    socket.send(packet);
  }

  /**
   * Calculate the broadcast IP we need to send the packet along. If we send it
   * to 255.255.255.255, it never gets sent. I guess this has something to do
   * with the mobile network not wanting to do broadcast.
   */
  private InetAddress getBroadcastAddress() throws IOException {
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

  /**
   * Listen on socket for responses, timing out after TIMEOUT_MS
   * 
   * @param socket
   *          socket on which the announcement request was sent
   * @throws IOException
   */
  private void listenForResponses(DatagramSocket socket) throws IOException {
    byte[] buf = new byte[1024];
    try {
      while (true) {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String response = new String(packet.getData(), 0, packet.getLength());
        Log.d(TAG, "Received response from <<"+packet.getAddress().getHostAddress()+">> " + response);
        
        if(listener != null && response.length() != 0 && response.startsWith(DISCOVER_SERVER_RESPONSE)){
        	JSONObject json = (JSONObject) new JSONObject(response.split("=")[1]); // a segunta parte é no formato json.
        	int porta = json.getInt("port");
        	listener.onDiscovererServer(packet.getAddress().getHostAddress() + ":" + porta);
        	 break;
        }
      }
    } catch (SocketTimeoutException e) {
      Log.d(TAG, "Receive timed out");
      if(listener != null) listener.onDiscovererTimeout();
      
    } catch (JSONException e) {
    	Log.e(TAG, e.getLocalizedMessage(), e);
	}
    
    socket.close();
  }

//  /**
//   * Calculate the signature we need to send with the request. It is a string
//   * containing the hex md5sum of the challenge and REMOTE_KEY.
//   * 
//   * @return signature string
//   */
//  private String getSignature(String challenge) {
//    MessageDigest digest;
//    byte[] md5sum = null;
//    try {
//      digest = java.security.MessageDigest.getInstance("MD5");
//      digest.update(challenge.getBytes());
//      digest.update(REMOTE_KEY.getBytes());
//      md5sum = digest.digest();
//    } catch (NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    }
//
//    StringBuffer hexString = new StringBuffer();
//    for (int k = 0; k < md5sum.length; ++k) {
//      String s = Integer.toHexString((int) md5sum[k] & 0xFF);
//      if (s.length() == 1)
//        hexString.append('0');
//      hexString.append(s);
//    }
//    return hexString.toString();
//  }
  
  public void setListener(DiscoveryListener listener) {
	this.listener = listener;
  }

  public static void main(String[] args) {
    new NetworkServerDiscoverer(null).start();
    while (true) {
    }
  }
}