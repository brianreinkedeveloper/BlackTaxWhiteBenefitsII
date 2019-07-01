package com.blacktaxandwhitebenefits.adapters

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.blacktaxandwhitebenefits.ProjectData
import com.blacktaxandwhitebenefits.R
import com.blacktaxandwhitebenefits.WebViewActivity
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.Blog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.recycle_item.view.*
import java.util.*


/*
    BlogAdapter is the RecyclerView adapter for viewing of output from the website.
 */

class BlogAdapter(val myList: MutableList<Blog>): RecyclerView.Adapter<BlogAdapter.ViewHolder>() {
    private lateinit var adapterContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.adapterContext=parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycle_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = myList[position].title


        val requestOptions: RequestOptions by lazy {
            RequestOptions()
                .placeholder(R.drawable.hands_40opacity_smaller)
//                .transforms(CenterCrop())
        }

        // Note: Image URL cannot be null.
        // URL can be a non-existant URL but does need to be something.
        if (myList[position].imageBlogURL != "") {
            Glide.with(this.adapterContext)
                .load(myList[position].imageBlogURL)
                .apply(requestOptions)
                .into(holder.imageView)

        } else {
            // Load default image.
            Glide.with(this.adapterContext)
                .load(R.drawable.no_image)
                .apply(requestOptions)
                .into(holder.imageView)
        }
    }


    inner class ViewHolder (v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
        // No findViewById() because this version of Android Studio uses
        //    kotlin-android-extensions plugin, which avoids the use of needing findViewById().
        val titleView: TextView = v.text_title
        val imageView: ImageView = v.imgBlogGraphic


        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position  = layoutPosition

            val webViewDataArray = ArrayList<String>(4)
            try {
                webViewDataArray.add(0, myList[position].date)
                webViewDataArray.add(1, myList[position].title)
                webViewDataArray.add(2, myList[position].imageBlogURL)
                webViewDataArray.add(3, myList[position].htmlArticle)
                webViewDataArray.add(4, myList[position].urlLink)

                val mIntentWebViewActivity = Intent(v?.context, WebViewActivity::class.java)
                // pass in some data to the intent.
                mIntentWebViewActivity.putStringArrayListExtra(ProjectData.putExtra_BlogWebView, webViewDataArray)
                v?.context?.startActivity(mIntentWebViewActivity)
            }
            catch (e: IndexOutOfBoundsException) {
                // don't do anything.
            }
        }
    }
}
