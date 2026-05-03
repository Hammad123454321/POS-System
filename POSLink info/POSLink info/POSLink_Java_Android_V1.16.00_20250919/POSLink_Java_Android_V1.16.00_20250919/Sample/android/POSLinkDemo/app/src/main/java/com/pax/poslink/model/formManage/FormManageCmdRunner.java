package com.pax.poslink.model.formManage;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.pax.poslink.CommSetting;
import com.pax.poslink.R;
import com.pax.poslink.aidl.BasePOSLinkCallback;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.customFormManage.GetFormList;
import com.pax.poslink.customFormManage.GetFormListRequest;
import com.pax.poslink.customFormManage.GetFormListResponse;
import com.pax.poslink.customFormManage.GetVarList;
import com.pax.poslink.customFormManage.GetVarListRequest;
import com.pax.poslink.customFormManage.GetVarListResponse;
import com.pax.poslink.customFormManage.RunForm;
import com.pax.poslink.customFormManage.RunFormRequest;
import com.pax.poslink.customFormManage.RunFormResponse;
import com.pax.poslink.customFormManage.SetVarList;
import com.pax.poslink.customFormManage.SetVarListRequest;
import com.pax.poslink.customFormManage.SetVarListResponse;
import com.pax.poslink.dal.print.ProcessingDialog;
import com.pax.poslink.formManage.DoSignature;
import com.pax.poslink.formManage.DoSignatureRequest;
import com.pax.poslink.formManage.DoSignatureResponse;
import com.pax.poslink.formManage.InputText;
import com.pax.poslink.formManage.InputTextRequest;
import com.pax.poslink.formManage.InputTextResponse;
import com.pax.poslink.formManage.ShowDialog;
import com.pax.poslink.formManage.ShowDialogForm;
import com.pax.poslink.formManage.ShowDialogFormRequest;
import com.pax.poslink.formManage.ShowDialogFormResponse;
import com.pax.poslink.formManage.ShowDialogRequest;
import com.pax.poslink.formManage.ShowDialogResponse;
import com.pax.poslink.formManage.ShowItem;
import com.pax.poslink.formManage.ShowItemRequest;
import com.pax.poslink.formManage.ShowItemResponse;
import com.pax.poslink.formManage.ShowMessage;
import com.pax.poslink.formManage.ShowMessageRequest;
import com.pax.poslink.formManage.ShowMessageResponse;
import com.pax.poslink.formManage.ShowTextBox;
import com.pax.poslink.formManage.ShowTextBoxRequest;
import com.pax.poslink.formManage.ShowTextBoxResponse;
import com.pax.poslink.fullIntegration.CmdRunner;
import com.pax.poslink.util.thread.AppThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class FormManageCmdRunner<Tin extends BaseRequest, Tout extends BaseResponse> extends CmdRunner<Tin, Tout> {


    // Add using dialog so that it can be removed when finish
    private static List<Dialog> usingDialogs = new ArrayList<>();

    public static class GetFormListRunner extends FormManageCmdRunner<GetFormListRequest, GetFormListResponse> {

        @Override
        public void run(final Context context, final GetFormListRequest request, final CommSetting commSetting, final BasePOSLinkCallback<GetFormListResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<GetFormListResponse>() {
                @Override
                public GetFormListResponse call() {
                    return GetFormList.getFormList(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<GetFormListResponse>() {
                @Override
                public void onFinish(GetFormListResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class GetVarListRunner extends FormManageCmdRunner<GetVarListRequest, GetVarListResponse> {
        @Override
        public void run(final Context context, final GetVarListRequest request, final CommSetting commSetting, final BasePOSLinkCallback<GetVarListResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<GetVarListResponse>() {
                @Override
                public GetVarListResponse call() {
                    return GetVarList.getVarList(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<GetVarListResponse>() {
                @Override
                public void onFinish(GetVarListResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class SetVarListRunner extends FormManageCmdRunner<SetVarListRequest, SetVarListResponse> {
        @Override
        public void run(final Context context, final SetVarListRequest request, final CommSetting commSetting, final BasePOSLinkCallback<SetVarListResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<SetVarListResponse>() {
                @Override
                public SetVarListResponse call() {
                    return SetVarList.setVarList(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<SetVarListResponse>() {
                @Override
                public void onFinish(SetVarListResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class RunFormRunner extends FormManageCmdRunner<RunFormRequest, RunFormResponse> {
        @Override
        public void run(final Context context, final RunFormRequest request, final CommSetting commSetting, final BasePOSLinkCallback<RunFormResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<RunFormResponse>() {
                @Override
                public RunFormResponse call() {
                    return RunForm.runForm(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<RunFormResponse>() {
                @Override
                public void onFinish(RunFormResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class ShowMessageRunner extends FormManageCmdRunner<ShowMessageRequest, ShowMessageResponse> {
        @Override
        public void run(final Context context, final ShowMessageRequest request, final CommSetting commSetting, final BasePOSLinkCallback<ShowMessageResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<ShowMessageResponse>() {
                @Override
                public ShowMessageResponse call() {
                    return ShowMessage.showMessage(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<ShowMessageResponse>() {
                @Override
                public void onFinish(ShowMessageResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class ShowDialogRunner extends FormManageCmdRunner<ShowDialogRequest, ShowDialogResponse> {
        @Override
        public void run(final Context context, final ShowDialogRequest request, final CommSetting commSetting, final BasePOSLinkCallback<ShowDialogResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<ShowDialogResponse>() {
                @Override
                public ShowDialogResponse call() {
                    return ShowDialog.showDialog(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<ShowDialogResponse>() {
                @Override
                public void onFinish(ShowDialogResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());

        }
    }

    public static class ShowItemRunner extends FormManageCmdRunner<ShowItemRequest, ShowItemResponse> {
        @Override
        public void run(final Context context, final ShowItemRequest request, final CommSetting commSetting, final BasePOSLinkCallback<ShowItemResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<ShowItemResponse>() {
                @Override
                public ShowItemResponse call() {
                    return ShowItem.showItem(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<ShowItemResponse>() {
                @Override
                public void onFinish(ShowItemResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());

        }
    }

    public static class ShowTextBoxRunner extends FormManageCmdRunner<ShowTextBoxRequest, ShowTextBoxResponse> {
        @Override
        public void run(final Context context, final ShowTextBoxRequest request, final CommSetting commSetting, final BasePOSLinkCallback<ShowTextBoxResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<ShowTextBoxResponse>() {
                @Override
                public ShowTextBoxResponse call() {
                    return ShowTextBox.showTextBox(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<ShowTextBoxResponse>() {
                @Override
                public void onFinish(ShowTextBoxResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());

        }
    }

    public static class ShowDialogFormRunner extends FormManageCmdRunner<ShowDialogFormRequest, ShowDialogFormResponse> {
        @Override
        public void run(final Context context, final ShowDialogFormRequest request, final CommSetting commSetting, final BasePOSLinkCallback<ShowDialogFormResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<ShowDialogFormResponse>() {
                @Override
                public ShowDialogFormResponse call() {
                    return ShowDialogForm.showDialogForm(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<ShowDialogFormResponse>() {
                @Override
                public void onFinish(ShowDialogFormResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());

        }
    }

    public static class DoSignatureRunner extends FormManageCmdRunner<DoSignatureRequest, DoSignatureResponse> {
        @Override
        public void run(final Context context, final DoSignatureRequest request, final CommSetting commSetting, final BasePOSLinkCallback<DoSignatureResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<DoSignatureResponse>() {
                @Override
                public DoSignatureResponse call() {
                    return DoSignature.doSignature(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<DoSignatureResponse>() {
                @Override
                public void onFinish(DoSignatureResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());

        }
    }

    public static class InputTextRunner extends FormManageCmdRunner<InputTextRequest, InputTextResponse> {
        @Override
        public void run(final Context context, final InputTextRequest request, final CommSetting commSetting, final BasePOSLinkCallback<InputTextResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start(context.getString(R.string.processing), false);
            AppThreadPool.getInstance().postTask(new Callable<InputTextResponse>() {
                @Override
                public InputTextResponse call() {
                    return InputText.inputText(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<InputTextResponse>() {
                @Override
                public void onFinish(InputTextResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());

        }
    }
}
