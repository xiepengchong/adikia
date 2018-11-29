package com.adikia.library.dm;

import android.content.Context;

import com.adikia.library.AdikiaConfig;
import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReplaceMaker extends Maker{

    public DexMaker maker(DexMaker maker,Method m) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String cName = "L"+m.getDeclaringClass().getName().replace(".","/")+ AdikiaConfig.REPLACE_SUFFIX +";";

        TypeId<?> backup = TypeId.get(cName);
        TypeId<?> superType = TypeId.get( m.getDeclaringClass().getSuperclass());

        maker.declare(backup, cName, m.getDeclaringClass().getModifiers(),superType);
        generateReplaceMethod(maker, backup,m);
        return maker;
    }

    private void generateReplaceMethod(DexMaker dexMaker, TypeId<?> declaringType, Method m) {

        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }

        MethodId methodN = declaringType.getMethod(TypeId.get(m.getReturnType()), m.getName(),params);
        Code code = dexMaker.declare(methodN, m.getModifiers());
        Local res = code.newLocal(TypeId.get(m.getReturnType()));

        Local<Object> output = code.newLocal(TypeId.OBJECT);
        Local<Object> adikiaManagerObject = code.newLocal(TypeId.OBJECT);
        Local i_ = code.newLocal(TypeId.INT);
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Integer> a = code.newLocal(TypeId.INT);

        Class adikiaManager = null;
        try {
            adikiaManager = Class.forName("com.adikia.library.AdikiaManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        MethodId<Integer, String> get
                = TypeId.get(adikiaManager).getMethod(TypeId.get(adikiaManager), "get");
        code.invokeStatic(get, adikiaManagerObject);

        Local<?> thisLocal = code.getThis(declaringType);

        MethodId<Object, Object> callOrigin = TypeId.get(adikiaManager).getMethod(TypeId.OBJECT, "callOrigin", TypeId.OBJECT, TypeId.OBJECT);

        int paramsNum = m.getParameterTypes().length;
        code.loadConstant(a,paramsNum);

        code.newArray(args, a);
        for(int i=0;i<paramsNum;i++){
            code.loadConstant(i_, i);
            code.aput(args,i_,code.getParameter(i,(TypeId)methodN.getParameters().get(i)));
        }

        code.invokeVirtual(callOrigin,output,adikiaManagerObject,thisLocal,args);

        if(m.getReturnType().equals(void.class)){
            code.returnVoid();
            return;
        }
        code.cast(res,output);
        code.returnValue(res);
    }
}
