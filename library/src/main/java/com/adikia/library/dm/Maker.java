package com.adikia.library.dm;

import android.content.Context;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;

import java.io.File;
import java.io.IOException;

class Maker {

    public ClassLoader load(Context context,DexMaker dexMaker) throws IOException {
        File outputDir = context.getFilesDir();
        return dexMaker.generateAndLoad(ReplaceMaker.class.getClassLoader(), outputDir);
    }

    protected TypeId getTypeIdFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return TypeId.INT;
        }else if(cls.getName().equals(long.class.getName())){
            return TypeId.LONG;
        }else if(cls.getName().equals(short.class.getName())){
            return TypeId.SHORT;
        }else if(cls.getName().equals(double.class.getName())){
            return TypeId.DOUBLE;
        }else if(cls.getName().equals(boolean.class.getName())){
            return TypeId.BOOLEAN;
        }else if(cls.getName().equals(float.class.getName())){
            return TypeId.FLOAT;
        }else if(cls.getName().equals(byte.class.getName())){
            return TypeId.BYTE;
        }else if(cls.getName().equals(char.class.getName())){
            return TypeId.CHAR;
        }else if(cls.getName().equals(void.class.getName())){
            return TypeId.VOID;
        }else{
            return  TypeId.get(cls);
        }
    }

}
