package me.aleksi.fewer.fever

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedItem(
    val id: Long,
    val feed_id: Int,
    val title: String,
    val author: String,
    val html: String,
    val url: String,
    val is_saved: Int,
    val is_read: Int,
    val created_on_time: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(feed_id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(html)
        parcel.writeString(url)
        parcel.writeInt(is_saved)
        parcel.writeInt(is_read)
        parcel.writeLong(created_on_time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FeedItem> {
        override fun createFromParcel(parcel: Parcel): FeedItem {
            return FeedItem(parcel)
        }

        override fun newArray(size: Int): Array<FeedItem?> {
            return arrayOfNulls(size)
        }
    }
}
