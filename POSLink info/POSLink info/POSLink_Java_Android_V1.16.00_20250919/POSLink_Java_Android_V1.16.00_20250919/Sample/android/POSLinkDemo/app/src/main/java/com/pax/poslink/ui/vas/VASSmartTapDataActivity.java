package com.pax.poslink.ui.vas;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.ui.base.TabAdapter;
import com.pax.poslink.util.Constant;

public class VASSmartTapDataActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter tabAdapterUsage;
    private Button vasCap, add, delete;
    private EditText vasCapEdt, collectIDEdt, securityEdt, endTapEdt;
    private ServiceUsageFragment serviceUsageFragment;
    private ServiceUpdateFragment serviceUpdateFragment;
    private NewServiceFragment newServiceFragment;

    private String vasDataJson = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_tap_data);
        vasDataJson = getIntent().getStringExtra("vasData");

        initView();

        initFragment();

        initServiceUsage();
        setupServiceUsageViewPager(savedInstanceState, viewPager);

        Button backBtn = findViewById(R.id.vas_special_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button saveBtn = findViewById(R.id.vas_special_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = getData();
                Intent intent = new Intent();
                if (!TextUtils.isEmpty(value)) {
                    intent.putExtra("vas_special", value);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });

        vasCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VASSmartTapDataActivity.this, VASGoogleCapActivity.class);
                intent.putExtra("vasCap", vasCapEdt.getText().toString());
                startActivityForResult(intent, Constant.MANAGE_VAS_CAP);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = viewPager.getCurrentItem();
                setData(index);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = viewPager.getCurrentItem();
                deleteData(index);
            }
        });

        initData();


    }

    private void initView() {
        tabLayout = findViewById(R.id.tl_tab);
        viewPager = findViewById(R.id.vp_content);
        vasCapEdt = findViewById(R.id.vas_google_vas_cap);
        collectIDEdt = findViewById(R.id.vas_collect_id);
        securityEdt = findViewById(R.id.vas_security);
        vasCap = findViewById(R.id.vas_btn_cap);
        endTapEdt = findViewById(R.id.vas_endtap);
        add = findViewById(R.id.vas_add);
        delete = findViewById(R.id.vas_delete);
    }

    private void initData() {
        Gson gson = new Gson();
        ManageRequest.GoogleSmartTapPushService vasData = new ManageRequest.GoogleSmartTapPushService();
        if (TextUtils.isEmpty(vasDataJson)) {
            return;
        }

        vasData = gson.fromJson(vasDataJson, ManageRequest.GoogleSmartTapPushService.class);
        securityEdt.setText(vasData.Security);
        collectIDEdt.setText(vasData.CollectID);
        endTapEdt.setText(vasData.EndTap);
        vasCapEdt.setText(vasData.GoogleSmartTapCap);

        serviceUsageFragment.setServiceUsages(vasData.ServiceUsages);
        serviceUpdateFragment.setServiceUpdates(vasData.ServiceUpdates);
        newServiceFragment.setNewServices(vasData.NewServices);

    }

    private void setData(int index) {
        switch (index) {
            case 0:
                serviceUsageFragment.addData();
                break;
            case 1:
                serviceUpdateFragment.addData();
                break;
            case 2:
                newServiceFragment.addData();
                break;
        }
    }

    private void deleteData(int index) {
        switch (index) {
            case 0:
                serviceUsageFragment.deleteData();
                break;
            case 1:
                serviceUpdateFragment.deleteData();
                break;
            case 2:
                newServiceFragment.deleteData();
                break;
        }
    }

    private String getData() {
        ManageRequest.GoogleSmartTapPushService smartTapData = new ManageRequest.GoogleSmartTapPushService();
        smartTapData.GoogleSmartTapCap = vasCapEdt.getText().toString().trim();
        smartTapData.CollectID = collectIDEdt.getText().toString().trim();
        smartTapData.Security = securityEdt.getText().toString().trim();
        smartTapData.ServiceUsages = serviceUsageFragment.getServiceUsages();
        smartTapData.ServiceUpdates = serviceUpdateFragment.getServiceUpdates();
        smartTapData.NewServices = newServiceFragment.getNewServices();
        smartTapData.EndTap = endTapEdt.getText().toString().trim();


        Gson gson = new Gson();
        return gson.toJson(smartTapData);
//        return "";
    }

    private void initFragment() {
        serviceUsageFragment = ServiceUsageFragment.newInstance();
        serviceUpdateFragment = ServiceUpdateFragment.newInstance();
        newServiceFragment = NewServiceFragment.newInstance();
    }

    private void initServiceUsage() {
        ViewCompat.setElevation(tabLayout, 10);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager)  {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), false);
            }
        });
    }

    private void setupServiceUsageViewPager(Bundle savedInstanceState, final ViewPager viewPager) {
        tabAdapterUsage = new TabAdapter(savedInstanceState, getSupportFragmentManager());
        String[] titles = getResources().getStringArray(R.array.tab_service_usage);
        tabAdapterUsage.addFragment(serviceUsageFragment, titles[0]);
        tabAdapterUsage.addFragment(serviceUpdateFragment, titles[1]);
        tabAdapterUsage.addFragment(newServiceFragment, titles[2]);
        viewPager.setAdapter(tabAdapterUsage);
        viewPager.setOffscreenPageLimit(tabAdapterUsage.getCount());
        viewPager.setCurrentItem(tabAdapterUsage.getSavedIndex(), false);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_VAS_CAP:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("vas_cap");
                    vasCapEdt.setText(result);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
