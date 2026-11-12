package com.estcomputer.patient.platform.integrity

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Debug
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File

internal class SystemIntegritySources(context: Context) : IntegritySources {
    override val buildTags: String? = Build.TAGS
    override val buildType: String? = Build.TYPE
    override val isAppDebuggable: Boolean =
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    override fun fileExists(path: String): Boolean = File(path).exists()

    override fun isDebuggerConnected(): Boolean = Debug.isDebuggerConnected()
}

public class AndroidDeviceIntegrityChecker internal constructor(
    private val sources: IntegritySources,
    private val dispatchers: DispatcherProvider,
) : DeviceIntegrityChecker {

    public constructor(context: Context, dispatchers: DispatcherProvider) :
        this(SystemIntegritySources(context), dispatchers)

    override suspend fun check(): IntegrityReport =
        withContext(dispatchers.io) { IntegrityRules.evaluate(sources) }
}
