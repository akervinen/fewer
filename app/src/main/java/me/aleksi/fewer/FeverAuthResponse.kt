package me.aleksi.fewer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeverAuthResponse(val api_version: Int, val auth: Int, val last_refreshed_on_time: Int?)
