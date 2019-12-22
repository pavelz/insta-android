package com.example.insta_android.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.insta_android.R
import com.example.insta_android.data.model.Photo
import java.io.File

class PhotoViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)) {
    private val photoView = itemView.findViewById<ImageView>(R.id.photo)
    var photo : Photo? = null
    var bitmap : Bitmap? = null
    val root = Environment.getExternalStorageDirectory().getPath().toString()

    // populate data in new/reused viewholder
    fun bindTo(photo : Photo?){
        this.photo = photo
        this.bitmap = BitmapFactory.decodeFile(root + "/INSTA/" + photo!!.fileName)
        photoView.setImageBitmap(bitmap)
    }
}
