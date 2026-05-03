package com.pax.poslink.ui.pay;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataAddition3Fragment extends BaseExtDataTabFragment {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private List<Pair<String, String>> showNameMapTag;
    private String extData;

    public static PaymentExtDataAddition3Fragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataAddition3Fragment fragment = new PaymentExtDataAddition3Fragment();
        args.putString(KEY, extData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            extData = bundle.getString(KEY);
        } else {
            extData = savedInstanceState.getString("payment_ext_data" + this.getClass().getName());
        }
        if (TextUtils.isEmpty(extData)) {
            extData = "";
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_ext_data_addition2, container, false);
        initJob(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String data = packExtData();
        outState.putString("payment_ext_data" + this.getClass().getName(), data);
    }

    private void initJob(final View view) {
        showNameMapTag = new ArrayList<Pair<String, String>>() {
            {
                add(new Pair<>("CustomizeData1", "CustomizeData1"));
                add(new Pair<>("CustomizeData2", "CustomizeData2"));
                add(new Pair<>("CustomizeData3", "CustomizeData3"));
            }
        };

        container = (ViewGroup) view.findViewById(R.id.addition2_container);
        initList(extData);

    }

    private void initList(String extData) {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity("CustomizeData1", UIUtil.findXMl(extData, "CustomizeData1"), InputType.TYPE_CLASS_TEXT, "Note the escape characters, i.e. & => &amp; "));
        renderEntityList.add(new NameValueStringEntity("CustomizeData2", UIUtil.findXMl(extData, "CustomizeData2"), InputType.TYPE_CLASS_TEXT, "Note the escape characters, i.e. & => &amp; "));
        renderEntityList.add(new NameValueStringEntity("CustomizeData3", UIUtil.findXMl(extData, "CustomizeData3"), InputType.TYPE_CLASS_TEXT, "Note the escape characters, i.e. & => &amp; "));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    private String packExtData() {
        String extData = "";
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueEntity) {
                NameValueEntity nameValueEntity = (NameValueEntity) renderEntity;
                if (!TextUtils.isEmpty((String) nameValueEntity.getValue())) {
                    String name = nameValueEntity.getName();
                    for (Pair<String, String> pair : showNameMapTag) {
                        if (pair.first.equals(name)) {
                            String tagName = pair.second;
                            extData += "<" + tagName + ">" + StringUtil.getescapeCharacter(nameValueEntity.getValue().toString()) + "</" + tagName + ">";
                            break;
                        }
                    }
                }
            }
        }
        return extData;
    }

    @Override
    public String getExtData() {
        return packExtData();
    }
}

