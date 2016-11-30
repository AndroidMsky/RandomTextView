package com.example.liangmutian.randomtextview;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by wuduogen838 on 16/11/30.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
