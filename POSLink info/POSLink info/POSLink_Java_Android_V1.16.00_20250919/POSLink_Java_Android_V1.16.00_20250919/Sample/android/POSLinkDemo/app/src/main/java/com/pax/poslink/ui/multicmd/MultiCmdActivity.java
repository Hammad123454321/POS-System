package com.pax.poslink.ui.multicmd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.PayloadRequest;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.ui.batch.BatchRequestFragment;
import com.pax.poslink.ui.manage.ManageRequestFragment;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.ui.base.TabAdapter;
import com.pax.poslink.ui.pay.PayRequestFragment;
import com.pax.poslink.ui.payload.PayloadRequestFragment;
import com.pax.poslink.ui.report.ReportRequestFragment;
import com.pax.poslink.util.JsonUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiCmdActivity extends BaseActivity implements RequestFragment.OnRequestListener{

    private ViewPager viewPager;
    private TabAdapter tabAdapter;
    private Button processBtn;
    private MultiCmdViewModel model;

    private ArrayList<BaseRequest> multiRequest = new ArrayList<>();
    private String action;
    private List<String> requestList = Arrays.asList("PaymentRequest", "ManageRequest", "BatchRequest", "ReportRequest", "PayloadRequest");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multicmd);
        processBtn = findViewById(R.id.process_btn);
        action = getIntent().getAction();
        viewPager = findViewById(R.id.viewpager);
        if (MultiCmdRequestFragment.ACTION_ADD.equals(action)) {
            processBtn.setText(R.string.save_request);
        } else if (MultiCmdRequestFragment.ACTION_UPDATE.equals(action)) {
            processBtn.setVisibility(View.GONE);
        }
        Log.v(getClass().getSimpleName(), "action:" + action);
        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!multiRequest.isEmpty()) {
                    Type type = new TypeToken<ArrayList<BaseRequest>>(){}.getType();
                    setResultOK("add_request", JsonUtil.generalTypeToJson(multiRequest, type, BaseRequest.class));
                } else {
                    finish();
                }
            }
        });
        model = new ViewModelProvider(this).get(MultiCmdViewModel.class);
        setupViewPager(savedInstanceState, viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager)  {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), false);
            }
        });
    }

    private void setupViewPager(Bundle savedInstanceState, final ViewPager viewPager) {
        tabAdapter = new TabAdapter(savedInstanceState, getSupportFragmentManager());
        String[] titles = getResources().getStringArray(R.array.tab_multicmd);
        String processName = "";
        if (MultiCmdRequestFragment.ACTION_ADD.equals(action)) {
            processName = "Add";
        } else if (MultiCmdRequestFragment.ACTION_UPDATE.equals(action)) {
            processName = "Update";
        }
        String requestName = getIntent().getStringExtra("requestName");
        String requestJson = getIntent().getStringExtra("request");
        int index = requestList.indexOf(requestName);
        setRequest(requestJson, index);
        tabAdapter.addFragment(PayRequestFragment.newInstance(processName), titles[0]);
        tabAdapter.addFragment(ManageRequestFragment.newInstance(processName), titles[1]);
        tabAdapter.addFragment(BatchRequestFragment.newInstance(processName), titles[2]);
        tabAdapter.addFragment(ReportRequestFragment.newInstance(processName), titles[3]);
        tabAdapter.addFragment(PayloadRequestFragment.newInstance(processName), titles[4]);
        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(tabAdapter.getCount());
        if (MultiCmdRequestFragment.ACTION_UPDATE.equals(action)) {
            viewPager.setCurrentItem(index);
        }
//        viewPager.setCurrentItem(tabAdapter.getSavedIndex(), false);
    }

    private void setRequest(String json, int index) {
        switch (index) {
            case 0:
                PaymentRequest paymentRequest = JsonUtil.gsonParseJson(json, PaymentRequest.class);
                model.setPaymentRequest(paymentRequest);
                break;
            case 1:
                ManageRequest manageRequest = JsonUtil.gsonParseJson(json, ManageRequest.class);
                model.setManageRequest(manageRequest);
                break;
            case 2:
                BatchRequest batchRequest = JsonUtil.gsonParseJson(json, BatchRequest.class);
                model.setBatchRequest(batchRequest);
                break;
            case 3:
                ReportRequest reportRequest = JsonUtil.gsonParseJson(json, ReportRequest.class);
                model.setReportRequest(reportRequest);
                break;
            case 4:
                PayloadRequest payloadRequest = JsonUtil.gsonParseJson(json, PayloadRequest.class);
                model.setPayloadRequest(payloadRequest);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof RequestFragment) {
            ((RequestFragment) fragment).setRequestListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        tabAdapter.saveIndex(outState, viewPager.getCurrentItem());
    }

    @Override
    public void onPreRequest(BaseRequest request) {
        if (MultiCmdRequestFragment.ACTION_ADD.equals(action)) {
            multiRequest.add(request);
            processBtn.setText(getString(R.string.save_multi_request, multiRequest.size()));
        } else if (MultiCmdRequestFragment.ACTION_UPDATE.equals(action)){
            Type type = new TypeToken<BaseRequest>(){}.getType();
            setResultOK("update_request", JsonUtil.generalTypeToJson(request, type, BaseRequest.class));
        }
    }

    private void setResultOK(String extraKey, String extraValue) {
        Intent intent = new Intent();
        intent.putExtra(extraKey, extraValue);
        setResult(RESULT_OK, intent);
        finish();
    }
}
