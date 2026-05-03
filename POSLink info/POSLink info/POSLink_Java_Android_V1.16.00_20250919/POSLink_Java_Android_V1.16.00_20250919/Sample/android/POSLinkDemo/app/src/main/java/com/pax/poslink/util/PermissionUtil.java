/*
 * COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2009-2020 PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 */

package com.pax.poslink.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class PermissionUtil {


    public static void askPermission(Activity activity, final String[] permissions, final PermissionGrantCallback permissionGrantCallback) {
        Nammu.askForPermission(activity, permissions, new PermissionCallback() {
            @Override
            public void permissionGranted() {
                LogStaticWrapper.getLog().v("Grant permission");
            }
            @Override
            public void permissionRefused() {
                LogStaticWrapper.getLog().v("Not Grant permission");
                if (permissionGrantCallback != null) permissionGrantCallback.onRefused();
            }
        });
//        if (!Nammu.checkPermission(permissionName)) {
//        }
//               UIUtil.showToast(activity, "Please grant " + permissionName + " permission in Setting", Toast.LENGTH_SHORT);
    }

    public static boolean hasPermission(Activity activity, String permissionName) {
        return Nammu.hasPermission(activity, permissionName);
    }

    public static void init(Context context) {
        Nammu.init(context);
    }

    public interface PermissionGrantCallback {
        void onRefused();
    }


    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
