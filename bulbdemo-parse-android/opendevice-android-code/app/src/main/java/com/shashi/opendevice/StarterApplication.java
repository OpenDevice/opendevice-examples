package com.shashi.opendevice;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by Sharath_Mk on 11/13/2015.
 */
public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "application id", "client id");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
