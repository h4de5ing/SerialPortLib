package com.code19.serialportlib

import android_serialport_api.BaudRate
import android_serialport_api.SerialPort
import java.io.File

/**
 * Created by luowei on 2017/9/13.
 */
object SerialPortIO : BaseIO() {
    private var serialPort: SerialPort? = null
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        serialPort = SerialPort(File(name), baud, 0)
        super.start(serialPort!!.inputStream, serialPort!!.outputStream, callback)
    }

    override fun stop() {
        super.stop()
        serialPort?.close()
        serialPort = null
    }

    fun findAllTtysDevices(): List<String> {
        val list = mutableListOf<String>()
        list.add("/dev/ttyS1")
        try {
            list.addAll((File("/dev/").list { _, name -> name.contains("ttyS") }.toMutableList()))
            list.sort()
        } catch (e: Exception) {
            //有些设备会出现没有权限问题 type=1400 audit(0.0:239): avc: denied { read } for name="/" dev="tmpfs" ino=2188 scontext=u:r:untrusted_app:s0:c133,c256,c512,c768 tcontext=u:object_r:device:s0 tclass=dir permissive=0 app=??
            e.printStackTrace()
        }
        return list
    }

    fun findAllBauds(): Array<Int> {
        return arrayOf(
            BaudRate.B57600,
            BaudRate.B115200,
            BaudRate.B230400,
            BaudRate.B460800,
            BaudRate.B500000,
            BaudRate.B576000,
            BaudRate.B921600
        )
    }
}