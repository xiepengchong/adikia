package com.adikia.library.dm;

import android.content.Context;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BackupMaker extends Maker{

    public DexMaker maker(DexMaker dexMaker,Method m) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TypeId<?> superType = TypeId.get( m.getDeclaringClass().getSuperclass());

        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        String cName = "L"+m.getDeclaringClass().getName().replace(".","/")+"_hkb;";
        TypeId<?> backup = TypeId.get(cName);

        dexMaker.declare(backup, cName, m.getDeclaringClass().getModifiers(),superType);
        generateBackupMethod(dexMaker, backup,m);
        return dexMaker;
    }

    private void generateBackupMethod(DexMaker dexMaker, TypeId<?> declaringType, Method m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId methodN = declaringType.getMethod(TypeId.get(m.getReturnType()), m.getName(),params);
        Code code = dexMaker.declare(methodN, m.getModifiers());
        Local res = code.newLocal(TypeId.get(m.getReturnType()));

        if(m.getReturnType().equals(void.class)){
            code.returnVoid();
            return;
        }

        code.loadConstant(res,null);
        code.returnValue(res);
    }
}
