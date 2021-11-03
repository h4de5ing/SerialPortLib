package com.code19.serialportlib

import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by luowei on 2017/9/14.
 * https://github.com/luv135/AsyncIO/blob/fa64145b48f1c925bc428961f6c05b9bd72ec439/baseiolibrary/src/main/java/com/unistrong/luowei/factorysuite/pc/serial/BaseIO.kt
 *
 */
abstract class BaseIO {

    private var isRun: Boolean = false
    private val executor = ThreadPoolExecutor(3, 10, 5, TimeUnit.SECONDS, LinkedBlockingQueue())

    fun start(
        inputStream: InputStream,
        outputStream: OutputStream,
        callback: (buffer: ByteArray, size: Int) -> Unit
    ) {
        isRun = true
        readThread = startReadThread(inputStream, callback)
        writeThread = startWriteThread(outputStream, readThread!!)
    }

    private var readThread: ReadThread? = null
    private var writeThread: WriteThread? = null
    open fun stop() {
        isRun = false
        readThread?.close()
        readThread = null
        writeThread?.close()
        writeThread = null

    }

    fun write(packet: Packet): Boolean = isRun && (writeThread?.write(packet) == true)

    private fun startWriteThread(outputStream: OutputStream, readThread: ReadThread): WriteThread {
        val writeThread = WriteThread(outputStream, readThread)
        executor.execute(writeThread)
        return writeThread
    }

    private fun startReadThread(
        inputStream: InputStream,
        callback: (buffer: ByteArray, size: Int) -> Unit
    ): ReadThread {
        val readThread = ReadThread(inputStream, callback)
        executor.execute(readThread)
        return readThread
    }

    inner class WriteThread(
        private val outputStream: OutputStream,
        private val readThread: ReadThread
    ) : Thread() {
        private val queen = LinkedList<Packet>()
        private val objecz = Object()

        init {
            readThread.setWriteThread(this)
        }

        override fun run() {
            while (isRun) {
                while (queen.isEmpty()) {
                    synchronized(objecz) {
                        objecz.wait()
                    }
                }
                val poll = queen.poll()
                try {
                    write(poll.buffer)
                    var buffer: ByteArray? = null
                    if (poll.callback != null) {
                        buffer = readThread.get(2000)
                    }
                    poll.callback?.invoke(buffer != null, buffer ?: poll.buffer)
                } catch (e: Exception) {
                    this@BaseIO.stop()
                }
            }
        }

        @Synchronized
        private fun write(buffer: ByteArray) {
            outputStream.write(buffer)
        }

        fun write(packet: Packet): Boolean {
            var rt = false
            if (queen.size < 10) {
                queen.offer(packet)
                rt = true
            }
            synchronized(objecz) {
                objecz.notify()
            }
            return rt
        }

        fun close() {
            write(Packet(ByteArray(0)))
            interrupt()
        }
    }

    class Packet(
        val buffer: ByteArray,
        val callback: ((success: Boolean, buffer: ByteArray) -> Unit)? = null
    )


    inner class ReadThread(
        private val inputStream: InputStream,
        private val callback: (buffer: ByteArray, size: Int) -> Unit
    ) : Thread() {
        private val readBuffer = ByteArray(1024)
        private var readSize = 0
        private val objecz = Object()

        override fun run() {
            while (isRun) {
                try {
                    readSize = inputStream.read(readBuffer)
                    if (readSize > 0) {
                        synchronized(objecz) {
                            objecz.notify()
                        }
                        callback.invoke(readBuffer, readSize)
                    }
                } catch (e: Exception) {
                    this@BaseIO.stop()
                }
            }
        }

        fun get(timeout: Long): ByteArray? {
            if (readSize <= 0) {
                synchronized(objecz) {
                    objecz.wait(timeout)
                }
            }
            if (readSize > 0)
                return readBuffer.copyOfRange(0, readSize)
            return null
        }

        private lateinit var writeThread: WriteThread

        fun setWriteThread(writeThread: WriteThread) {
            this.writeThread = writeThread
        }

        fun close() {
            interrupt()
        }
    }
}