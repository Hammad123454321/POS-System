package com.pax.poslink.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by Justin.Z on 2020-5-25
 */
public class AlwaysMarqueeButtonView extends AppCompatButton {
    public AlwaysMarqueeButtonView(Context context) {
        super(context);
    }

    public AlwaysMarqueeButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlwaysMarqueeButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
