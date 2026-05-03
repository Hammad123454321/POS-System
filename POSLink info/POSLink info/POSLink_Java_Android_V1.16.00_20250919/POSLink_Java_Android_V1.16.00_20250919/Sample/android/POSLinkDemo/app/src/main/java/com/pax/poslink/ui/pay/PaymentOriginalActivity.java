package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.model.payment.PaymentOriginalValueSetter;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin.Z on 2020-5-25
 */
public class PaymentOriginalActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private String msg = "";
    private PaymentRequest.Original original;
    private Gson gson = new Gson();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        msg = bundle.getString(Constant.BUNDLE_KEY_PAYMENT_ORIGINAL);
        setContentView(R.layout.payment_commercial_activity);
        initView();
    }

    private void initView() {
        original = TextUtils.isEmpty(msg) ? new PaymentRequest.Original() : gson.fromJson(msg, PaymentRequest.Original.class);
        container = findViewById(R.id.commercial_container);
        Button backBtn = (Button) findViewById(R.id.payment_extData_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button clearBtn = findViewById(R.id.payment_extData_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                original = new PaymentRequest.Original();
                initList();
            }
        });

        Button saveBtn = (Button) findViewById(R.id.payment_extData_save);
        saveBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "";
                getData();
                Intent intent = new Intent();
                msg = gson.toJson(original);
                intent.putExtra(Constant.BUNDLE_KEY_PAYMENT_ORIGINAL, msg);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        initList();
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGTRANSDATE, original.TransDate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGPAN, original.Pan, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGEXPIRYDATE, original.ExpiryDate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGTRANSTIME, original.TransTime, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGSETTLEMENTDATE, original.SettlementDate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGTRANSTYPE, original.TransType, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGAMOUNT, original.Amount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGBATCHNUMBER, original.BatchNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.ORIGTRANSID, original.TransId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.PS2000, original.PaymentService2000, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.AUTH_RESPONSE, original.AuthorizationResponse, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentOriginalValueSetter.TRANSACTIONIDENTIFIER, original.TransactionIdentifier, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }


    private void getData() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentOriginalValueSetter valueSetter = PaymentOriginalValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(original, nameValueStringEntity.getValue());

            }
        }
    }
}