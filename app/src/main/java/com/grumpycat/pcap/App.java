package com.grumpycat.pcap;

import android.app.Application;

import com.grumpycat.pcap.tools.Config;

/**
 * Created by cc.he on 2018/11/27
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config.init(this);
    }
}
