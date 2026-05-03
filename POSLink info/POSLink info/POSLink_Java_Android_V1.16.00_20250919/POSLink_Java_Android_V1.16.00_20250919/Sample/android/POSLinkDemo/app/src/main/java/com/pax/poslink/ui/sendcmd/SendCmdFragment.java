package com.pax.poslink.ui.sendcmd;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.pax.poscomm.utils.CommLog;
import com.pax.poslink.AsyncPosLinkTask;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.poslink.POSLinkCreator;
import com.pax.poslink.ui.base.TaskFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SendCmdFragment extends TaskFragment implements View.OnTouchListener,
        ProcessProgressDialog.OnSetListener, View.OnClickListener {

    protected ViewGroup responseContainer;

    protected List<RenderEntity> responseRenderEntityList = new ArrayList<>();

    private Button btn_sendcmd;
    private EditText edit_request1, edit_response1, edit_request2,
            edit_request3, edit_response2, edit_response3;
    protected ViewGroup requestContainer;
    private LinearLayout linearLayout;

    private View rootView;
    private PosLink posLink;
    private ProcessTransResult ptr;
    private volatile boolean isShowing;
    private int screenHeight;

    private int blankHeight = 0;

    public static final String CH_STX = "[02]";
    public static final String CH_ETX = "[03]";
    public static final String CH_ENQ = "[05]";
    public static final String CH_FS = "[1c]";
    public static final String CH_GS = "[1d]";
    public static final String CH_US = "[1f]";
    public static final String CH_RS = "[1e]";

    public static final String S_STX = "\u0002";
    public static final String S_ETX = "\u0003";
    public static final String S_ENQ = "\u0005";
    public static final String S_FS = "\u001c";
    public static final String S_GS = "\u001d";
    public static final String S_US = "\u001f";
    public static final String S_RS = "\u001e";

    public static SendCmdFragment newInstance() {
        return new SendCmdFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.sendcmd_fragment, container, false);


        initView(rootView);
        init();
        return rootView;
    }

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean focus) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            if (focus && !isShowing) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                view.requestFocus();
                isShowing = true;
            }
        }
    };

    private void initView(View rootView) {
        btn_sendcmd = rootView.findViewById(R.id.manage_request_btn_process);
        btn_sendcmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });
        edit_request1 = rootView.findViewById(R.id.edit_request1);
        edit_request2 = rootView.findViewById(R.id.edit_request2);
        edit_request3 = rootView.findViewById(R.id.edit_request3);
        edit_request1.setOnClickListener(this);
        edit_request1.setOnFocusChangeListener(onFocusChangeListener);
        edit_request2.setOnFocusChangeListener(onFocusChangeListener);
        edit_request3.setOnFocusChangeListener(onFocusChangeListener);
        edit_request1.setText("[02]T00[1c]1.44[1c]01[1c]100[1c][1c]1[1c][1c][1c][1f][1f][1f][1f][1f][1f][1f][1f][1f][1f][1f][1f][1f][1f][1f][1c][1c][1c][1c]0[03]f");
        edit_request2.setText("[02]A20[1c]1.39[1c]0[1c][1c]00[1c]600[1c]0[03]c");
        edit_request3.setText("[02]A08[1c]1.39[1c]0[1c]90000[03]J");

        rootView.findViewById(R.id.btn_etx).setOnClickListener(this);
        rootView.findViewById(R.id.btn_stx).setOnClickListener(this);
        rootView.findViewById(R.id.btn_fs).setOnClickListener(this);
        rootView.findViewById(R.id.btn_gs).setOnClickListener(this);
        rootView.findViewById(R.id.btn_us).setOnClickListener(this);
        screenHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                int newBlankheight = screenHeight - rect.bottom;

                if (newBlankheight != blankHeight) {
                    if(newBlankheight==0){
                        // keyboard close
                        isShowing = false;
                    }else{
                        // keyboard pop
                    }
                }
                blankHeight = newBlankheight;
            }
        });
        responseContainer = rootView.findViewById(R.id.manage_response_container);
        linearLayout = rootView.findViewById(R.id.send_cmd_response);
        showCorrespondingRequestView();
    }

    private void init() {
        posLink = POSLinkCreator.createPoslink(getActivity());
    }

    private void showCorrespondingRequestView() {
    }

    private void sendMsg() {
        mTask = new AsyncPosLinkTask(this);
        dataFragment.setData(mTask);
        mTask.execute();
    }

    private String replaceMsg(String msg) {
        for (int i = 0; i <= 256; i++) {
            String s = Integer.toHexString(i).toLowerCase();
            if (i <= 10) {
                s = "0" + s;
            }
            char c = (char) Integer.parseInt(s, 16);
            msg = msg.replace("[" + s + "]", c + "");
        }
        return msg;
    }

    private String replaceMsgResponse(String msg) {
        return CommLog.getHexString(msg);
    }

    @Override
    public void run() {
        List<String> list = new ArrayList<>();
        list.add(replaceMsg(edit_request1.getText().toString()));
        list.add(replaceMsg(edit_request2.getText().toString()));
        list.add(replaceMsg(edit_request3.getText().toString()));
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (TextUtils.isEmpty(str)) {
                iterator.remove();
            }
        }

        posLink.SetCommSetting(SettingINI.getCommSettingFromFile(posLink.appDataFolder + "/" + SettingINI.FILENAME));
        if (list.isEmpty()) {
            return;
        }
        ptr = posLink.ProcessTrans(list);

    }

    @Override
    public void onTaskCompleted() {
        List<String> responses = ptr.Responses;
        setResponse(responses);
        if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {
            Log.i(TAG, "Transaction succeed!");
        } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_TIMEOOUT;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            mHandler.sendMessage(msg);

            Log.e(TAG, "Transaction TimeOut! " + String.valueOf(ptr.Code));
            Log.e(TAG, "Transaction TimeOut! " + ptr.Msg);
        } else {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_FAILURE;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            mHandler.sendMessage(msg);

            Log.e(TAG, "Transaction Error! " + String.valueOf(ptr.Code));
            Log.e(TAG, "Transaction Error! " + ptr.Msg);
        }
        super.onTaskCompleted();
    }

    private void setResponse(List<String> responses) {
        if (linearLayout.getVisibility() == View.GONE) {
            linearLayout.setVisibility(View.VISIBLE);
        }
        responseRenderEntityList.clear();
        responseContainer.removeAllViews();

        for (String response : responses) {
            response = replaceMsgResponse(response);
            responseRenderEntityList.add(new NameValueStringUnEditableEntity("Response_Str", response));
        }

        for (RenderEntity renderEntity : responseRenderEntityList) {
            if (renderEntity instanceof NameValueEntity && TextUtils.isEmpty(((NameValueEntity<String>) renderEntity).getValue())) {
                continue;
            }
            CommonItemView itemView = renderEntity.createView(responseContainer);
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.payment_process_prompt), true, false, this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {

    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                break;
            case Constant.TRANSACTION_TIMEOOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_etx:
                setTextToEdit(CH_ETX);
                break;
            case R.id.btn_stx:
                setTextToEdit(CH_STX);
                break;
            case R.id.btn_fs:
                setTextToEdit(CH_FS);
                break;
            case R.id.btn_us:
                setTextToEdit(CH_US);
                break;
            case R.id.btn_gs:
                setTextToEdit(CH_GS);
                break;
        }
    }

    private void setTextToEdit(String msg) {
        View rootview = getActivity().getWindow().getDecorView();
        View currentView = rootview.findFocus();
        if (currentView instanceof EditText) {
            EditText mEditText = ((EditText) currentView);
            mEditText.getText().insert(getEditTextCursorIndex(mEditText), msg);
            if (!isShowing) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                mEditText.requestFocus();
                isShowing = true;
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            hideSoftInput();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideSoftInput();
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive() && isShowing) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            isShowing = false;
        }
    }

    private int getEditTextCursorIndex(EditText mEditText) {
        return mEditText.getSelectionStart();
    }
}
