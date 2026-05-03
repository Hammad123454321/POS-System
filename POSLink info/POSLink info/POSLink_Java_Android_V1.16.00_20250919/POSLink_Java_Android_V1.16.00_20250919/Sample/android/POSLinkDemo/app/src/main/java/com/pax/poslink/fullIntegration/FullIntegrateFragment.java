package com.pax.poslink.fullIntegration;

import android.app.Dialog;
import android.text.TextUtils;
import android.view.View;

import com.pax.poslink.CommSetting;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.SettingINI;
import com.pax.poslink.aidl.BasePOSLinkCallback;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.fullIntegration.mock.MockResponse;
import com.pax.poslink.ui.base.BaseRequestResponseFragment;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Add new cmd:
 * Add to  {@link #respMap} {@link #respUIMap} {@link #runnerMap}
 * {@link FullIntegrationValueSetter}
 */
public class FullIntegrateFragment extends BaseRequestResponseFragment<BaseRequest, FullIntegrateRequestFragment>{

    private static final String CMD_INPUTACCOUNT_EMV = "INPUTACCOUNTWITHEMV";
    private static final String CMD_SET_EMV_TLV_DATA = "SETEMVTLVDATA";
    private static final String CMD_GET_EMV_TLV_DATA = "GETEMVTLVDATA";
    private static final String CMD_REMOVE_CARD = "REMOVECARD";
    private static final String CMD_COMPLETE_ONLINE_EMV = "COMPLETEONLINEEMV";
    private static final String CMD_MOCK_TRANSACTION = "Mock_Transaction";
    private static final String CMD_AUTHORIZECARD = "AUTHORIZECARD";
    private static final String CMD_GET_PIN_BLOCK = "GETPINBLOCK";
    private static final String CMD_UPDATE_RESOURCE = "UPDATERESOURCE";
    private static final String CMD_CHECK_FILE = "CHECKFILE";
    private static final String CMD_INIT = "INIT";
    private static final String CMD_GETVAR = "GETVAR";
    private static final String CMD_SETVAR = "SETVAR";
    private static final String CMD_VAS_SET_MERCHANT_PARAMETERS = "VASSETMERCHANTPARAMETERS";
    private static final String CMD_VAS_PUSH_DATA = "VASPUSHDATA";

    private final Map<String, BaseResponse> respMap = new HashMap<String, BaseResponse>() {
        {
            put(CMD_INPUTACCOUNT_EMV, new InputAccount.InputAccountResponse());
            put(CMD_SET_EMV_TLV_DATA, new EMVTLVData.SetTLVResponse());
            put(CMD_GET_EMV_TLV_DATA, new EMVTLVData.GetTLVResponse());
            put(CMD_REMOVE_CARD, new RemoveCard.RemoveCardResponse());
            put(CMD_COMPLETE_ONLINE_EMV, new CompleteOnlineEMV.CompleteOnlineEMVResponse());
            put(CMD_AUTHORIZECARD, new AuthorizeCard.AuthorizeResponse());
            put(CMD_MOCK_TRANSACTION, new MockResponse());
            put(CMD_GET_PIN_BLOCK, new GetPINBlock.GetPINBlockResponse());
            put(CMD_UPDATE_RESOURCE, new UpdateResource.UpdateResourceResponse());
            put(CMD_CHECK_FILE, new CheckFile.CheckFileResponse());
            put(CMD_INIT, new Init.InitResponse());
            put(CMD_GETVAR, new Variable.GetVariableResponse());
            put(CMD_SETVAR, new Variable.SetVariableResponse());
            put(CMD_VAS_SET_MERCHANT_PARAMETERS, new VasSetMerchantParameters.VasSetMerchantParametersResponse());
            put(CMD_VAS_PUSH_DATA, new VasPushData.VasPushDataResponse());
        }
    };
    private final Map<String, ResponseUI> respUIMap = new HashMap<String, ResponseUI>(){
        {
            put(CMD_INPUTACCOUNT_EMV, new ResponseUI.InputAccountUI());
            put(CMD_SET_EMV_TLV_DATA, new ResponseUI.SetEMVTLVUI());
            put(CMD_GET_EMV_TLV_DATA, new ResponseUI.GetEMVTLVUI());
            put(CMD_REMOVE_CARD, new ResponseUI.RemoveCardUI());
            put(CMD_COMPLETE_ONLINE_EMV, new ResponseUI.CompleteOnlineEMVUI());
            put(CMD_AUTHORIZECARD, new ResponseUI.AuthorizeCardUI());
            put(CMD_MOCK_TRANSACTION, new ResponseUI.MockTransUI());
            put(CMD_GET_PIN_BLOCK, new ResponseUI.GetPINBlockUI());
            put(CMD_UPDATE_RESOURCE, new ResponseUI.UpdateResourceUI());
            put(CMD_CHECK_FILE, new ResponseUI.CheckFileUI());
            put(CMD_INIT, new ResponseUI.InitUI());
            put(CMD_SETVAR, new ResponseUI.SetVarUI());
            put(CMD_GETVAR, new ResponseUI.GetVarUI());
            put(CMD_VAS_SET_MERCHANT_PARAMETERS, new ResponseUI.VasSetParametersUI());
            put(CMD_VAS_PUSH_DATA, new ResponseUI.VasPushDataUI());
        }
    };

    private final Map<String, CmdRunner> runnerMap = new HashMap<String, CmdRunner>() {
        {
            put(CMD_INPUTACCOUNT_EMV, new CmdRunner.InputAccountRunner());
            put(CMD_SET_EMV_TLV_DATA, new CmdRunner.SetEMVTLVDataRunner());
            put(CMD_GET_EMV_TLV_DATA, new CmdRunner.GetEMVTLVDataRunner());
            put(CMD_REMOVE_CARD, new CmdRunner.RemoveCardRunner());
            put(CMD_COMPLETE_ONLINE_EMV, new CmdRunner.CompleteOnlineEMVRunner());
            put(CMD_AUTHORIZECARD, new CmdRunner.AuthorizeCardRunner());
            put(CMD_MOCK_TRANSACTION, new CmdRunner.MockTransRunner());
            put(CMD_GET_PIN_BLOCK, new CmdRunner.GetPINBlockRunner());
            put(CMD_UPDATE_RESOURCE, new CmdRunner.UpdateResourceRunner());
            put(CMD_CHECK_FILE, new CmdRunner.CheckFileRunner());
            put(CMD_INIT, new CmdRunner.InitRunner());
            put(CMD_GETVAR, new CmdRunner.GetVarRunner());
            put(CMD_SETVAR, new CmdRunner.SetVarRunner());
            put(CMD_VAS_SET_MERCHANT_PARAMETERS, new CmdRunner.VasSetParametersRunner());
            put(CMD_VAS_PUSH_DATA, new CmdRunner.VasPushDataRunner());
        }
    };

    public static FullIntegrateFragment newInstance() {
        return new FullIntegrateFragment();
    }

    @Override
    protected FullIntegrateRequestFragment createRequestFragment() {
        return FullIntegrateRequestFragment.newInstance();
    }

    @Override
    public void onPreRequest(BaseRequest request) {
        FullIntegrateRequestFragment fragment = findRequestFragment();
        final String cmd = fragment.getRequestCmd();
        if (responseLayout.getVisibility() == View.VISIBLE) {
            initFullIntegrateRequestView(cmd);
        }
        CommSetting commSetting = SettingINI.getCommSettingFromFile(getContext().getFilesDir().getPath() + "/" + SettingINI.FILENAME);
        runnerMap.get(cmd).run(getContext(), request, commSetting, new BasePOSLinkCallback<BaseResponse>() {
            @Override
            public void onFinish(BaseResponse response) {
                // dismiss all dialog in case some critical exception in the middle of the procedure.
                List<Dialog> usingDialogs = CmdRunner.getUsingDialogs();
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

    private void initFullIntegrateRequestView(String command) {
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
            if (renderEntity instanceof NameValueStringUnEditableEntity && TextUtils.isEmpty(((NameValueStringUnEditableEntity) renderEntity).getValue())) {
                continue;
            }
            CommonItemView itemView = renderEntity.createView(responseContainer);
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }
}


