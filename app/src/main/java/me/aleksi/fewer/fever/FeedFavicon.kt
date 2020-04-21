package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class FeedFavicon(
    val id: Long,
    val data: String
) : Parcelable

@JsonClass(generateAdapter = true)
data class FeedFaviconsResponse(
    val favicons: List<FeedFavicon>
)
