package com.pax.poslink.ui.pay;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
public class PaymentExtDataCashierFragment extends BaseExtDataTabFragment {
    private final static String tags[] = {
            "ShiftID",
    };

    private Map<String, EditText> m_extData = new HashMap<String, EditText>();
    private String extData;

    public static PaymentExtDataCashierFragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataCashierFragment fragment = new PaymentExtDataCashierFragment();
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
        View view = inflater.inflate(R.layout.payment_ext_data_cashier, container, false);
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
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_shiftId));
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
