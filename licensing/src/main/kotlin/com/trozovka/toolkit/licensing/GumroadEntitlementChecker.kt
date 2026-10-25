package com.trozovka.toolkit.licensing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One-time-purchase entitlement check: a key is verified against Gumroad at most once, then
 * trusted forever after with zero further network calls -- so the consuming app keeps working
 * indefinitely offline once activated. A network failure only blocks confirming a *new,
 * never-before-verified* key; it can never revoke a key already confirmed.
 *
 * [ownerBypassCode], if non-blank and matched, grants entitlement without ever calling Gumroad --
 * for the developer's own on-device testing. Consuming apps should source it from a gitignored
 * local.properties BuildConfig field, never a hardcoded string.
 */
class GumroadEntitlementChecker(
    private val store: GumroadVerifyOnceStore,
    private val productId: String,
    private val ownerBypassCode: String = "",
) {
    suspend fun isLicensed(): Boolean {
        val key = store.licenseKey?.takeIf { it.isNotBlank() } ?: return false

        if (ownerBypassCode.isNotBlank() && key == ownerBypassCode) {
            store.lastCheckMessage = "Owner bypass code -- unlimited, no Gumroad check performed"
            return true
        }

        if (key == store.verifiedKey) {
            return true
        }

        return withContext(Dispatchers.IO) {
            when (val result = GumroadLicenseVerifier.verify(productId, key)) {
                is GumroadLicenseVerifier.Result.Valid -> {
                    store.verifiedKey = key
                    store.lastCheckMessage = "Valid -- activated for good, no further checks needed"
                    true
                }
                is GumroadLicenseVerifier.Result.Invalid -> {
                    store.lastCheckMessage = "Invalid or expired license key"
                    false
                }
                is GumroadLicenseVerifier.Result.NetworkError -> {
                    store.lastCheckMessage = "Couldn't reach Gumroad (${result.message}); try again when online"
                    false
                }
            }
        }
    }
}
