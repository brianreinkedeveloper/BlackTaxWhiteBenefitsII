package com.blacktaxandwhitebenefits.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.blacktaxandwhitebenefits.data.SavedBlog
import com.blacktaxandwhitebenefits.data.SavedBlogRepository


class SavedBlogViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: SavedBlogRepository = SavedBlogRepository(application)
    private var allSavedBlogs: LiveData<List<SavedBlog>> = repository.getAllSavedBlogs()

    // The ViewModel pulls in database functions from repository.

    fun insert(savedBlog: SavedBlog) {
        repository.insert(savedBlog)
    }

    fun update(savedBlog: SavedBlog) {
        repository.update(savedBlog)
    }

    fun delete(savedBlog: SavedBlog) {
        repository.delete(savedBlog)
    }

    fun deleteAllSavedBlogs() {
        repository.deleteAllSavedBlogs()
    }

    fun getAllSavedBlogs(): LiveData<List<SavedBlog>> {
        return allSavedBlogs
    }
}