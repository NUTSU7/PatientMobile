package com.estcomputer.patient.log

import android.util.Log
import com.estcomputer.patient.domain.util.LogCode
import com.estcomputer.patient.domain.util.PatientLog

internal class AndroidPatientLog(private val isDebug: Boolean) : PatientLog {
    override fun event(code: LogCode) {
        if (isDebug) Log.d(TAG, code.name)
    }

    private companion object {
        const val TAG = "Patient"
    }
}
