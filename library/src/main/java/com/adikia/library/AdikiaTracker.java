package com.adikia.library;

import java.lang.reflect.Method;

public class AdikiaTracker {

    public AdikiaTracker(Method m,AdikiaCallback ac){
        this.method = m;
        this.callback = ac;
    }

    private Method method;
    private AdikiaCallback callback;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public AdikiaCallback getCallback() {
        return callback;
    }

    public void setCallback(AdikiaCallback callback) {
        this.callback = callback;
    }
}
