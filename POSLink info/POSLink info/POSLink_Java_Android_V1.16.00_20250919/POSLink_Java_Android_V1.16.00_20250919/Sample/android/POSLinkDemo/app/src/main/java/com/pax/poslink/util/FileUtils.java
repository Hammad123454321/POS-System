/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.

 * Module Date: 8/1/2019
 * Module Auth: Fahy.F
 * Description:

 * Revision History:
 * Date                   Author                       Action
 * 8/1/2019              Fahy.F                       Create
 * ============================================================================
 */
package com.pax.poslink.util;

import android.content.Context;

import com.pax.poslink.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils extends IOUtil {

    public static String readFile(String path) {
        File file = new File(path);
        InputStream in = null;
        BufferedReader sr = null;
        String alldata = "";
        try {
            in = new FileInputStream(file);
            sr = new BufferedReader(new InputStreamReader(in));
            String data;
            while ((data = sr.readLine()) != null) {
                alldata += data;
            }

        } catch (Exception e) {
            LogStaticWrapper.getLog().exceptionLog(e);
        } finally {
            close(sr);
            close(in);
        }
        return alldata;
    }

    public static boolean writeFile(String outFile, ByteArrayOutputStream baos) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            LogStaticWrapper.getLog().exceptionLog(e);
        } finally {
            close(fos);
        }
        return false;
    }


    public static byte[] getFromRaw(Context context) {
        try {
            InputStream in = context.getResources().openRawResource(R.raw.apns);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            in.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
