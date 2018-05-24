package com.moka.mvvm.demo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View

class LiveDataActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LiveDataActivity"
    }

    private lateinit var testViewModel: TestViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)
        testViewModel = ViewModelProviders.of(this).get(TestViewModel::class.java)
        Log.d(TAG, "找到viewModel$testViewModel")
//        testViewModel.dataLive.value = "1234"
        testViewModel.dataLive.observe(this, Observer<String> {
            Log.d(TAG, "收到消息：$it")
        })
        testViewModel.dataRx.observable(this).subscribe({
            Log.d(TAG, "收到消息：${if (it.isNull) "null" else it.get()}")
        })

    }

    fun refreshData(view: View) {
        testViewModel.refresh()
    }

    var fragment: LiveDataFragment? = null
    fun addOrRemoveFragment(view: View) {
        if (fragment == null) {
            fragment = LiveDataFragment()
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment)
                    .commitNow()

        } else {
            supportFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitNow()
            fragment = null
        }
    }
}
