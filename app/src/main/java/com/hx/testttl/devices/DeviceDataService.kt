package com.hx.testttl.devices

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hx.testttl.devices.DeviceDataService.Companion.sendDeviceDataMsg

/**
 * 设备通讯的服务
 */
class DeviceDataService : Service() {

    private lateinit var usbDevice: UsbDevice
    private lateinit var usbDeviceConnection: UsbDeviceConnection
    private lateinit var usbSerialDriver: UsbSerialDriver
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var usbReadThread: UsbReadThread

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 链接usb
        connectUsbDevice()
        // 初始化广播
        initBroadCastReceiver()
    }

    /**
     * 注册接收广播实现开始结束指令
     */
    private fun initBroadCastReceiver() {
        val deviceDataBroadcastReceiver =
            DeviceDataBroadcastReceiver(object : DeviceDataBroadcastReceiver.DeviceOnReceive {
                override fun getIntent(intent: Intent?) {
                    when (intent?.action) {
                        START_ACTION -> {
                            usbReadThread.startReceive = true
                            usbSerialPort.write("69050181018843".decodeHex(), 1000)
                        }

                        STOP_ACTION -> {
                            usbReadThread.startReceive = false
                            usbSerialPort.write("69050181028943".decodeHex(), 1000)
                        }

                        SET_THRESHOLD_ACTION -> {
                            usbSerialPort.write("6909018301006400322443".decodeHex(), 1000)
                        }
                    }
                }
            })
        val intentFilter = IntentFilter()
        intentFilter.addAction(START_ACTION)
        intentFilter.addAction(STOP_ACTION)
        intentFilter.addAction(SET_THRESHOLD_ACTION)
        registerReceiver(deviceDataBroadcastReceiver, intentFilter)

    }

    /**
     * 链接USB设备
     */
    private fun connectUsbDevice() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val findAllDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        Log.d("设备数量:", findAllDrivers.size.toString())
        if (findAllDrivers.isEmpty()) {
            Log.d("无设备", "无设备")
            sendDeviceErrorMsg(this,"无传感器设备")
            return
        }
        findAllDrivers.forEach {
            if (it.device.productId == 29987) {
                usbDevice = it.device
                usbSerialDriver = it
                return@forEach
            }
        }
        if (!usbManager.hasPermission(usbDevice)) {
            val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
            val broadcast = PendingIntent.getBroadcast(
                this, 0, Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(usbDevice, broadcast)
            return
        }
        usbDeviceConnection = usbManager.openDevice(usbDevice)
        if (this::usbDeviceConnection.isInitialized) {
            Log.d("设备链接", "设备链接成功")
            usbSerialPort = usbSerialDriver.ports.get(0)
            if (this::usbSerialPort.isInitialized) {
                usbSerialPort.open(usbDeviceConnection)
                usbSerialPort.setParameters(
                    115200,
                    8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )
            }
            usbReadThread = UsbReadThread(this,usbSerialPort)
            usbReadThread.start()
        } else {
            Log.d("设备链接", "设备链接失败")
            sendDeviceErrorMsg(this,"设备链接失败")
        }
    }


    companion object {
        val START_ACTION = "com.hx.start_action"
        val STOP_ACTION = "com.hx.stop_action"
        val SET_THRESHOLD_ACTION = "com.hx.set_threshold_action"
        val DEVICE_ERROR_MSG = "com.hx.device_error_msg"
        val DEVICE_DATA_MSG = "com.hx.device_data_msg"

        /**
         * 发送开始指令
         */
        fun sendStartAction(activity: Activity) {
            activity.sendBroadcast(Intent(START_ACTION))
        }

        /**
         * 发送结束指令
         */
        fun sendStopAction(activity: Activity) {
            activity.sendBroadcast(Intent(STOP_ACTION))
        }

        /**
         * 发送设置阈值指令
         */
        fun sendSetThresholdAction(activity: Activity) {
            activity.sendBroadcast(Intent(SET_THRESHOLD_ACTION))
        }

        /**
         * 发送失败指令
         */
        fun sendDeviceErrorMsg(service: Service, msg: String) {
            service.sendBroadcast(Intent(DEVICE_ERROR_MSG).putExtra("data", msg))
        }
        /**
         * 发送数据接收指令
         */
        fun sendDeviceDataMsg(service: Service,data:String) {
            service.sendBroadcast(Intent(DEVICE_DATA_MSG).putExtra("data",data))
        }
    }
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    val byteIterator = chunkedSequence(2)
        .map { it.toInt(16).toByte() }
        .iterator()

    return ByteArray(length / 2) { byteIterator.next() }
}

class UsbReadThread(service: Service,usbSerialPort: UsbSerialPort) : Thread() {
    val usbSerialPort = usbSerialPort
    val service = service
    var startReceive = false
    override fun run() {
        val byteArray = ByteArray(100)
        while (true) {
            if (startReceive) {
                usbSerialPort.read(byteArray, 1000)
                Log.d("收到数据", ConvertUtils.bytes2HexString(byteArray))
                sendDeviceDataMsg(service,ConvertUtils.bytes2HexString(byteArray))
            }
        }
    }
}