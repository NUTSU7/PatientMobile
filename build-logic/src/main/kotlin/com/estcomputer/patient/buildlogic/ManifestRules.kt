package com.estcomputer.patient.buildlogic

import org.w3c.dom.Element
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

internal object ManifestRules {

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private const val MAIN_ACTIVITY = "com.estcomputer.patient.MainActivity"
    private const val DYNAMIC_RECEIVER_PERMISSION = "com.estcomputer.patient.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
    private val ALLOWED_PERMISSIONS = setOf(
        "android.permission.CAMERA",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.SCHEDULE_EXACT_ALARM",
        "android.permission.RECEIVE_BOOT_COMPLETED",
        "android.permission.USE_BIOMETRIC",
        DYNAMIC_RECEIVER_PERMISSION,
    )
    private val COMPONENT_TAGS = listOf("activity", "activity-alias", "service", "receiver", "provider")
    private val PERMISSION_TAGS = listOf("uses-permission", "uses-permission-sdk-23")

    /** Returns one message per rule violation in a merged manifest. Empty means the manifest passes. */
    fun violations(manifestXml: String): List<String> {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(InputSource(StringReader(manifestXml)))
        val root = document.documentElement
        val result = mutableListOf<String>()

        PERMISSION_TAGS.forEach { tag ->
            elements(root, tag).forEach { element ->
                val name = element.androidAttr("name")
                if (name !in ALLOWED_PERMISSIONS) result += "permission not allowed: $name"
            }
        }

        val application = elements(root, "application").firstOrNull()
        if (application == null) {
            result += "manifest has no application element"
            return result
        }
        if (application.androidAttr("allowBackup") != "false") result += "allowBackup must be \"false\""
        if (application.androidAttr("debuggable") == "true") result += "debuggable must not be \"true\""
        if (application.androidAttr("usesCleartextTraffic") == "true") {
            result += "usesCleartextTraffic must not be \"true\""
        }

        COMPONENT_TAGS.forEach { tag ->
            elements(application, tag).forEach { component ->
                val name = component.androidAttr("name")
                if (isExported(component) && name != MAIN_ACTIVITY) {
                    result += "exported $tag not allowed: $name"
                }
            }
        }
        return result
    }

    private fun isExported(component: Element): Boolean {
        val exported = component.androidAttrOrNull("exported")
        return when (exported) {
            null -> elements(component, "intent-filter").isNotEmpty()
            else -> exported == "true"
        }
    }

    private fun elements(parent: Element, tag: String): List<Element> {
        val nodes = parent.getElementsByTagName(tag)
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }.filter { it.parentNode === parent }
    }

    private fun Element.androidAttrOrNull(name: String): String? =
        if (hasAttributeNS(ANDROID_NS, name)) getAttributeNS(ANDROID_NS, name) else null

    private fun Element.androidAttr(name: String): String = androidAttrOrNull(name) ?: ""
}
