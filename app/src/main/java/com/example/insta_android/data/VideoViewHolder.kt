package com.example.insta_android.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.insta_android.R
import com.example.insta_android.data.model.PhotoVideo

class VideoViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)) {
    private val photoView = itemView.findViewById<ImageView>(R.id.photo)
    var photoVideo : PhotoVideo? = null
    var bitmap : Bitmap? = null
    var id: Int = 0
    val root = Environment.getExternalStorageDirectory().getPath().toString()

    // populate data in new/reused ViewHolder
    fun bindTo(photoVideo : PhotoVideo?){
        this.photoVideo = photoVideo
        this.bitmap = BitmapFactory.decodeFile(root + "/INSTA/" + photoVideo!!.fileName )
        println("👀 loading bitmap $root + /INSTA/ + $photoVideo!!.fileName")
        photoView.setImageBitmap(bitmap)
    }
}
