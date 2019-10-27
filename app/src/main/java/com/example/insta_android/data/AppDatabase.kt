package com.example.insta_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.insta_android.data.PhotoDao
import com.example.insta_android.data.model.Photo
import androidx.room.Room
import android.icu.lang.UCharacter.GraphemeClusterBreak.V



@Database(entities = arrayOf(Photo::class), version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        val DATABASE_NAME = "photos"
        var INSTANCE:AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase? {

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