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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ConvertSigUtils {

    public static int convertSigToPic(String sigdata, String type, String outFile) throws IOException {
        if (sigdata.length() == 0)
            return -1;
        if (outFile.length() == 0)
            return -2;


        Bitmap bmp = generateBmp(sigdata);
        if (bmp != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (type.length() == 0 || type.toLowerCase().equals("bmp")) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            } else if (type.toLowerCase().equals("ico")) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            } else if (type.toLowerCase().equals("jpg")) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            } else if (type.toLowerCase().equals("png")) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            } else {
                return -4;   //fail
            }

            if (FileUtils.writeFile(outFile, baos)) return 0; //success
        }
        return -5 ;
    }

    public static Bitmap generateBmp(String data){
        String div = "\\^";
        String signature_divide[] = data.split(div);
        String x;
        String y;
        String x_y;

        int magin =10;
        int minx;
        int miny;
        int newWidth;
        int newHeight;

        ArrayList<Integer> xVal =new ArrayList<Integer>(signature_divide.length);
        ArrayList<Integer> yVal =new ArrayList<Integer>(signature_divide.length);
        System.out.println("size ="+signature_divide.length);
        for(int i=0; i<signature_divide.length-1; i++)
        {
            try
            {
                x_y = signature_divide[i];
                int pos = x_y.indexOf(",");
                x = x_y.substring(0, pos);
                y = x_y.substring(pos+1);
                if(Integer.parseInt(y)==65535)
                    continue;
                xVal.add(Integer.parseInt(x));
                yVal.add(Integer.parseInt(y));
                //if(Integer.parseInt(x) <= 480 && Integer.parseInt(y) <= 320)
                //bmp.setPixel(Integer.parseInt(x), Integer.parseInt(y), Color.BLACK);

            }catch(Exception e)
            {
                //e.printStackTrace(); ignore correct point
            }
        }

        Collections.sort(yVal);
        Collections.sort(xVal);

        //workaroud  to avoid crash when the sigdata may be invalid.
        if(xVal.isEmpty()) {
            for (int i = 0; i < signature_divide.length-1; i++) {
                yVal.add(0);
                xVal.add(0);
            }
        }
        Bitmap bmp = null;
        if (!xVal.isEmpty() && !yVal.isEmpty()) {
            minx = Integer.parseInt(xVal.get(0).toString());
            System.out.println("minx="+minx);
            miny = Integer.parseInt(yVal.get(0).toString());
            System.out.println("miny="+miny);

            newWidth = Integer.parseInt(xVal.get(xVal.size()-1).toString()) - minx +1 +magin*2;
            newHeight = Integer.parseInt(yVal.get(yVal.size()-1).toString()) - miny +1 + magin*2;

            System.out.println("widht1 = "+newWidth);
            System.out.println("height1 = "+newHeight);

            //BufferedImage bimage = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_RGB);
            bmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.WHITE);


//        ColorDrawable drawable = new ColorDrawable(Color.parseColor("#AA0000"));
//        Canvas canvas2 = new Canvas(bmp);
//        drawable.draw(canvas2);
            //set background white
//        for(int i=0;i<newWidth;i++)2
//            for(int j=0;j<newHeight;j++)
//                bmp.setPixel(i, j, Color.WHITE);


            //connect dots
            Point p1 = new Point();
            Point p2 = new Point();
            Paint blackPen = new Paint();
            blackPen.setColor(Color.BLACK);
            blackPen.setStrokeWidth(2);

            Canvas canvas = new Canvas();
            canvas.setBitmap(bmp);
            for(int i=1;i<signature_divide.length-1;i++)
            {
                x_y =  signature_divide[i-1];
                int pos = signature_divide[i-1].indexOf(",");
                y = x_y.substring(pos + 1);
                x = x_y.substring(0, pos);
                if(Integer.parseInt(y) == 65535)
                    continue;
                p1.x = Integer.parseInt(x) + magin - minx;
                p1.y = Integer.parseInt(y) + magin - miny;

                x_y =  signature_divide[i];
                pos = signature_divide[i].indexOf(",");
                y = x_y.substring(pos + 1);
                x = x_y.substring(0, pos);
                if(Integer.parseInt(y) == 65535)
                    continue;
                p2.x = Integer.parseInt(x)+ magin - minx;
                p2.y = Integer.parseInt(y)+ magin - miny;

                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, blackPen);

            }
            canvas.save();
            canvas.restore();
        }

        return bmp;
    }

}
