package com.adikia

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.adikia.library.AdikiaCallback
import com.adikia.library.AdikiaManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (v == start_hook) {
            var src = Origin::class.java!!.getDeclaredMethod("print", String::class.java)
            var dest = Replace::class.java!!.getDeclaredMethod("print", String::class.java)
            var backup = Backup::class.java!!.getDeclaredMethod("print", String::class.java)

            AdikiaManager.get().hook(src, dest, backup, object : AdikiaCallback() {

                override fun beforeInvokeMethod(receiver: Any?, vararg args: Any?): Array<out Any?>? {
                    return super.beforeInvokeMethod(receiver, "修改后的输入参数")
                }

                override fun afterInvokeMethod(receiver: Any?, result: Any?, vararg args: Any?): Any? {
                    return "修改之后的返回值"
                }

                override fun invokeMethod(receiver: Any?, vararg args: Any?): Any? {
                    Log.v("XPC","this="+receiver);
                    return super.invokeMethod(receiver, *args)
                }

            })

        } else if (stop_hook == v) {

            var src = Origin::class.java!!.getDeclaredMethod("print")
            AdikiaManager.get().restore(src)

        } else if (origin_method == v) {
            Origin().print("origin")
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
        origin_method.setOnClickListener(this)
        call_print.setOnClickListener(this)
    }
}
