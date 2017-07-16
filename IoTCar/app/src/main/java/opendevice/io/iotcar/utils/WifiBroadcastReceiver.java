package opendevice.io.iotcar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import opendevice.io.iotcar.MainActivity;

import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by ricardo on 10/07/17.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private final String TARGET_WIFI = "IoTCar";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean connected = false;

        if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (SupplicantState.isValidState(state) && state == SupplicantState.COMPLETED) {
                connected = checkConnectedToDesiredWifi(context);
            }
        } else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI){
                connected = checkConnectedToDesiredWifi(context);
            }
        }


        if(connected){
            Intent i = new Intent(Constansts.ACTION_CAR_DETECTED);
            i.setClass(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }

    /** Detect you are connected to a specific network. */
    private boolean checkConnectedToDesiredWifi(Context context) {
        boolean connected = false;

        WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null && wifi.getSSID() != null) {
            connected = wifi.getSSID().toLowerCase().contains(TARGET_WIFI.toLowerCase());
        }

        return connected;
    }
}
