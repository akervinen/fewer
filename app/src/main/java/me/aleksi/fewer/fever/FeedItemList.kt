package me.aleksi.fewer.fever

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedItemList(
    val api_version: Int,
    val auth: Int,
    val last_refreshed_on_time: String,
    val total_items: Int,
    val items: List<FeedItem>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.createTypedArrayList(FeedItem)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(api_version)
        parcel.writeInt(auth)
        parcel.writeString(last_refreshed_on_time)
        parcel.writeInt(total_items)
        parcel.writeTypedList(items)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FeedItemList> {
        override fun createFromParcel(parcel: Parcel): FeedItemList {
            return FeedItemList(parcel)
        }

        override fun newArray(size: Int): Array<FeedItemList?> {
            return arrayOfNulls(size)
        }
    }
}
