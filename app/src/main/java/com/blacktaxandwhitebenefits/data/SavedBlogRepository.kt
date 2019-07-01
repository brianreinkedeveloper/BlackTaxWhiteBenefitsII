package com.blacktaxandwhitebenefits.data

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SavedBlogRepository(application: Application) {

    private var savedBlogDao: SavedBlogDao
    private var allSavedBlogs: LiveData<List<SavedBlog>>

    init {
        val database: SavedBlogDatabase = SavedBlogDatabase.getInstance(application.applicationContext)!!
        savedBlogDao = database.savedBlogDao()
        allSavedBlogs = savedBlogDao.getAllSavedBlogs()
    }

    fun insert(savedBlog: SavedBlog) = GlobalScope.launch(Dispatchers.IO) {
        savedBlogDao.insert(savedBlog)
    }

    fun update(savedBlog: SavedBlog) = GlobalScope.launch(Dispatchers.IO) {
        savedBlogDao.update(savedBlog)
    }

    fun delete(savedBlog: SavedBlog) = GlobalScope.launch(Dispatchers.IO) {
        savedBlogDao.delete(savedBlog)
    }

    fun deleteAllSavedBlogs() = GlobalScope.launch(Dispatchers.IO) {
        savedBlogDao.deleteAllSavedBlogs()
    }

    fun getAllSavedBlogs(): LiveData<List<SavedBlog>> {
        return allSavedBlogs
    }
}