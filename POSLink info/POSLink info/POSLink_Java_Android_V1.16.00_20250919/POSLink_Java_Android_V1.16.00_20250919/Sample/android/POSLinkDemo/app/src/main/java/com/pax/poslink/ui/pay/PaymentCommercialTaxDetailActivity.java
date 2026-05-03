package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.model.payment.PaymentCommercialTaxDetailValueSetter;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin.Z
 */
public class PaymentCommercialTaxDetailActivity extends BaseActivity implements View.OnClickListener {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private Spinner mPassthruType = null;
    private Button btn_add, btn_delete, btn_cancel, btn_save;
    private TextView data;
    private StringBuilder stringBuilder;
    private String taxDetailJson;
    private Gson gson;


    private List<PaymentRequest.CommercialCard.TaxDetail> taxDetails = new ArrayList<>();

    private static final String[] TAX_TYPE = {
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_commercail_taxdetail);
        Bundle bundle = getIntent().getExtras();
        stringBuilder = new StringBuilder();
        gson = new Gson();
        String taxDetailmsg = bundle.getString("Payment_TaxDetail_Display");
        taxDetailJson = bundle.getString("Payment_TaxDetail");
        if (!TextUtils.isEmpty(taxDetailJson))
            taxDetails = gson.fromJson(taxDetailJson, new TypeToken<List<PaymentRequest.CommercialCard.TaxDetail>>(){}.getType());
        stringBuilder.append(TextUtils.isEmpty(taxDetailmsg) ? "" : taxDetailmsg);
        initView();
    }

    private void initView() {
        container = findViewById(R.id.taxdetail_container);
        data = findViewById(R.id.tv_taxdetail_data);
        data.setText(stringBuilder.toString());
        mPassthruType = (Spinner) findViewById(R.id.payment_taxdetail_taxtype);
        mPassthruType.setSelection(0);

        btn_add = findViewById(R.id.btn_taxdetail_add);
        btn_add.setOnClickListener(this);
        btn_delete = findViewById(R.id.btn_taxdetail_delete);
        btn_delete.setOnClickListener(this);
        btn_cancel = findViewById(R.id.btn_taxdetail_cancel);
        btn_cancel.setOnClickListener(this);
        btn_save = findViewById(R.id.btn_taxdetail_save);
        btn_save.setOnClickListener(this);
        initList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_taxdetail_add:
                PaymentRequest.CommercialCard.TaxDetail taxDetail = getTaxDetail();
                taxDetails.add(taxDetail);
                if (stringBuilder.length() != 0)
                    stringBuilder.append("|");
                stringBuilder.append(formatTaxDetail(taxDetail));
                data.setText(stringBuilder.toString());
                break;
            case R.id.btn_taxdetail_delete:
                if (stringBuilder.length() < 1)
                    return;
                taxDetails.remove(taxDetails.size() - 1);
                int index = stringBuilder.lastIndexOf("|");
                if (index < 0) {
                    stringBuilder = new StringBuilder();
                } else {
                    stringBuilder.delete(index, stringBuilder.length());
                }
                data.setText(stringBuilder.toString());
                break;
            case R.id.btn_taxdetail_cancel:
                finish();
                break;
            case R.id.btn_taxdetail_save:
                Gson gson = new Gson();
                String taxDetailJson = gson.toJson(taxDetails);
                Intent intent = new Intent();
                intent.putExtra("Payment_TaxDetail", taxDetailJson);
                intent.putExtra("Payment_TaxDetail_Display", stringBuilder.toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
                default:
                    break;
        }
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity(PaymentCommercialTaxDetailValueSetter.TAX_AMOUNT, "", InputType.TYPE_CLASS_NUMBER, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialTaxDetailValueSetter.TAX_RATE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialTaxDetailValueSetter.MERCHANT_TAX_ID, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialTaxDetailValueSetter.CUSTOMER_TAX_ID, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialTaxDetailValueSetter.VAT_INVOICE_NUMBER, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentCommercialTaxDetailValueSetter.ALTERNATE_TAX_ID, "", InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    private PaymentRequest.CommercialCard.TaxDetail getTaxDetail() {
        PaymentRequest.CommercialCard.TaxDetail taxDetail = new PaymentRequest.CommercialCard.TaxDetail();
        int position = mPassthruType.getSelectedItemPosition();
        taxDetail.TaxType = TAX_TYPE[position];
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentCommercialTaxDetailValueSetter valueSetter = PaymentCommercialTaxDetailValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(taxDetail, nameValueStringEntity.getValue());
            }
        }
        return taxDetail;
    }

    private String formatTaxDetail(PaymentRequest.CommercialCard.TaxDetail taxDetail) {
        String result = "";
        String ch = ",";
        result += taxDetail.TaxType;
        result += ch;
        result += taxDetail.TaxAmount;
        result += ch;
        result += taxDetail.TaxRate;
        result += ch;
        result += taxDetail.MerChantTaxID;
        result += ch;
        result += taxDetail.CustomerTaxID;
        result += ch;
        result += taxDetail.VATInvoiceNumber;
        result += ch;
        result += taxDetail.AlternateTaxID;
        return result;
    }

}
