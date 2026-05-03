package com.pax.poslink.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.fullIntegration.InputAccount;

public class EnterValueDialog extends Dialog {

    private TextView title;
    private EditText editText;
    private Button btn_ok, btn_cancel, btSkip;
    private boolean sendArea = false;

    public EnterValueDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.dialog_enter_value, null);
        addContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        title = findViewById(R.id.tv_title);
        editText = findViewById(R.id.edit_enter);
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
        btSkip = findViewById(R.id.btn_skip);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        if (sendArea) {
            ViewTreeObserver vto = title.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int[] top = new int[2];
                    title.getLocationOnScreen(top);
                    int[] bottom = new int[2];
                    btn_cancel.getLocationOnScreen(bottom);
                    InputAccount.InputAreaParams inputAreaParams = new InputAccount.InputAreaParams();
                    inputAreaParams.width = title.getWidth();
                    inputAreaParams.height = bottom[1] - top[1] - title.getHeight() - 20;
                    inputAreaParams.offsetX = top[0] == 0 ? 134 : top[0];
                    inputAreaParams.offsetY = top[1] == 0 ? 550 : top[1] + 10;
                    inputAreaParams.hint = "Please input CVV";
                    boolean supportArea = InputAccount.getInstance().sendInputArea(inputAreaParams, new InputAccount.EnterSecurityCallBack() {
                        @Override
                        public void onAddedSecurityCharacter() {
                            Log.d("TEST", "onAddedSecurityCharacter: ");
                        }

                        @Override
                        public void onDeletedSecurityCharacter() {
                            Log.d("TEST", "onDeletedSecurityCharacter: ");
                        }
                    });
                    boolean supportSkipCvv = InputAccount.getInstance().isSupportSkipInputCVV();
                    if (supportArea) {
                        hideEditText();
                    }
                    if (supportSkipCvv) {
                        btSkip.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public void setTitle(String msg) {
        title.setText(msg);
    }

    public void setInputType(int inputType) {
        editText.setInputType(inputType);
    }

    public void setHint(String msg) {
        editText.setHint(msg);
    }

    public void showSkipButton() {
        btSkip.setVisibility(View.VISIBLE);
    }

    public void setOkOnClickListener(View.OnClickListener l) {
        btn_ok.setOnClickListener(l);
    }

    public void setSkipOnClickListener(View.OnClickListener l) {
        btSkip.setOnClickListener(l);
    }

    public void setCancelOnClickListener(View.OnClickListener l) {
        btn_cancel.setOnClickListener(l);
    }

    public void needSendArea(boolean sendArea) {
        this.sendArea = sendArea;
    }

    public void showTestArea() {
        findViewById(R.id.text_area).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAccount.getInstance().hideInputArea();
            }
        });
        findViewById(R.id.btn_resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAccount.getInstance().resumeInputArea();
            }
        });
    }

    public String getValue() {
        return editText.getText().toString();
    }

    public void hideEditText() {
        editText.setVisibility(View.INVISIBLE);
    }

}
