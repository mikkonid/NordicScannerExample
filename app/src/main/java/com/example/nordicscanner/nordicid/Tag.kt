package com.example.nordicscanner.nordicid

import com.nordicid.nurapi.NurTag

fun NurTag.print(): String {
    return "Tag(timestamp=$timestamp, rssi=$rssi, scaledRssi=$scaledRssi, freq=$freq, pc=$pc, channel=$channel, antennaId=$antennaId, updateCount=$updateCount, epc=${epc?.contentToString()}, epcStr=$epcString, irData=${irData?.contentToString()}, irDataStr=$dataString, userdata=$userdata, XPC_W1=$xpC_W1, XPC_W2=$xpC_W2)"
}
