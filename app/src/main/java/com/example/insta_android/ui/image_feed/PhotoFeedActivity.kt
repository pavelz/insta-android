package com.example.insta_android.ui.image_feed

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.insta_android.databinding.ActivityMainBinding
import com.example.insta_android.databinding.PhotoFeedActivityBinding
import com.example.insta_android.recyclerview.MoreSpaceLayoutManager
import com.example.insta_android.ui.login.LoginActivity
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class PhotoFeedActivity: AppCompatActivity() {

    var client = OkHttpClient()
    private var moshi = Moshi.Builder().build()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // TODO: carry all image feed load here from main activity
        Config.Context(applicationContext)
        Log.i("ACTIVITY", "PhotoFeed::onCreate")
        MoreSpaceLayoutManager(this)
        var context = this.applicationContext

        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var edit = preferences.edit()
        var token = preferences.getString("auth_token","")
        System.out.printf("----------- TOKEN: %s \n", token)
        val binding = PhotoFeedActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logoutScreen.setOnClickListener { view ->
            Log.i("CREATE", "FFFF")
            var k = Intent(this, LoginActivity::class.java)
            startActivity(k)
        }
        binding.takeAPicture.setOnClickListener { view ->
            var k = Intent(this, MainActivity::class.java)
            startActivity(k)
        }

        val refresh = findViewById<SwipeRefreshLayout>(R.id.refresh)
        refresh.setOnRefreshListener {
            // TODO implement refresh reload etc hahah

            val mediaDataSource = MediaFeed(this.applicationContext)
            mediaDataSource.sync()
            refresh.isRefreshing = false
        }

        var recycle = findViewById<RecyclerView>(R.id.recycle)
        recycle.setHasFixedSize(false)

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
        }
        print("PRINT\n")
        val client: FlipperClient = AndroidFlipperClient.getInstance(this)
        print("PRINT FLIPPER START\n")
        client.addPlugin(DatabasesFlipperPlugin(Config.context))
        Log.i("CREATE", "WOPOWOWOOW")
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        requestPermissions()
        Log.i("CREATE","INITIAL INTENT DONE")
    }

    override fun onNewIntent(result: Intent){
        println("ACTIVITY RESULT!!!")
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        requestPermissions()
        super.onNewIntent(result)
    }

    private fun requestPermissions(){
        println("------------- ACCESS!")
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
    }
    private val mLayoutManager by lazy {
        MoreSpaceLayoutManager(this)
    }
    private fun attacheDatasourceToPageList() {
        val mediaDataSource = MediaFeed(this.applicationContext)
        mediaDataSource.sync()
        Log.i("ATTACH", "attachedDataSource >>>>>>>>>>>")
        val viewModel = ViewModelProviders.of(this).get(PhotoViewModel::class.java)
        val recyclerView = findViewById<RecyclerView>(R.id.recycle)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.setItemViewCacheSize(20)
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
