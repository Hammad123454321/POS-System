package com.pax.poslink.ui.batch;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.R;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.model.MultiMerchantValueSetter;
import com.pax.poslink.model.batch.BatchItemValueSetter;
import com.pax.poslink.main.MainConst;
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

public class BatchRequestFragment extends RequestFragment<BatchRequest> {

    private Spinner mTransTypeRequestEdit = null;
    private String multiMerchantJson = "";
    private String spinnerIndex = "";
    private MultiCmdViewModel model;
    private BatchRequest batchRequest;

    private Map<String, List<RenderEntity>> commandMapRenderList = new HashMap<String, List<RenderEntity>>();

    public BatchRequestFragment() {
        // Required empty public constructor
    }

    public static BatchRequestFragment newInstance() {
        return new BatchRequestFragment();
    }

    public static BatchRequestFragment newInstance(String processBtnName) {
        BatchRequestFragment fragment = new BatchRequestFragment();
        fragment.setProcessBtn(processBtnName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_batch_request;
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public void initView(View view) {
        final String[] strArrayTrans = getResources().getStringArray(R.array.batch_trans);
        requestContainer = view.findViewById(R.id.batch_request_container);
        mTransTypeRequestEdit = view.findViewById(R.id.batch_request_transType);
        List<String> list = Arrays.asList(strArrayTrans);
        model = new ViewModelProvider(requireActivity()).get(MultiCmdViewModel.class);
        batchRequest = model.getBatchRequest();
        try {
            spinnerIndex = MainConst.slManageTrans[batchRequest.TransType - 1];
        } catch (Exception e) {
            //ignore
        }
        mTransTypeRequestEdit.setSelection(!list.contains(spinnerIndex) ? 0 : list.indexOf(spinnerIndex));
        mTransTypeRequestEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String command = strArrayTrans[position];
                showCorrespondingRequestView(command);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showCorrespondingRequestView(String command) {
        requestRenderEntityList.clear();
        requestContainer.removeAllViews();

        List<String> edcTypes = MainConst.EDC_TYPES;
        List<String> transTypes = MainConst.TRANS_TYPE;
        List<String> cardTypes = MainConst.CARD_TYPES;
        List<String> safIndicatorListName = MainConst.SAF_INDICATOR;
        List<String> safIndicatorListValue = MainConst.SAF_INDICATOR_VALUE;
        int transTypeIndex = 0;
        try {
            transTypeIndex = MainConst.TRANS_TYPE.indexOf(MainConst.slTrans[batchRequest.PaymentType]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int cardTypeIndex = 0;
        try {
            cardTypeIndex = MainConst.CARD_TYPES.indexOf(MainConst.slCardType[batchRequest.CardType]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RenderEntity transTypeEntity = new NameValueSelectEntity(BatchItemValueSetter.PAYMENT_TYPE, "", transTypes, transTypes, transTypeIndex);
        RenderEntity cardTypeEntity = new NameValueSelectEntity(BatchItemValueSetter.CARD_TYPE, "", cardTypes, cardTypes, cardTypeIndex);

        RenderEntity edcTypeEntity = new NameValueSelectEntity(BatchItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, batchRequest.EDCType);
        RenderEntity timeStampEntity = new NameValueStringEntity(BatchItemValueSetter.TIMESTAMP, batchRequest.Timestamp, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity extDataEntity = new NameValueStringEntity(BatchItemValueSetter.EXT_DATA, batchRequest.ExtData, InputType.TYPE_CLASS_TEXT, "");
        RenderEntity safIndicatorEntity = new NameValueSelectEntity(BatchItemValueSetter.SAF_INDICATOR, "", safIndicatorListName, safIndicatorListValue, StringUtil.parseInt(batchRequest.SAFIndicator));

        commandMapRenderList.put("BATCHCLOSE", Arrays.<RenderEntity>asList(
                timeStampEntity,
                extDataEntity,
                new ButtonEntity(BatchItemValueSetter.MULTI_MERCHANT, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
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
                })
        ));

        commandMapRenderList.put("FORCEBATCHCLOSE", Arrays.<RenderEntity>asList(
                timeStampEntity,
                extDataEntity
        ));

        commandMapRenderList.put("BATCHCLEAR", Arrays.<RenderEntity>asList(
                edcTypeEntity,
                timeStampEntity,
                extDataEntity
        ));

        commandMapRenderList.put("PURGEBATCH", Arrays.<RenderEntity>asList(
                edcTypeEntity,
                timeStampEntity,
                extDataEntity
        ));

        commandMapRenderList.put("SAFUPLOAD", Arrays.<RenderEntity>asList(
                safIndicatorEntity,
                extDataEntity
        ));

        commandMapRenderList.put("DELETESAFFILE", Arrays.<RenderEntity>asList(
                safIndicatorEntity,
                extDataEntity
        ));

        commandMapRenderList.put("DELETETRANSACTION", Arrays.<RenderEntity>asList(
                edcTypeEntity,
                transTypeEntity,
                cardTypeEntity,
                new NameValueStringEntity(BatchItemValueSetter.RECORD_NUM, batchRequest.RecordNum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(BatchItemValueSetter.REF_NUM, batchRequest.RefNum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(BatchItemValueSetter.AUTH_CODE, batchRequest.AuthCode, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(BatchItemValueSetter.ECR_REF_NUM, batchRequest.ECRRefNum, InputType.TYPE_CLASS_TEXT, ""),
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
    public BatchRequest getRequest() {
        BatchRequest batchrequest = new BatchRequest();
        setBatchRequest(batchrequest);
        return batchrequest;
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

    private void setBatchRequest(BatchRequest request) {
        String TransType = mTransTypeRequestEdit.getSelectedItem().toString();
        request.TransType = request.ParseTransType(TransType);
        for (RenderEntity renderEntity : requestRenderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                BatchItemValueSetter valueSetter = BatchItemValueSetter.VALUE_SETTER_MAP.get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));

            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                BatchItemValueSetter valueSetter = BatchItemValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
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
