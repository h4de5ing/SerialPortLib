package com.code19.app

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.code19.app.databinding.ActivityMainBinding
import com.code19.serialportlib.BaseIO
import com.code19.serialportlib.SerialPortIO
import com.code19.serialportlib.SerialPortIO.findAllBauds
import com.code19.serialportlib.SerialPortIO.findAllTtysDevices
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mIsOpen = false
    private var mName = "/dev/ttyS1"
    private var mBaud = 0
    private var continuity = false
    private var sendMS = 100L
    private var hexSend = false
    private var hexReceive = false
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tv.movementMethod = ScrollingMovementMethod()
        binding.spName.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, findAllTtysDevices())
        binding.spBuad.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, findAllBauds())
        binding.spName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mName = parent!!.getItemAtPosition(position).toString()
            }
        }
        binding.spBuad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mBaud = parent!!.getItemAtPosition(position).toString().toInt()
            }
        }
        binding.btnSend.setOnClickListener {
            sendMS =
                if (binding.sendMs.text.isEmpty()) 100 else binding.sendMs.text.toString().toLong()
            val sendStr = binding.etInput.text.toString()
            if (sendStr.isNotEmpty()) {
                if (continuity) {
                    if (isTimer) {
                        binding.btnSend.text = "发送"
                        isTimer = false
                    } else {
                        binding.btnSend.text = "停止"
                        isTimer = true
                    }
                } else sendOneTime(sendStr)
            }
        }
        binding.continuity.setOnCheckedChangeListener { _, isChecked -> continuity = isChecked }
        binding.hexSend.setOnCheckedChangeListener { _, isChecked -> hexSend = isChecked }
        binding.hexReceive.setOnCheckedChangeListener { _, isChecked -> hexReceive = isChecked }
        timer()
    }

    private var isTimer = false
    private fun timer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (isTimer) {
                    val sendStr = binding.etInput.text.toString()
                    if (sendStr.isNotEmpty()) {
                        sendOneTime(sendStr)
                    }
                }
            }
        }, 0, sendMS)
    }

    private fun sendOneTime(message: String) {
        if (hexSend) {
            val data = DataUtils.int2bytes2(message)
            updateTv("s:${DataUtils.bytes2HexString(data)} \n")
            SerialPortIO.write(BaseIO.Packet(data))
        } else {
            updateTv("s:$message \n")
            SerialPortIO.write(BaseIO.Packet(message.toByteArray()))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uart_control, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_open) {
            if (!mIsOpen) {
                try {
                    SerialPortIO.start(mName, mBaud) { buffer, size ->
                        if (hexReceive) {
                            updateTv("r:${DataUtils.bytes2HexString(buffer, size)}\n")
                        } else {
                            updateTv("r:${String(buffer, 0, size)}\n")
                        }
                    }
                    mIsOpen = true
                    item.title = getString(R.string.close)
                    updateTv("open [ $mName $mBaud ] success \n")
                    binding.btnSend.isEnabled = true
                } catch (e: Exception) {
                    updateTv("exception: [$mName $mBaud] $e\n")
                    binding.btnSend.isEnabled = false
                    e.printStackTrace()
                }
            } else {
                item.title = getString(R.string.open)
                updateTv("close success \n")
                mIsOpen = false
                binding.btnSend.isEnabled = false
                SerialPortIO.stop()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTv(message: String) {
        runOnUiThread {
            binding.tv.append(message)
            val offset: Int = binding.tv.lineCount * binding.tv.lineHeight - binding.tv.height
            if (offset >= 6000) {
                binding.tv.text = ""
            } else {
                binding.tv.scrollTo(0, offset.coerceAtLeast(0))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SerialPortIO.stop()
    }
}
