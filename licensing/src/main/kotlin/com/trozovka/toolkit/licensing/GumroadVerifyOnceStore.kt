package com.trozovka.toolkit.licensing

import android.content.Context

/**
 * SharedPreferences-backed cache for the verify-once-trust-forever pattern: once a key is
 * confirmed valid against Gumroad, it's remembered so the app never has to re-check it, even
 * offline forever after. [prefsName] should be unique per consuming app to avoid collisions
 * if a device ever runs more than one Trozovka app sharing this library.
 */
class GumroadVerifyOnceStore(context: Context, prefsName: String) {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    var licenseKey: String?
        get() = prefs.getString(KEY_LICENSE, null)
        set(value) = prefs.edit().putString(KEY_LICENSE, value).apply()

    var verifiedKey: String?
        get() = prefs.getString(KEY_VERIFIED_KEY, null)
        set(value) = prefs.edit().putString(KEY_VERIFIED_KEY, value).apply()

    var lastCheckMessage: String
        get() = prefs.getString(KEY_LAST_MESSAGE, "Not yet verified") ?: "Not yet verified"
        set(value) = prefs.edit().putString(KEY_LAST_MESSAGE, value).apply()

    companion object {
        private const val KEY_LICENSE = "license_key"
        private const val KEY_VERIFIED_KEY = "verified_key"
        private const val KEY_LAST_MESSAGE = "last_check_message"
    }
}
