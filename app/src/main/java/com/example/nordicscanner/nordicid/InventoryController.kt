package com.example.nordicscanner.nordicid

import com.nordicid.nurapi.*

private var DataWords = 2

interface InventoryControllerListener {
    fun tagFound(tag: NurTag, isNew: Boolean)
    fun readerDisconnected()
    fun readerConnected()
    fun inventoryStateChanged()
    fun IOChangeEvent(event: NurEventIOChange?)
}

class InventoryController(
    private val nurApi: NurApi,
    private val inventoryListener: InventoryControllerListener
) {
    var isInventoryRunning = false
        private set

    val nurApiListener = createNurApiListener()
    private val tagStorage = NurTagStorage()

    fun handleInventoryResult() {
        synchronized(nurApi.storage) {
            var tmp: HashMap<String, String>
            val tagStorage = nurApi.storage
            // Add tags tp internal tag storage
            for (i in 0 until tagStorage.size()) {
                var tag = tagStorage[i]
                if (tagStorage.addTag(tag)) {
                    tmp = HashMap()
                    // Add new
                    tmp["epc"] = tag.epcString
                    tmp["rssi"] = tag.rssi.toString()
                    tmp["timestamp"] = tag.timestamp.toString()
                    tmp["freq"] = tag.freq.toString() + " kHz Ch: " + tag.channel.toString()
                    tmp["found"] = "1"
                    tag.userdata = tmp
                    inventoryListener.tagFound(tag, true)
                } else { // Update
                    tag = tagStorage.getTag(tag.epc)
                    tmp = tag.userdata as? HashMap<String, String>
                        ?: HashMap<String, String>().also { tag.userdata = it }
                    tmp["rssi"] = tag.rssi.toString()
                    tmp["timestamp"] = tag.timestamp.toString()
                    tmp["freq"] = tag.freq.toString() + " kHz (Ch: " + tag.channel.toString() + ")"
                    tmp["found"] = tag.updateCount.toString()
                    inventoryListener.tagFound(tag, false)
                }
            }
            tagStorage.clear()
        }
    }

    private fun prepareDataInventory() {
        val irConfig = NurIRConfig()
        irConfig.IsRunning = false
        irConfig.irType = NurApi.IRTYPE_EPCDATA
        irConfig.irAddr = 0
        irConfig.irWordCount = DataWords
        try {
            nurApi.irConfig = irConfig
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun startContinuousInventory(): Boolean {
        if (!nurApi.isConnected) return false
        // Enable inventory stream zero reading report
        if (nurApi.setupOpFlags and NurApi.OPFLAGS_INVSTREAM_ZEROS == 0) {
            nurApi.setupOpFlags = nurApi.setupOpFlags or NurApi.OPFLAGS_INVSTREAM_ZEROS
        }
        // Make sure antenna autoswitch is enabled
        if (nurApi.setupSelectedAntenna != NurApi.ANTENNAID_AUTOSELECT) {
            nurApi.setupSelectedAntenna = NurApi.ANTENNAID_AUTOSELECT
        }
        prepareDataInventory()
        isInventoryRunning = true
        nurApi.startInventoryStream()
        inventoryListener.inventoryStateChanged()
        return true
    }

    fun stopInventory() {
        try {
            isInventoryRunning = false
            if (nurApi.isConnected) {
                nurApi.stopInventoryStream()
                val ir = NurIRConfig()
                ir.IsRunning = false
                nurApi.irConfig = ir
                nurApi.setupOpFlags = nurApi.setupOpFlags and NurApi.OPFLAGS_INVSTREAM_ZEROS.inv()
            }
        } catch (err: Exception) {
            err.printStackTrace()
        }
        inventoryListener.inventoryStateChanged()
    }

    private fun createNurApiListener() = object : NurApiListener {
        override fun inventoryStreamEvent(event: NurEventInventory) {
            handleInventoryResult()
            // Restart reading if needed
            if (event.stopped && isInventoryRunning) {
                try {
                    nurApi.startInventoryStream()
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        }

        override fun connectedEvent() {
            inventoryListener.readerConnected()
        }

        override fun disconnectedEvent() {
            inventoryListener.readerDisconnected()
            stopInventory()
        }

        override fun IOChangeEvent(event: NurEventIOChange) {
            inventoryListener.IOChangeEvent(event)
        }

        override fun bootEvent(arg0: String) {}
        override fun clientConnectedEvent(arg0: NurEventClientInfo) {}
        override fun clientDisconnectedEvent(arg0: NurEventClientInfo) {}
        override fun deviceSearchEvent(arg0: NurEventDeviceInfo) {}
        override fun frequencyHopEvent(arg0: NurEventFrequencyHop) {}
        override fun inventoryExtendedStreamEvent(arg0: NurEventInventory) {}
        override fun nxpEasAlarmEvent(arg0: NurEventNxpAlarm) {}
        override fun programmingProgressEvent(arg0: NurEventProgrammingProgress) {}
        override fun traceTagEvent(arg0: NurEventTraceTag) {}
        override fun triggeredReadEvent(arg0: NurEventTriggeredRead) {}
        override fun logEvent(arg0: Int, arg1: String) {}
        override fun debugMessageEvent(arg0: String) {}
        override fun epcEnumEvent(event: NurEventEpcEnum) {}
        override fun autotuneEvent(event: NurEventAutotune) {}
        override fun tagTrackingScanEvent(event: NurEventTagTrackingData) {}
        override fun tagTrackingChangeEvent(event: NurEventTagTrackingChange) {}
    }
}
