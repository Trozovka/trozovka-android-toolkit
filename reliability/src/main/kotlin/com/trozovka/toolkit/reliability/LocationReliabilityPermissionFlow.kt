package com.trozovka.toolkit.reliability

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * The fine/coarse location -> background location -> POST_NOTIFICATIONS -> battery-optimization-
 * exemption permission chain needed by any foreground service that must keep running with the
 * screen off. Must be constructed as an Activity property, not from inside onCreate --
 * ActivityResultContracts registration requires that timing.
 */
class LocationReliabilityPermissionFlow(
    private val activity: ComponentActivity,
    private val onReady: () -> Unit,
    private val onDenied: () -> Unit = {},
) {
    private var pending = false

    private val requestForegroundPermissions = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            requestBackgroundLocationIfNeeded()
        } else {
            pending = false
            onDenied()
        }
    }

    private val requestBackgroundLocation = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        requestBatteryOptimizationExemptionIfNeeded()
    }

    private val batteryOptimizationSettings = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (pending) {
            pending = false
            onReady()
        }
    }

    /** Kicks off the full chain. Calls [onReady] once every step is satisfied (or already was), [onDenied] if the core fine-location permission is refused. */
    fun begin() {
        pending = true
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            needed += Manifest.permission.ACCESS_FINE_LOCATION
            needed += Manifest.permission.ACCESS_COARSE_LOCATION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }

        if (needed.isNotEmpty()) {
            requestForegroundPermissions.launch(needed.toTypedArray())
        } else {
            requestBackgroundLocationIfNeeded()
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        val backgroundGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (!backgroundGranted) {
            requestBackgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            requestBatteryOptimizationExemptionIfNeeded()
        }
    }

    private fun requestBatteryOptimizationExemptionIfNeeded() {
        if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(activity)) {
            batteryOptimizationSettings.launch(BatteryOptimizationHelper.exemptionRequestIntent(activity))
        } else {
            pending = false
            onReady()
        }
    }
}
