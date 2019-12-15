package com.example.insta_android.data

import com.example.insta_android.data.model.Photo
import okhttp3.*

import java.io.IOException
import android.content.Context
import android.os.Environment
import android.os.StrictMode
import android.util.Base64.decode
import com.example.insta_android.Config
import com.example.insta_android.MainActivity
import com.example.insta_android.R
import com.querydsl.sql.types.StringAsObjectType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*

class PhotoDataSource (var context:Context){
    val DATABASE = "photos"
    var db: AppDatabase? = null
    fun FeedSync(){

    }

    class PhotoJson(s: String, s1: String) {
        fun Photo(){}
        var url: String = ""
        var name: String = ""
        var filename: String = ""
        var image: String = ""
    }
    private var moshi = Moshi.Builder().build()
    private var photoJsonAdapter = moshi.adapter(MainActivity.Photo::class.java)

    fun fetch_images(url: String): List<PhotoJson>? {
        var str = arrayOf("")

        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var token = preferences.getString("auth_token","")
        var email = preferences.getString("user_email","")
        System.out.printf("%s %s\n", token, email)

        print("request\n")
        var request = Request.Builder()
            .header("X-User-Email", email)
            .header("X-User-Token", token)
            .header("ContentType","application/json")
            .header("Accept", "application/json")
            .url(url)
            .get()
            .build()


        var client = OkHttpClient()
        var resp = client.newCall(request).execute()

        resp.use {
            if(!it!!.isSuccessful){
                throw IOException("no images for u!")
            }
            var text = resp.body!!.source().readString(Charset.defaultCharset())

            var ListType = Types.newParameterizedType(List::class.java, PhotoJson::class.java)
            var adapter: JsonAdapter<List<PhotoJson>> = moshi.adapter(ListType)
            var data:List<PhotoJson>? = adapter.fromJson(text)
            System.out.printf(">>>>>>>>>>>>>> TEXT: %s \n", text)
            return data
        }

        var blank: List<PhotoJson>? = List<PhotoJson>(0){
            PhotoJson(
                "hello",
                "hi"
            )
        }
        return  blank
    }

    fun sync() {
        // 1. make network connection
        // 2. get json of the feed and parse it
        // 3. load all files
        // 4.
        val photoStream = photoStreamURL()
        System.out.printf("######   conneting %s\n", photoStream
        )
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //val root = Environment.getExternalStorageDirectory().getPath().toString()
        //File(root + "/INSTA").walk().forEach {
        //    it.delete()
        //}

        db = AppDatabase.getDatabase(context)
        db!!.photoDao().deleteAll()
        val images = fetch_images(photoStream)
        val root = Environment.getExternalStorageDirectory().getPath().toString()
        images!!.forEach {
            println(it.filename)
            println(it)
            val write = File(root + "/INSTA/" + it.filename)
            try {
                write.writeBytes(Base64.getMimeDecoder().decode(it.image))
            }catch(e: Exception){
                println("image ${it.filename} is not readable ${e}.")
            }
        }
//        images!!.forEach {
//            val photo = Photo(it.url!! , it.name!! , "")
//            db!!.photoDao().insertAll(photo)
//            // TODO load the image too. from the link
//            loadPhoto(Config.serverURL() + it.url!! , it.name)
//        }

    }


    private fun photoStreamURL(): String {
        return Config.serverURL() + "/photos.json"
    }

    private fun loadPhoto(url:String, name:String){
        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var token = preferences.getString("auth_token","")
        var email = preferences.getString("user_email","")
        System.out.printf("%s %s \n", token, email)
        var request = Request.Builder()
            .header("X-User-Email", email)
            .header("X-User-Token", token)
            .url(url)
            .get()
            .build()

        var client = OkHttpClient()
        var response = client.newCall(request).execute()

        response.use{
            if(!it.isSuccessful){
                throw IOException("Failed to load the image $url")
            }
            val root = Environment.getExternalStorageDirectory().getPath().toString()

            val dir = File("$root/INSTA")

            val file = File.createTempFile(
                name, /* prefix */
                ".jpg", /* suffix */
                dir /* directory */
            )
            file!!.writeBytes(it.body!!.bytes())
        }
    }


    private fun loadPhotoFeed(){
        val feed = photoStreamURL()

        // GET
        val request = Request.Builder()
            .header("ContentType","application/json")
            .header("Accept", "application/json")
            .url(photoStreamURL())
            .get()
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        response.use {
            if(!it.isSuccessful){
                throw IOException("unexpected: " + it)
            }
        }
    }
}