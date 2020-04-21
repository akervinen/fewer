package me.aleksi.fewer.fever

import android.graphics.Bitmap
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Feed(
    val id: Long,
    val favicon_id: Long,
    val title: String,
    val url: String,
    val site_url: String,
    val is_spark: Int,
    val last_updated_on_time: Long,

    @Transient
    var favicon: Bitmap? = null
) : Parcelable

@JsonClass(generateAdapter = true)
data class FeedsResponse(
    val feeds: List<Feed>,
    val feeds_groups: List<FeedsGroupsRelationships>
)
