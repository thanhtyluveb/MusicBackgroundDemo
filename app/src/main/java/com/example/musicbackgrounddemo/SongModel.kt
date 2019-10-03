package com.example.musicbackgrounddemo

import android.os.Parcel
import android.os.Parcelable

data class SongModel(
    var nameSong: String? = "",
    var authorSong: String?="",
    var songLocalUri: Int = 0,
    var imgSong: String? = "1234"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameSong)
        parcel.writeString(authorSong)
        parcel.writeInt(songLocalUri)
        parcel.writeString(imgSong)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongModel> {
        override fun createFromParcel(parcel: Parcel): SongModel {
            return SongModel(parcel)
        }

        override fun newArray(size: Int): Array<SongModel?> {
            return arrayOfNulls(size)
        }
    }
}