package com.hx.testttl.devices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 设备数据的广播
 */
class DeviceDataBroadcastReceiver(deviceOnReceive: DeviceOnReceive):BroadcastReceiver() {
    val deviceOnReceive = deviceOnReceive
    interface DeviceOnReceive {
        fun getIntent(intent: Intent?)
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        deviceOnReceive.getIntent(intent)
    }
}