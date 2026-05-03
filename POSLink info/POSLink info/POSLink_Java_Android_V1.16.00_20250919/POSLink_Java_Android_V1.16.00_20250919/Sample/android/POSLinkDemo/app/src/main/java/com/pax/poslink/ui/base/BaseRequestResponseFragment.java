package com.pax.poslink.ui.base;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pax.poslink.R;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.util.adapter.RenderEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRequestResponseFragment<T extends BaseRequest, D extends RequestFragment> extends TaskFragment implements RequestFragment.OnRequestListener<T>{

    private String REQUEST_TAG;

    protected View responseLayout;
    protected ViewGroup responseContainer;

    protected List<RenderEntity> responseRenderEntityList = new ArrayList<>();

    protected abstract D createRequestFragment();

    protected D findRequestFragment() {
        D requestFragment = (D) getChildFragmentManager().findFragmentByTag(REQUEST_TAG);
        return requestFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_request_response, container, false);
        responseLayout = view.findViewById(R.id.response_layout);
        responseContainer = view.findViewById(R.id.response_container);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        D fragment = createRequestFragment();
        REQUEST_TAG = fragment.getClass().getSimpleName();
        fragment.setRequestListener(this);
        fragmentTransaction.replace(R.id.request_layout, fragment, REQUEST_TAG);
        fragmentTransaction.commit();
    }
}
