package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class FeedItem(
    val id: Long,
    val feed_id: Long,
    val title: String,
    val author: String,
    val html: String,
    val url: String,
    val is_saved: Int,
    var is_read: Int,
    val created_on_time: Long,

    @Transient
    var feed: Feed? = null
) : Parcelable
