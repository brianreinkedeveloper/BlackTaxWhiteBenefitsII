package com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking

data class Blog (
    // This List is what is getting passed into the RecyclerView.  These should not be changed ever!
    val title: String,
    val urlLink: String,
    val date: String,
    val id: String,                  //unique ID of article.
    val modifiedDate: String,
    val htmlArticle: String,
    val imageBlogURL: String
)