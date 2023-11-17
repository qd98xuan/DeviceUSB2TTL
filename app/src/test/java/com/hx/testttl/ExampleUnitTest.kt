package com.hx.testttl

import com.blankj.utilcode.util.ConvertUtils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun calculationHex() {
//        69 05 01 81 01 88 43
//        69,09,01,83,01,00,64,00,32,24,43
//        69,5,1,81,1,88,43
//        69,9,1,83,1,0,64,0,32,24,43
        var sum2 = 0 // 二进制
        var hexArray = arrayListOf(69,5,1,81,1,88,43)
        for (i in 1..hexArray.size-3) {
            sum2+=ConvertUtils.hexString2Int(hexArray[i].toString())
        }

        val sumHex = ConvertUtils.int2HexString(sum2)
        var sumBytes = ConvertUtils.hexString2Bytes(sumHex)
        if (sumBytes.size>1) {
            sumBytes = byteArrayOf(sumBytes[1])
        }
        var checkHex = ConvertUtils.bytes2HexString(sumBytes)

        println(checkHex)
    }
}