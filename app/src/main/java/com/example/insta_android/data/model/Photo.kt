package com.example.insta_android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "file_name") val fileName: String?
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}
