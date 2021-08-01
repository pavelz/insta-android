package com.example.insta_android.data

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.insta_android.data.model.PhotoVideo

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos")
    fun getAll(): List<PhotoVideo>

    @Query("SELECT * FROM photos WHERE uid IN (:photoIds)")
    fun loadAllByIds(photoIds: IntArray): List<PhotoVideo>

    @Query("SELECT * FROM photos WHERE file_name LIKE :file " +
             " LIMIT 1")
    fun findByFileName(file: String): PhotoVideo

    @Query("DELETE from photos")
    fun deleteAll()

    @Insert
    fun insertAll(vararg photoVideos: PhotoVideo)

    @Delete
    fun delete(photoVideo: PhotoVideo)

    @Query("SELECT * FROM photos ")
    fun getPAll(): DataSource.Factory<Int, PhotoVideo>
}