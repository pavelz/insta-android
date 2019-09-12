package com.example.insta_android

import android.app.IntentService
import android.content.Intent

class ImageLoader: IntentService(ImageLoader::class.simpleName){
    override fun onHandleIntent(p0: Intent?) {
        val dataString = p0!!.getStringExtra("dataString")
        System.out.printf("string!: %s\n", dataString)
    }
}