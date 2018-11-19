package com.adikia;

import android.app.Application;
import android.content.Context;

import com.adikia.library.AdikiaCallback;
import com.adikia.library.AdikiaManager;

import java.lang.reflect.Method;

public class AdikiaApplication extends Application{


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
