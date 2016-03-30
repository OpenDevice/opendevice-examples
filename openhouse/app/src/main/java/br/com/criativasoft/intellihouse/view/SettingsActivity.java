package br.com.criativasoft.intellihouse.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import br.com.criativasoft.intellihouse.Constants;
import br.com.criativasoft.intellihouse.R;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String ENABLE_WEB_SERVICE = Constants.Settings.ENABLE_WEB_SERVICE;
	public static final String WEB_SERVICE_PORT = Constants.Settings.WEB_SERVICE_PORT;
	
	public static final String THEME = "THEME";
	
	public SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences); 
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        
        EditTextPreference portPref = (EditTextPreference) findPreference(WEB_SERVICE_PORT);
		EditText editText = portPref.getEditText();
		editText.setKeyListener(new DigitsKeyListener());
		portPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean rtnval = true;
                if (newValue != null && Integer.parseInt(newValue.toString()) < 1025) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle("Alerta");
                    builder.setMessage("Use uma porta maior que 1025 !");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    rtnval = false;
                }
                return rtnval;
            }
        });
		

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		if(ENABLE_WEB_SERVICE.equals(key)){
			boolean enabled = sharedPreferences.getBoolean(key, false);
			WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
			
			if(enabled && ! wifiManager.isWifiEnabled()){
				wifiManager.setWifiEnabled(true);
			}
			
		}
		
	}
	
}


