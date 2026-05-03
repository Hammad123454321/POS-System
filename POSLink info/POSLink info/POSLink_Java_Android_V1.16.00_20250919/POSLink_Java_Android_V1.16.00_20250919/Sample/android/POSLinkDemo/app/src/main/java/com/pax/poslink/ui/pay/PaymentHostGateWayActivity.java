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
import com.pax.poslink.model.payment.PaymentHostGateWayValueSetter;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin.Z on 2020-5-25
 */
public class PaymentHostGateWayActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private String msg = "";
    private PaymentRequest.HostGateWay hostGateWay;
    private Gson gson = new Gson();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        msg = bundle.getString("Payment_HostGateWay");
        setContentView(R.layout.payment_commercial_activity);
        initView();
    }

    private void initView() {
        hostGateWay = TextUtils.isEmpty(msg) ? new PaymentRequest.HostGateWay() : gson.fromJson(msg, PaymentRequest.HostGateWay.class);
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
                hostGateWay = new PaymentRequest.HostGateWay();
                initList();
            }
        });

        Button saveBtn = (Button) findViewById(R.id.payment_extData_save);
        saveBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "";
                getHostGateWay();
                Intent intent = new Intent();
                msg = gson.toJson(hostGateWay);
                intent.putExtra("Payment_HostGateWay", msg);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        initList();
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.HREF, hostGateWay.HRef, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.GATEWAYID, hostGateWay.GatewayId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.TOKENREQUEST, hostGateWay.TokenRequestFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.TOKEN, hostGateWay.Token, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.CARDTYPE, hostGateWay.CardType, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.PASSTHRUDATA, hostGateWay.PassThruData, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.RETURNREASON, hostGateWay.ReturnReason, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.STATIONNO, hostGateWay.StationId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.GLOBALUID, hostGateWay.GlobalUid, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.CUSTOMIZEDATA1, hostGateWay.CustomizeData1, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.CUSTOMIZEDATA2, hostGateWay.CustomizeData2, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.CUSTOMIZEDATA3, hostGateWay.CustomizeData3, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.EWICDISCOUNTAMOUNT, hostGateWay.EwicDiscountAmount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.TOKENSERIALNUMBER, hostGateWay.TokenSerialNum, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostGateWayValueSetter.STATEMENTDESCRIPTOR, hostGateWay.StatementDescriptor, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }


    private void getHostGateWay() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentHostGateWayValueSetter valueSetter = PaymentHostGateWayValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(hostGateWay, nameValueStringEntity.getValue());

            }
        }
    }
}
