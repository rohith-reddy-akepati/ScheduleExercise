package com.yinzcam.scheduleexercise.util

import java.util.Locale

/**
 * Builds team logo URLs from a team's TriCode.
 *
 * Per the assessment spec, logos live at:
 *   http://yc-app-resources.s3.amazonaws.com/nfl/logos/
 * and are named "nfl_{tricode}_light.png", e.g.
 *   http://yc-app-resources.s3.amazonaws.com/nfl/logos/nfl_phi_light.png
 * (the spec's own example is http, not https - kept as given; see the network
 * security config, which allows cleartext traffic to this specific S3 host).
 */
object LogoUrlBuilder {

    private const val LOGO_BASE_URL = "http://yc-app-resources.s3.amazonaws.com/nfl/logos/"

    fun logoUrl(triCode: String): String? {
        if (triCode.isBlank()) return null
        return LOGO_BASE_URL + "nfl_" + triCode.lowercase(Locale.US) + "_light.png"
    }
}
