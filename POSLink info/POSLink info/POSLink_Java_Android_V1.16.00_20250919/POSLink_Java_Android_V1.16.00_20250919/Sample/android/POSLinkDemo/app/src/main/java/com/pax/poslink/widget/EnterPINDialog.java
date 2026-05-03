package com.pax.poslink.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pax.poslink.R;
import com.pax.poslink.peripheries.PedManager;
import com.pax.poslink.util.UIUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-3-13
 */
public class EnterPINDialog extends Dialog {

    private EditText editText;
    private Button btn_ok, btn_once;
    private ImageView iv1, iv2, iv3, iv4, iv5, iv6, iv7, iv8, iv9, iv0;
    private Button btnClear, btnCancel, btnOk;
    public static final String NUM = "NUM";


    public EnterPINDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.dialog_enter_pin, null);
        addContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        editText = findViewById(R.id.edit_enter);
        iv1 = (ImageView) findViewById(R.id.btn_one);
        iv2 = (ImageView) findViewById(R.id.btn_two);
        iv3 = (ImageView) findViewById(R.id.btn_three);
        iv4 = (ImageView) findViewById(R.id.btn_four);
        iv5 = (ImageView) findViewById(R.id.btn_five);
        iv6 = (ImageView) findViewById(R.id.btn_six);
        iv7 = (ImageView) findViewById(R.id.btn_seven);
        iv8 = (ImageView) findViewById(R.id.btn_eight);
        iv9 = (ImageView) findViewById(R.id.btn_nine);
        iv0 = (ImageView) findViewById(R.id.btn_zero);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnOk = (Button) findViewById(R.id.btn_sure);
        btn_ok = findViewById(R.id.btn_ok);
        btn_once = findViewById(R.id.btn_ok_once);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedHashMap<PedManager.KeyCode, View> linkedHashMap = new LinkedHashMap<>();
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_1, iv1);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_2, iv2);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_3, iv3);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_4, iv4);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_5, iv5);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_6, iv6);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_7, iv7);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_8, iv8);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_9, iv9);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_0, iv0);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_ENTER, btnOk);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_CLEAR, btnClear);
                linkedHashMap.put(PedManager.KeyCode.KEYCODE_CANCEL, btnCancel);
                PedManager.getInstance(getContext()).setPedKeyBoardLayout(false, linkedHashMap);
                UIUtil.showToast(getContext(), "setPED success.", Toast.LENGTH_SHORT);
                dismiss();
            }
        });

        btn_once.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PedManager.getInstance(getContext()).setPedKeyBoardLayout(true, getPedKeyInfos());
                UIUtil.showToast(getContext(), "setPED success.", Toast.LENGTH_SHORT);
                dismiss();
            }
        });
    }

    private List<PedManager.PedKeyInfo> getPedKeyInfos() {
        List<PedManager.PedKeyInfo> pedKeyInfoList = new ArrayList<>();
        Map<View, PedManager.KeyCode> views = new LinkedHashMap<View, PedManager.KeyCode>() {
            {
                put(iv1, PedManager.KeyCode.KEYCODE_1);
                put(iv2, PedManager.KeyCode.KEYCODE_2);
                put(iv3, PedManager.KeyCode.KEYCODE_3);
                put(iv4, PedManager.KeyCode.KEYCODE_4);
                put(iv5, PedManager.KeyCode.KEYCODE_5);
                put(iv6, PedManager.KeyCode.KEYCODE_6);
                put(iv7, PedManager.KeyCode.KEYCODE_7);
                put(iv8, PedManager.KeyCode.KEYCODE_8);
                put(iv9, PedManager.KeyCode.KEYCODE_9);
                put(iv0, PedManager.KeyCode.KEYCODE_0);
                put(btnOk, PedManager.KeyCode.KEYCODE_ENTER);
                put(btnCancel, PedManager.KeyCode.KEYCODE_CANCEL);
                put(btnClear, PedManager.KeyCode.KEYCODE_CLEAR);
            }
        };
        for (Map.Entry<View, PedManager.KeyCode> entry : views.entrySet()) {
            View view = (View) entry.getKey();
            PedManager.KeyCode type = (PedManager.KeyCode) entry.getValue();
            int x, y, w, h;
            if (null != view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                x = location[0];
                y = location[1];
                w = view.getWidth();
                h = view.getHeight();
            } else {
                x = 0;
                y = 0;
                w = 0;
                h = 0;
            }
            pedKeyInfoList.add(new PedManager.PedKeyInfo(type, x, y, w, h));
        }
        return pedKeyInfoList;
    }


    public void showOKButton() {
        btn_ok.setVisibility(View.VISIBLE);
        btn_once.setVisibility(View.VISIBLE);
    }

    public void setPINCallback(PinDialogCallback pinCallback) {
        pinCallback.onStart(this, editText);
    }

    public interface PinDialogCallback {
        void onStart(EnterPINDialog pinDialog, EditText editText);
    }
}