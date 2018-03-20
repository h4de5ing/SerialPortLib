package com.code19.app.serialportlib

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.code19.serialportlib.BaseIO
import com.code19.serialportlib.SerialPortIO
import com.code19.serialportlib.SerialPortIO.findAllBauds
import com.code19.serialportlib.SerialPortIO.findAllTtysDevices

class MainActivity : AppCompatActivity() {
    private lateinit var mSpName: Spinner
    private lateinit var mSpBaud: Spinner
    private lateinit var mEtData: EditText
    private lateinit var mBtnSend: Button
    private lateinit var mTvResult: TextView
    private var mIsOpen = false
    private var mName = ""
    private var mBaud = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSpName = findViewById(R.id.sp_name)
        mSpBaud = findViewById(R.id.sp_buad)
        mEtData = findViewById(R.id.et_input)
        mBtnSend = findViewById(R.id.btn_send)
        mTvResult = findViewById(R.id.tv_result)
        findViewById<Button>(R.id.btn_clean).setOnClickListener { mTvResult.text = "" }
        mTvResult.movementMethod = ScrollingMovementMethod.getInstance()
        val devices = findAllTtysDevices()
        val names = findAllBauds()
        mSpName.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, devices)
        mSpBaud.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        mSpName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mName = parent!!.getItemAtPosition(position).toString()
            }

        }
        mSpBaud.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mBaud = parent!!.getItemAtPosition(position).toString().toInt()
            }
        }
        mBtnSend.setOnClickListener {
            val sendStr = mEtData.text.toString()
            if (sendStr.isNotEmpty()) {
                runOnUiThread { mTvResult.append("s:$sendStr \n") }
                SerialPortIO.write(BaseIO.Packet(sendStr.toByteArray()))
            }
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
                    SerialPortIO.start("/dev/$mName", mBaud, { buffer, size ->
                        //val data = ByteArray(size)
                        //System.arraycopy(buffer, 0, data, 0, size)
                        runOnUiThread { mTvResult.append("r:${String(buffer, 0, size)}\n") }
                    })
                    mIsOpen = true
                    item.title = getString(R.string.close)
                    mTvResult.append("open [ $mName $mBaud ] success \n")
                    mBtnSend.isEnabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    mTvResult.append("exception: [$mName $mBaud] $e\n")
                    mBtnSend.isEnabled = false
                }
            } else {
                item.title = getString(R.string.open)
                mTvResult.append("close success \n")
                mIsOpen = false
                mBtnSend.isEnabled = false
                SerialPortIO.stop()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        SerialPortIO.stop()
    }
}
