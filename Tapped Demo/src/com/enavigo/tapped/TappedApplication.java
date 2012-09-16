package com.enavigo.tapped;

import android.app.Application;

import com.enavigo.tapped.utils.Constants;
import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;

public class TappedApplication extends Application {
    private KCSClient sharedClient;

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        KinveySettings settings = new KinveySettings(Constants.KINVEY_KEY, Constants.KINVEY_SECRET_KEY);
        sharedClient = KCSClient.getInstance(this.getApplicationContext(), settings);
    }

    public KCSClient getKinveyService() {
        return sharedClient;
    }
}
