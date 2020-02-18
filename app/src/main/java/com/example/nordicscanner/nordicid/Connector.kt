package com.example.nordicscanner.nordicid

import android.content.Context
import com.nordicid.nurapi.NurApi
import com.nordicid.nurapi.NurApiBLEAutoConnect

class Connector(
    private val nurApi: NurApi
) {
    private var bleAuto: NurApiBLEAutoConnect? = null

    fun connect(context: Context, address: String) {
        bleAuto = NurApiBLEAutoConnect(context, nurApi)
        bleAuto?.address = address
    }

    fun disconnect() {
        bleAuto?.onStop()
        bleAuto = null
    }

}
