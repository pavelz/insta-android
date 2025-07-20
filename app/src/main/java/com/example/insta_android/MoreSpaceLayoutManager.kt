package com.example.insta_android.recyclerview

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MoreSpaceLayoutManager(context: Context) : LinearLayoutManager(context) {
    private val extraLayoutSpace = context.resources.displayMetrics.heightPixels * 3 / 4

    override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {
        Log.i("CREATE", "LAYOUT EXTRA")
        Log.i("CREATE", extraLayoutSpace.toString())
        return 15000
    }
}