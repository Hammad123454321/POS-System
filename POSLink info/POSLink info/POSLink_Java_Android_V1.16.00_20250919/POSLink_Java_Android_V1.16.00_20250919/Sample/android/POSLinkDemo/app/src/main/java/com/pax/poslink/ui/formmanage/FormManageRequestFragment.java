package com.pax.poslink.ui.formmanage;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.pax.poslink.main.MainConst;
import com.pax.poslink.ui.DirectoryLogActivity;
import com.pax.poslink.MainApplication;
import com.pax.poslink.R;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.customFormManage.GetFormListRequest;
import com.pax.poslink.customFormManage.GetVarListRequest;
import com.pax.poslink.customFormManage.RunFormRequest;
import com.pax.poslink.customFormManage.SetVarListRequest;
import com.pax.poslink.formManage.DoSignatureRequest;
import com.pax.poslink.model.formManage.FormManageValueSetter;
import com.pax.poslink.formManage.InputTextRequest;
import com.pax.poslink.formManage.ShowDialogFormRequest;
import com.pax.poslink.formManage.ShowDialogRequest;
import com.pax.poslink.formManage.ShowItemRequest;
import com.pax.poslink.formManage.ShowMessageRequest;
import com.pax.poslink.formManage.ShowTextBoxRequest;
import com.pax.poslink.model.manage.ManageItemValueSetter;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameStringWithUnitEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FormManageRequestFragment extends RequestFragment {

    private Spinner cmdEdit = null;
    private static final String CMD_GET_FORM_LIST = "GETFORMLIST";
    private static final String CMD_GET_VAR_LIST = "GETVARLIST";
    private static final String CMD_SET_VAR_LIST = "SETVARLIST";
    private static final String CMD_RUN_FORM = "RUNFORM";
    private static final String CMD_SHOW_MESSAGE = "SHOWMESSAGE";
    private static final String CMD_SHOW_DIALOG = "SHOWDIALOG";
    private static final String CMD_SHOW_ITEM = "SHOWITEM";
    private static final String CMD_SHOW_TEXTBOX = "SHOWTEXTBOX";
    private static final String CMD_SHOW_DIALOG_FORM = "SHOWDIALOGFORM";
    private static final String CMD_DOSIGNATURE = "DOSIGNATURE";
    private static final String CMD_INPUT_TEXT = "INPUTTEXT";

    private static final List<String> CMDS = new ArrayList<String>() {
        {
            addAll(Arrays.asList(
                    CMD_GET_FORM_LIST,
                    CMD_GET_VAR_LIST,
                    CMD_SET_VAR_LIST,
                    CMD_RUN_FORM,
                    CMD_SHOW_MESSAGE,
                    CMD_SHOW_DIALOG,
                    CMD_SHOW_ITEM,
                    CMD_SHOW_TEXTBOX,
                    CMD_SHOW_DIALOG_FORM,
                    CMD_DOSIGNATURE,
                    CMD_INPUT_TEXT
            ));

        }
    };

    private final Map<String, List<RenderEntity>> commandMapRenderList = new HashMap<String, List<RenderEntity>>() {
        {
            List<String> edcTypes = MainConst.EDC_TYPES;
            List<String> transTypes = MainConst.TRANS_TYPE;
            put(CMD_GET_FORM_LIST, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(FormManageValueSetter.FORM_TYPE, "1", InputType.TYPE_CLASS_TEXT, "0:external or 1:internal")
            ));
            put(CMD_GET_VAR_LIST, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(FormManageValueSetter.VAR_NAME_LIST, "", InputType.TYPE_CLASS_TEXT, "varName1,varName2,...")
            ));
            put(CMD_SET_VAR_LIST, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(FormManageValueSetter.VAR_LIST, "{\"VARNAME1\":\"VARVALUE1\"}", InputType.TYPE_CLASS_TEXT, "json format:{\"VARNAME1\":\"VARVALUE1\",\"VARNAME2\":\"VARVALUE2\"}")
            ));
            put(CMD_RUN_FORM, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(FormManageValueSetter.FORM_NAME, "BPSDIALOG", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(FormManageValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, "0-9999"),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, "0", InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1")
            ));

            put(CMD_SHOW_MESSAGE, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DISPLAY_MESSAGE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DISPLAY_MESSAGE2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOPDOWN, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAX_LINE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOTAL_LINE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.IMAGE_NAME, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.IMAGE_DESCRIPTION, "", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_SHOW_DIALOG, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "title", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_4, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_SHOW_ITEM, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOPDOWN, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAX_LINE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOTAL_LINE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ITEMS_PASSTHRUDATA, "Fruit,,3,0,,4,,,", InputType.TYPE_CLASS_TEXT, "Fruit,,3,0,,4,,,")
            ));

            NameValueStringEntity.ClickCallback sigfileClick = new NameValueStringEntity.ClickCallback() {
                @Override
                public void onClick(View v, final NameValueStringEntity entity) {
                    setActivityResultReceiver(new ActivityResultReceiver() {
                        @Override
                        public void onReceive(String data) {
                            entity.setValue(data);
                            UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                        }
                    });
                    String dir = entity.getValue();
                    File f1 = new File(dir);
                    if (!f1.exists()) {
                        dir = Environment.getExternalStorageDirectory().toString();
                    } else {
                        if (f1.isFile())
                            dir = dir.substring(0, dir.lastIndexOf("/"));
                    }
                    Intent intent3 = new Intent(getActivity(), DirectoryLogActivity.class);
                    Bundle bundle3 = new Bundle();
                    bundle3.putString("FilePath", dir);
                    intent3.putExtras(bundle3);
                    startActivityForResult(intent3, Constant.MANAGE_SAVE_IMAGE_RESULT);
                }
            };
            put(CMD_SHOW_TEXTBOX, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TEXT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_COLOR1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_COLOR2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_COLOR3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_KEY1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_KEY2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_KEY3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENABLE_HARD_KEY, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.HARD_KEY_LIST, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.SIGNATURE_BOX, "", InputType.TYPE_CLASS_TEXT, "1 or 0"),
                    new NameValueBrowserEntity(ManageItemValueSetter.SAVE_SIG_PATH, "Browse", MainApplication.getInstance().getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), InputType.TYPE_CLASS_TEXT, "", sigfileClick),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, "0", InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1"),
                    new NameValueStringEntity(ManageItemValueSetter.BARCODE_TYPE, "7", InputType.TYPE_CLASS_NUMBER, "Empty or 7(QRCode)"),
                    new NameValueStringEntity(ManageItemValueSetter.BARCODE_DATA, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TEXT_TITLE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TEXT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TYPE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_LENGTH, "", InputType.TYPE_CLASS_TEXT, "")

            ));
            put(CMD_SHOW_DIALOG_FORM, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "title", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL1, "label1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL1_PROPERTY, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL2, "label2", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL2_PROPERTY, "2", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL3, "label3", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL3_PROPERTY, "3", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL4, "label4", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL4_PROPERTY, "4", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_TYPE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_DOSIGNATURE, Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, edcTypes.get(0), edcTypes, edcTypes),
                    new NameValueStringEntity(ManageItemValueSetter.UPLOAD, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.H_REF_NUM, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_INPUT_TEXT, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TYPE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DEFAULT_VALUE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, "")
            ));
        }
    };

    private final Map<String, Map<String, FormManageValueSetter>> valueSetterMap = new HashMap<String, Map<String, FormManageValueSetter>>() {
        {
            put(CMD_GET_FORM_LIST, FormManageValueSetter.GET_FORM_LIST_MAP);
            put(CMD_GET_VAR_LIST, FormManageValueSetter.GET_VAR_LIST_MAP);
            put(CMD_SET_VAR_LIST, FormManageValueSetter.SET_VAR_LIST_MAP);
            put(CMD_RUN_FORM, FormManageValueSetter.RUN_FORM_MAP);
            put(CMD_SHOW_MESSAGE, FormManageValueSetter.SHOW_MESSAGE_MAP);
            put(CMD_SHOW_DIALOG, FormManageValueSetter.SHOW_DIALOG_MAP);
            put(CMD_SHOW_ITEM, FormManageValueSetter.SHOW_ITEM_MAP);
            put(CMD_SHOW_TEXTBOX, FormManageValueSetter.SHOW_TEXTBOX_MAP);
            put(CMD_SHOW_DIALOG_FORM, FormManageValueSetter.SHOW_DIALOG_FORM_MAP);
            put(CMD_DOSIGNATURE, FormManageValueSetter.DOSIGNATURE_MAP);
            put(CMD_INPUT_TEXT, FormManageValueSetter.INPUT_TEXT_MAP);
        }
    };

    private final Map<String, BaseRequest> requestMap = new HashMap<String, BaseRequest>() {
        {
            put(CMD_GET_FORM_LIST, new GetFormListRequest());
            put(CMD_GET_VAR_LIST, new GetVarListRequest());
            put(CMD_SET_VAR_LIST, new SetVarListRequest());
            put(CMD_RUN_FORM, new RunFormRequest());
            put(CMD_SHOW_MESSAGE, new ShowMessageRequest());
            put(CMD_SHOW_DIALOG, new ShowDialogRequest());
            put(CMD_SHOW_ITEM, new ShowItemRequest());
            put(CMD_SHOW_TEXTBOX, new ShowTextBoxRequest());
            put(CMD_SHOW_DIALOG_FORM, new ShowDialogFormRequest());
            put(CMD_DOSIGNATURE, new DoSignatureRequest());
            put(CMD_INPUT_TEXT, new InputTextRequest());
        }
    };

    public FormManageRequestFragment() {
        // Required empty public constructor
    }

    public static FormManageRequestFragment newInstance() {
        return new FormManageRequestFragment();
    }

    public static FormManageRequestFragment newInstance(String processName) {
        FormManageRequestFragment fragment = new FormManageRequestFragment();
        fragment.setProcessBtn(processName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_manage_request;
    }

    @Override
    protected void initView(View view) {
        requestContainer = view.findViewById(R.id.manage_request_container);
        cmdEdit = view.findViewById(R.id.manage_request_transType);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, CMDS);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmdEdit.setAdapter(arrayAdapter);
        cmdEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String command = CMDS.get(position);
                showCorrespondingRequestView(command);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_SAVE_IMAGE_RESULT:
                if (resultCode == RESULT_OK) {
                    onActivityResultReceive(data.getStringExtra("FilePath"));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showCorrespondingRequestView(String command) {
        requestRenderEntityList.clear();
        requestContainer.removeAllViews();

        List<RenderEntity> commandRenderList = commandMapRenderList.get(command);
        if (commandRenderList != null) {
            requestRenderEntityList.addAll(commandRenderList);
        }
        for (Object object : requestRenderEntityList) {
            if (object instanceof RenderEntity) {
                RenderEntity renderEntity = (RenderEntity) object;
                String name = ((NameValueEntity) renderEntity).getName();
                CommonItemView itemView = renderEntity.createView(requestContainer);
                requestContainer.addView(itemView.getView());
                itemView.getView().setTag(itemView);
                itemView.render(renderEntity);
            }
        }
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public BaseRequest getRequest() {
        final String cmd = cmdEdit.getSelectedItem().toString();
        BaseRequest request = requestMap.get(cmd);
        setManageRequest(cmd, request);
        return request;
    }

    private void setManageRequest(String cmd, BaseRequest request) {
        for (Object object : requestRenderEntityList) {
            RenderEntity renderEntity = (RenderEntity) object;
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                FormManageValueSetter valueSetter = valueSetterMap.get(cmd).get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));
            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                FormManageValueSetter valueSetter = valueSetterMap.get(cmd).get(nameValueStringEntity.getName());
                valueSetter.onSet(request, nameValueStringEntity.getValue());
            }
        }
    }

    public String getRequestCmd() {
        return  cmdEdit.getSelectedItem().toString();
    }

}
