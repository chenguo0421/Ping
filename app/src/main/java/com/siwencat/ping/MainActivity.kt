package com.siwencat.ping

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class MainActivity : AppCompatActivity() {

    private var running: Boolean = false
    private var timer: Timer? = null
    private var timerTask: MyTimerTask? = null
    private var pingError:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        initListener()
    }

    private fun initListener() {
        tv_ping.setOnClickListener {
            if (running) {
                stopPingTimer()
                return@setOnClickListener
            }
            if (edt_domain.text.toString().trim().isEmpty()) {
                Toast.makeText(this@MainActivity, "IP/域名不能为空", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            running = true
            timer = Timer()
            timerTask = MyTimerTask()
            timer?.schedule(timerTask, 1000, 1000)
        }

        tv_stop.setOnClickListener{
            stopPingTimer()
        }
    }

    private fun initData() {
    }

    private fun initView() {
    }


    private fun stopPingTimer(){
        running = false
        this.timer?.cancel()
    }

    inner class MyTimerTask(): TimerTask(){

        @SuppressLint("SetTextI18n")
        override fun run() {
            val ret = ping(edt_domain.text.toString().trim())
            Log.d("TimerTask" , "ping ret = $ret")
            runOnUiThread{
                tv_ping_ret.text = tv_ping_ret.text.toString().trim() + "\n" + ret
            }
        }

    }

    private fun executeCmd(var0: String, var1: Boolean): Process? {
        if (!var1) {
            try {
                return Runtime.getRuntime().exec(var0)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                return Runtime.getRuntime().exec(arrayOf("su", "-c", var0))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun ping(domain: String):String {
        var waitFor: Int
        val reader: BufferedReader
        var process: Process? = null
        val bufferedReader: BufferedReader
        try {
            process = executeCmd("ping -c 1 -w 5 $domain", false)
            reader = BufferedReader(InputStreamReader(process?.inputStream))
            bufferedReader = BufferedReader(InputStreamReader(process?.errorStream))
            waitFor = process?.waitFor()!!
            pingError = false
        } catch (e: Exception) {
            process?.destroy()
            return e.message!!
        }
        var domain: String = ""
        if (waitFor == 0) {
            waitFor = 0
            while (true) {
                var line: String?
                try {
                    line = reader.readLine()
                } catch (e: java.lang.Exception) {
                    break
                }
                if (line == null) {
                    break
                }
                if (waitFor == 1) {
                    try {
                        domain = "$line" + "\n"
                    } catch (e: java.lang.Exception) {
                        break
                    }
                }
                ++waitFor
            }
        } else if (waitFor == 1) {
            try {
                domain = "请求超时\n";
            } catch (e: java.lang.Exception) {
                process.destroy()
                return e.message!!
            }
        } else {
            try {
                pingError = true
            } catch (e: java.lang.Exception) {
                process.destroy()
                return e.message!!
            }
            var var16: String = domain
            while (true) {
                var var6: String?
                try {
                    var6 = bufferedReader.readLine()
                } catch (e: java.lang.Exception) {
                    break
                }
                domain = var16
                if (var6 == null) {
                    break
                }
                try {
                    var16 = var16 + var6 + "\n"
                } catch (e: java.lang.Exception) {
                    break
                }
            }
        }

        return try {
            process.destroy()
            domain
        } catch (e: java.lang.Exception) {
            e.message!!
        }
    }
}