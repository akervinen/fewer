package me.aleksi.fewer.fever

import com.squareup.moshi.JsonClass

/**
 * Authentication response by Fever API.
 *
 * Every response includes [api_version] and [auth], but only authenticated responses include
 * [last_refreshed_on_time].
 *
 * @param[api_version] version of Fever API
 * @param[auth] whether request was authenticated: 0 or 1
 * @param[last_refreshed_on_time] when feeds were last refreshed
 */
@JsonClass(generateAdapter = true)
data class FeverAuthResponse(
    val api_version: Int,
    val auth: Int,
    val last_refreshed_on_time: String?
)
