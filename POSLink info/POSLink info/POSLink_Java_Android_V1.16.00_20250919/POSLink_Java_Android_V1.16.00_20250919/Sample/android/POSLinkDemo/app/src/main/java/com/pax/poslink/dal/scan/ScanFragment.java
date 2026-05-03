/*
 * COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2009-2020 PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 */

package com.pax.poslink.dal.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseFragment;

/**
 * Created by Eminem.H on 2018/9/6.
 * Description:
 */
public class ScanFragment extends BaseFragment {

    private View rootView = null;
    //    private ScanPresenter scanPresenter;
    private ScanPresenter scanPresenter;

    public static ScanFragment newInstance() {
        Bundle args = new Bundle();
        ScanFragment fragment = new ScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        }
        initView(rootView);
        return rootView;
    }


    private void initView(View rootView) {
        scanPresenter = new ScanPresenter(rootView, rootView.getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            scanPresenter.startScan(getActivity().getWindowManager().getDefaultDisplay().getRotation());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        scanPresenter.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
