package com.pax.poslink.dal.print;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Leon.F on 2018/3/23.
 */

public class PrintUtil {
    public static Bitmap shotScrollView(ScrollView scrollView) {
        Bitmap bitmap = null;
        View childAt = scrollView.getChildAt(0);
        bitmap = Bitmap.createBitmap(childAt.getWidth(), childAt.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
//        iterDraw(childAt, canvas);
        return bitmap;
    }

    private static void iterDraw(View view, Canvas canvas) {
        int save = canvas.save();
        canvas.translate(view.getLeft(), view.getTop());
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                iterDraw(((ViewGroup) view).getChildAt(i), canvas);
            }
        } else {
            view.draw(canvas);
        }
        canvas.restoreToCount(save);
    }

    public static String getSystemDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        return df.format(new Date());
    }

    public static String getSystemTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(new Date());
    }
}
