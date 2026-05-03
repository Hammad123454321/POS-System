/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.

 * Module Date: 2019-5-28
 * Module Auth: Justin.Z
 * Description:

 * Revision History:
 * Date                   Author                       Action
 * 2019-5-28               Justin.Z                      Create
 * ============================================================================
 */
package com.pax.poslink.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.pax.poslink.R;

public class FloatImageView extends View {

    private Paint paint;
    private Rect rect;
    private Paint textPaint;
    private int height;
    private int width;
    private int index;

    private String showText;
    private int backgroundColor;
    private int textSize;
    private int textColor;

    public FloatImageView(Context context) {
        this(context, null);
    }

    public FloatImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray t = getContext().obtainStyledAttributes(attrs,
                R.styleable.FloatImageView);
        this.backgroundColor = t.getColor(R.styleable.FloatImageView_backgroundColor, Color.BLUE);
        this.textSize = t.getDimensionPixelSize(R.styleable.FloatImageView_textSize, 20);
        this.showText = t.getString(R.styleable.FloatImageView_text);
        this.textColor = t.getColor(R.styleable.FloatImageView_textColor, Color.BLACK);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        index = (height / 3) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        paint.setColor(backgroundColor);
        rect = new Rect(-200, height / 2, width + 200, height / 2 + index);
        canvas.rotate(-45, width / 2 , height / 2);
        canvas.drawRect(rect, paint);
        canvas.drawText(showText, width / 2,height / 2 + ((index * 3) / 4), textPaint);
        canvas.restore();
    }

    private void initPaint() {
        paint = new Paint();
        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);//Paint设置水平居中
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk > 21)
            setElevation(1);

    }

    public void setText(String msg){
        this.showText = msg;
    }

    public void setBackGroundColor(int color) {
        this.backgroundColor = color;
    }

}
