package com.pax.poslink.ui.vas;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;


public class VASSpecialDataActivity extends BaseActivity {

    private String mVasProgram;
    private String dataJson = "";
    private BaseExtDataTabFragment mCurrentFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vas_special_data);
        mVasProgram = getIntent().getStringExtra("vas_program");
        dataJson = getIntent().getStringExtra("vas_specialData");

        if ("1".equals(mVasProgram)) {
            mCurrentFragment = VASAppleFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("vas_specialData", dataJson);
            mCurrentFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.vas_fragment_container, mCurrentFragment);
            transaction.commit();
        } else if ("2".equals(mVasProgram)) {
            // TODO: 5/6/2019 add vas google fragment
            mCurrentFragment = VASGoogleFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("vas_specialData", dataJson);
            mCurrentFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.vas_fragment_container, mCurrentFragment);
            transaction.commit();
        }

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
                String value = mCurrentFragment.getExtData();
                Intent intent = new Intent();
                if (!TextUtils.isEmpty(value)) {
                    intent.putExtra("vas_special", value);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
    }
}
