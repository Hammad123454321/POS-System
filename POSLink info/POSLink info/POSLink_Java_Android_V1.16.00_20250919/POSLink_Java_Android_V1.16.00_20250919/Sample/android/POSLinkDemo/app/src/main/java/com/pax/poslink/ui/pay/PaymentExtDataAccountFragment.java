package com.pax.poslink.ui.pay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataAccountFragment extends BaseExtDataTabFragment {
    private final static String tags[] = {
            "Account", "ExpDate", "CVV", "EBTFoodStampVoucher",
            "VoucherNum", "Force","FirstName","LastName","CountryCode","StateCode","CityName","EmailAddress", "EBTType"};

    private Map<String, EditText> m_extData = new HashMap<String, EditText>();
    private String extData;

    public static PaymentExtDataAccountFragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataAccountFragment fragment = new PaymentExtDataAccountFragment();
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
        View view = inflater.inflate(R.layout.payment_ext_data_account, container, false);
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
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_account));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_expDate));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_CVV));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_EBTFoodStampVoucher));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_voucherNum));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_force));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_firstName));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_lastName));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_countryCode));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_stateCode));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_cityName));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_email));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_ebt));
    }

    private String packExtData()
    {
        String extData = "";
        Set<Map.Entry<String, EditText>> allSet=m_extData.entrySet();
        Iterator<Map.Entry<String, EditText>> iter=allSet.iterator();
        while(iter.hasNext()){
            Map.Entry<String, EditText> item=iter.next();
            if(item.getValue().getText().toString().length() > 0)
                extData += "<" + item.getKey() + ">" + StringUtil.getescapeCharacter(item.getValue().getText().toString()) + "</" + item.getKey() + ">";
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

