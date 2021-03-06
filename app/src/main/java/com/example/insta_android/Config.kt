package com.example.insta_android

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.StrictMode
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception


class Config {

    companion object Code {
        private var the_url:String = ""
        @SuppressLint("StaticFieldLeak")
        var context:Context? = null
        fun Context(ctx : Context){
            context = ctx
        }
        private fun isEmulator(): Boolean {
            return (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || "google_sdk" == Build.PRODUCT)
        }

        private fun checkAlive(url:String): Boolean {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val  client = OkHttpClient()
            println("$url/alive")
            try {
                val req = Request.Builder()
                    .url("$url/alive")
                    .get()
                    .build()
                var res = client.newCall(req).execute()
                res.use {
                    if (!it.isSuccessful) {
                        println("💀 no repsonse from ${url}")
                        return false
                    }
                    println("👍 good! from ${url}")
                    return true
                }
            } catch(e: Exception) {
                println("💣 exception ${e}")
                return false
            }

        }

        fun serverURL():String{
            // check if running in Emulator then use special address
            // if not in emulator, then select phone_dev -> local net ip address - check if that address works.
            // FALLBACK to production if server does not respond.
            if(the_url != ""){
                return the_url
            }
            var env = "phone_dev"
            if(isEmulator()) {
                env = "dev"
            }
            // probably do a check here if phone_dev server abvailable - then fall back to
            // production
            env = "phone_dev"
            val url = when (env) {
                "dev" ->
                    context!!.getString(R.string.dev)
                "production" ->
                    context!!.getString(R.string.production)
                "phone_dev" ->
                    context!!.getString(R.string.phone_dev)
                else ->
                    context!!.getString(R.string.production)
            }

            return if(checkAlive(url)){   //  fall back when we are walking around - so we can access production as well. TODO: get this working in iOS app too
                the_url = url
                the_url
            } else {
                the_url = context!!.getString(R.string.production)
                the_url
            }
        }
    }
}