package com.pax.poslink.util.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.pax.poslink.MainApplication

/**
 * Created by Justin.Z on 2020-7-28
 */
class FloatViewUtil private constructor() {

    private var floatView: View? = null
    private lateinit var params: ViewGroup.LayoutParams
    private var mFloatViewShow = false
    private var mWindowManager: WindowManager? = null

    companion object {
        fun getInstance() = Helper.instance
    }

    private object Helper {
        val instance = FloatViewUtil()
    }

    fun setView(view: View, params: ViewGroup.LayoutParams) {
        floatView = view
        this.params = params
    }

    fun showFloatView(clickListener: View.OnClickListener) {
        floatView?.let { it ->
            it.setOnClickListener(clickListener)
            if (mFloatViewShow) return
            mWindowManager = MainApplication.getInstance().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            mWindowManager?.let {
                it.addView(floatView, params)
                mFloatViewShow = true
            }
        }
    }

    fun hideFloatView() {
        if (null != floatView && mFloatViewShow) {
            if (mWindowManager == null) {
                mWindowManager = MainApplication.getInstance().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            }
            mWindowManager?.let {
                it.removeView(floatView)
                mFloatViewShow = false
            }
        }
    }



}