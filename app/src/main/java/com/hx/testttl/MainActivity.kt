package com.hx.testttl

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ConvertUtils
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hx.testttl.devices.DeviceDataBroadcastReceiver
import com.hx.testttl.devices.DeviceDataService
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback

class MainActivity : AppCompatActivity() {
    lateinit var setHeight: TextView
    lateinit var start: TextView
    lateinit var close: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setHeight = findViewById(R.id.tv_set_height)
        start = findViewById(R.id.tv_start)
        close = findViewById(R.id.tv_close)

        val intent = Intent(this,DeviceDataService::class.java)
        startService(intent)



        setHeight.setOnClickListener {
            DeviceDataService.sendSetThresholdAction(this)
        }

        start.setOnClickListener {
            DeviceDataService.sendStartAction(this)
        }

        close.setOnClickListener {
            DeviceDataService.sendStopAction(this)
        }

        val deviceDataBroadcastReceiver = DeviceDataBroadcastReceiver(object :DeviceDataBroadcastReceiver.DeviceOnReceive{
            override fun getIntent(intent: Intent?) {
                when(intent?.action) {
                    DeviceDataService.DEVICE_DATA_MSG->{
                        Log.d("广播接收到的数据",intent?.getStringExtra("data").toString())
                    }
                    DeviceDataService.DEVICE_ERROR_MSG->{
                        Log.d("广播接收到的数据",intent?.getStringExtra("data").toString())
                    }
                }
            }
        })
        val intentFilter = IntentFilter()
        intentFilter.addAction(DeviceDataService.DEVICE_DATA_MSG)
        intentFilter.addAction(DeviceDataService.DEVICE_ERROR_MSG)
        registerReceiver(deviceDataBroadcastReceiver,intentFilter)
    }
}