package com.example.insta_android.ui.image_feed

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.insta_android.Config
import com.example.insta_android.MainActivity
import com.example.insta_android.R
import com.example.insta_android.data.MediaFeed
import com.example.insta_android.data.PhotoAdapter
import com.example.insta_android.data.model.PhotoVideo
import com.example.insta_android.data.model.PhotoViewModel
import com.example.insta_android.ui.login.LoginActivity
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.android.synthetic.main.image_feed.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList


class PhotoFeedActivity: AppCompatActivity() {

    var client = OkHttpClient()
    private var moshi = Moshi.Builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // TODO: carry all image feed load here from main activity
        Config.Context(applicationContext)
        Log.i("ACTIVITY", "PhotoFeed::onCreate")
        setContentView(R.layout.image_feed)
        val refresh = findViewById<SwipeRefreshLayout>(R.id.refresh)
        refresh.setOnRefreshListener {
            // TODO implement refresh reload etc hahah

            val mediaDataSource = MediaFeed(this.applicationContext)
            mediaDataSource.sync()
            refresh.isRefreshing = false
        }

//        val mgr:ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        Log.i("ACTIVITY", "WAT")
//        val a = mgr.appTasks.size
//        System.out.printf("APPTASK: %d\n", a)
//        if(mgr.getAppTasks().size > 1) {
//            Log.i("ACTIVITIES", "MORE THAN TWO")
//            var policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//            StrictMode.setThreadPolicy(policy)
            requestPermissions()
//            //attacheDatasourceToPageList()
//        }


        var context = this.applicationContext

        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var edit = preferences.edit()
        var token = preferences.getString("auth_token","")
        System.out.printf("----------- TOKEN: %s \n", token)

        logout_screen.setOnClickListener { view ->
            var k = Intent(this, LoginActivity::class.java)
            startActivity(k)
        }
        take_a_picture.setOnClickListener { view ->
            var k = Intent(this, MainActivity::class.java)
            startActivity(k)
        }
        if(token == "" || token == null) {
            println(">>>>>>> WHAT321")
            try {
                var k = Intent(this, LoginActivity::class.java)

                startActivityForResult(k,1)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        } else {
            // TODO fix - copied from activityresult

//            val root = Environment.getExternalStorageDirectory().getPath().toString()
            val root = context.getExternalFilesDir(null).toString()
            var dir = File(root, "INSTA")
            try {
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        System.out.printf("################ borked\n")
                    } else {
                        println("---------- INSTA created")
                    }
                } else {
                    System.out.printf("###################### already exists INSTA \n")
                }
            } catch(e: Exception){
                System.out.printf("Create INSTA dir execption: %s\n", e)
            }
            val photoDataSource = MediaFeed(this.applicationContext)

//            photoDataSource.sync(doneCallback)
//            println("${root}/INSTA/image.jpg")
//            Picasso.setSingletonInstance(Picasso.Builder(context).build())
//            Picasso.get().load(File("${root}/INSTA/")).into(imageView)
        }
        val client: FlipperClient = AndroidFlipperClient.getInstance(this)
        client.addPlugin(DatabasesFlipperPlugin(Config.context))
        Log.i("CREATE", "WOPOWOWOOW")
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("ACTIVITY RESULT!!!")
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        requestPermissions()
    }

    private fun requestPermissions(){
        println("------------- ACCESS!")
//        if (Build.VERSION.SDK_INT >= 30) {
//            if (!Environment.isExternalStorageManager()) {
//                val getpermission = Intent()
//                getpermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                startActivity(getpermission)
//            }
//        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET, Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION ), 0)
        } else {
            attacheDatasourceToPageList()
        }
    }

    // this is called when callback is returned.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        println("ON PERMISIONS\n--------------------------------\n")
        var root =  applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

//        val root = Environment.getExternalStorageDirectory().getPath().toString()
        try{
            Files.createDirectory(Paths.get("$root/INSTA"))
        } catch(e: java.lang.Exception){
            System.out.printf("---------> %s\n", e)
        }
        println("*** dir created at $root /INSTA")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val mediaDataSource = MediaFeed(this.applicationContext)
        mediaDataSource.sync()
        println("YO")

        attacheDatasourceToPageList()

        // TODO implement SwipeRefresh layout as in https://stackoverflow.com/questions/44454797/pull-to-refresh-recyclerview-android

        return

/*        if(!( ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            return
        }
        println("ACCESS GRANTED")
        try{
            Files.createDirectory(Paths.get(root + "/INSTA"))
        } catch(e: Exception){
            System.out.printf("---------> %s\n", e)
        }

        val dir = File(root + "/INSTA")
        dir.mkdirs()
        val outfile = File(dir, "FIle.txt")
        try {
            val f = FileOutputStream(outfile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val policy:StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var images = fetch_images(Config.serverURL() + "/photos.json")
        print("$images")

        File(root + "/INSTA").walk().forEach {
            it.delete()
        }
        images!!.iterator().forEach {
            System.out.printf("photo url: %s\n", it.url)
            load_image("http://95.216.150.207:3001/" + it.url, it.name)
        }
        var binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)*/
        // TODO: sync all images from the site. compare list against waht you have and add new.
        // TODO: load some images into image list on the device.

    }

    private fun attacheDatasourceToPageList() {
        val mediaDataSource = MediaFeed(this.applicationContext)
        mediaDataSource.sync()
        Log.i("ATTACH", "attachedDataSource >>>>>>>>>>>")
        val viewModel = ViewModelProviders.of(this).get(PhotoViewModel::class.java)
        val recyclerView = findViewById<RecyclerView>(R.id.recycle)

        val adapter = PhotoAdapter()
        println("OBSERVER SET")
        viewModel.photoVideoList.observe(
            this ,
            Observer<PagedList<PhotoVideo>> { pagedList ->
                println("PAGED LIST CALLED"); adapter.submitList(pagedList)
            })
        recyclerView.adapter = adapter
    }

    fun fetch_images(url: String): List<MainActivity.Photo>? {
        var str = arrayOf("")
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        print("request\n")
        var request = Request.Builder()
            .header("Content-Type","application/json")
            .header("Accept", "application/json")
            .url(url)
            .get()
            .build()

        var resp = client.newCall(request).execute()

        resp.use {
            if(!it!!.isSuccessful){
                throw IOException("no images for u!")
            }
            var text = resp.body!!.source().readString(Charset.defaultCharset())

            var ListType = Types.newParameterizedType(List::class.java, MainActivity.Photo::class.java)
            var adapter: JsonAdapter<List<MainActivity.Photo>> = moshi.adapter(ListType)
            var data:List<MainActivity.Photo>? = adapter.fromJson(text)
            return data
        }
    }
    fun load_image(url:String, name:String) : File?{
        var request = Request.Builder()
            .url(url)
            .get()
            .build()
        var resp = client.newCall(request).execute()
        var file:File? = null
        resp.use {
            if(!it!!.isSuccessful){
                throw IOException("no images for u!")
            }

            val root = Environment.getExternalStorageDirectory().getPath().toString()

            var dir = File(root + "/INSTA")

            file = File.createTempFile(
                name, /* prefix */
                ".jpg", /* suffix */
                dir /* directory */
            )
            file!!.writeBytes(it.body!!.bytes())
        }
        return file
    }
}
