package com.trozovka.toolkit.licensing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GumroadLicenseVerifierTest {

    @Test
    fun requestBodySendsProductIdNotPermalink() {
        val body = GumroadLicenseVerifier.requestBody("aPlo00yFqtyRLIbq6ZyjzQ==", "ABCD-1234")
        assertEquals("product_id=aPlo00yFqtyRLIbq6ZyjzQ%3D%3D&license_key=ABCD-1234", body)
        assertFalse(body.contains("product_permalink"))
    }

    @Test
    fun blankProductIdIsInvalidWithoutNetworkCall() {
        assertEquals(GumroadLicenseVerifier.Result.Invalid, GumroadLicenseVerifier.verify("", "ABCD-1234"))
    }
}
