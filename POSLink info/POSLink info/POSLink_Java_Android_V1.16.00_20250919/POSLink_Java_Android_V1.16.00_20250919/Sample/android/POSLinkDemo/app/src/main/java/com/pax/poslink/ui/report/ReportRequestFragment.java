package com.pax.poslink.ui.report;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.pax.poslink.R;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.main.MainConst;
import com.pax.poslink.model.MultiMerchantValueSetter;
import com.pax.poslink.model.manage.ManageItemValueSetter;
import com.pax.poslink.model.report.ReportItemValueSetter;
import com.pax.poslink.ui.MultiMerchantActivity;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.ui.multicmd.MultiCmdViewModel;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_MULTI_MERCHANT;
import static com.pax.poslink.util.Constant.PAYMENT_MULTI_MERCHANT_RESULT;

public class ReportRequestFragment extends RequestFragment<ReportRequest> {

    private Spinner mTransTypeRequestEdit;
    private String multiMerchantJson = "";
    private String spinnerIndex = "";
    private MultiCmdViewModel model;
    private ReportRequest reportRequest;

    private Map<String, List<RenderEntity>> commandMapRenderList = new HashMap<String, List<RenderEntity>>();

    public ReportRequestFragment() {
        // Required empty public constructor
    }

    public static ReportRequestFragment newInstance() {
        return new ReportRequestFragment();
    }

    public static ReportRequestFragment newInstance(String processBtnName) {
        ReportRequestFragment fragment = new ReportRequestFragment();
        fragment.setProcessBtn(processBtnName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_report_request;
    }

    @Override
    protected void initView(View view) {
        requestContainer = view.findViewById(R.id.report_request_container);

        final String[] mStrArrayTrans = getResources().getStringArray(R.array.report_trans);
        mTransTypeRequestEdit = view.findViewById(R.id.report_request_transType);
        List<String> list = Arrays.asList(mStrArrayTrans);
        model = new ViewModelProvider(requireActivity()).get(MultiCmdViewModel.class);
        reportRequest = model.getReportRequest();
        try {
            spinnerIndex = MainConst.slManageTrans[reportRequest.TransType - 1];
        } catch (Exception e) {
            //ignore
        }
        mTransTypeRequestEdit.setSelection(!list.contains(spinnerIndex) ? 0 : list.indexOf(spinnerIndex));
        mTransTypeRequestEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showCorrespondingRequestView(mStrArrayTrans[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showCorrespondingRequestView(String command) {
        requestRenderEntityList.clear();
        requestContainer.removeAllViews();

        List<String> reportTransTypes = MainConst.REPORT_TRANS_TYPE;
        List<String> edcTypes = MainConst.EDC_TYPES;
        List<String> transTypes = MainConst.TRANS_TYPE;
        List<String> cardTypes = MainConst.CARD_TYPES;

        int transTypeIndex = 0;
        try {
            transTypeIndex = MainConst.TRANS_TYPE.indexOf(MainConst.slTrans[reportRequest.PaymentType]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int cardTypeIndex = 0;
        try {
            cardTypeIndex = MainConst.CARD_TYPES.indexOf(MainConst.slCardType[reportRequest.CardType]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RenderEntity transTypeEntity = new NameValueSelectEntity(ReportItemValueSetter.PAYMENT_TYPE, "", transTypes, transTypes, transTypeIndex);
        RenderEntity cardTypeEntity = new NameValueSelectEntity(ReportItemValueSetter.CARD_TYPE, "", cardTypes, cardTypes, cardTypeIndex);
        RenderEntity edcTypeEntity = new NameValueSelectEntity(ReportItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, reportRequest.EDCType);
        RenderEntity lastTransactionEntity = new NameValueStringEntity(ReportItemValueSetter.LAST_TRANSACTION, reportRequest.LastTransaction, InputType.TYPE_CLASS_NUMBER, "");
        RenderEntity recordNumEntity = new NameValueStringEntity(ReportItemValueSetter.RECORD_NUM, reportRequest.RecordNum, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity refNumEntity = new NameValueStringEntity(ReportItemValueSetter.REF_NUM, reportRequest.RefNum, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity authCodeEntity = new NameValueStringEntity(ReportItemValueSetter.AUTH_CODE, reportRequest.AuthCode, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity ecrRefNumEntity = new NameValueStringEntity(ReportItemValueSetter.ECR_REF_NUM, reportRequest.ECRRefNum, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity extDataEntity = new NameValueStringEntity(ReportItemValueSetter.EXT_DATA, reportRequest.ExtData, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity ecrTransIdEntity = new NameValueStringEntity(ReportItemValueSetter.ECR_TRANS_ID, reportRequest.ECRTransID, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity hRefEntity = new NameValueStringEntity(ReportItemValueSetter.HREF, reportRequest.HRefNum, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity transactionResultTypeEntity = new NameValueStringEntity(ReportItemValueSetter.TRANSACTION_RESULT_TYPE, reportRequest.TransactionResultType, InputType.TYPE_CLASS_TEXT, "");

        commandMapRenderList.put("LOCALTOTALREPORT", Arrays.<RenderEntity>asList(
                edcTypeEntity,
                cardTypeEntity
        ));
        commandMapRenderList.put("LOCALDETAILREPORT", Arrays.<RenderEntity>asList(
                edcTypeEntity,
                cardTypeEntity,
                transTypeEntity,
                lastTransactionEntity,
                recordNumEntity,
                refNumEntity,
                authCodeEntity,
                ecrRefNumEntity,
                extDataEntity,
                new ButtonEntity(ReportItemValueSetter.MULTI_MERCHANT, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                multiMerchantJson = data;
                                entity.setValue(formatMultiMerchant(data));
                                entity.setRealData(multiMerchantJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), MultiMerchantActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT, multiMerchantJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_MULTI_MERCHANT_RESULT);
                    }
                }),
                transactionResultTypeEntity
        ));
        commandMapRenderList.put("LOCALFAILEDREPORT", Arrays.<RenderEntity>asList(
        ));
        commandMapRenderList.put("HOSTREPORT", Arrays.<RenderEntity>asList(
        ));
        commandMapRenderList.put("HISTORYREPORT", Arrays.<RenderEntity>asList(
        ));

        List<String> safIndicatorListName = MainConst.SAF_INDICATOR;
        List<String> safIndicatorListValue = MainConst.SAF_INDICATOR_VALUE;
        commandMapRenderList.put("SAFSUMMARYREPORT", Arrays.<RenderEntity>asList(
                new NameValueSelectEntity(ReportItemValueSetter.SAF_INDICATOR, "", safIndicatorListName, safIndicatorListValue, StringUtil.parseInt(reportRequest.SAFIndicator)),
                extDataEntity
        ));
        commandMapRenderList.put("HOSTDETAILREPORT", Arrays.<RenderEntity>asList(
                edcTypeEntity,
                cardTypeEntity,
                transTypeEntity,
                authCodeEntity,
                ecrTransIdEntity,
                hRefEntity,
                extDataEntity
        ));

        List<RenderEntity> commandRenderList = commandMapRenderList.get(command);
        if (commandRenderList != null) {
            requestRenderEntityList.addAll(commandRenderList);
        }

        for (RenderEntity renderEntity : requestRenderEntityList) {
            String name = ((NameValueEntity) renderEntity).getName();
            CommonItemView itemView = renderEntity.createView(requestContainer);
            requestContainer.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case PAYMENT_MULTI_MERCHANT_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT));
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

    @Override
    public ReportRequest getRequest() {
        ReportRequest request = new ReportRequest();
        setReportRequest(request);
        return request;
    }

    private void setReportRequest(ReportRequest request) {
        String TransType = mTransTypeRequestEdit.getSelectedItem().toString();
        request.TransType = request.ParseTransType(TransType);
        for (RenderEntity renderEntity : requestRenderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                ReportItemValueSetter valueSetter = ReportItemValueSetter.VALUE_SETTER_MAP.get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));

            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                ReportItemValueSetter valueSetter = ReportItemValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (!TextUtils.isEmpty(nameValueStringEntity.getRealData())) {
                    valueSetter.onSet(request, nameValueStringEntity.getRealData());
                } else {
                    valueSetter.onSet(request, nameValueStringEntity.getValue());
                }
            }
        }
    }

    private String toFormat(String name, String value) {
        String result = "";
        if (TextUtils.isEmpty(value))
            return result;
        result += name + ": " + value + "\n";
        return result;
    }

    private String formatMultiMerchant(String fleetCardJson) {
        String result = "";
        Gson gson = new Gson();
        MultiMerchant multiMerchant = gson.fromJson(fleetCardJson, MultiMerchant.class);
        result += toFormat(MultiMerchantValueSetter.MM_ID, multiMerchant.Id);
        result += toFormat(MultiMerchantValueSetter.MM_NAME, multiMerchant.Name);

        return result;
    }
}
