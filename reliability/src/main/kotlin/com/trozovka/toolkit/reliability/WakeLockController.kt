package com.trozovka.toolkit.reliability

import android.content.Context
import android.os.PowerManager

/**
 * Owns a single partial wake lock keyed by [tag]. Intended for a single foreground service's
 * own lifecycle (acquire in onCreate, release in onDestroy) -- not thread-safe for concurrent
 * acquire/release from multiple callers.
 */
class WakeLockController(private val context: Context, private val tag: String) {
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire() {
        if (wakeLock?.isHeld == true) return
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    fun release() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    val isHeld: Boolean
        get() = wakeLock?.isHeld == true
}
