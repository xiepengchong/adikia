package com.adikia.library;

import android.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdikiaManager {

    private static class InstanceHolder {
        private static AdikiaManager sInstance = new AdikiaManager();
    }

    public static AdikiaManager get() {
        return InstanceHolder.sInstance;
    }

    private Map<Pair<String, String>, AdikiaEntry> mHookMap = new ConcurrentHashMap<>();

    public void hook(Method originMethod, Method hookMethod, Method backMethod,AdikiaCallback callback) {
        if (originMethod == null || hookMethod == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        Pair<String, String> key = Pair.create(hookMethod.getDeclaringClass().getName(), hookMethod.getName());
        if (mHookMap.containsKey(key)) {
            AdikiaEntry mh = mHookMap.get(key);
            mh.restore();
        }
        AdikiaEntry methodHook = new AdikiaEntry(originMethod, hookMethod, backMethod,callback);
        mHookMap.put(key, methodHook);
        methodHook.hook();
    }

    public void restore(Method hkm) {
        Pair<String, String> key = Pair.create(hkm.getDeclaringClass().getName(), hkm.getName());
        if (mHookMap.containsKey(key)) {
            AdikiaEntry mehk = mHookMap.get(key);
            mehk.restore();
            mHookMap.remove(key);
        }
    }

    public Object callOrigin(Object receiver, Object... args) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        AdikiaEntry methodHook = mHookMap.get(Pair.create(className, methodName));
        if (methodHook != null) {
            try {
                return methodHook.callOrigin(receiver, args);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
