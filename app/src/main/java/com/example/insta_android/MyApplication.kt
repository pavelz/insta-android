package com.example.insta_android

import android.app.Application
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.insta_android.Config.Code.context
import com.example.insta_android.ui.image_feed.PhotoFeedActivity
import com.example.insta_android.ui.login.LoginActivity
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;


class MyApplication : Application() {
    override fun onCreate() {
        println("😆 APPLICATION")
        super.onCreate()
        SoLoader.init(this , false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client: FlipperClient = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this , DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(context))

            client.start()
        }
        //var k = Intent(this, PhotoFeedActivity::class.java)

        //startActivity(k)
    }
}