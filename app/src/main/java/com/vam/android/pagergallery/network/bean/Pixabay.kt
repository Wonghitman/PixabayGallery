package com.vam.android.pagergallery.network.bean

import com.google.gson.annotations.SerializedName

data class Pixabay(
    val hits: List<PhotoItem>,
    val total: Int,
    val totalHits: Int
) {
    data class PhotoItem(

        @SerializedName("webformatURL")val previewURL: String,
        @SerializedName("id")val photoId: Int,
        @SerializedName("largeImageURL")val fullURL: String,
        @SerializedName("webformatHeight") val photoHeight:Int,
        @SerializedName("user") val photoUser:String,
        @SerializedName("likes") val likesNum:Int,
        @SerializedName("downloads") val downloadsNum:Int,
    )
}