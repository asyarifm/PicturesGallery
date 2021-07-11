package com.asyarifm.picturesgallery.model

import com.google.gson.annotations.SerializedName

data class ItemUrl(
    @SerializedName("thumb") val thumb: String?,
    @SerializedName("small") val small: String,
    @SerializedName("medium") val medium: String?,
    @SerializedName("regular") val regular: String?,
    @SerializedName("large") val large: String?,
    @SerializedName("full") val full: String?,
    @SerializedName("raw") val raw: String?
)
