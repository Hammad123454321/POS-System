package com.posplatform.pos_app

import android.app.Application
import com.pax.poslink.POSLinkAndroid

class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        runCatching {
            POSLinkAndroid.init(this)
        }
    }
}
