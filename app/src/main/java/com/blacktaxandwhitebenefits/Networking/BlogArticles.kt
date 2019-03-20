package com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking

import com.google.gson.annotations.SerializedName

data class BlogArticles (
    @SerializedName("id")  var id: String,

    @SerializedName("date")  var date: String,

    @SerializedName("modified")  var modifiedDate: String,

    // Link to the full webpage.
    @SerializedName("link") val URLLink: String,


    @SerializedName("title") val title: Title,

    // Content is the HTML of the article.
    @SerializedName("content") val content: WebContent,

    // The image associated with the blog.
    @SerializedName("jetpack_featured_media_url") val imageBlogURL: String

////    val wp-featuredmedia-link: String

)