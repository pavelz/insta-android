package com.example.insta_android.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.example.insta_android.data.PhotoDao

class PhotoViewModel(photoDao: PhotoDao): ViewModel(){
    val photoList: LiveData<PagedList<Photo>> = photoDao.getPAll().toLiveData(Config(pageSize=20,enablePlaceholders=true,maxSize=200))
}