package com.adikia.library;

import android.content.Context;
import android.util.Pair;

import com.adikia.library.dm.BackupMaker;
import com.adikia.library.dm.ReplaceMaker;
import com.android.dx.DexMaker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdikiaManager {
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE;
    static {
        PRIMITIVE_TO_SIGNATURE = new HashMap<Class<?>, String>(9);
        PRIMITIVE_TO_SIGNATURE.put(byte.class, "B");
        PRIMITIVE_TO_SIGNATURE.put(char.class, "C");
        PRIMITIVE_TO_SIGNATURE.put(short.class, "S");
        PRIMITIVE_TO_SIGNATURE.put(int.class, "I");
        PRIMITIVE_TO_SIGNATURE.put(long.class, "J");
        PRIMITIVE_TO_SIGNATURE.put(float.class, "F");
        PRIMITIVE_TO_SIGNATURE.put(double.class, "D");
        PRIMITIVE_TO_SIGNATURE.put(void.class, "V");
        PRIMITIVE_TO_SIGNATURE.put(boolean.class, "Z");
    }

    private static class InstanceHolder {
        private static AdikiaManager sInstance = new AdikiaManager();
    }
    private ClassLoader hClasssLoader = null;

    public ClassLoader loaded(){
        if(hClasssLoader == null){
            hClasssLoader = AdikiaManager.class.getClassLoader();
        }
        return hClasssLoader;
    }

    public static AdikiaManager get() {
        return InstanceHolder.sInstance;
    }

    private Map<Pair<String, String>, AdikiaEntry> mHookMap = new ConcurrentHashMap<>();
    private Map<Pair<String, String>, AdikiaTracker> mHookTracker = new ConcurrentHashMap<>();

    public DexMaker configuration(DexMaker maker, Method m) {

        try {
            maker = new BackupMaker().maker(maker, m);
            maker = new ReplaceMaker().maker(maker, m);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return maker;
    }

    public void addHook(Method originMethod,AdikiaCallback callback){
        Pair<String, String> key = Pair.create(originMethod.getDeclaringClass().getName(), originMethod.getName());
        mHookTracker.put(key,new AdikiaTracker(originMethod,callback));
    }

    public void startHook(Context context){

        DexMaker maker = new DexMaker();
        for(AdikiaTracker tracker : mHookTracker.values()){
            maker = configuration(maker,tracker.getMethod());
        }

        try {
            hClasssLoader = new BackupMaker().load(context,maker);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(AdikiaTracker tracker : mHookTracker.values()){
            try {
                Method[] m = genMethod(tracker.getMethod());
                String sig=getMethodSignature(tracker.getMethod());
                sig=sig.replace(".","/");
                AdikiaEntry.init(tracker.getMethod().getDeclaringClass(),tracker.getMethod().getName(),sig,Modifier.isStatic(tracker.getMethod().getModifiers()));

                hook(tracker.getMethod(),m[0],m[1],tracker.getCallback());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
    public static String getSignature(Class<?> clazz) {
        String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        } else if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        } else {
            return "L" + clazz.getName() + ";";
        }
    }

    static String getMethodSignature(Method m) {
        StringBuilder result = new StringBuilder();

        result.append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            result.append(getSignature(parameterType));
        }
        result.append(')');
        result.append(getSignature(m.getReturnType()));
        return result.toString();
    }

    private Method[] genMethod(Method m) throws ClassNotFoundException, NoSuchMethodException {

        Method[] methods = new Method[2];
        String backupClassName = m.getDeclaringClass().getName()+"_hkb";
        String replaceClassName = m.getDeclaringClass().getName()+"_hkr";
        String methodName = m.getName();
        Class<?>[] args = m.getParameterTypes();
        Class backupClass = hClasssLoader.loadClass(backupClassName);
        Class replaceClass = hClasssLoader.loadClass(replaceClassName);
        methods[0] = replaceClass.getDeclaredMethod(methodName,args);
        methods[1] = backupClass.getDeclaredMethod(methodName,args);
        return methods;
    }


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

    public Object callOrigin(Object receiver, Object args) {
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
