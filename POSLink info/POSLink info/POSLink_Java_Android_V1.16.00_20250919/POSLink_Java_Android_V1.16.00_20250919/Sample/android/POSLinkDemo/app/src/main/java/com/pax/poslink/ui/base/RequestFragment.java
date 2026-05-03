package com.pax.poslink.ui.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.util.adapter.RenderEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class RequestFragment<T extends BaseRequest> extends BaseFragment {
    protected ViewGroup requestContainer;
    protected Button processBtn;

    protected List<RenderEntity> requestRenderEntityList = new ArrayList<>();

    protected ActivityResultReceiver activityResultReceiver;

    protected OnRequestListener<T> mRequestListener;

    private RequestFragment<T> mInstance;

    private static final String ARG_PROCESS_BTN_NAME = "arg_btn_name";

    protected static final String ARG_SPINNER_STR = "arg_spinner_str";

    protected void setActivityResultReceiver(ActivityResultReceiver activityResultReceiver) {
        this.activityResultReceiver = activityResultReceiver;
    }

    protected void onActivityResultReceive(String data) {
        if (activityResultReceiver != null) activityResultReceiver.onReceive(data);
    }

    /**
     * @return Get current fragment's request layout id
     */
    protected abstract @LayoutRes
    int getLayoutId();

    protected abstract void initView(View view);

    protected abstract void onClickProcessBtn(View v);

    public abstract T getRequest();

    public interface OnRequestListener<T extends BaseRequest> {
        void onPreRequest(T request);
    }

    public void setRequestListener(OnRequestListener<T> requestListener) {
        mRequestListener = requestListener;
    }

    public void setProcessBtn(String name) {
        Bundle args = new Bundle();
        args.putString(ARG_PROCESS_BTN_NAME, name);
        setArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_request, container, false);
        processBtn = view.findViewById(R.id.process_btn);
        String processName = null;
        if (getArguments() != null)
            processName = getArguments().getString(ARG_PROCESS_BTN_NAME);
        if (TextUtils.isEmpty(processName))
            processName = getString(R.string.btn_process);
        processBtn.setText(processName);
        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickProcessBtn(v);
            }
        });
        int layoutId = getLayoutId();
        if (layoutId > 0) {
            FrameLayout requestLayer = view.findViewById(R.id.request_container);
            inflater.inflate(layoutId, requestLayer);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());
    }

    protected void preRequest(T request) {
        if (mRequestListener != null) {
            mRequestListener.onPreRequest(request);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRequestListener = null;
    }
}
