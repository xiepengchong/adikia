package com.adikia

import android.util.Log
import com.adikia.library.AdikiaManager

class Replace{

    fun print(input:String):String{
        var ouput = AdikiaManager.get().callOrigin(this,input)

        return ouput as String;

    }
}