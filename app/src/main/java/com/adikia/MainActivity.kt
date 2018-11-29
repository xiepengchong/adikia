package com.adikia

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.adikia.library.dm.ReplaceMaker
import com.adikia.library.AdikiaCallback
import com.adikia.library.AdikiaManager
import com.adikia.library.dm.BackupMaker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (v == start_hook) {
            var src = Origin::class.java!!.getDeclaredMethod("print", String::class.java)
            AdikiaManager.get().addHook(src,object : AdikiaCallback() {

                override fun beforeInvokeMethod(receiver: Any?, vararg args: Any?): Array<out Any?>? {
                    return super.beforeInvokeMethod(receiver,arrayOf<Any>("修改之后的输入值"))
                }

                override fun afterInvokeMethod(receiver: Any?, result: Any?, vararg args: Any?): Any? {
                    return "修改之后的返回值"
                }

                override fun invokeMethod(receiver: Any?, vararg args: Any?): Any? {
                    return super.invokeMethod(receiver, args)
                }

            })
            AdikiaManager.get().startHook(this@MainActivity)

        } else if (stop_hook == v) {

            var src = Origin::class.java!!.getDeclaredMethod("print",String::class.java)
            AdikiaManager.get().restore(src)

        } else if (generateDex == v) {
            Toast.makeText(MainActivity@this,"该功能，已经move到AdikiaManager中，不再暴漏", Toast.LENGTH_LONG).show();
        } else if (call_print == v) {
            val output = Origin().print("origin")
            Log.v("XPC", "输出=" + output)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start_hook.setOnClickListener(this)
        stop_hook.setOnClickListener(this)
        generateDex.setOnClickListener(this)
        call_print.setOnClickListener(this)
    }
}
