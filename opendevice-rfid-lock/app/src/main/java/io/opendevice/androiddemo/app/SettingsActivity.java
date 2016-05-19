package io.opendevice.androiddemo.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.Toast;

import io.opendevice.androiddemo.R;

public class SettingsActivity extends android.preference.PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        checkValues();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // EditTextPreference pref = (EditTextPreference)findPreference("deviceID");
        }
    }

    private void checkValues()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String strUserName = sharedPrefs.getString("username", "NA");
        boolean bAppUpdates = sharedPrefs.getBoolean("applicationUpdates",false);
        String downloadType = sharedPrefs.getString("downloadType","1");

        String msg = "Cur Values: ";
        msg += "\n userName = " + strUserName;
        msg += "\n bAppUpdates = " + bAppUpdates;
        msg += "\n downloadType = " + downloadType;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }




}
