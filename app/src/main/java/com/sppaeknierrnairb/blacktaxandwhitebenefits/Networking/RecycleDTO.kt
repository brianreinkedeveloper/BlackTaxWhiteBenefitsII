package com.sppaeknierrnairb.blacktaxandwhitebenefits.Networking

data class RecycleDTO (
    // This List is what is getting passed into the RecyclerView.
    val title: String,
    val urlLink: String,
    val date: String,
    val id: String,
    val modifiedDate: String,
    val htmlArticle: String,
    val imageBlogURL: String

)