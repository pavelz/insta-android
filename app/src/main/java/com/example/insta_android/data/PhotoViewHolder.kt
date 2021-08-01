package com.example.insta_android.data


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.insta_android.R
import com.example.insta_android.data.model.PhotoVideo
import android.util.Log
import android.view.View

class PhotoViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)) {
    private val photoView = itemView.findViewById<ImageView>(R.id.photo)
    private val videoView = itemView.findViewById<VideoView>(R.id.video)
    var photoVideo : PhotoVideo? = null
    var bitmap : Bitmap? = null
    var id: Int = 0
    val root = Environment.getExternalStorageDirectory().getPath().toString()
    fun toggleVideo(){

        if(photoVideo!!.className == "Video"){
            Log.i("VIDEO CLICK", "Trying to load video: ${photoVideo!!.fileName}")
            videoView.setVideoPath(root + "/INSTA/" + photoVideo!!.fileName!!)
            photoView.visibility = View.INVISIBLE
            videoView.visibility = View.VISIBLE
            videoView.start()
        }
    }
    // populate data in new/reused ViewHolder
    fun bindTo(photoVideo : PhotoVideo?){
        Log.i("PAGER", photoVideo!!.toString())
        if( photoVideo!!.className == "Photo" ) {
            Log.i("Yee", "🧨 🧨 PHOTO")
            this.photoVideo = photoVideo
            this.bitmap = BitmapFactory.decodeFile(root + "/INSTA/" + photoVideo!!.fileName)
            println("👀 loading bitmap $root/INSTA/${photoVideo!!.fileName}")
            photoView.setImageBitmap(bitmap)
        } else if (photoVideo!!.className == "Video") {
            Log.i("Yee", "🧨 🧨 VIDEO")
            this.photoVideo = photoVideo
            this.bitmap = BitmapFactory.decodeFile(root + "/INSTA/" + photoVideo!!.screenshot)
            println("👀 loading bitmap $root/INSTA/${photoVideo!!.screenshot}")
            photoView.setImageBitmap(bitmap)
        }
    }
}
