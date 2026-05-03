package com.pax.poslink.ui.pay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.poslink.R;
import com.pax.poslink.util.Constant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by linhb on 2015-09-25.
 */
public class PaymentExtDataAddPassthruActitity extends Activity implements View.OnClickListener {

    private Context mContext = null;

    private String[] mStrArrayTypes = null;

    private Integer[][] widgetIDList = {
            {R.id.payment_ext_passthru_line1, R.id.payment_ext_passthru_edit1},
            {R.id.payment_ext_passthru_line2, R.id.payment_ext_passthru_edit2},
            {R.id.payment_ext_passthru_line3, R.id.payment_ext_passthru_edit3},
            {R.id.payment_ext_passthru_line4, R.id.payment_ext_passthru_edit4},
            {R.id.payment_ext_passthru_line5, R.id.payment_ext_passthru_edit5},
            {R.id.payment_ext_passthru_line6, R.id.payment_ext_passthru_edit6},
            {R.id.payment_ext_passthru_line7, R.id.payment_ext_passthru_edit7},
            {R.id.payment_ext_passthru_line8, R.id.payment_ext_passthru_edit8},
            {R.id.payment_ext_passthru_line9, R.id.payment_ext_passthru_edit9},
            {R.id.payment_ext_passthru_line10, R.id.payment_ext_passthru_edit10},
            {R.id.payment_ext_passthru_line11, R.id.payment_ext_passthru_edit11},
            {R.id.payment_ext_passthru_line12, R.id.payment_ext_passthru_edit12},
            {R.id.payment_ext_passthru_line13, R.id.payment_ext_passthru_edit13},
            {R.id.payment_ext_passthru_line14, R.id.payment_ext_passthru_edit14},
    };

    private final static String[][] WIDGETLIST = {
            {"FLEET", "Product Code", "Product Amount", "Unit Price", "Quantity", "Unit of Measure"},
            {"FSA", "FSA Type", "Amount"},
            {"eWIC", "UPC PLU Ind", "UPC PLU Data", "UPC Price", "UPC Qty"},
            {"LEVEL3", "L3ItemSeqNum", "L3ItemCode", "L3ItemDesc", "L3Qty", "L3UnitOfMeasure", "L3UnitCost",
            "L3ItemTotal", "L3DiscountAmt", "L3TaxAmount", "L3TaxRate", "L3CmdtyCode", "L3DiscntRate","L3QtyExptIndtor","L3DiscntExptIndtor"},
            {"LODGING", "ROOM NO.", "CHECKIN DATE", "CHECKOUT DATE", "Days", "Room Rate", "GuestName", "GuestNum", "NoShowFlag", "SpecialProgramIndicator"
                    },
    };

    private Map<Integer[], EditText> mDynWidget= new HashMap<Integer[], EditText>();
    private Map<String, EditText> mEditWidget = new HashMap<String, EditText>();
    private Map<String, String> mData = new HashMap<String, String>();

    private Spinner mPassthruType = null;

    private TextView mPassthruData = null;

    private String mRequestCommand = "";
    private BroadcastReceiver mCommandDishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Constant.BROADCAST_PASSTHRU)) {
                mRequestCommand = intent.getStringExtra(Constant.COMMAND_NAME_PASSTHRU);
                for (String i : mStrArrayTypes) {
                    if (i.equals(mRequestCommand)) {
                        initVisibleView();
                        optionsView(mRequestCommand);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_ext_data_add_passthru);
        mContext = this;

        Bundle bundle=getIntent().getExtras();
        String passthruData=bundle.getString("payment_ext_data_passthru");
        if(passthruData == null)
            passthruData = "";
        unpackData(passthruData);

        mStrArrayTypes = getResources().getStringArray(R.array.payment_ext_passthru_type_prompt);
        initJob();

        mPassthruData = (TextView)findViewById(R.id.payment_ext_passthru_data);
        mPassthruData.setText(passthruData);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_PASSTHRU);
        registerReceiver(mCommandDishBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mCommandDishBroadcastReceiver);
    }

    private void initJob() {
        Button backBtn = (Button)findViewById(R.id.payment_ext_passthru_cancel);
        backBtn.setOnClickListener(this);
        Button saveBtn = (Button)findViewById(R.id.payment_ext_passthru_save);
        saveBtn.setOnClickListener(this);
        Button addBtn = (Button)findViewById(R.id.payment_ext_passthru_add);
        addBtn.setOnClickListener(this);

        for (Integer[] i: widgetIDList) {
            mDynWidget.put(i, (EditText)findViewById(i[1]));
        }

        mPassthruType = (Spinner)findViewById(R.id.payment_ext_passthru_type);
        mPassthruType.setSelection(0);
        mPassthruType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Intent i = new Intent(Constant.BROADCAST_PASSTHRU);
                i.putExtra(Constant.COMMAND_NAME_PASSTHRU, mStrArrayTypes[position]);
                mContext.sendBroadcast(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //do nothing
            }
        });

        for(String[] i : WIDGETLIST) {
            int count = 0;
            for(int j = 1; j < i.length; ++j, count++){
                mEditWidget.put(i[j], (EditText) findViewById(widgetIDList[count][1]));
            }
        }

        initVisibleView();
        optionsView("FLEET");
    }

    private void initVisibleView(){
        for (Integer[] i: widgetIDList) {
            for(Integer j : i)
                findViewById(j).setVisibility(View.GONE);
        }
    }

    private void optionsView(String mRequestCommand){
        for (String[] i : WIDGETLIST) {
            if(i[0].equals(mRequestCommand) && i.length >= 1)
            {
                Map<Integer[], String> prompt = new HashMap<Integer[], String>();
                for (int j = 1; j < i.length; ++j) {
                    prompt.put(widgetIDList[j-1], i[j]);
                }
                setRequestView(prompt);
                break;
            }
        }
    }

    private void setRequestView(Map<Integer[], String> prompt)
    {
        TextView text;
        EditText edit;

        Set<Map.Entry<Integer[], String>> allSet=prompt.entrySet();
        Iterator<Map.Entry<Integer[], String>> iter=allSet.iterator();
        while(iter.hasNext()){
            Map.Entry<Integer[], String> item=iter.next();
            if(item.getValue().length() > 0)
            {
                text = (TextView)findViewById(item.getKey()[0]);
                text.setVisibility(View.VISIBLE);
                text.setText(item.getValue());

                edit = mDynWidget.get(item.getKey());
                edit.setVisibility(View.VISIBLE);
                edit.setText("");
            }
        }
    }

    private void addData(){
        String data = "";
        int type = mPassthruType.getSelectedItemPosition();
        if(mEditWidget.get(WIDGETLIST[type][1]).getText().toString().length() == 0){
            Toast.makeText(this, WIDGETLIST[type][1] + " should not be empty!!", Toast.LENGTH_LONG).show();
            return;
        }
        for(int i = 1; i < WIDGETLIST[type].length; ++i){
            String tmp = mEditWidget.get(WIDGETLIST[type][i]).getText().toString();
            if(tmp.length() > 0)
                data += tmp;
            data += ",";
        }
        if(data.charAt(data.length() - 1) == ',')
            data = data.substring(0, data.length() - 1);

        String curr = mData.get(WIDGETLIST[type][0]);
        if(curr.length() > 0){
            mData.put(WIDGETLIST[type][0], curr + "|" + data);
        }
        else{
            mData.put(WIDGETLIST[type][0], WIDGETLIST[type][0] + ":" + data);
        }

        String all = "";
        Set<Map.Entry<String, String>> allSet=mData.entrySet();
        Iterator<Map.Entry<String, String>> iter=allSet.iterator();
        while(iter.hasNext()){
            Map.Entry<String, String> item=iter.next();
            if(item.getValue().length() > 0)
                all += item.getValue() + "@";
        }

        while(all.length() > 0 && all.charAt(all.length() - 1) == '@')
            all = all.substring(0, all.length() - 1);
        mPassthruData.setText(all);
    }

    private void unpackData(String data){
        String[] curr = data.split("@");

        for(String[] i : WIDGETLIST){
            mData.put(i[0], "");
            for(String j : curr){
                if(j.startsWith(i[0])){
                    mData.put(i[0], j);
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payment_ext_passthru_cancel:
                finish();
                break;
            case R.id.payment_ext_passthru_save:
                Intent intent = new Intent();
                intent.putExtra("payment_ext_data_passthru", mPassthruData.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.payment_ext_passthru_add:
                addData();
                break;
        }
    }
}

