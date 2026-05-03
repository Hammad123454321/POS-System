package com.pax.poslink.ui.pay;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.UIUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataCommerceFragment extends BaseExtDataTabFragment {
    private final static String tags[] = {
            "CustomerCode", "TaxExempt", "TaxExemptID", "MerchantTaxID", "DestinationZipCode", "ProductDescription","LocalTax","NationalTax","CustomerTaxID","SummaryCommodityCode",
    "DiscountAmt","FreightAmt","DutyAmt","ShipFromZipCode","VATInvoiceRefNum","OrderDate","VATTaxAmt","VATTaxRate","AlternateTaxAmt","AlternateTaxID"};

    private Map<String, EditText> m_extData = new HashMap<String, EditText>();
    private String extData;

    public static PaymentExtDataCommerceFragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataCommerceFragment fragment = new PaymentExtDataCommerceFragment();
        args.putString(KEY, extData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(savedInstanceState == null){
            Bundle bundle = getArguments();
            extData = bundle.getString(KEY);
        }
        else {
            extData = savedInstanceState.getString("payment_ext_data" + this.getClass().getName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_ext_data_commerce, container, false);
        initJob(view);
        unpackExtData(extData);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String data = packExtData();
        outState.putString("payment_ext_data" + this.getClass().getName(), data);
    }

    private void initJob(View view)
    {
        int count = 0;
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_customerCode));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_taxExempt));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_taxExemptId));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_MerchantTaxID));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_DestinationZipCode));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_ProductDescription));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_LocalTax));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_NationalTax));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_CustomerTaxID));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_SummaryCommodityCode));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_DiscountAmt));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_FreightAmt));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_DutyAmt));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_ShipFromZipCode));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_VATInvoiceRefNum));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_OrderDate));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_VATTaxAmt));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_VATTaxRate));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_AlternateTaxAmt));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_AlternateTaxID));
    }

    private String packExtData()
    {
        String extData = "";
        Set<Map.Entry<String, EditText>> allSet=m_extData.entrySet();
        Iterator<Map.Entry<String, EditText>> iter=allSet.iterator();
        while(iter.hasNext()){
            Map.Entry<String, EditText> item=iter.next();
            if(item.getValue().getText().toString().length() > 0)
                extData += "<" + item.getKey() + ">" + item.getValue().getText().toString() + "</" + item.getKey() + ">";
        }
        return extData;
    }

    private void unpackExtData(String data)
    {
        int i;
        for(i = 0; i < tags.length; ++i) {
            EditText tmp = m_extData.get(tags[i]);
            tmp.setText(UIUtil.findXMl(data, tags[i]));
            m_extData.put(tags[i], tmp);
        }
    }

    @Override
    public String getExtData()
    {
        return packExtData();
    }
}
