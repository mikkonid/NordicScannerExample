package com.example.nordicscanner.nordicid

import android.util.Log
import com.nordicid.nurapi.AccessoryExtension
import com.nordicid.nurapi.NurApi
import com.nordicid.nurapi.NurApiErrors

interface BarcodeScannerListener {
    fun onSuccess(result: String)
    fun onFailure()
}

class BarcodeScanner(
    private val nurApi: NurApi,
    private val listener: BarcodeScannerListener
) {

    private val accessoryExtension = AccessoryExtension(nurApi)

    private var waitingForScan = false

    init {
        accessoryExtension.registerBarcodeResultListener { result ->
            if (!waitingForScan || result.status != NurApiErrors.NUR_SUCCESS) {
                listener.onFailure()
                return@registerBarcodeResultListener
            }
            listener.onSuccess(result.strBarcode)
            waitingForScan = false
        }
    }

    fun scanQr() {
        if (nurApi.isConnected.not()) {
            return
        }
        if (waitingForScan) {
            cancel()
        } else {
            scan()
        }
    }

    fun cancel() {
        if (waitingForScan) {
            accessoryExtension.cancelBarcodeAsync()
            waitingForScan = false
        }
    }

    private fun scan() {
        try {
            accessoryExtension.readBarcodeAsync(15000)
            waitingForScan = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
