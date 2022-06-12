package com.example.insta_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.insta_android.data.model.PhotoVideo
import androidx.room.Room
import com.example.insta_android.data.model.Video


@Database(entities = arrayOf(PhotoVideo::class, Video::class), version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        val DATABASE_NAME = "/sdcard/INSTA/photos"
        var INSTANCE:AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase? {
            println("💥 database create")

            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries().build()
            }
            return INSTANCE
        }
    }
    fun destroyInstance() {
        INSTANCE = null
    }
}