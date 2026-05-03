package com.pax.poslink.ui.multicmd;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.MultipleCommandsRequest;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.JsonUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.view.View.DRAWING_CACHE_QUALITY_AUTO;


public class MultiCmdRequestFragment extends RequestFragment<MultipleCommandsRequest> {

    private MultiRequestAdapter listAdapter;

    private int currentClickedItem;

    public static final String ACTION_ADD = "action.add";
    public  static final String ACTION_UPDATE = "action.update";

    static final String[] slManageTrans =
            {"INIT", "GETVAR", "SETVAR", "SHOWDIALOG", "GETSIGNATURE", "SHOWMESSAGE",
                    "CLEARMESSAGE", "RESET", "UPDATEIMAGE", "DOSIGNATURE",
                    "DELETEIMAGE", "SHOWTHANKYOU", "REBOOT", "GETPINBLOCK",
                    "INPUTACCOUNT", "RESETMSR", "INPUTTEXT", "CHECKFILE",
                    "AUTHORIZECARD", "COMPLETEONLINEEMV", "REMOVECARD",
                    "GETEMVTLVDATA", "SETEMVTLVDATA", "INPUTACCOUNTWITHEMV",
                    "COMPLETECONTACTLESSEMV", "SETSAFPARAMETERS", "SHOWTEXTBOX", "REPRINT", "PRINTER", "SHOWITEM", "CARDINSERTDETECTION",
                    "TOKENADMINISTRATIVE", "SHOWDIALOGFORM", "CAMERASCAN", "VASSETMERCHANTPARAMETERS", "VASPUSHDATA", "MIFARECARD",
                    "GETSAFPARAMETERS", "UPLOADFTP", "SESSIONKEYINJECTION", "MACCALCULATION", "GETPEDINFORMATION", "UPDATERESOURCE", "INCREASEKSN","SHOWMESSAGECENTER",
                    "CUSTOMDATAENCRYPTION"};
    static final String[] slBatchTrans =
            {"BATCHCLOSE", "FORCEBATCHCLOSE", "BATCHCLEAR", "PURGEBATCH", "SAFUPLOAD",
                    "DELETESAFFILE", "DELETETRANSACTION"};
    static final String[] slReportTrans =
            {"LOCALTOTALREPORT", "LOCALDETAILREPORT", "LOCALFAILEDREPORT",
                    "HOSTREPORT", "HISTORYREPORT", "SAFSUMMARYREPORT", "HOSTDETAILREPORT"};
    static final String[] slTrans =
            {"UNKNOWN", "AUTH", "SALE", "RETURN", "VOID", "POSTAUTH",
                    "FORCEAUTH", "CAPTURE", "REPEATSALE", "CAPTUREALL", "ADJUST",
                    "INQUIRY", "ACTIVATE", "DEACTIVATE", "RELOAD", "VOID SALE",
                    "VOID RETURN", "VOID AUTH", "VOID POSTAUTH", "VOID FORCEAUTH", "VOID WITHDRAWAL",
                    "REVERSAL", "WITHDRAWAL", "ISSUE", "CASHOUT", "REPLACE",
                    "MERGE", "REPORTLOST", "REDEEM", "STATUS_CHECK", "SETUP",
                    "INIT", "VERIFY", "REACTIVATE", "FORCED ISSUE", "FORCED ADD",
                    "UNLOAD", "RENEW", "TOKENIZE", "GETCONVERTDETAIL", "CONVERT",
                    "INCREMENTALAUTH", "BALANCEWITHLOCK", "REDEMPTIONWITHUNLOCK", "REWARDS", "REENTER",
                    "TRANSACTION ADJUSTMENT", "TRANSFER"};
    static final String[] slTrend =
            {"ALL", "CREDIT", "DEBIT", "CHECK", "EBT_FOODSTAMP", "EBT_CASHBENEFIT",
                    "GIFT", "LOYALTY", "CASH", "EBT", "CHECKCARDTYPE", "DOQRPAYMENT", "PREINPUTACCOUNT"};

    private MultipleCommandsRequest MultipleCommandsRequest = new MultipleCommandsRequest();

    public MultiCmdRequestFragment() {
        // Required empty public constructor
    }

    public static MultiCmdRequestFragment newInstance() {
        return new MultiCmdRequestFragment();
    }

    public static MultiCmdRequestFragment newInstance(String processName) {
        MultiCmdRequestFragment fragment = new MultiCmdRequestFragment();
        fragment.setProcessBtn(processName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_multi_cmd_request;
    }

    @Override
    protected void initView(View view) {
        Button addBtn = view.findViewById(R.id.add_request_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MultiCmdActivity.class);
                intent.setAction(ACTION_ADD);
                startActivityForResult(intent, Constant.CONVERGE_ADD_REQUEST_RESULT);
            }
        });
        ListView listView = view.findViewById(R.id.request_list);
        listAdapter = new MultiRequestAdapter();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentClickedItem = position;
                BaseRequest baseRequest = MultipleCommandsRequest.requests.get(position);
                String requestName = baseRequest.getClass().getSimpleName();
                Intent intent = new Intent(getActivity(), MultiCmdActivity.class);
                intent.setAction(ACTION_UPDATE);
                intent.putExtra("requestName", requestName);
                String requestJson = JsonUtil.gsonToJson(baseRequest);
                intent.putExtra("request", requestJson);
                startActivityForResult(intent, Constant.CONVERGE_UPDATE_REQUEST_RESULT);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MultipleCommandsRequest.requests.remove(position);
                listAdapter.notifyDataSetChanged();
                return true;
            }
        });
        listView.setFastScrollEnabled(true);
        listView.setSmoothScrollbarEnabled(true);
        listView.setDrawingCacheEnabled(true);
        listView.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_AUTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constant.CONVERGE_ADD_REQUEST_RESULT:
                    String addJson = data.getStringExtra("add_request");
                    Type addType = new TypeToken<ArrayList<BaseRequest>>(){}.getType();
                    ArrayList<BaseRequest> list = JsonUtil.jsonToGeneralType(addJson, addType, BaseRequest.class);
                    if (list != null) {
                        if (MultipleCommandsRequest.requests.isEmpty()) {
                            MultipleCommandsRequest.requests = list;
                        } else {
                            MultipleCommandsRequest.requests.addAll(list);
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
                case Constant.CONVERGE_UPDATE_REQUEST_RESULT:
                    String updateJson = data.getStringExtra("update_request");
                    Type updateType = new TypeToken<BaseRequest>(){}.getType();
                    BaseRequest request = JsonUtil.jsonToGeneralType(updateJson, updateType, BaseRequest.class);
                    if (request != null) {
                        MultipleCommandsRequest.requests.set(currentClickedItem, request);
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public MultipleCommandsRequest getRequest() {
        return MultipleCommandsRequest;
    }

    private class MultiRequestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return MultipleCommandsRequest.requests != null ? MultipleCommandsRequest.requests.size() : 0;
        }

        @Override
        public BaseRequest getItem(int position) {
            return MultipleCommandsRequest.requests != null ? MultipleCommandsRequest.requests.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView textView;
            if (convertView == null) {
                TextView tv = new TextView(getContext());
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_size_tiny));
                tv.setMaxLines(2);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                float density = (int) UIUtil.getScreenDensity(getContext());
                int horizontalPadding = (int) (16 * density);
                int verticalPadding = (int) (8 * density);
                tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
                AbsListView.LayoutParams lps = new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(lps);
                convertView = tv;
            }
            textView = (TextView) convertView;
            final BaseRequest request = getItem(position);
            if (request != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // render in async runner, because json parse is too slow
                        textView.setText(formatRequest(position, request));
                    }
                });
            }
            return textView;
        }

        private String formatRequest(int position, BaseRequest request) {
            String result = "";
            if (request != null) {
                String requestName = request.getClass().getSimpleName();
                if (request instanceof PaymentRequest) {
                    PaymentRequest paymentRequest = (PaymentRequest) request;
                    String transType = slTrans[paymentRequest.TransType];
                    String tenderType = slTrend[paymentRequest.TenderType];
                    result = String.format("%s. Payment: %s --- %s", position + 1, tenderType, transType);
                } else if (request instanceof ManageRequest) {
                    ManageRequest manageRequest = (ManageRequest) request;
                    String tenderType = slManageTrans[manageRequest.TransType - 1];
                    result = String.format("%s. Manage: %s", position + 1, tenderType);
                } else if (request instanceof ReportRequest) {
                    ReportRequest reportRequest = (ReportRequest) request;
                    String transType = slReportTrans[reportRequest.TransType - 1];
                    result = String.format("%s. Report: %s", position + 1, transType);
                } else if (request instanceof BatchRequest) {
                    BatchRequest batchRequest = (BatchRequest) request;
                    String transType = slBatchTrans[batchRequest.TransType - 1];
                    result = String.format("%s. Batch: %s", position + 1, transType);
                }
                else {
                    result = (position + 1) + ". " +  requestName.substring(0, requestName.indexOf("Request"));
                }
            }
            return result;
        }
    }
}
