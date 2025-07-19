package com.example.insta_android.data


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import android.widget.ImageButton
import androidx.core.view.updateLayoutParams
import com.example.insta_android.Config
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class PhotoViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)) {
    private val photoView = itemView.findViewById<ImageView>(R.id.photo)
    private val videoView = itemView.findViewById<VideoView>(R.id.video)
    var photoVideo : PhotoVideo? = null
    var bitmap : Bitmap? = null
    var id: Int = 0
    val root = Environment.getExternalStorageDirectory().getPath().toString()

    fun toggleVideo(){
        if(photoVideo == null) return

        if(photoVideo!!.className == "Video"){
            Log.i("CREATE", "Trying to load video: ${photoVideo!!.fileName}")
                Log.i(">>>> VIDEO", photoVideo.toString())
            //videoView.setVideoPath(root + "/INSTA/" + photoVideo!!.fileName!!)
            val uri = Uri.parse(Config.serverURL() +  photoVideo!!.url)
            videoView.setVideoURI(uri)
            videoView.requestFocus()
            photoView.visibility = View.INVISIBLE
            videoView.visibility = View.VISIBLE
            videoView.setOnPreparedListener { mediaPlayer ->
                var layout = videoView.layoutParams
                layout.height = mediaPlayer.videoHeight
                videoView.setLayoutParams(layout)
                mediaPlayer.isLooping = true
                videoView.start()
            }
        }
    }
    // populate data in new/reused ViewHolder
    fun bindTo(photoVideo : PhotoVideo?){
        Log.i("PAGER", photoVideo!!.toString())
        if( photoVideo!!.className == "Photo" ) {
            Log.i("CREATE", "🧨 🧨 PHOTO")
            this.photoVideo = photoVideo
            val uri = Uri.parse(Config.serverURL() + photoVideo.url)
            // photoView.setImageURI(uri)
            val bmp = getImageBitmap(Config.serverURL() + photoVideo.url!!)
            photoView.setImageBitmap(bmp)
//            this.bitmap = BitmapFactory.decodeFile(root + "/INSTA/" + photoVideo!!.fileName)
//            println("👀 loading bitmap $root/INSTA/${photoVideo!!.fileName}")
//            photoView.setImageBitmap(bitmap)
        } else if (photoVideo!!.className == "Video") {
            Log.i("CREATE", "🧨 🧨 VIDEO")
            val bmp = getImageBitmap(Config.serverURL() + photoVideo.screenshot!!)
            photoView.setImageBitmap(bmp)
            this.photoVideo = photoVideo
//            this.bitmap = BitmapFactory.decodeFile(root + "/INSTA/" + photoVideo!!.screenshot)
//            println("👀 loading bitmap $root/INSTA/${photoVideo!!.screenshot}")
//            photoView.setImageBitmap(bitmap)
        }

    }
    companion object Cache{
        var Cache =  hashMapOf<String, Bitmap?>()
    }
    fun getImageBitmap(url:String ): Bitmap? {
        if(Cache.Cache[url] != null){
            return Cache.Cache[url]
        }
        var bm:Bitmap? = null;
        try {
            val aURL:URL  = URL(url);
            val conn:URLConnection  = aURL.openConnection();
            conn.connect();
            val isf:InputStream  = conn.getInputStream();
            val bis:BufferedInputStream  = BufferedInputStream(isf);
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            bis.close()
        } catch (e: IOException) {
            Log.e("TAG", "Error getting bitmap", e)
        }
        Cache.Cache[url] = bm
        return bm;
    }
}
