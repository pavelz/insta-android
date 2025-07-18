package com.example.insta_android.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.icu.text.DisplayContext
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.insta_android.Config
import com.example.insta_android.Config.Code.context
import com.example.insta_android.R
import com.example.insta_android.data.model.PhotoVideo

class PhotoAdapter(): PagedListAdapter<PhotoVideo , PhotoViewHolder>(diffCallback){
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int){
        holder.bindTo(getItem(position))

        val deleteButton = holder.itemView.findViewById<ImageButton>(R.id.deletePhotoVideo)
        val copyLink = holder.itemView.findViewById<ImageButton>(R.id.copyLink)
        deleteButton.setOnClickListener {
            // do post delete
            //
        }
        copyLink.setOnClickListener {
            Toast.makeText(context, "copy link", Toast.LENGTH_SHORT ).show()
            var clipboard = Config.context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            val clip = ClipData.newPlainText("simple text", "url to post")
            clipboard.setPrimaryClip(clip)
        }
        holder.itemView.setOnClickListener {
            println("CLICKED💥");
            val id = holder.id;

            val db = AppDatabase.getDatabase(Config.context!!);
            //holder.photoVideo?.let { it1 -> db!!.photoDao().delete(it1) }
            Log.i("CREATE", "${holder.photoVideo}")
            holder.toggleVideo()
            Log.i("🥳 CLICK", holder.photoVideo.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): PhotoViewHolder = PhotoViewHolder(parent)
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PhotoVideo>() {
            override fun areItemsTheSame(oldItem: PhotoVideo , newItem: PhotoVideo): Boolean =
                oldItem.fileName == newItem.fileName // TODO: maybe Url is more unique.

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: PhotoVideo , newItem: PhotoVideo): Boolean =
                oldItem == newItem // in Kotlin == and in java is .equals.
        }
    }
}
