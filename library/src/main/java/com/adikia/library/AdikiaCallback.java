package com.adikia.library;

import android.util.Log;

public abstract class AdikiaCallback {

    private final static String TAG = "Adikia.Callback";

    private Object[] args = new Object[0];

    public Object invokeMethod(Object receiver,Object ... args){
        if(AdikiaConfig.DEBUG){
            Log.v(TAG,"invokeMethod");
        }
        return null;
    }

    public Object[] beforeInvokeMethod(Object receiver,Object ... args){
        if(AdikiaConfig.DEBUG){
            Log.v(TAG,"beforeInvokeMethod");
        }
        return args;
    }

    public Object afterInvokeMethod(Object receiver,Object result,Object ... args){
        if(AdikiaConfig.DEBUG){
            Log.v(TAG,"afterInvokeMethod");
        }
        return result;
    }

}
