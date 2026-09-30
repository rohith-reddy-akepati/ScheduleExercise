package com.yinzcam.scheduleexercise.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogoUrlBuilderTest {

    @Test
    fun `builds lowercase logo url from tricode`() {
        assertEquals(
            "http://yc-app-resources.s3.amazonaws.com/nfl/logos/nfl_phi_light.png",
            LogoUrlBuilder.logoUrl("PHI")
        )
    }

    @Test
    fun `already-lowercase tricode is unaffected`() {
        assertEquals(
            "http://yc-app-resources.s3.amazonaws.com/nfl/logos/nfl_gb_light.png",
            LogoUrlBuilder.logoUrl("gb")
        )
    }

    @Test
    fun `blank tricode returns null`() {
        assertNull(LogoUrlBuilder.logoUrl(""))
        assertNull(LogoUrlBuilder.logoUrl("   "))
    }
}
