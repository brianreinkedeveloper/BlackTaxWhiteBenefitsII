package com.blacktaxandwhitebenefits.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blacktaxandwhitebenefits.R
import com.blacktaxandwhitebenefits.data.SavedBlog
import kotlinx.android.synthetic.main.recycle_item.view.*
import kotlinx.android.synthetic.main.saved_bookmark_item.view.*


/*
    SavedBlogAdapter is the adapter to show saved items from our database.
 */

class SavedBlogAdapter : ListAdapter<SavedBlog, SavedBlogAdapter.ViewHolderBlog>(DIFF_CALLBACK){
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(note: SavedBlog)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SavedBlog>() {
            override fun areItemsTheSame(oldItem: SavedBlog, newItem: SavedBlog): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SavedBlog, newItem: SavedBlog): Boolean {
//                return oldItem.title == newItem.title && oldItem.description == newItem.description
//                        && oldItem.priority == newItem.priority
                return oldItem == newItem
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBlog {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.saved_bookmark_item, parent, false)
        return ViewHolderBlog(itemView)
    }


    override fun onBindViewHolder(holder: ViewHolderBlog, position: Int) {
        val currentBlog: SavedBlog = getItem(position)
        holder.textBookmarkTitle.text = currentBlog.title
    }

    fun getNoteAt(position: Int): SavedBlog {
        return getItem(position)
    }

    inner class ViewHolderBlog(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textBookmarkTitle: TextView = itemView.text_bookmark_title

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(getItem(position))
                }
            }
        }
    }
}
