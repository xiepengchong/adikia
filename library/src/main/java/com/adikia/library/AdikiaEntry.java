package com.adikia.library;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class AdikiaEntry {

    private static final String TAG = "Adikia.Entry";

    private Method srcMethod;
    private Method destMethod;
    private Method backupMethod;

    private AdikiaCallback callback;

    private long backupMethodPtr;


    public AdikiaEntry(Method src, Method dest, Method backup, AdikiaCallback cb) {
        this.srcMethod = src;
        this.destMethod = dest;
        this.backupMethod = backup;
        this.callback = cb;

        this.srcMethod.setAccessible(true);
        this.destMethod.setAccessible(true);
        this.backupMethod.setAccessible(true);
    }

    public void hook() {
        if (backupMethodPtr == 0) {
            if (AdikiaConfig.DEBUG) {
                Log.v(TAG, "hook caller");
            }
            long bak = hook(srcMethod, destMethod, backupMethod);
            backupMethodPtr = bak;
        }
    }

    public void restore() {
        if (backupMethodPtr != 0) {
            if (AdikiaConfig.DEBUG) {
                Log.v(TAG, "restore caller");
            }
            restore(srcMethod, backupMethodPtr);
            backupMethodPtr = 0;
        }
    }

    public Object callOrigin(Object receiver, Object... args) throws InvocationTargetException, IllegalAccessException {

        Object result = null;
        if (backupMethodPtr != 0) {
            Object[] argsModified = callback.beforeInvokeMethod(receiver, args);
            if ((result = callback.invokeMethod(receiver, argsModified)) == null) {
                result = backupMethod.invoke(receiver,argsModified);
            }
            result = callback.afterInvokeMethod(receiver, args, result);
        } else {
            result = callOriginMethod(receiver, args);
        }
        return result;
    }

    Object callOriginMethod(Object receiver, Object... args) throws InvocationTargetException, IllegalAccessException {

        if (AdikiaConfig.DEBUG) {
            Log.v(TAG, "callOriginMethod start");
        }
        Class returnClz = srcMethod.getReturnType();
        if (AdikiaConfig.DEBUG) {
            Log.v(TAG, "callOriginMethod returnClz.getName()=" + returnClz.getName()+" srcMethod="+srcMethod);
        }
        if (returnClz != null && "void".equalsIgnoreCase(returnClz.getName())) {
            srcMethod.invoke(receiver, args);
            return null;
        }


        return srcMethod.invoke(receiver, args);
    }

    private static native long hook(Method src, Method dest, Method backup);

    private static native Method restore(Method src, long backup);

    public static native void init(Class cls,String name,String sig,boolean istatic);

    public static void m1() {
    }

    public static void m2() {
    }

    static {
        System.loadLibrary("adikia");
    }


}
