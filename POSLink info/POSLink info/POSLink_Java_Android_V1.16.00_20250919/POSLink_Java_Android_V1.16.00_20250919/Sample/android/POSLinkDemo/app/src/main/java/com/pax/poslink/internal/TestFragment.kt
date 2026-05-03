package com.pax.poslink.internal

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.pax.poslink.PosLink
import com.pax.poslink.R
import com.pax.poslink.SettingINI
import com.pax.poslink.ui.base.BaseFragment
import com.pax.poslink.util.OverlayPermissionUtil
import com.pax.poslink.util.view.FloatViewUtil

/**
 * Created by Justin.Z on 2020-7-27
 */
class TestFragment : BaseFragment() {

    private val REQUEST_BASIC = 0x1000

    private lateinit var rootView: View
    private lateinit var context: Fragment
    private val DEFAULT_FLAG = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

    companion object {
        fun newInstance(): TestFragment? {
            return TestFragment()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (inflater != null) {
            rootView = inflater.inflate(R.layout.fragment_for_test, container, false)
        }
        initView(rootView)
        return rootView
    }

    private fun initView(view: View) {

        view.findViewById<Button>(R.id.btn_test_a14).setOnClickListener {
            if (!OverlayPermissionUtil.canDrawOverlay(getContext())) {
                OverlayPermissionUtil.startOverlayPermissionSettingForResult(context, REQUEST_BASIC)
            } else {
                setView()
            }
        }

        view.findViewById<Button>(R.id.btn_test_cancel).setOnClickListener {
            val thread = Thread() {
                val p = PosLink(activity)
                p.SetCommSetting(SettingINI.getCommSettingFromFile(p.appDataFolder + "/" + SettingINI.FILENAME))
                p.CancelTrans();
            }
            thread.start()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BASIC) {
            if (OverlayPermissionUtil.canDrawOverlay(getContext())) {
                setView()
            }
        }
    }

    private fun setView() {
        val imageView = ImageView(activity)
        imageView.setImageResource(R.mipmap.back)
        val windowLayoutParams = WindowManager.LayoutParams(
                100,
                100,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                DEFAULT_FLAG,
                PixelFormat.TRANSLUCENT

        )
        val a = activity!!.resources.displayMetrics.heightPixels
        windowLayoutParams.let {
            it.gravity = Gravity.TOP or Gravity.LEFT
            it.x = 20
            it.y = a - 200
            FloatViewUtil.getInstance().setView(imageView, it)
        }
    }



}