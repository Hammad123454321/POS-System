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
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataTraceFragment extends BaseExtDataTabFragment {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private List<Pair<String, String>> showNameMapTag;
    private String extData;

    public static PaymentExtDataTraceFragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataTraceFragment fragment = new PaymentExtDataTraceFragment();
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
        View view = inflater.inflate(R.layout.payment_ext_data_trace, container, false);
        initJob(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String data = packExtData();
        outState.putString("payment_ext_data" + this.getClass().getName(), data);
    }

    private void initJob(final View view)
    {
        showNameMapTag = new ArrayList<Pair<String, String>>() {
            {
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_timestamp), "TimeStamp"));
//                add(new Pair<>(view.getResources().getString(R.string.payment_ext_origecrrefnum), "OrigECRRefNum"));
            }
        };
        container = (ViewGroup) view.findViewById(R.id.trace_container);
        initList(extData);
    }

    private void initList(String extData) {
        renderEntityList.clear();
        container.removeAllViews();

        for (Pair<String, String> entry : showNameMapTag) {
            String value = UIUtil.findXMl(extData, entry.second);
            renderEntityList.add(new NameValueStringEntity(entry.first, value, InputType.TYPE_CLASS_TEXT, ""));
        }

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
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                if (!TextUtils.isEmpty(nameValueStringEntity.getValue())) {
                    String name = nameValueStringEntity.getName();
                    for (Pair<String, String> pair : showNameMapTag) {
                        if (pair.first.equals(name)) {
                            String tagName = pair.second;
                            extData += "<" + tagName + ">" + StringUtil.getescapeCharacter(nameValueStringEntity.getValue().toString()) + "</" + tagName + ">";
                            break;
                        }
                    }
                }
            }
        }
        return extData;
    }

    @Override
    public String getExtData()
    {
        return packExtData();
    }
}
