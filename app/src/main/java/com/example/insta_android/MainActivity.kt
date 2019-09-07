package com.example.insta_android

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider


import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

import android.graphics.BitmapFactory
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.os.StrictMode
import android.util.Log
import android.view.View
import com.example.insta_android.ui.login.LoginActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private var locationManager : LocationManager? = null

    lateinit var aBitmap: Bitmap
    var state:Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState

        var context = this.applicationContext
        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var edit = preferences.edit()
        var token = preferences.getString("auth_token","")
        System.out.printf("----------- TOKEN: %s \n", token)

        if(token == ""){
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
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->

            dispatchTakePictureIntent()
        }
        fab3.setOnClickListener(){
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
        val root = Environment.getExternalStorageDirectory().getPath().toString()
        val dir = File(root + "/INSTA")
        dir.mkdirs()
        val outfile = File(dir, "FIle.txt")
        try {
            val f = FileOutputStream(outfile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if(currentPhotoPath != ""){
            outState!!.putString("image_path", currentPhotoPath)
            outState!!.putString("image_filename", currentPhotoFilename)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        var path = savedInstanceState!!.getString("image_path")
        System.out.printf("LOADED PATH: %s\n", path)
        var file = File(path)
        var data = file.readBytes()
        currentPhotoPath = path
        currentPhotoFilename = savedInstanceState!!.getString("image_filename")
    }

    override fun onResume() {
        super.onResume()
        if(currentPhotoPath != "") {
            findViewById<View>(R.id.imageView2).post { setPic() }
        }
    }
    var lat: Double = 0.0
    var lng: Double = 0.0

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lat = location.latitude
            lng = location.longitude
            sendPhoto()
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    var currentPhotoPath: String = ""
    var currentPhotoFilename: String = ""

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val root = Environment.getExternalStorageDirectory().getPath().toString()
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
    var photoUri: Uri? = null
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    System.out.println("Error filing")
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
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    System.out.println("HEY INTENT")
                }
            }
        }
    }

    public override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?){
        setPic()
        var file = File(currentPhotoPath)
        var data = file.readBytes()
        System.out.printf("file: %s \npath: %s\n", currentPhotoFilename, currentPhotoPath)

    }
    public fun sendFile(){
        sendPhoto()
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
            galleryAddPic()

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
    private var MEDIA_TYPE_JPEG = "image/jpeg".toMediaTypeOrNull();

    var client = OkHttpClient()
    var toSend = false
    @Throws(IOException::class)
    fun sendPhoto(){
        if(toSend != true) { return }
        var requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("photo[name]", currentPhotoFilename)
            .addFormDataPart("photo[image]",currentPhotoFilename,
                File(currentPhotoPath).asRequestBody(MEDIA_TYPE_JPEG)
            )
            .addFormDataPart("location[lat]", lat.toString())
            .addFormDataPart("location[lng]", lng.toString())
            .build()

        var request = Request.Builder()
            .url("http://kek.arslogi.ca:3001/photos")
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}
