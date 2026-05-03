package com.pax.poslink.ui.formmanage;

import android.app.Dialog;
import android.view.View;

import com.pax.poslink.CommSetting;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.SettingINI;
import com.pax.poslink.aidl.BasePOSLinkCallback;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.customFormManage.GetFormListResponse;
import com.pax.poslink.customFormManage.GetVarListResponse;
import com.pax.poslink.customFormManage.RunFormResponse;
import com.pax.poslink.customFormManage.SetVarListResponse;
import com.pax.poslink.formManage.DoSignatureResponse;
import com.pax.poslink.formManage.InputTextResponse;
import com.pax.poslink.formManage.ShowDialogFormResponse;
import com.pax.poslink.formManage.ShowDialogResponse;
import com.pax.poslink.formManage.ShowItemResponse;
import com.pax.poslink.formManage.ShowMessageResponse;
import com.pax.poslink.formManage.ShowTextBoxResponse;
import com.pax.poslink.model.formManage.FormManageCmdRunner;
import com.pax.poslink.model.formManage.FormManageResponseUI;
import com.pax.poslink.ui.base.BaseRequestResponseFragment;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormManageFragment extends BaseRequestResponseFragment<BaseRequest, FormManageRequestFragment> {

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

    private final Map<String, BaseResponse> respMap = new HashMap<String, BaseResponse>() {
        {
            put(CMD_GET_FORM_LIST, new GetFormListResponse());
            put(CMD_GET_VAR_LIST, new GetVarListResponse());
            put(CMD_SET_VAR_LIST, new SetVarListResponse());
            put(CMD_RUN_FORM, new RunFormResponse());
            put(CMD_SHOW_MESSAGE, new ShowMessageResponse());
            put(CMD_SHOW_DIALOG, new ShowDialogResponse());
            put(CMD_SHOW_ITEM, new ShowItemResponse());
            put(CMD_SHOW_TEXTBOX, new ShowTextBoxResponse());
            put(CMD_SHOW_DIALOG_FORM, new ShowDialogFormResponse());
            put(CMD_DOSIGNATURE, new DoSignatureResponse());
            put(CMD_INPUT_TEXT, new InputTextResponse());
        }
    };
    private final Map<String, FormManageResponseUI> respUIMap = new HashMap<String, FormManageResponseUI>() {
        {
            put(CMD_GET_FORM_LIST, new FormManageResponseUI.GetFormListUI());
            put(CMD_GET_VAR_LIST, new FormManageResponseUI.GetVarListUI());
            put(CMD_SET_VAR_LIST, new FormManageResponseUI.SetVarListUI());
            put(CMD_RUN_FORM, new FormManageResponseUI.RunFormUI());
            put(CMD_SHOW_MESSAGE, new FormManageResponseUI.ShowMessageUI());
            put(CMD_SHOW_DIALOG, new FormManageResponseUI.ShowDialogUI());
            put(CMD_SHOW_ITEM, new FormManageResponseUI.ShowItemUI());
            put(CMD_SHOW_TEXTBOX, new FormManageResponseUI.ShowTextBoxUI());
            put(CMD_SHOW_DIALOG_FORM, new FormManageResponseUI.ShowDialogFormUI());
            put(CMD_DOSIGNATURE, new FormManageResponseUI.DoSignatureUI());
            put(CMD_INPUT_TEXT, new FormManageResponseUI.InputTextUI());
        }
    };

    private final Map<String, FormManageCmdRunner> runnerMap = new HashMap<String, FormManageCmdRunner>() {
        {
            put(CMD_GET_FORM_LIST, new FormManageCmdRunner.GetFormListRunner());
            put(CMD_GET_VAR_LIST, new FormManageCmdRunner.GetVarListRunner());
            put(CMD_SET_VAR_LIST, new FormManageCmdRunner.SetVarListRunner());
            put(CMD_RUN_FORM, new FormManageCmdRunner.RunFormRunner());
            put(CMD_SHOW_MESSAGE, new FormManageCmdRunner.ShowMessageRunner());
            put(CMD_SHOW_DIALOG, new FormManageCmdRunner.ShowDialogRunner());
            put(CMD_SHOW_ITEM, new FormManageCmdRunner.ShowItemRunner());
            put(CMD_SHOW_TEXTBOX, new FormManageCmdRunner.ShowTextBoxRunner());
            put(CMD_SHOW_DIALOG_FORM, new FormManageCmdRunner.ShowDialogFormRunner());
            put(CMD_DOSIGNATURE, new FormManageCmdRunner.DoSignatureRunner());
            put(CMD_INPUT_TEXT, new FormManageCmdRunner.InputTextRunner());
        }
    };

    public static FormManageFragment newInstance() {
        return new FormManageFragment();
    }

    @Override
    protected FormManageRequestFragment createRequestFragment() {
        return FormManageRequestFragment.newInstance();
    }

    @Override
    public void onPreRequest(BaseRequest request) {
        FormManageRequestFragment fragment = findRequestFragment();
        final String cmd = fragment.getRequestCmd();
        if (responseLayout.getVisibility() == View.VISIBLE) {
            initFormRequestResponse(cmd);
        }

        CommSetting commSetting = SettingINI.getCommSettingFromFile(getContext().getFilesDir().getPath() + "/" + SettingINI.FILENAME);
        runnerMap.get(cmd).run(getContext(), request, commSetting, new BasePOSLinkCallback<BaseResponse>() {
            @Override
            public void onFinish(BaseResponse response) {
                // dismiss all dialog in case some critical exception in the middle of the procedure.
                List<Dialog> usingDialogs = FormManageCmdRunner.getUsingDialogs();
                for (Dialog usingDialog : usingDialogs) {
                    usingDialog.dismiss();
                }
                usingDialogs.clear();

                ProcessTransResult processTransResult = response.getProcessTransResult();
                if (processTransResult.Code != ProcessTransResult.ProcessTransResultCode.OK) {
                    DialogUtils.showMsgDialog(getContext(), "Error:", processTransResult.Msg);
                    return;
                }
                respMap.put(cmd, response);
                showResponse(cmd, response);
            }
        });

    }

    private void initFormRequestResponse(String command) {
        BaseResponse baseResponse = respMap.get(command);
        showResponse(command, baseResponse);
    }

    private void showResponse(String cmd, BaseResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }
        responseRenderEntityList.clear();
        responseContainer.removeAllViews();

        List<RenderEntity> renderEntities = respUIMap.get(cmd).createRenderList(response);

        responseRenderEntityList.addAll(renderEntities);

        for (RenderEntity renderEntity : responseRenderEntityList) {
            CommonItemView itemView = renderEntity.createView(responseContainer);
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }
}
