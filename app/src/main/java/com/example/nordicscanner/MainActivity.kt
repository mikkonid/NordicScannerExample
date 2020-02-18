package com.example.nordicscanner

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nordicscanner.nordicid.*
import com.nordicid.nurapi.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val nurApi = NurApi()
    private val connector = Connector(nurApi)
    private val inventoryController = InventoryController(nurApi, createInventoryListener())
    private val barcodeScanner = BarcodeScanner(nurApi, createBarcodeListener())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BleScanner.init(this)
        nurApi.listener = createNurApiListener()

        buttonConnect.setOnClickListener {
            connector.connect(this, inputDeviceAddress.text.toString())
        }
        buttonScanRfid.setOnClickListener {
            if (inventoryController.isInventoryRunning) {
                inventoryController.stopInventory()
            } else {
                inventoryController.startContinuousInventory()
            }
        }
        buttonScanQr.setOnClickListener {
            barcodeScanner.scanQr()
        }
        buttonClear.setOnClickListener {
            labelStatus.text = ""
        }
    }

    override fun onPause() {
        super.onPause()
        connector.disconnect()
    }

    private fun createNurApiListener() = object : NurApiListener {
        @SuppressLint("SetTextI18n")
        override fun connectedEvent() {
            inventoryController.nurApiListener.connectedEvent()
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nConnected"
            }
        }

        override fun disconnectedEvent() {
            inventoryController.nurApiListener.disconnectedEvent()
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nDisconnected"
            }
        }

        override fun inventoryStreamEvent(event: NurEventInventory) {
            inventoryController.nurApiListener.inventoryStreamEvent(event)
        }

        override fun IOChangeEvent(event: NurEventIOChange) {
            inventoryController.nurApiListener.IOChangeEvent(event)
        }

        override fun debugMessageEvent(p0: String?) {}

        override fun logEvent(p0: Int, p1: String?) {}

        override fun clientConnectedEvent(p0: NurEventClientInfo?) {}

        override fun autotuneEvent(p0: NurEventAutotune?) {}

        override fun clientDisconnectedEvent(p0: NurEventClientInfo?) {}

        override fun frequencyHopEvent(p0: NurEventFrequencyHop?) {}

        override fun nxpEasAlarmEvent(p0: NurEventNxpAlarm?) {}

        override fun programmingProgressEvent(p0: NurEventProgrammingProgress?) {}

        override fun bootEvent(p0: String?) {}

        override fun epcEnumEvent(p0: NurEventEpcEnum?) {}

        override fun inventoryExtendedStreamEvent(p0: NurEventInventory?) {}

        override fun tagTrackingScanEvent(p0: NurEventTagTrackingData?) {}

        override fun triggeredReadEvent(p0: NurEventTriggeredRead?) {}

        override fun traceTagEvent(p0: NurEventTraceTag?) {}

        override fun tagTrackingChangeEvent(p0: NurEventTagTrackingChange?) {}

        override fun deviceSearchEvent(p0: NurEventDeviceInfo?) {}
    }

    private fun createInventoryListener() = object : InventoryControllerListener {
        private val cache = HashMap<String, NurTag>()

        override fun tagFound(tag: NurTag, isNew: Boolean) {
            if (!cache.containsKey(tag.epcString)) {
            }
            cache[tag.epcString] = tag
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nTagFound: ${tag.print()}, isNew: $isNew"
            }
        }

        override fun readerDisconnected() {
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nReader disconnected"
            }
        }

        override fun readerConnected() {
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nReader connected"
            }
        }

        override fun inventoryStateChanged() {
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nInventory State Changed"
            }
        }

        override fun IOChangeEvent(event: NurEventIOChange?) {
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nIOChangeEvent: $event"
            }
        }
    }

    private fun createBarcodeListener() = object : BarcodeScannerListener {
        override fun onSuccess(result: String) {
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nQR code: $result"
            }
        }

        override fun onFailure() {
            runOnUiThread {
                labelStatus.text = "${labelStatus.text}\nFailed to scan QR code"
            }
        }

    }
}
