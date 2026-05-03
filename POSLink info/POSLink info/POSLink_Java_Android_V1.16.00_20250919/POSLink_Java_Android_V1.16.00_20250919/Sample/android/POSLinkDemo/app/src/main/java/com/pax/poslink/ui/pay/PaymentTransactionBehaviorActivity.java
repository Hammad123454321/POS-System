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
import com.pax.poslink.model.payment.PaymentTransactionBehaviorValueSetter;
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
public class PaymentTransactionBehaviorActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private String msg = "";
    private PaymentRequest.TransactionBehavior transactionBehavior;
    private Gson gson = new Gson();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        msg = bundle.getString(Constant.BUNDLE_KEY_PAYMENT_TRANSACTION);
        setContentView(R.layout.payment_commercial_activity);
        initView();
    }

    private void initView() {
        transactionBehavior = TextUtils.isEmpty(msg) ? new PaymentRequest.TransactionBehavior() : gson.fromJson(msg, PaymentRequest.TransactionBehavior.class);
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
                transactionBehavior = new PaymentRequest.TransactionBehavior();
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
                msg = gson.toJson(transactionBehavior);
                intent.putExtra(Constant.BUNDLE_KEY_PAYMENT_TRANSACTION, msg);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        initList();
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.SIGN, transactionBehavior.SignatureCaptureFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.TIPREQ, transactionBehavior.TipRequestFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.SIGNUPLOAD, transactionBehavior.SignatureUploadFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.REPORTSTATUS, transactionBehavior.StatusReportFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.CARDTYPEBITMAP, transactionBehavior.AcceptedCardType, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.DISPROGPROMPTS, transactionBehavior.ProgramPromptsFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.GETSIGN, transactionBehavior.SignatureAcquireFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.ENTRYMODEBITMAP, transactionBehavior.EntryMode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.RECEIPTPRINT, transactionBehavior.ReceiptPrintFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.CPMODE, transactionBehavior.CardPresentMode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.DEBITNETWORK, transactionBehavior.DebitNetwork, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.USERLANGUAGE, transactionBehavior.UserLanguage, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.ADDLRSPDATAREQUEST, transactionBehavior.AddlRspDataFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.FORCECC, transactionBehavior.ForceCC, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.FORCEFSA, transactionBehavior.ForceFsa, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.FORCE, transactionBehavior.ForceDuplicate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.ACCESSIBILITYPINPAD, transactionBehavior.AccessibilityPinPad, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.DISTRANSPROMPTS, transactionBehavior.DistransPrompts, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.COFINDICATOR, transactionBehavior.CoFIndicator, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.COFINITIATOR, transactionBehavior.CoFInitiator, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentTransactionBehaviorValueSetter.GIFTCARDINDICATOR, transactionBehavior.GiftCardIndicator, InputType.TYPE_CLASS_TEXT, ""));


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
                PaymentTransactionBehaviorValueSetter valueSetter = PaymentTransactionBehaviorValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(transactionBehavior, nameValueStringEntity.getValue());

            }
        }
    }
}
