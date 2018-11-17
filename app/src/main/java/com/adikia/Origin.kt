package com.adikia

import android.util.Log

class Origin{

    fun print(input:String):String{
        Log.v("XPC","调用的对象="+this+"   输入参数="+input)
        return input;
    }
}