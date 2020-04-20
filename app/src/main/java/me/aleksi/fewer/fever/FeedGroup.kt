package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class FeedGroup(
    val id: Long,
    val title: String,

    @Transient
    val feeds: MutableList<Feed> = mutableListOf()
) : Parcelable

@JsonClass(generateAdapter = true)
data class FeedGroupsResponse(
    val groups: List<FeedGroup>,
    val feeds_groups: List<FeedsGroupsRelationships>
)
