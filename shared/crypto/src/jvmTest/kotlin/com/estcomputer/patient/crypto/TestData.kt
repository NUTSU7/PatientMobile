package com.estcomputer.patient.crypto

import java.io.File

internal object TestData {
    private val root = File("../../testdata/crypto")

    fun text(name: String): String = File(root, name).readText()

    fun bytes(name: String): ByteArray = File(root, name).readBytes()
}
