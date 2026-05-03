package com.pax.poslink;

import android.os.Build;
import android.service.dreams.DreamService;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ScreenSaverService extends DreamService {
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setContentView(R.layout.dream_service_screen_saver);
    }
}
