/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *
 * Module Date: 2019/7/30
 * Module Auth: Frank.W
 * Description:
 *
 * Revision History:
 * Date                   Author                       Action
 * 2019/7/30              Frank.W                       Create
 * ============================================================================
 */

package com.pax.poslink.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.fragment.app.Fragment;

/**
 * Create by Fahy.F on 4/8/2019
 */
public class OverlayPermissionUtil {

    public static boolean canDrawOverlay(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean startOverlayPermissionSettingForResult(Object context, int requestCode) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, requestCode);
            } else if (context instanceof Fragment) {
                Fragment fragment = (Fragment) context;
                intent.setData(Uri.parse("package:" + fragment.getActivity().getPackageName()));
                fragment.startActivityForResult(intent, requestCode);
            } else {
                throw new IllegalArgumentException("Context: " + context + " is invalid");
            }
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

}
