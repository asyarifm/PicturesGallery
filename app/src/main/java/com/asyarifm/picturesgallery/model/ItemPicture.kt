package com.asyarifm.picturesgallery.model

import com.google.gson.annotations.SerializedName

data class ItemPicture(
    @SerializedName("id") val id: String,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("color") val color: String? = "#000000",
    @SerializedName("likes") val likes: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("alt_description") val alt_description: String?,
    @SerializedName("urls") val urls: ItemUrl,
    @SerializedName("links") val links: ItemLink,
    @SerializedName("user") val user: ItemUser
)