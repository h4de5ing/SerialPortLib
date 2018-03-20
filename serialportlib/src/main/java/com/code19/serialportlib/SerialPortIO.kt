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

    fun findAllTtysDevices(): Array<String> {
        val list = File("/dev/").list({ _, name -> name.contains("ttyS") })
        list.sort()
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