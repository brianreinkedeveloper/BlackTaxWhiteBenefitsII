package com.blacktaxandwhitebenefits.data

import androidx.room.Entity
import androidx.room.PrimaryKey


/*
    The data being saved to the database.  We don't necessarily need to have all this data but I'm including
    it anyway.  Technically, all we need is a reference to the blog article itself.
 */

@Entity(tableName = "blacktaxsaveblog_table")
data class SavedBlog (
    // Database content
    val title: String,
    val urlLink: String,
    val date: String,           // posted date of article.
    val id: String,             //unique ID of article. This plus the title is really all we need.
    val modifiedDate: String,
    val htmlArticle: String,
    val imageBlogURL: String,

    @PrimaryKey(autoGenerate = true)
    var databaseId: Int = 0
)