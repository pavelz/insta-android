package com.example.insta_android.data

import com.example.insta_android.data.model.Photo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import
class PhotoDataSource {
    val DATABASE = "photos"
    var db: AppDatabase? = null
    fun FeedSync(){

    }

    fun sync(){
        // 1. make network connection
        // 2. get json of the feed and parse it
        // 3. load all files
        // 4.
        db = AppDatabase.getDatabase(getApplicationContext())
        var photo = Photo( "hi")
        db!!.photoDao().insertAll(photo)


    private fun loadPhotoFeed(){
        val feed = "http://kek.arslogi.ca:3000/photos.json"

        // GET
        var request = Request.Builder()
            .header("ContentType","application/json")
            .header("Accept", "application/json")
            .url(url)
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