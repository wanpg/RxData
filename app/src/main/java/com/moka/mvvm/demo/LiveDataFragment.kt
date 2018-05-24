package com.moka.mvvm.demo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LiveDataFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LiveDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LiveDataFragment : Fragment() {

    companion object {
        const val TAG = "LiveDataFragment"
    }

    lateinit var testViewModel: TestViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testViewModel = ViewModelProviders.of(activity!!).get(TestViewModel::class.java)
        Log.d(TAG, "找到viewModel$testViewModel")
        testViewModel.dataLive.observe(this, Observer<String> {
            Log.d(TAG, "收到消息：$it")
        })
//        testViewModel.dataRx.observable(this).subscribe({
//            Log.d(TAG, "收到消息：$it")
//        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_live_data, container, false)
        inflate.findViewById<Button>(R.id.button).setOnClickListener {
            testViewModel.refresh()
        }
        return inflate
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        testViewModel.dataRx.observable(this).subscribe({
            Log.d(LiveDataActivity.TAG, "收到消息：${if (it.isNull) "null" else it.get()}")
        })
    }
}
