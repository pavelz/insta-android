package com.example.insta_android.data

import com.example.insta_android.data.model.Photo
import okhttp3.*

import java.io.IOException
import android.content.Context

class PhotoDataSource (var context:Context){
    val DATABASE = "photos"
    var db: AppDatabase? = null
    fun FeedSync(){

    }

    fun sync(){
        // 1. make network connection
        // 2. get json of the feed and parse it
        // 3. load all files
        // 4.
        db = AppDatabase.getDatabase(context)
        var photo = Photo( "hi")
        db!!.photoDao().insertAll(photo)
    }
    private fun loadPhotoFeed(){
        val feed = "http://kek.arslogi.ca:3000/photos.json"

        // GET
        var request = Request.Builder()
            .header("ContentType","application/json")
            .header("Accept", "application/json")
            .url("http://kek.arslogi.ca:3001/photos.json")
            .get()
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        response.use {
            if(!it!!.isSuccessful){
                throw IOException("unexpected: " + it)
            }
        }
    }
}