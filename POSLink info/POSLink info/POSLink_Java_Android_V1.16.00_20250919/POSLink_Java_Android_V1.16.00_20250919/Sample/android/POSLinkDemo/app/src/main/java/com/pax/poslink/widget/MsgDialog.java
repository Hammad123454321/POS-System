/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.

 * Module Date: 2019/1/11
 * Module Auth: Justin.Z
 * Description:

 * Revision History:
 * Date                   Author                       Action
 * 2019/1/11               Justin.Z                      Create
 * ============================================================================
 */
package com.pax.poslink.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.WindowManager;
import android.widget.TextView;

import com.pax.poslink.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgDialog extends Dialog {

    private TextView textView;
    private Context context;
    private String content;

    public MsgDialog(@NonNull Context context, String content) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.content = content;
    }

    public MsgDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected MsgDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_showmsg);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        textView = findViewById(R.id.msg_main);
        SpannableString sp = new SpannableString(content);
        Pattern p = Pattern.compile("\\[Response]");
        Pattern p2 = Pattern.compile("\\[Request]");
        Matcher m = p.matcher(content);
        Matcher m2 = p2.matcher(content);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            sp.setSpan(new ForegroundColorSpan(Color.parseColor("#ff6600")), start ,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        while (m2.find()) {
            int start = m2.start();
            int end = m2.end();
            sp.setSpan(new ForegroundColorSpan(Color.parseColor("#ff6600")), start ,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(sp);
        textView.setTextSize(18);
        //textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width= WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height= WindowManager.LayoutParams.WRAP_CONTENT;

        getWindow().getDecorView().setPadding(5, 5, 5, 5);

        getWindow().setAttributes(layoutParams);
    }
}
