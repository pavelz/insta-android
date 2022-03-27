package com.example.insta_android

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider

import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

import android.graphics.BitmapFactory
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.os.StrictMode
import android.provider.DocumentsContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.insta_android.Config.Code.context
import com.example.insta_android.data.LoginDataSource
import com.example.insta_android.databinding.ActivityMainBinding
import com.example.insta_android.ui.login.LoginActivity

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.JsonAdapter
import org.apache.commons.io.IOUtils
import java.nio.file.Files
import java.nio.file.Paths


class MainActivity : AppCompatActivity() {
    private var currentImageInputStream: InputStream? = null
    private var locationManager : LocationManager? = null
    lateinit var aBitmap: Bitmap
    var state:Bundle? = null

    private val RSS_JOB_ID = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState
        Config.Context(applicationContext)
        val url = Config.serverURL()
        println("################# URL")
        println(url)

        var context = this.applicationContext
        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var edit = preferences.edit()
        var token = preferences.getString("auth_token","")
        System.out.printf("----------- TOKEN: %s \n", token)

        requestPermissions()
        if(token == "") {
            try {
                var k = Intent(this, LoginActivity::class.java)

                startActivity(k)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }

        if(::aBitmap.isInitialized){
            print("----- aBitmap is there!/s")
            val imageView: ImageView =  findViewById(R.id.imageView2)
            imageView.setImageBitmap(aBitmap!!)
        }
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar as Toolbar?)

        takePhoto.setOnClickListener { view ->
            // TODO start getting videos as well.
            dispatchTakePictureIntent()
        }

        pickFromGallery.setOnClickListener(){
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
        sendPhotoVideo.setOnClickListener(){
            println("new")
            try {
                // Request location updates
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
                toSend = true
            } catch(ex: SecurityException) {
                Log.d("myTag", "Security Exception, no location available");
            }
        }
        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;
        System.out.printf(">>>> path: %s\n",Environment.getExternalStorageDirectory().getPath().toString())



    }
    // this is called when callback is returned.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("ACCESS GRANTED")
        val root = Environment.getExternalStorageDirectory().getPath().toString()
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
        var images = fetch_images(serverURL() + "/photos.json")
        print("$images")

        File(root + "/INSTA").walk().forEach {
            it.delete()
        }
        images!!.iterator().forEach {
            if(it.name != null) { // TODO remove XXX fixme shortime
                System.out.printf("photo url: %s / %s\n" , it.url, it.name)
                load_image(Config.serverURL() + it.url , it.name)
            }
        }
        var binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        // TODO: sync all images from the site. compare list against waht you have and add new.
        // TODO: load some images into image list on the device.

    }

    class Photo(s: String, s1: String) {
        fun Photo(){}
        var url: String = ""
        var name: String = ""
    }
    class ArrayPhoto {
        fun ArrayPhoto(){}
        var photos: Array<Photo> = Array<Photo>(10){Photo("hello", "hi") }
    }

    private var moshi = Moshi.Builder().build()
    private var photoJsonAdapter = moshi.adapter(Photo::class.java)

    fun load_images(images: List<Photo>?){
        images!!.forEach {

        }
    }

    fun load_image(url:String, name:String) : File?{
        var request = Request.Builder()
            .url(url)
            .get()
            .build()
        var resp = client.newCall(request).execute()
        var file:File? = null
        resp.use{
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
    fun fetch_images(url: String): List<Photo>? {
        var str = arrayOf("")

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

            var ListType = Types.newParameterizedType(List::class.java, Photo::class.java)
            var adapter: JsonAdapter<List<Photo>> = moshi.adapter(ListType)
            var data:List<MainActivity.Photo>? = adapter.fromJson(text)
            System.out.printf(">>>>>>>>>>>>>> TEXT: %s \n", text)
            return data
        }

        var blank: List<Photo>? = List<Photo>(0){Photo("hello", "hi") }
        return  blank
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(currentPhotoPath != ""){
            outState!!.putString("image_path", currentPhotoPath)
            outState!!.putString("image_filename", currentPhotoFilename)
        }
    }

    fun requestPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET ), 0)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        println("LOADING RESTORE STATE")
        if(savedInstanceState != null) {
            var path = savedInstanceState!!.getString("image_path")
            System.out.printf("LOADED PATH: %s\n", path)
            if( path != null) {
                var file = File(path)
                var data = file.readBytes()
                currentPhotoPath = path
                currentPhotoFilename = savedInstanceState!!.getString("image_filename")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(currentPhotoPath != "") {
            //findViewById<View>(R.id.imageView2).post { setPic() }
            findViewById<View>(R.id.imageView2).viewTreeObserver.addOnGlobalLayoutListener { println(">>> LAYOUT CALLBACK"); setPic() }
        }
    }
    var lat: Double = 0.0
    var lng: Double = 0.0

    //tedefine the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lat = location.latitude
            lng = location.longitude
            sendPhotoVideo()
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    var currentPhotoPath: String = ""
    var currentPhotoFilename: String = ""
    var currentPhotoUri:Uri? = null


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val root = Environment.getExternalStorageDirectory().getPath().toString()
        try{
            Files.createDirectory(Paths.get(root + "/INSTA"))
        } catch(e: Exception){
            System.out.println("---------> Beh sdasdas\n")
        }

        val dir = File(root + "/INSTA")
        val storageDir: File = dir //getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            System.out.printf("path: %s \n",absolutePath)
            currentPhotoFilename = absoluteFile.name
            currentPhotoPath = absolutePath
        }
    }

    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_VIDEO_CAPTURE = 1
    val PICK_IMAGE = 2
    var photoUri: Uri? = null
    private fun dispatchTakePictureIntent() {
        // TODO need cameraLib to take videos
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    System.out.println(ex)
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.insta_android.fileprovider",
                        it
                    )
                    photoUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_VIDEO_CAPTURE)
                    System.out.println("HEY INTENT")
                }
            }
        }
    }

    // TODO:
    // create video view under along with phtotview -> clickable to play
    // ensure send button sends video or photo correctly
    private fun dispatchTakeVideoIntent() {
        // TODO need cameraLib to take videos
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            // Ensure that there's a camera activity to handle the intent
            takeVideoIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val videoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    System.out.println(ex)
                        null
                }
                // Continue only if the File was successfully created
                videoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.insta_android.fileprovider",
                        it
                    )
                    photoUri = photoURI
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
                    System.out.println("HEY INTENT")
                }
            }
        }
    }


    public override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?){
        super.onActivityResult(reqCode, resCode, data)
        if(reqCode == PICK_IMAGE){
            println("PICK CODE!")
            println(data!!.data!!.path)
            currentPhotoPath = data.data!!.path!!
            val url = data.data!!
            val a = DocumentsContract.getDocumentId(url)
            println(a)
            println(url.scheme)
            println(url.path)


            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, url)
            val image = findViewById<ImageView>(R.id.imageView2)
            val proj:Array<out String> = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME )
            val cursor = contentResolver.query(url, proj,null, null, null)
            println(contentResolver.getType(url))
            val fn = contentResolver.openFileDescriptor(url, "r")
            val index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            currentImageInputStream = context!!.contentResolver.openInputStream(data.data!!)


            if(cursor.moveToFirst()){
                println("FOUND SOMETHING!!")
                println(cursor.getString(index))
                currentPhotoFilename = cursor.getString(index)

                if(cursor.getString(index) == null) {
                    // its a file ?

                }
            }

            image.setImageBitmap(bitmap)
        }else if(reqCode == REQUEST_TAKE_PHOTO) {
            setPic()
            var file = File(currentPhotoPath)
            var data = file.readBytes()
            System.out.printf("file: %s \npath: %s\n", currentPhotoFilename, currentPhotoPath)
        }
    }
    public fun sendFile(){
        sendPhotoVideo()
    }

    private fun setPic() {
        // Get the dimensions of the View
        val imageView: ImageView =  findViewById(R.id.imageView2)
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight
            System.out.printf("++++++++ W: %d H: %d\n", photoW, photoH)
            System.out.printf("++++++++ W: %d H: %d\n", targetW, targetH)
            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true

        }

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            //galleryAddPic()

            var ex = ExifInterface(currentPhotoPath)
            var attr = ex.getAttribute(ExifInterface.TAG_ORIENTATION).toInt()
            var rotatedBitmap:Bitmap = bitmap
            if(attr == 6){
                var matrix = Matrix()
                matrix.postRotate((90).toFloat())
                rotatedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
                )
            }
            aBitmap = rotatedBitmap
            imageView.setImageBitmap(rotatedBitmap)
        }
    }
    private fun serverURL():String{
        var url = Config.serverURL()
        println("get ServerURL ${url}")
        return url
    }

    companion object StreamHelpers{
        fun convertStreamToFile(In: InputStream): File? {
            val tempFile = File.createTempFile("temp","file")
            tempFile.deleteOnExit()
            try{
                val out = FileOutputStream(tempFile)
                IOUtils.copy(In, out)
            } catch(e:Exception){
            }
            return tempFile
        }
    }

    private var MEDIA_TYPE_JPEG = "image/jpeg".toMediaTypeOrNull();
    private var MEDIA_TYPE_MP4 = "image/jpeg".toMediaTypeOrNull();

    var client = OkHttpClient()
    var toSend = false
    @Throws(IOException::class)
    fun sendPhotoVideo(){
        println("SEND PHOTO")
        if(toSend != true) { return }

        var prefs = applicationContext.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var token = prefs.getString("auth_token","")
        var email = prefs.getString("user_email","")
        var file:File? = null
        println("CURRRENT PHOTO PATH: ${currentPhotoPath}")
        var photoFile:File?
        if(currentImageInputStream != null){
            file = convertStreamToFile(currentImageInputStream!!)
        } else {
            file = File(currentPhotoPath)
        }
        var requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_email", email)
            .addFormDataPart("user_token", token)
            .addFormDataPart("video[name]", currentPhotoFilename)
            .addFormDataPart("video[video]",currentPhotoFilename,
                file!!.asRequestBody(MEDIA_TYPE_MP4)
            )
            .addFormDataPart("location[lat]", lat.toString())
            .addFormDataPart("location[lng]", lng.toString())
            .build()


        var request = Request.Builder()

            .url( serverURL() + "/videos")
            .post(requestBody)
            .build()
        System.out.println("Gettting to send")
        var req = client.newCall(request)
        var response = req.execute()
        response.use {
            if (!it!!.isSuccessful) {
                throw IOException("Unexpected code " + response)
            }
        }
        toSend = false
    }
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_logout -> {
                var ls = LoginDataSource(applicationContext)
                ls.logout()

                var k = Intent(this, MainActivity::class.java)

                startActivity(k)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
