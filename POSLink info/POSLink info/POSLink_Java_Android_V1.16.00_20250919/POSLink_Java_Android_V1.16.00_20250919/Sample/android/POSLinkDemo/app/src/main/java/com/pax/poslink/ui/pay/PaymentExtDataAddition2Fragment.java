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
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataAddition2Fragment extends BaseExtDataTabFragment {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private List<Pair<String, String>> showNameMapTag;
    private String extData;

    public static PaymentExtDataAddition2Fragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataAddition2Fragment fragment = new PaymentExtDataAddition2Fragment();
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
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_origTransDate), "OrigTransDate"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_origPAN), "OrigPAN"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_origExpiryDate), "OrigExpiryDate"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_gatewayId), "GatewayID"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_origTransTime), "OrigTransTime"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_DisProgPrompts), "DisProgPrompts"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_EntryModeBitmap), "EntryModeBitmap"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_GetSign), "GetSign"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_ReceiptPrint), "ReceiptPrint"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_CPmode), "CPMode"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_posEchoData), "POSEchoData"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_MMID), "MM_ID"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_MMName), "MM_Name"));
                add(new Pair<>(view.getResources().getString(R.string.payment_ext_DebitNetwork), "DebitNetwork"));
                add(new Pair<>("StationNo", "StationNo"));
                add(new Pair<>("OrigSettlementDate", "OrigSettlementDate"));
                add(new Pair<>("OrigTransType", "OrigTransType"));
                add(new Pair<>("ForceFSA", "ForceFSA"));
                add(new Pair<>("ForceCC", "ForceCC"));
                add(new Pair<>("AddlRspDataRequest", "AddlRspDataRequest"));
                add(new Pair<>("UserLanguage", "UserLanguage"));
                add(new Pair<>("GlobalUID", "GlobalUID"));
                add(new Pair<>("OrigAmount", "OrigAmount"));
            }
        };

        container = (ViewGroup) view.findViewById(R.id.addition2_container);
        initList(extData);

    }

    private void initList(String extData) {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_origTransDate), UIUtil.findXMl(extData, "OrigTransDate"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_origPAN), UIUtil.findXMl(extData, "OrigPAN"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_origExpiryDate), UIUtil.findXMl(extData, "OrigExpiryDate"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_gatewayId), UIUtil.findXMl(extData, "GatewayID"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_origTransTime), UIUtil.findXMl(extData, "OrigTransTime"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_DisProgPrompts), UIUtil.findXMl(extData, "DisProgPrompts"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_EntryModeBitmap), UIUtil.findXMl(extData, "EntryModeBitmap"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_GetSign), UIUtil.findXMl(extData, "GetSign"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_ReceiptPrint), UIUtil.findXMl(extData, "ReceiptPrint"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_CPmode), UIUtil.findXMl(extData, "CPMode"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_posEchoData), UIUtil.findXMl(extData, "POSEchoData"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_MMID), UIUtil.findXMl(extData, "MM_ID"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_MMName), UIUtil.findXMl(extData, "MM_Name"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(getResources().getString(R.string.payment_ext_DebitNetwork), UIUtil.findXMl(extData, "DebitNetwork"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("StationNo", UIUtil.findXMl(extData, "StationNo"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("OrigSettlementDate", UIUtil.findXMl(extData, "OrigSettlementDate"), InputType.TYPE_CLASS_TEXT, ""));

        String origTransType = UIUtil.findXMl(extData, "OrigTransType");
        List<String> itemValues = Arrays.asList("", "01", "02", "07");
        int selectedItem = itemValues.indexOf(origTransType);
        renderEntityList.add(new NameValueStringEntity("OrigTransType",  UIUtil.findXMl(extData, "OrigTransType"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("ForceFSA", UIUtil.findXMl(extData, "ForceFSA"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("ForceCC", UIUtil.findXMl(extData, "ForceCC"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("AddlRspDataRequest", UIUtil.findXMl(extData, "AddlRspDataRequest"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("UserLanguage", UIUtil.findXMl(extData, "UserLanguage"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("GlobalUID", UIUtil.findXMl(extData, "GlobalUID"), InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity("OrigAmount", UIUtil.findXMl(extData, "OrigAmount"), InputType.TYPE_CLASS_TEXT, ""));

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

