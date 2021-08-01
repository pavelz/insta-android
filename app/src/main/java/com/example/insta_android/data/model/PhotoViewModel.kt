package com.example.insta_android.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.example.insta_android.Config.Code.context
import com.example.insta_android.data.AppDatabase

class PhotoViewModel: ViewModel(){
    val db = AppDatabase.getDatabase(context!!)
    val photoVideoList: LiveData<PagedList<PhotoVideo>> = db!!.photoDao().getPAll().toLiveData(Config(pageSize=20,enablePlaceholders=true,maxSize=200))
}