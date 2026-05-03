package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.model.payment.PaymentCommercialLineItemDetailValueSetter;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin.Z
 */
public class PaymentCommercialLineItemDetailActivity extends BaseActivity implements View.OnClickListener {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private ButtonEntity taxDetailDataEntity;
    private Button btn_add, btn_delete, btn_cancel, btn_save;
    private TextView data;

    private String lineItemDetailJson, lineItemDetailDisplay = "";
    private String taxmDetailJson, taxDetailDisplay = "";
    private StringBuilder stringBuilder;
    private Gson gson;

    private PaymentRequest.CommercialCard.LineItemDetail lineItemDetail;
    private List<PaymentRequest.CommercialCard.LineItemDetail> lineItemDetailList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_commercial_lineitemdetail);
        stringBuilder = new StringBuilder();

        Bundle bundle = getIntent().getExtras();
        gson = new Gson();
        String msg = bundle.getString("Payment_LineItemDetail_Display");
        lineItemDetailJson = bundle.getString("Payment_LineItemDetail");
        if (!TextUtils.isEmpty(lineItemDetailJson))
            lineItemDetailList = gson.fromJson(lineItemDetailJson, new TypeToken<List<PaymentRequest.CommercialCard.LineItemDetail>>(){}.getType());
        stringBuilder.append(TextUtils.isEmpty(msg) ? "" : msg);
        initView();
    }

    private void initView() {
        lineItemDetail = new PaymentRequest.CommercialCard.LineItemDetail();

        data = findViewById(R.id.tv_lineitemdetail_data);
        data.setText(stringBuilder.toString());

        container = findViewById(R.id.taxdetail_container);
        btn_save = findViewById(R.id.btn_lineitem_save);
        btn_add = findViewById(R.id.btn_lineitem_add);
        btn_cancel = findViewById(R.id.btn_lineitem_cancel);
        btn_delete = findViewById(R.id.btn_lineitem_delete);
        btn_save.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

        initList();
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        taxDetailDataEntity = new ButtonEntity(PaymentCommercialLineItemDetailValueSetter.TAX_DETAIL, taxDetailDisplay, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
//                        lineItemDetailJson = data;
                        taxmDetailJson = data;
                        UIBusiness.notifyDataSetChangeForContainer(renderEntityList, container);
                    }
                });

                Intent intent = new Intent(PaymentCommercialLineItemDetailActivity.this, PaymentCommercialTaxDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Payment_TaxDetail", taxmDetailJson);
                bundle.putString("Payment_TaxDetail_Display", taxDetailDisplay);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_TAXDETAIL_RESULT);
            }
        });

        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_SEQUENCE_NUMBER, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.PRODUCT_CODE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_COMMODITY_CODE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_DESCRIPTION, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_QUANTITY, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_MEASUREMENT_UNIT, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_UNIT_PRICE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_DISCOUNT_AMOUNT, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.ITEM_DISCOUNT_RATE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(taxDetailDataEntity);
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialLineItemDetailValueSetter.LINE_ITEM_TOTAL, "", InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_lineitem_add:
                lineItemDetail = getLineItemDetail();
                if (checkLineItemDetial(lineItemDetail))
                    return;
                lineItemDetailList.add(lineItemDetail);
                if (stringBuilder.length() != 0)
                    stringBuilder.append("[1D]");
                stringBuilder.append(formatLineItemDetail());
                data.setText(stringBuilder.toString());
                break;
            case R.id.btn_lineitem_save:
                Gson gson = new Gson();
                String lineItemDetails = gson.toJson(lineItemDetailList);
                Intent intent = new Intent();
                intent.putExtra("Payment_LineItemDetail", lineItemDetails);
                intent.putExtra("Payment_LineItemDetail_Display", stringBuilder.toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.btn_lineitem_cancel:
                finish();
                break;
            case R.id.btn_lineitem_delete:
                if (stringBuilder.length() < 1)
                    return;
                lineItemDetailList.remove(lineItemDetailList.size() - 1);
                int index = stringBuilder.lastIndexOf("[1D]");
                if (index < 0) {
                    stringBuilder = new StringBuilder();
                } else {
                    stringBuilder.delete(index, stringBuilder.length());
                }
                data.setText(stringBuilder.toString());
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.PAYMENT_TAXDETAIL_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        lineItemDetailDisplay = data.getStringExtra("Payment_TaxDetail_Display");
                        taxDetailDataEntity.setValue(lineItemDetailDisplay);
                        onActivityResultReceive(data.getStringExtra("Payment_TaxDetail"));
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

    private PaymentRequest.CommercialCard.LineItemDetail getLineItemDetail() {
        PaymentRequest.CommercialCard.LineItemDetail lineItemDetail = new PaymentRequest.CommercialCard.LineItemDetail();
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentCommercialLineItemDetailValueSetter valueSetter = PaymentCommercialLineItemDetailValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (PaymentCommercialLineItemDetailValueSetter.TAX_DETAIL.equals(nameValueStringEntity.getName())) {
                    valueSetter.onSet(lineItemDetail, taxmDetailJson);
                } else {
                    valueSetter.onSet(lineItemDetail, nameValueStringEntity.getValue());
                }
            }
        }
        return lineItemDetail;
    }

    private String formatLineItemDetail() {
        String retBuf = "";
        String ch = "[1E]";
        retBuf += lineItemDetail.ItemSequenceNumber;
        retBuf += ch;
        retBuf += lineItemDetail.ProductCode;
        retBuf += ch;
        retBuf += lineItemDetail.ItemCommodityCode;
        retBuf += ch;
        retBuf += lineItemDetail.ItemDescription;
        retBuf += ch;
        retBuf += lineItemDetail.ItemQuantity;
        retBuf += ch;
        retBuf += lineItemDetail.ItemMeasurementUnit;
        retBuf += ch;
        retBuf += lineItemDetail.ItemUnitPrice;
        retBuf += ch;
        retBuf += lineItemDetail.ItemDiscountAmount;
        retBuf += ch;
        retBuf += lineItemDetail.ItemDiscountRate;
        retBuf += ch;
        retBuf += lineItemDetailDisplay;
        retBuf += ch;
        retBuf += lineItemDetail.LineItemTotal;
        return retBuf;
    }

    private boolean checkLineItemDetial(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail) {
        boolean isEmpty = checkIsEmpty(lineItemDetail.ItemSequenceNumber) && checkIsEmpty(lineItemDetail.ProductCode)
                && checkIsEmpty(lineItemDetail.ItemCommodityCode) && checkIsEmpty(lineItemDetail.ItemDescription)
                && checkIsEmpty(lineItemDetail.ItemQuantity) && checkIsEmpty(lineItemDetail.ItemMeasurementUnit)
                && checkIsEmpty(lineItemDetail.ItemUnitPrice) && checkIsEmpty(lineItemDetail.ItemDiscountAmount)
                && checkIsEmpty(lineItemDetail.ItemDiscountRate) && checkIsEmpty(lineItemDetailDisplay)
                && checkIsEmpty(lineItemDetail.LineItemTotal);

        return isEmpty;
    }

    private boolean checkIsEmpty(String value) {
        if (value == null || value.length() < 1) {
            return true;
        }
        return false;
    }
}
