package com.example.insta_android.data

import android.opengl.Visibility
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.insta_android.Config
import com.example.insta_android.data.model.Photo

class PhotoAdapter(): PagedListAdapter<Photo , PhotoViewHolder>(diffCallback){
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int){
        holder.bindTo(getItem(position))
        holder.itemView.setOnClickListener {
            println("CLICKED💥");
            val id = holder.id;
            val db = AppDatabase.getDatabase(Config.context!!);
            holder.photo?.let { it1 -> db!!.photoDao().delete(it1) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): PhotoViewHolder = PhotoViewHolder(parent)
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo , newItem: Photo): Boolean =
                oldItem.fileName == newItem.fileName // TODO: maybe Url is more unique.

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: Photo , newItem: Photo): Boolean =
                oldItem == newItem // in Kotlin == and in java is .equals.
        }
    }
}
