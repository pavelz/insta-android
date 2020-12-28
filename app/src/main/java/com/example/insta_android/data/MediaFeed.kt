package com.example.insta_android.data

import com.example.insta_android.data.model.Photo
import okhttp3.*

import java.io.IOException
import android.content.Context
import android.os.Environment
import android.os.StrictMode
import com.example.insta_android.Config
import com.example.insta_android.MainActivity
import com.example.insta_android.data.model.Video
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset
import java.security.MessageDigest

class MediaFeed (var context:Context){
    val DATABASE = "photos"
    var db: AppDatabase? = null
    fun FeedSync(){

    }

    class MediaJson(s: String , s1: String) {
        var url: String = ""
        var name: String = ""
        var filename: String = ""
        var image: String = "" // TODO not being used atm, but not sure if binary loading from rails is ok... maybe from golang
        var screenshot: String = ""
        @field:Json(name = "class")
        var className: String = ""
    }

    private var moshi = Moshi.Builder().build()
    private var photoJsonAdapter = moshi.adapter(MainActivity.Photo::class.java)

    fun fetch_images(url: String): List<MediaJson>? {
        var str = arrayOf("")

        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var token = preferences.getString("auth_token","")
        var email = preferences.getString("user_email","")
        System.out.printf("%s %s\n", token, email)

        print("request\n")
        println("🏀 User email: ${email}")
        println("🏀 User token: ${token}")

        var request = Request.Builder()
            .header("X-User-Email", email)
            .header("X-User-Token", token)
            .header("Content-Type","application/json")
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
            print("starting to decode")
            var text = resp.body!!.source().readString(Charset.defaultCharset())
            print("decoded")

            var ListType = Types.newParameterizedType(List::class.java, MediaJson::class.java)
            var adapter: JsonAdapter<List<MediaJson>> = moshi.adapter(ListType)
            var data:List<MediaJson>? = adapter.fromJson(text)
            System.out.printf(">>>>>>>>>>>>>> TEXT: %s\n", text)
            return data
        }
    }

    fun sync(done: ()-> Any = {println("NO CALLBACK GIVEN")}) {
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
        //db!!.photoDao().getPAll().toLiveData(10)
        db!!.photoDao().deleteAll()
        val images = fetch_images(photoStream)
        val root = Environment.getExternalStorageDirectory().getPath().toString()
        images!!.forEach {
            print("IT: $it")
            if(it.className == "Photo") {
                var photo = Photo(it.url!! , it.name!! , "")
                val write = File(root + "/INSTA/" + it.filename)

                try {
                    // TODO: load file for 'url' into the dir. DONE
                    photo = loadPhoto(photo)
                    db!!.photoDao().insertAll(photo)
                    // write.writeBytes(Base64.getMimeDecoder().decode(it.image))
                } catch (e: Exception) {
                    println("image ${it.filename} is not readable ${e}.")
                }
            }

            if(it.className == "Video"){
                println("🎥 Video 💥")
//                val Video = Video(it.url!!, "video.mp4", it.filename ,it.screenshot)
//                var write = File(root + "/INSA/" +  it.filename)

            }
        }
        done.invoke()
    }


    private fun photoStreamURL(): String {
        return Config.serverURL() + "/photos.json"
    }

    // to load files out of the single request
    private fun loadPhoto(photo: Photo):Photo{
        val localPhoto = photo
        val url = Config.serverURL() + photo.url
        val name = photo.name
        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var token = preferences.getString("auth_token","")
        var email = preferences.getString("user_email","")

        System.out.printf("%s %s \n", token, email)
        var request = Request.Builder()
            .header("X-User-Email", email)
            .header("X-User-Token", token)
            .url(url!!)
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
            val localFile = MessageDigest.getInstance("SHA-1")
                .digest(photo.url!!.toByteArray())
                .map { String.format("%02X", it) }
                .joinToString(separator = "")

            val file = File("$dir/${localFile.toString()}.jpg")

            file.writeBytes(it.body!!.bytes()) // TODO don't have to do that do it in memory.
            localPhoto.fileName = "${localFile.toString()}.jpg"
        }
        return localPhoto
    }


    private fun loadPhotoFeed(){
        val feed = photoStreamURL()

        // GET
        val request = Request.Builder()
            .header("Content-Type","application/json")
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