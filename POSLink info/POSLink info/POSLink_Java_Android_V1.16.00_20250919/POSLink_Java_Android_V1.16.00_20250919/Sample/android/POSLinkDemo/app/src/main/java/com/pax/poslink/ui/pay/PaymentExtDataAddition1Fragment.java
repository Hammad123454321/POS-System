package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataAddition1Fragment extends BaseExtDataTabFragment implements View.OnClickListener {
    private final static String tags[] = {
            "TableNum", "GuestNum", "SignatureCapture", "TicketNum", "HRefNum",
            "TipRequest", "SignUploadFlag", "ReportStatus", "Token", "TokenRequest",
            "CardType", "CardTypeBitmap", "PassthruData", "Odometer", "VehicleNo",
            "JobNo", "DriverID", "EmployeeNo", "LicenseNo", "JobID",
            "DepartmentNo", "CustomerData", "UserID", "VehicleID", "ReturnReason", "FleetPromptCode"
    };

    private Map<String, EditText> m_extData = new HashMap<String, EditText>();
    private String extData;

    public static PaymentExtDataAddition1Fragment newInstance(String extData) {

        Bundle args = new Bundle();

        PaymentExtDataAddition1Fragment fragment = new PaymentExtDataAddition1Fragment();
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
        View view = inflater.inflate(R.layout.payment_ext_data_addition1, container, false);
        Button mPassthruData_btn = (Button)view.findViewById(R.id.payment_ext_passthruData_btn);
        mPassthruData_btn.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payment_ext_passthruData_btn:
                Intent intent = new Intent(getActivity(), PaymentExtDataAddPassthruActitity.class);
                Bundle bundle = new Bundle();
                bundle.putString("payment_ext_data_passthru", m_extData.get("PassthruData").getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_EXTDATA_ADD_PASSTHRU_RESULT);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.PAYMENT_EXTDATA_ADD_PASSTHRU_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        m_extData.get("PassthruData").setText(data.getStringExtra("payment_ext_data_passthru"));
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

    private void initJob(View view)
    {
        int count = 0;
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_tableNum));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_guestNum));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_sigCapture));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_ticketNum));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_HRefNum));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_tipRequest));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_signUploadFlag));
        m_extData.put(tags[count++], (EditText)view.findViewById(R.id.payment_ext_reportStatus));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_token));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_tokenRequest));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_cardType));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_cardTypeBitmap));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_passthruData));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_odometer));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_vehicleNo));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_jobNo));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_driverId));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_employeeNo));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_licenseNo));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_jobId));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_departmentNo));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_customerData));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_userId));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_vehicleId));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_returnReason));
        m_extData.put(tags[count++], (EditText) view.findViewById(R.id.payment_ext_FleetPromptCode));
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

