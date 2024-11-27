package com.ziio.twitterdemo;

import android.app.Application;

import java.util.HashMap;

/**
 * Application data
 */
public class MyApplication extends Application {

    private static MyApplication mApp;

    // public infos
    public HashMap<String,String> infoMap = new HashMap<>();

    public static MyApplication getInstance(){
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

}
