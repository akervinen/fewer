package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class FeedItemList(
    val api_version: Int,
    val auth: Int,
    val last_refreshed_on_time: String,
    val total_items: Int,
    val items: List<FeedItem>
) : Parcelable
