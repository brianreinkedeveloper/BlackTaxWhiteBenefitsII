package com.blacktaxandwhitebenefits

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blacktaxandwhitebenefits.adapters.SavedBlogAdapter
import com.blacktaxandwhitebenefits.data.SavedBlog
import com.blacktaxandwhitebenefits.viewmodels.SavedBlogViewModel
import kotlinx.android.synthetic.main.activity_saved_bookmarks.*
import java.util.ArrayList


class SavedBookMarksActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "com.blacktaxandwhitebenefits.EXTRA_ID"
        const val EXTRA_TITLE = "com.blacktaxandwhitebenefits.EXTRA_TITLE"
        const val EXTRA_DESCRIPTION = "com.blacktaxandwhitebenefits.EXTRA_DESCRIPTION"
        const val EXTRA_PRIORITY = "com.blacktaxandwhitebenefits.EXTRA_PRIORITY"
    }


    private lateinit var savedBlogViewModel: SavedBlogViewModel
    private var bookMarkAdapter = SavedBlogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_bookmarks)

        this.savedBlogViewModel = ViewModelProviders.of(this).get(SavedBlogViewModel::class.java)
        this.savedBlogViewModel.getAllSavedBlogs().observe(this, Observer<List<SavedBlog>> {
            /*
                * Callback for database change.
                * This runs on initial project open and any other database action (like insert, delete, and update).
                * Note: The Observer occurs every time the screen is rotated?  Is this right?
                * ** To read the contents of the table, you must run it from here.  Otherwise,
                *    values will be null!
             */

            // Determine if the records are null.
            if (this.savedBlogViewModel.getAllSavedBlogs().value.isNullOrEmpty()) {
                bookmark_recycler_view.visibility = View.INVISIBLE
                text_bookmark_noitems.visibility = View.VISIBLE
            } else {
                bookMarkAdapter.submitList(it)
            }
        })

        // Setting up RecyclerView / adapter.
        bookmark_recycler_view.layoutManager = LinearLayoutManager(this)
        bookmark_recycler_view.setHasFixedSize(true)
        bookmark_recycler_view.adapter = this.bookMarkAdapter
        setupBookMarksCallbacks(this.bookMarkAdapter)
    }


    // ****************
    //  Setting up RecyclerView adapter callbacks for swipe events.
    // *****************
    private fun setupBookMarksCallbacks(adapter: SavedBlogAdapter) {
        // RecyclerView touch movements for onMove and onSwipe.
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                savedBlogViewModel.delete(adapter.getNoteAt(viewHolder.adapterPosition))
                Toast.makeText(baseContext, "Bookmark Deleted!", Toast.LENGTH_SHORT).show()
            }
        }
        ).attachToRecyclerView(bookmark_recycler_view)


        adapter.setOnItemClickListener(object : SavedBlogAdapter.OnItemClickListener {
            override fun onItemClick(savedBlog: SavedBlog) {
                val webViewDataArray = ArrayList<String>(6)

                webViewDataArray.add(0, savedBlog.date)         // article posted date
                webViewDataArray.add(1, savedBlog.title)
                webViewDataArray.add(2, savedBlog.imageBlogURL)
                webViewDataArray.add(3, savedBlog.htmlArticle)
                webViewDataArray.add(4, savedBlog.urlLink)
                webViewDataArray.add(5, savedBlog.id)
                webViewDataArray.add(6, resources.getString(R.string.function_bookmark))


                val mIntentWebViewActivity = Intent(baseContext, WebViewActivity::class.java)
                // pass in some data to the intent.
                mIntentWebViewActivity.putStringArrayListExtra(ProjectData.putExtra_BlogWebView, webViewDataArray)
                startActivity(mIntentWebViewActivity)
            }
        })
    }
}