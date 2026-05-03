package com.pax.poslink.fullIntegration;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pax.poslink.R;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.peripheries.MiscSettings;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;

import java.io.InputStream;

/**
 * Created by Leon.F on 2018/5/15.
 */
public class InputAccountDialog extends Dialog {

    private EditText cardNumEdt;
    private AlphaAnimation blinking;

    private boolean isManual = false;
    private Button confirmBtn;
    private SparseArray<Light> clssLight = new SparseArray<>();

    private String prompt;
    private String amount;
    private DialogCallback dialogCallback;
    private byte searchMode;
    private int maxAccountLength;

    private LinearLayout linManualCardno;
    private ImageView ivUntouch;
    private LinearLayout llClssLight;
    private ImageView ivSwipe;
    private ImageView ivInsert;
    private Params params;
    private TextView promptTv;
    private boolean supportInputArea;
    private int count;

    public InputAccountDialog(@NonNull Context context, Params params, DialogCallback dialogCallback) {
        super(context);
        init(params, dialogCallback);
    }

    public InputAccountDialog(@NonNull Context context, int themeResId, Params params, DialogCallback dialogCallback) {
        super(context, themeResId);
        init(params, dialogCallback);
    }

    private void init(Params params, DialogCallback dialogCallback) {
        this.params = params;
        searchMode = params.getMode();
        prompt = params.getSearchCardPromptMsg();
        amount = params.getTransAmount();
        this.dialogCallback = dialogCallback;
        maxAccountLength = params.getMaxLength();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.dialog_input_account, null);
        addContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        promptTv = (TextView) rootView.findViewById(R.id.prompt_tv);
        cardNumEdt = (EditText) rootView.findViewById(R.id.cardNum_edt);
        TextView amountTv = (TextView) rootView.findViewById(R.id.amount_tv);
        confirmBtn = (Button) rootView.findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClicked();
            }
        });
        LinearLayout amountLayout = (LinearLayout) rootView.findViewById(R.id.search_amount_layout);
        linManualCardno = (LinearLayout) rootView.findViewById(R.id.lin_manual_cardno);
        ivSwipe = (ImageView) rootView.findViewById(R.id.iv_swipe);
        ivInsert = (ImageView) rootView.findViewById(R.id.iv_insert);
        ivUntouch = (ImageView) rootView.findViewById(R.id.iv_untouch);
        llClssLight = (LinearLayout) rootView.findViewById(R.id.clssLight);

        if (amount == null) {
            amountLayout.setVisibility(View.INVISIBLE);
        } else {
            amountTv.setText(StringUtil.showFloatValueString(amount));
        }
        if (Convenience.isButtonClickEnough()) {
            findViewById(R.id.test).setVisibility(View.VISIBLE);
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
        promptTv.setText(prompt);
        showSearchMode();
        dialogCallback.onCreate(this, new LightChanger(), new ModeChanger());
    }

    @Override
    protected void onStart() {
        super.onStart();
        MiscSettings.setHomeKeyEnable(getContext(), false);
        MiscSettings.setRecentKeyEnable(getContext(), false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MiscSettings.setHomeKeyEnable(getContext(), true);
        MiscSettings.setRecentKeyEnable(getContext(),true);
    }

    private class CardNumWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int length = s.toString().length();
            // 判定是否启用寻卡线程
            if (length == 0) {
                confirmBtnChange();
                isManual = false;
            }

            if (length == 1 && cardNumEdt.isFocusable()) {
                confirmBtnChange();
                isManual = true;
            }

            if (s.length() == 0)
                return;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (i != 4 && i != 9 && i != 14 && i != 19
                        && i != 24 && i != 29 && s.charAt(i) == ' ') {
                    continue;
                } else {
                    sb.append(s.charAt(i));
                    if ((sb.length() == 5 || sb.length() == 10 || sb.length() == 15 || sb.length() == 20 || sb.length() == 25 || sb.length() == 30)
                            && sb.charAt(sb.length() - 1) != ' ') {
                        sb.insert(sb.length() - 1, ' ');
                    }
                }
            }

            if (!sb.toString().equals(s.toString())) {
                int index = start + 1;
                if (sb.charAt(start) == ' ') {
                    if (before == 0) {
                        index++;
                    } else {
                        cardNumEdt.setText(sb.subSequence(0, sb.length() - 1));
                        index--;
                    }
                } else {
                    if (before == 1) {
                        index--;
                    }
                }
                cardNumEdt.setText(sb.toString());
                cardNumEdt.setSelection(index);
            }

            if (sb.toString().charAt(sb.length() - 1) == ' ') {
                cardNumEdt.setText(sb.toString().substring(0, sb.length() - 1));
                cardNumEdt.setSelection(sb.length() - 1);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }


    private void confirmBtnChange() {
        String content = cardNumEdt.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        }
    }

    private void setLightImage(int index, int status) {
        Light lightTemp = clssLight.get(index);
        if (lightTemp == null) {
            return;
        }
        if (status == Light.STATUS_BLINK) {
            lightTemp.view.setImageResource(lightTemp.status[Light.STATUS_ON]);
            lightTemp.view.startAnimation(blinking);
        } else {
            lightTemp.view.clearAnimation();
            lightTemp.view.setImageResource(lightTemp.status[status]);
        }
    }

    public static class Light {

        public static final int STATUS_OFF = 0;
        public static final int STATUS_ON = 1;
        public static final int STATUS_BLINK = 2;
        final ImageView view;
        final Integer[] status;

        Light(ImageView view, Integer[] status) {
            this.view = view;
            this.status = status;
        }

    }


    public void onConfirmClicked() {
        if (supportInputArea) {
            dialogCallback.onConfirmCardNum("");
            return;
        }
        if (isManual) {
            String content = cardNumEdt.getText().toString().replace(" ", "");
            if (content == null || content.length() == 0) {
                Toast.makeText(getContext(), "Please swipe card or enter account", Toast.LENGTH_LONG).show();
                cardNumEdt.setText("");
                return;
            }

            if (content.length() < params.minLength || content.length() > params.maxLength) {
                Toast.makeText(getContext(), "The data length must be between " + params.minLength + " to " + params.maxLength, Toast.LENGTH_LONG).show();
//                cardNumEdt.setText("");
                return;
            }

            String cardNum = cardNumEdt.getText().toString();
            cardNum = cardNum.replace(" ", "");
            dialogCallback.onConfirmCardNum(cardNum);
        }
    }

    private void showSearchMode() {
        if ((searchMode & SearchCardMode.KEYIN) == SearchCardMode.KEYIN) {
            prompt += "/Enter Account";
            ViewTreeObserver vto = promptTv.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                        promptTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int[] top = new int[2];
                        promptTv.getLocationOnScreen(top);
                        int[] bottom = new int[2];
                        ivSwipe.getLocationOnScreen(bottom);
                        InputAccount.InputAreaParams inputAreaParams = new InputAccount.InputAreaParams();
                        inputAreaParams.width = promptTv.getWidth();
                        inputAreaParams.height = bottom[1] - top[1] + 30;
                        inputAreaParams.offsetX = top[0] == 0 ? 134 : top[0];
                        inputAreaParams.offsetY = top[1] == 0 ? 430 : top[1] - 30;
                        inputAreaParams.hint = "card number";
                        inputAreaParams.strokeWidth = 3;
                        inputAreaParams.strokeColor = "#a1a3a6";
                        inputAreaParams.paddingLeft = 16;
                        inputAreaParams.cornerRadius = 8;
                        inputAreaParams.imgWidth = 50;
                        inputAreaParams.imgHeight = 50;
                        try (InputStream in = getContext().getAssets().open("card.png");) {
                            byte[] bytes = new byte[in.available()];
                            in.read(bytes);
                            inputAreaParams.leftImg = bytes;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        supportInputArea = InputAccount.getInstance().sendInputArea(inputAreaParams,
                                new InputAccount.EnterSecurityCallBack() {
                                    @Override
                                    public void onAddedSecurityCharacter() {
                                        confirmBtn.setEnabled(true);
                                        count++;
                                        Log.d("TEST", "onAddedSecurityCharacter: " + count);
                                    }

                                    @Override
                                    public void onDeletedSecurityCharacter() {
                                        count--;
                                        if (count == 0) {
                                            confirmBtn.setEnabled(false);
                                        }
                                        Log.d("TEST", "onDeletedSecurityCharacter: " + count);
                                    }
                                });
                        linManualCardno.setVisibility(View.VISIBLE);
                        confirmBtn.setVisibility(View.VISIBLE);
                        if (supportInputArea) {
                            cardNumEdt.setVisibility(View.INVISIBLE);
                            confirmBtn.setEnabled(true);
                            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
                        } else {
                            cardNumEdt.addTextChangedListener(new CardNumWatcher());
                            cardNumEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxAccountLength + 4)});
                            confirmBtn.setEnabled(false);
                        }

                }
            });
        } else {
            cardNumEdt.setVisibility(View.GONE);
        }

        if ((searchMode & SearchCardMode.TAP) == SearchCardMode.TAP) {// 支持非接
            ivUntouch.setImageResource(R.mipmap.untouch_card_en);
            blinking = new AlphaAnimation(1, 0);
            blinking.setDuration(500);
            blinking.setRepeatCount(Animation.INFINITE);
            blinking.setRepeatMode(Animation.REVERSE);
            clssLight.clear();
            clssLight.put(R.id.light1, new Light((ImageView) findViewById(R.id.light1), new Integer[]{R.mipmap.blue_off, R.mipmap.blue_on}));
            clssLight.put(R.id.light2, new Light((ImageView) findViewById(R.id.light2), new Integer[]{R.mipmap.yellow_off, R.mipmap.yellow_on}));
            clssLight.put(R.id.light3, new Light((ImageView) findViewById(R.id.light3), new Integer[]{R.mipmap.green_off, R.mipmap.green_on}));
            clssLight.put(R.id.light4, new Light((ImageView) findViewById(R.id.light4), new Integer[]{R.mipmap.red_off, R.mipmap.red_on}));
            llClssLight.setVisibility(View.VISIBLE);
        } else {
            llClssLight.setVisibility(View.GONE);
            ivUntouch.setImageResource(R.mipmap.no_untouch_card_en);
        }

        ivSwipe.setImageResource((searchMode & SearchCardMode.SWIPE) == SearchCardMode.SWIPE ? R.mipmap.swipe_card_en : R.mipmap.no_swipe_card_en);
        ivInsert.setImageResource((searchMode & SearchCardMode.INSERT) == SearchCardMode.INSERT ? R.mipmap.insert_card_en : R.mipmap.no_insert_card_en);
        ivUntouch.setImageResource((searchMode & SearchCardMode.TAP) == SearchCardMode.TAP ? R.mipmap.untouch_card_en : R.mipmap.no_untouch_card_en);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    public static class Params {
        private byte mode;
        private String transAmount;
        private String searchCardPromptMsg;
        private int minLength;
        private int maxLength;

        public Params(byte mode, String transAmount, String searchCardPromptMsg, int minLength, int maxLength) {
            this.mode = mode;
            this.transAmount = transAmount;
            this.searchCardPromptMsg = searchCardPromptMsg;
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        public byte getMode() {
            return mode;
        }

        public String getTransAmount() {
            return transAmount;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public int getMinLength() {
            return minLength;
        }

        public String getSearchCardPromptMsg() {
            return searchCardPromptMsg;
        }
    }

    public class LightChanger {

        public void change(int index, int status) {
            setLightImage(index, status);
        }
    }

    public class ModeChanger {
        public void change(byte mode) {
            searchMode = mode;
            showSearchMode();
        }
    }

    public interface DialogCallback {
        void onCreate(InputAccountDialog inputAccountDialog, LightChanger lightChanger, ModeChanger modeChanger);
        void onConfirmCardNum(String cardNum);
    }
}
