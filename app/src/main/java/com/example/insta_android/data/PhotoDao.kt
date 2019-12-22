package com.example.insta_android.data

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.insta_android.data.model.Photo

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos")
    fun getAll(): List<Photo>

    @Query("SELECT * FROM photos WHERE uid IN (:photoIds)")
    fun loadAllByIds(photoIds: IntArray): List<Photo>

    @Query("SELECT * FROM photos WHERE file_name LIKE :file " +
             " LIMIT 1")
    fun findByFileName(file: String): Photo

    @Query("DELETE from photos")
    fun deleteAll()

    @Insert
    fun insertAll(vararg photos: Photo)

    @Delete
    fun delete(photo: Photo)

    @Query("SELECT * FROM photos ")
    fun getPAll(): DataSource.Factory<Int, Photo>
}