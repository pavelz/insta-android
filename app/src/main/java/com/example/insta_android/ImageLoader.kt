package com.example.insta_android

import android.app.IntentService
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Environment

class ImageLoader: IntentService(ImageLoader::class.simpleName){
    override fun onHandleIntent(p0: Intent?) {
        val dataString = p0!!.getStringExtra("dataString")
        val root = Environment.getExternalStorageDirectory().getPath().toString()
        var ext = this.getExternalFilesDir(null)

        val query = "select sqlite_version() AS sqlite_version"
        val db = SQLiteDatabase.openOrCreateDatabase(":memory:", null)
        val cursor = db.rawQuery(query, null)
        var sqliteVersion = ""
        if (cursor.moveToNext()) {
            sqliteVersion = cursor.getString(0);
        }
        
        System.out.printf("string!: %s\n root!: %s\next: %s \n sqlite version: %s \n", dataString, root,ext, sqliteVersion)
    }
}