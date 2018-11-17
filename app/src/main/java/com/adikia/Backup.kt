package com.adikia

import android.util.Log

class Backup{

    fun print(input:String):String{
        Log.v("XPC","this is backup method="+this+"   input="+input)
        return input;
    }
}