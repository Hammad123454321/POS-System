package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.model.payment.PaymentCommercialValueSetter;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin.Z
 */
public class PaymentCommercialCardActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private PaymentRequest.CommercialCard commercialCard;
    private String msg = "";
    private Gson gson = new Gson();
    private String taxDetailJson, lineItemDetailJson;
    private String taxDetailDisplay, lineItemDetailDisplay;
    private ButtonEntity taxDetailDataEntity, lineItemDetailDataEntity;

    private final Map<String, String> ENTITY_VALUE = new HashMap<String, String>() {
        {
            put(PaymentCommercialValueSetter.TAX_DETAIL, taxDetailJson);
            put(PaymentCommercialValueSetter.LINEITEMDETAIL, lineItemDetailJson);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        msg = bundle.getString("Payment_Commercial");
        taxDetailDisplay = bundle.getString("Payment_TaxDetail_Display");
        lineItemDetailDisplay = bundle.getString("Payment_LineItemDetail_Display");
        setContentView(R.layout.payment_commercial_activity);
        initView();
    }

    private void initView() {
        commercialCard = TextUtils.isEmpty(msg) ? new PaymentRequest.CommercialCard() : gson.fromJson(msg, PaymentRequest.CommercialCard.class);
        taxDetailJson = gson.toJson(commercialCard.TaxDetails);
        lineItemDetailJson = gson.toJson(commercialCard.LineItemDetails);
        container = findViewById(R.id.commercial_container);
        Button backBtn = (Button) findViewById(R.id.payment_extData_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button saveBtn = (Button) findViewById(R.id.payment_extData_save);
        saveBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "";
                getCommercial();
                Intent intent = new Intent();
                msg = gson.toJson(commercialCard);
                intent.putExtra("Payment_Commercial", msg);
                intent.putExtra("Payment_TaxDetail_Display", taxDetailDisplay);
                intent.putExtra("Payment_LineItemDetail_Display", lineItemDetailDisplay);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button clearBtn = findViewById(R.id.payment_extData_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taxDetailDisplay = "";
                lineItemDetailDisplay = "";
                commercialCard = new PaymentRequest.CommercialCard();
                initList();
            }
        });
        initList();
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        taxDetailDataEntity = new ButtonEntity(PaymentCommercialValueSetter.TAX_DETAIL, taxDetailDisplay, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        taxDetailJson = data;
                        ENTITY_VALUE.put(PaymentCommercialValueSetter.TAX_DETAIL, taxDetailJson);
                        UIBusiness.notifyDataSetChangeForContainer(renderEntityList, container);
                    }
                });

                Intent intent = new Intent(PaymentCommercialCardActivity.this, PaymentCommercialTaxDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Payment_TaxDetail", taxDetailJson);
                bundle.putString("Payment_TaxDetail_Display", taxDetailDisplay);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_TAXDETAIL_RESULT);
            }
        });

        lineItemDetailDataEntity = new ButtonEntity(PaymentCommercialValueSetter.LINEITEMDETAIL, lineItemDetailDisplay, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        lineItemDetailJson = data;
                        ENTITY_VALUE.put(PaymentCommercialValueSetter.LINEITEMDETAIL, lineItemDetailJson);
//                        entity.setValue(lineItemDetailJson);
                        UIBusiness.notifyDataSetChangeForContainer(renderEntityList, container);
                    }
                });

                Intent intent = new Intent(PaymentCommercialCardActivity.this, PaymentCommercialLineItemDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Payment_LineItemDetail", lineItemDetailJson);
                bundle.putString("Payment_LineItemDetail_Display", lineItemDetailDisplay);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_LINEIETMDETAIL_RESULT);
            }
        });

        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.PO_NUMBER, commercialCard.PONumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.CUSTOMER_CODE, commercialCard.CustomerCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.TAX_EXEMPT, commercialCard.TaxExempt, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.TAX_EXEMPT_ID, commercialCard.TaxExemptID, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.MERCHANT_TAX_ID, commercialCard.MerchantTaxID, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.DESTINATION_ZIP_CODE, commercialCard.DestinationZipCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.PRODUCT_DESCRIPTION, commercialCard.ProductDescription, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.SHIP_FROM_ZIP_CODE, commercialCard.ShipFromZipCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.DESTINATION_COUNTRY_CODE, commercialCard.DestinationCountryCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(taxDetailDataEntity);
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.SUMMARY_COMMODITY_CODE, commercialCard.SummaryCommodityCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.DISCOUNT_AMOUNT, commercialCard.DiscountAmount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.FREIGHT_AMOUNT, commercialCard.FreightAmount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.DUTY_AMOUNT, commercialCard.DutyAmount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.ORDER_DATE, commercialCard.OrderDate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(lineItemDetailDataEntity);
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.SHIPPING_COMPANY, commercialCard.ShippingCompany, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.SHIPPING_TRACKING_NUMBER, commercialCard.ShippingTrackingNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialValueSetter.ADDITIONAL_FEES, commercialCard.AdditionalFees, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.PAYMENT_TAXDETAIL_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        taxDetailDisplay= data.getStringExtra("Payment_TaxDetail_Display");
                        taxDetailDataEntity.setValue(taxDetailDisplay);
                        onActivityResultReceive(data.getStringExtra("Payment_TaxDetail"));
                        break;
                    default:
                        break;
                }
                break;
            case Constant.PAYMENT_LINEIETMDETAIL_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        lineItemDetailDisplay= data.getStringExtra("Payment_LineItemDetail_Display");
                        lineItemDetailDataEntity.setValue(lineItemDetailDisplay);
                        onActivityResultReceive(data.getStringExtra("Payment_LineItemDetail"));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getCommercial() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentCommercialValueSetter valueSetter = PaymentCommercialValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (ENTITY_VALUE.containsKey(nameValueStringEntity.getName())) {
                    valueSetter.onSet(commercialCard, ENTITY_VALUE.get(nameValueStringEntity.getName()));
                } else {
                    valueSetter.onSet(commercialCard, nameValueStringEntity.getValue());
                }
            }
        }
    }

}
