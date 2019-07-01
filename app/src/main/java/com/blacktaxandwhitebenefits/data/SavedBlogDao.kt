package com.blacktaxandwhitebenefits.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SavedBlogDao {
    @Insert
    fun insert(savedBlog: SavedBlog)

    @Update
    fun update(savedBlog: SavedBlog)

    @Delete
    fun delete(savedBlog: SavedBlog)

    @Query("DELETE FROM blacktaxsaveblog_table")
    fun deleteAllSavedBlogs()

    @Query("SELECT * FROM blacktaxsaveblog_table ORDER BY date DESC")
    fun getAllSavedBlogs(): LiveData<List<SavedBlog>>
}