package com.moka.mvvm.demo

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moka.mvvm.RxData

class TestViewModel : ViewModel() {

    val dataLive = MutableLiveData<String>()

    val dataRx = RxData<String>("")

    fun refresh() {
        dataLive.value = "hello live world!!"
        dataRx.set(null)
    }

}