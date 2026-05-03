package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.ui.base.TabContainer;
import com.pax.poslink.ui.base.TabItemEntity;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.FragmentCreator;
import com.pax.poslink.util.FragmentsHolder;
import com.pax.poslink.util.adapter.CommonBaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linhb on 2015-09-05.
 */
public class PaymentExtDataActivity extends BaseActivity {
    private static final String TAG = "PaymentExtDataActivity";

    private List<TabItemEntity> renderEntityList = new ArrayList<>();

    private final Map<Integer, FragmentCreator> fragmentCreatorMap = new HashMap<>();
    private CommonBaseAdapter<TabItemEntity> adapter;

    {
        fragmentCreatorMap.put(0, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataAccountFragment.newInstance(extData);
            }
        });
        fragmentCreatorMap.put(1, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataCheckFragment.newInstance(extData);
            }
        });
        fragmentCreatorMap.put(2, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataTraceFragment.newInstance(extData);
            }
        });
        fragmentCreatorMap.put(3, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataCashierFragment.newInstance(extData);
            }
        });
//        fragmentCreatorMap.put(4, new FragmentCreator() {
//            @Override
//            public Fragment create() {
//                return PaymentExtDataCommerceFragment.newInstance(extData);
//            }
//        });
        fragmentCreatorMap.put(4, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataMOTOECommerceFragment.newInstance(extData);
            }
        });
        fragmentCreatorMap.put(5, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataAddition1Fragment.newInstance(extData);
            }
        });
        fragmentCreatorMap.put(6, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataAddition2Fragment.newInstance(extData);
            }
        });
        fragmentCreatorMap.put(7, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentExtDataAddition3Fragment.newInstance(extData);
            }
        });
    }
    private FragmentsHolder fragmentsHolder = new FragmentsHolder(fragmentCreatorMap);

    private String extData = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_ext_data);

        Bundle bundle=getIntent().getExtras();
        extData=bundle.getString("Payment_ExtData");
        System.out.println("Payment_ExtData =" + extData);

        initTab();
        fragmentsHolder.initFragments(savedInstanceState, getSupportFragmentManager(), R.id.main_fragment_container);
        renderEntityList.get(fragmentsHolder.getCurrentFragmentIndex()).setSelected(true);
        adapter.notifyDataSetChanged();


        Button backBtn = (Button)findViewById(R.id.payment_extData_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button saveBtn = (Button)findViewById(R.id.payment_extData_save);
        saveBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String backExtData = "";

                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    backExtData+= ((BaseExtDataTabFragment)fragment).getExtData();
                }
                Intent intent = new Intent();
                intent.putExtra("Payment_ExtData", backExtData);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void initTab() {
        ViewGroup tabTl = (ViewGroup) findViewById(R.id.main_tab_layout);
        ViewCompat.setElevation(tabTl, 10);
        final TabContainer tabContainer = new TabContainer(tabTl);
        adapter = new CommonBaseAdapter<>(renderEntityList);

        List<String> mTabName = Arrays.asList(getResources().getStringArray(R.array.tab_payment_ext));
        for (String tabName : mTabName) {
            renderEntityList.add(new TabItemEntity(tabName, new TabItemEntity.OnItemClickCallback() {
                @Override
                public void onClick(View v, TabItemEntity renderEntity) {
                    fragmentsHolder.switchFragment(renderEntityList.indexOf(renderEntity), getSupportFragmentManager(), R.id.main_fragment_container);
                    renderEntity.setSelected(true);
                    for (int i = 0; i < renderEntityList.size(); i++) {
                        TabItemEntity tabItemEntity = renderEntityList.get(i);
                        if (tabItemEntity != renderEntity) {
                            tabItemEntity.setSelected(false);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }));
        }
        tabContainer.setAdapter(adapter);
    }

    private Bundle setFragmentArg() {

        Bundle bundle = new Bundle();
        bundle.putString("payment_ext_data_each_page", ""); //clear last info in extend data
        return bundle;
    }
}
