package com.pax.poslink.fullIntegration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.pax.poslink.CommSetting;
import com.pax.poslink.MainApplication;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.R;
import com.pax.poslink.aidl.BasePOSLinkCallback;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.constant.CardEvent;
import com.pax.poslink.dal.print.ProcessingDialog;
import com.pax.poslink.fullIntegration.mock.MockRequest;
import com.pax.poslink.fullIntegration.mock.MockResponse;
import com.pax.poslink.http.OkHttpUtil;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.sharepref.SharedPrefKey;
import com.pax.poslink.util.sharepref.SharedPreferenceHelper;
import com.pax.poslink.util.thread.AppThreadPool;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.widget.EnterPINDialog;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import static com.pax.poslink.aidl.step.IOneStep.Const.STATE_INPUT_FORMAT_ERROR;

/**
 * Created by Leon.F on 2018/5/15.
 */
public abstract class CmdRunner<Tin extends BaseRequest, Tout extends BaseResponse> {

    public abstract void run(Context context, Tin request, CommSetting commSetting, BasePOSLinkCallback<Tout> posLinkCallback);

    // Add using dialog so that it can be removed when finish
    private static List<Dialog> usingDialogs = new ArrayList<>();


    public static class InputAccountRunner extends CmdRunner<InputAccount.InputAccountRequest, InputAccount.InputAccountResponse> {
        public void run(final Context context, final InputAccount.InputAccountRequest request, CommSetting commSetting, BasePOSLinkCallback<InputAccount.InputAccountResponse> posLinkCallback) {
            final Dialog processingDialog = ProcessProgressDialog.createDialog(context, context.getString(R.string.processing), true, false,
                    new ProcessProgressDialog.OnSetListener() {
                        @Override
                        public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
                            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel Process", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    InputAccount.getInstance().abort();
                                }
                            });
                        }
                    });
            processingDialog.show();
            usingDialogs.add(processingDialog);
            InputAccount.getInstance().setReportStatusListener(new InputAccount.ReportStatusListener() {
                @Override
                public void onReportStatus(int status) {
                    switch (status) {
                        case 9000000:
                            UIUtil.showToast(context, "Ready for card input.", Toast.LENGTH_SHORT);
                            break;
                        case 9000001:
                            UIUtil.showToast(context, "Ready for PIN entry.", Toast.LENGTH_SHORT);
                            break;
                        case 9000002:
                            UIUtil.showToast(context, "Ready for Signature.", Toast.LENGTH_SHORT);
                            break;
                        case 9000003:
                            UIUtil.showToast(context, "Ready for Online Processing.", Toast.LENGTH_SHORT);
                            break;
                        case 9000004:
                            UIUtil.showToast(context, "Ready for second card input.", Toast.LENGTH_SHORT);
                            break;
                        case 9000005:
                            UIUtil.showToast(context, "Ready for Signature retry by pressing CLEAR key. ", Toast.LENGTH_SHORT);
                            break;
                        case 9000006:
                            UIUtil.showToast(context, "Ready for PIN retry by inputting wrong offline PIN for EMV", Toast.LENGTH_SHORT);
                            break;
                        case 9000007:
                            UIUtil.showToast(context, "Please See Phone.", Toast.LENGTH_SHORT);
                            break;
                        case 9000008:
                            UIUtil.showToast(context, "Please Try Another Card.", Toast.LENGTH_SHORT);
                            break;
                        case 9000009:
                            UIUtil.showToast(context, "Please Present One Card Only", Toast.LENGTH_SHORT);
                            break;
                        case 9000010:
                            UIUtil.showToast(context, "Fallback Swipe.", Toast.LENGTH_SHORT);
                            break;
                        case 9020002:
                            UIUtil.showToast(context, "Ready for entering cashback.", Toast.LENGTH_SHORT);
                            break;
                        case 9040003:
                            UIUtil.showToast(context, "Remove Card.", Toast.LENGTH_SHORT);
                            break;
                        case 9040010:
                            UIUtil.showToast(context, "Re-Insert Card.", Toast.LENGTH_SHORT);
                            break;
                        default:
                            break;
                    }
                }
            });
            InputAccount.inputAccountWithEMV(context, request, commSetting, posLinkCallback, new InputAccount.InputAccountCallbackCompat() {
                @Override
                public void onInputAccountStart() {
                    processingDialog.dismiss();
                    byte mode = 0;
                    mode |= StringUtil.isTrue(request.getMagneticSwipeEntryFlag()) ? SearchCardMode.SWIPE : 0;
                    mode |= StringUtil.isTrue(request.getContactEMVEntryFlag()) ? SearchCardMode.INSERT : 0;
                    mode |= StringUtil.isTrue(request.getManualEntryFlag()) ? SearchCardMode.KEYIN : 0;
//                    mode |= StringUtil.isTrue(request.getLaserScannerFlag()) ? SearchCardMode.QR : 0;
                    mode |= StringUtil.isTrue(request.getContactlessEntryFlag()) ? SearchCardMode.TAP : 0;
                    long totalAmt = StringUtil.parseLong(request.getAmount()) + StringUtil.parseInt(request.getCashBackAmt());
                    int minAccountLength = request.getMinAccountLength();
                    minAccountLength = minAccountLength == 0 ? 10 : minAccountLength;
                    int maxAccountLength = request.getMaxAccountLength();
                    maxAccountLength = maxAccountLength == 0 ? 19 : maxAccountLength;
                    final InputAccountDialog inputAccountDialog = new InputAccountDialog(context, new InputAccountDialog.Params(mode, String.valueOf(totalAmt), "Please Input Account", minAccountLength, maxAccountLength), new InputAccountDialog.DialogCallback() {
                        private InputAccountDialog inputAccountDialog;

                        @Override
                        public void onCreate(final InputAccountDialog inputAccountDialog,
                                             final InputAccountDialog.LightChanger lightChanger, final InputAccountDialog.ModeChanger modeChanger) {
                            this.inputAccountDialog = inputAccountDialog;
                            InputAccount.getInstance().handleInputAccount(new FullIntegrationBase.CurrentStepCallback() {
                                @Override
                                public void onSuccess() {
                                    inputAccountDialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            }, new InputAccount.CardEventListener() {
                                /**
                                 * @param event {@link com.pax.poslink.constant.CardEvent}
                                 */
                                @Override
                                public void onEvent(String event) {
                                    switch (event) {
                                        case CardEvent.CLSS_LIGHT_STATUS_NOT_READY:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_OFF);
                                            break;
                                        case CardEvent.CLSS_LIGHT_STATUS_IDLE:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_BLINK);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_OFF);
                                            break;
                                        case CardEvent.CLSS_LIGHT_STATUS_READY_FOR_TXN:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_ON);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_OFF);
                                            break;
                                        case CardEvent.CLSS_LIGHT_STATUS_PROCESSING:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_ON);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_ON);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_OFF);
                                            break;
                                        case CardEvent.CLSS_LIGHT_STATUS_REMOVE_CARD:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_ON);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_BLINK);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_BLINK);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_OFF);
                                            break;
                                        case CardEvent.CLSS_LIGHT_STATUS_COMPLETE:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_BLINK);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_OFF);
                                            break;
                                        case CardEvent.CLSS_LIGHT_STATUS_ERROR:
                                            lightChanger.change(R.id.light1, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light2, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light3, InputAccountDialog.Light.STATUS_OFF);
                                            lightChanger.change(R.id.light4, InputAccountDialog.Light.STATUS_BLINK);
                                            break;
                                        case CardEvent.FALLBACK_SWIPE:
                                        case CardEvent.FALLBACK_SWIPE_CHIP_NOT_ACCEPTED:
                                            byte currentMode = SearchCardMode.SWIPE | SearchCardMode.KEYIN;
                                            modeChanger.change(currentMode);
                                            break;
                                        case CardEvent.RETRY_INSERT:
                                        case CardEvent.FALLBACK_INSERT:
                                            byte currentMode1 = SearchCardMode.INSERT | SearchCardMode.KEYIN;
                                            modeChanger.change(currentMode1);
                                            UIUtil.showToast(context, event, Toast.LENGTH_SHORT);
                                            break;
                                        case CardEvent.SECURITY_INPUT_FORMAT_ERROR:
                                            UIUtil.showToast(context, event, Toast.LENGTH_SHORT);
                                            break;
                                        case CardEvent.FALLBACK_MANUAL:
                                            byte currentMode2 = SearchCardMode.KEYIN;
                                            modeChanger.change(currentMode2);
                                            UIUtil.showToast(context, event, Toast.LENGTH_SHORT);
                                            break;
                                    }
                                }
                            });

                        }

                        @Override
                        public void onConfirmCardNum(String cardNum) {

                            InputAccount.getInstance().handleInputCardNum(cardNum, new InputAccount.InputCardNumCallback() {

                                @Override
                                public void onSuccess(String maskedPan) {
                                    UIUtil.showToast(context, maskedPan, Toast.LENGTH_SHORT);
                                    inputAccountDialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onLuhnCheckResults(String s) {
                                    UIUtil.showToast(context, s, Toast.LENGTH_SHORT);
                                }
                            });

                        }
                    });
                    inputAccountDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            InputAccount.getInstance().abort();
                        }
                    });
                    inputAccountDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if ((event.getAction()) == KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                    inputAccountDialog.onConfirmClicked();
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                    inputAccountDialog.show();
                    usingDialogs.add(inputAccountDialog);
                    usingDialogs.add(processingDialog);
                }

                @Override
                public void onEnterExpiryDate() {
                    Dialog alertDialog = DialogUtils.showEnterValueDialog(context, "Expiry", InputType.TYPE_CLASS_NUMBER, "MMYY", new DialogUtils.EnterValueEditTextDialogCallback() {
                        @Override
                        public void onOK(final Dialog dialog, String msg) {
                            InputAccount.getInstance().handleInputExpiry(msg, new FullIntegrationBase.CurrentStepCallback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onCancel() {
                            InputAccount.getInstance().abort();
                        }
                    });
                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            InputAccount.getInstance().abort();
                        }
                    });
                    usingDialogs.add(alertDialog);
                }

                @Override
                public void onEnterZip() {
                    Dialog alertDialog = DialogUtils.showEnterValueDialog(context, "Zip", InputType.TYPE_CLASS_TEXT, "", new DialogUtils.EnterValueEditTextDialogCallback() {
                        @Override
                        public void onOK(final Dialog dialog, String msg) {
                            InputAccount.getInstance().handleInputZip(msg, new FullIntegrationBase.CurrentStepCallback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onCancel() {
                            InputAccount.getInstance().abort();
                        }
                    });
                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            InputAccount.getInstance().abort();
                        }
                    });
                    usingDialogs.add(alertDialog);
                }

                @Override
                public void onEnterCVV() {
                    Dialog alertDialog = DialogUtils.showEnterValueWithSkipDialog(context, "CVV", InputType.TYPE_CLASS_TEXT,
                            "length 3 or 4", new DialogUtils.EnterValueWithSkipCallback() {
                                @Override
                                public void onOK(final Dialog dialog, String msg) {
                                    InputAccount.getInstance().handleInputCVV(msg, new FullIntegrationBase.CurrentStepCallback() {
                                        @Override
                                        public void onSuccess() {
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onFail(String code, String msg) {
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancel() {
                                    InputAccount.getInstance().abort();
                                }

                                @Override
                                public void onSkip(final Dialog dialog) {
                                    boolean support = InputAccount.getInstance().skipInputCVV(new FullIntegrationBase.CurrentStepCallback() {

                                        @Override
                                        public void onSuccess() {
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onFail(String code, String msg) {
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    if (!support) {
                                        Toast.makeText(context, "BroadPOS not support", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                            InputAccount.getInstance().abort();
                        }
                    });
                    usingDialogs.add(alertDialog);

                }

                @Override
                public void onSelectEMVApp(final List<String> list) {
                    AlertDialog dialog = DialogUtils.showListDialog(context, "Select EMV App", list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            InputAccount.getInstance().handleSelectEMVApp(list.get(which), new FullIntegrationBase.CurrentStepCallback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            InputAccount.getInstance().abort();
                        }
                    });
                    usingDialogs.add(dialog);
                }

                @Override
                public void onProcessing(String code, String msg) {
                    //jis1 success
                    if ("200002".equals(code)) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    final ProcessingDialog processingDialog = new ProcessingDialog(progressDialog);
                    processingDialog.start(msg, false);
                    InputAccount.getInstance().handleProcessing(new InputAccount.ProcessingMessageUpdateCallback() {
                        @Override
                        public void onUpdate(String code, String msg) {
                            progressDialog.setMessage(msg);
                        }
                    }, new FullIntegrationBase.CurrentStepCallback() {
                        @Override
                        public void onSuccess() {
                            processingDialog.dismiss();
                        }

                        @Override
                        public void onFail(String code, String msg) {
                            // not used
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        }
                    });
                    usingDialogs.add(progressDialog);
                }

                @Override
                public void onWarnRemoveCard() {
                    final AlertDialog dialog = DialogUtils.showWarnDialog(context, "Remove Card", "Please remove card", false);
                    usingDialogs.add(dialog);
                    InputAccount.getInstance().handleWarnRemoveCard(new FullIntegrationBase.CurrentStepCallback() {

                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                            usingDialogs.remove(dialog);
                        }

                        @Override
                        public void onFail(String code, String msg) {
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onSelectLanguage(String message) {
                    AlertDialog dialog = DialogUtils.showListDialog(context, "Select Language", new String[]{message}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            InputAccount.getInstance().handleSelectLanguage(new FullIntegrationBase.CurrentStepCallback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            InputAccount.getInstance().abort();
                        }
                    });
                    usingDialogs.add(dialog);
                }
            });
        }
    }

    public static class SetEMVTLVDataRunner extends CmdRunner<EMVTLVData.SetTLVRequest, EMVTLVData.SetTLVResponse> {

        @Override
        public void run(final Context context, final EMVTLVData.SetTLVRequest request, final CommSetting commSetting, final BasePOSLinkCallback<EMVTLVData.SetTLVResponse> posLinkCallback) {

            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<EMVTLVData.SetTLVResponse>() {
                @Override
                public EMVTLVData.SetTLVResponse call() {
                    return EMVTLVData.setEMVTLVData(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<EMVTLVData.SetTLVResponse>() {
                @Override
                public void onFinish(EMVTLVData.SetTLVResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }


    public static class GetEMVTLVDataRunner extends CmdRunner<EMVTLVData.GetTLVRequest, EMVTLVData.GetTLVResponse> {

        @Override
        public void run(final Context context, final EMVTLVData.GetTLVRequest request, final CommSetting commSetting, final BasePOSLinkCallback<EMVTLVData.GetTLVResponse> posLinkCallback) {

            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<EMVTLVData.GetTLVResponse>() {
                @Override
                public EMVTLVData.GetTLVResponse call() {
                    return EMVTLVData.getEMVTLVData(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<EMVTLVData.GetTLVResponse>() {
                @Override
                public void onFinish(EMVTLVData.GetTLVResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class RemoveCardRunner extends CmdRunner<RemoveCard.RemoveCardRequest, RemoveCard.RemoveCardResponse> {
        @Override
        public void run(final Context context, final RemoveCard.RemoveCardRequest request, CommSetting commSetting,
                        final BasePOSLinkCallback<RemoveCard.RemoveCardResponse> posLinkCallback) {

            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            RemoveCard.removeCard(context, request, commSetting, new BasePOSLinkCallback<RemoveCard.RemoveCardResponse>() {
                @Override
                public void onFinish(RemoveCard.RemoveCardResponse response) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(response);
                }
            }, new RemoveCard.RemoveCardCallback() {
                @Override
                public void onWarnRemoveCard() {
                    processingDialog.dismiss();
                    String message1 = TextUtils.isEmpty(request.getMessage1()) ?
                            MainApplication.getInstance().getApplicationContext().getResources().getString(R.string.wait_remove_card) :
                            request.getMessage1();
                    String message2 = request.getMessage2();
                    final AlertDialog dialog = DialogUtils.showWarnDialog(context, "Remove Card",
                            message1 + "\n" + message2, false);
                    RemoveCard.getInstance().handleWarnRemoveCard(new FullIntegrationBase.CurrentStepCallback() {

                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onFail(String code, String msg) {
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            usingDialogs.add(processingDialog.getProgressDialog());
        }
    }

    public static class CompleteOnlineEMVRunner extends CmdRunner<CompleteOnlineEMV.CompleteOnlineEMVRequest, CompleteOnlineEMV.CompleteOnlineEMVResponse> {
        @Override
        public void run(final Context context, final CompleteOnlineEMV.CompleteOnlineEMVRequest request, final CommSetting commSetting,
                        final BasePOSLinkCallback<CompleteOnlineEMV.CompleteOnlineEMVResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<CompleteOnlineEMV.CompleteOnlineEMVResponse>() {
                @Override
                public CompleteOnlineEMV.CompleteOnlineEMVResponse call() throws Exception {
                    return CompleteOnlineEMV.completeOnlineEMV(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<CompleteOnlineEMV.CompleteOnlineEMVResponse>() {
                @Override
                public void onFinish(CompleteOnlineEMV.CompleteOnlineEMVResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });

        }
    }

    public static class AuthorizeCardRunner extends CmdRunner<AuthorizeCard.AuthorizeRequest, AuthorizeCard.AuthorizeResponse> {
        @Override
        public void run(final Context context, final AuthorizeCard.AuthorizeRequest request, CommSetting commSetting,
                        BasePOSLinkCallback<AuthorizeCard.AuthorizeResponse> posLinkCallback) {
            final Dialog processingDialog = ProcessProgressDialog.createDialog(context, context.getString(R.string.processing), true, false,
                    new ProcessProgressDialog.OnSetListener() {
                        @Override
                        public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
                            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel Process", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AuthorizeCard.getInstance().abort();
                                }
                            });
                        }
                    });
            final TextToSpeech textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                }
            });
            processingDialog.show();
            AuthorizeCard.authorize(context, request, commSetting, posLinkCallback, new AuthorizeCard.AuthorizeCallback() {
                @Override
                public void onEnterPinStart() {
                    processingDialog.dismiss();
                    //for test
                    if (Convenience.isButtonClickEnough()) {
                        String tts = "This voice is a test voice, click the PIN to cancel the voice!";
                        textToSpeech.setLanguage(Locale.US);
                        textToSpeech.speak(tts, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    final EnterPINDialog dialog = new EnterPINDialog(context);
                    usingDialogs.add(dialog);
                    dialog.show();
                    dialog.setPINCallback(new EnterPINDialog.PinDialogCallback() {
                        @Override
                        public void onStart(EnterPINDialog pinDialog, final EditText editText) {
                            AuthorizeCard.getInstance().handleInputPinStart(new AuthorizeCard.EnterPinCallBackExpand() {
                                final StringBuilder pinChars = new StringBuilder();

                                @Override
                                public void onAddedPinCharacter() {
                                    pinChars.append("*");
                                    editText.setText(pinChars.toString());
                                    textToSpeech.stop();
                                }

                                @Override
                                public void onClearPin() {
                                    pinChars.delete(0, pinChars.length());
                                    editText.setText(pinChars.toString());
                                    textToSpeech.stop();
                                }

                                @Override
                                public void onTouchPINPad() {
                                    textToSpeech.stop();
                                }
                            }, new AuthorizeCard.AuthorizeCurrentStepCallback() {
                                @Override
                                public void onLastPinTry() {
                                    Toast toast = Toast.makeText(context, "onLastPinTry", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.TOP, 0, 0);
                                    toast.show();
                                }

                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            });
            usingDialogs.add(processingDialog);
        }
    }

    public static List<Dialog> getUsingDialogs() {
        return usingDialogs;
    }

    public static class MockTransRunner extends CmdRunner<MockRequest, MockResponse> {

        @Override
        public void run(Context context, MockRequest request, CommSetting commSetting,
                        final BasePOSLinkCallback<MockResponse> posLinkCallback) {

            SharedPreferenceHelper.save(SharedPrefKey.CURRENT_MOCK_HOST, request.getHost());
            SharedPreferenceHelper.save(SharedPrefKey.CURRENT_MOCK_PORT, String.valueOf(request.getPort()));
            SharedPreferenceHelper.save(SharedPrefKey.CURRENT_MOCK_PATH, request.getPath());

            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            usingDialogs.add(processingDialog.getProgressDialog());

            final MockResponse mockResponse = new MockResponse();
            ProcessTransResult processTransResult = new ProcessTransResult();
            processTransResult.Code = ProcessTransResult.ProcessTransResultCode.OK;
            mockResponse.setProcessTransResult(processTransResult);
            OkHttpUtil.postAsync(request.getHost(), String.format("https://%s:%s%s", request.getHost(), String.valueOf(request.getPort()), request.getPath()), request.getAPIKey(), request.pack(), new OkHttpUtil.HttpCallback() {
                @Override
                public void onSuccess(String code, String body) {
                    processingDialog.dismiss();
                    mockResponse.setResultCode(code);
                    mockResponse.unpack(body);
                    posLinkCallback.onFinish(mockResponse);
                }

                @Override
                public void onFail(String code) {
                    processingDialog.dismiss();
                    mockResponse.setResultCode(code);
                    posLinkCallback.onFinish(mockResponse);
                }
            });

        }
    }

    public static class GetPINBlockRunner extends CmdRunner<GetPINBlock.GetPINBlockRequest, GetPINBlock.GetPINBlockResponse> {
        @Override
        public void run(final Context context, final GetPINBlock.GetPINBlockRequest request, final CommSetting commSetting,
                        final BasePOSLinkCallback<GetPINBlock.GetPINBlockResponse> posLinkCallback) {
            final Dialog processingDialog = ProcessProgressDialog.createDialog(context, context.getString(R.string.processing), true, false,
                    new ProcessProgressDialog.OnSetListener() {
                        @Override
                        public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
                            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel Process", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GetPINBlock.getInstance().abort();
                                }
                            });
                        }
                    });
            processingDialog.show();
            usingDialogs.add(processingDialog);
            final TextToSpeech textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                }
            });

            GetPINBlock.getPinBlock(context, request, commSetting, posLinkCallback, new GetPINBlock.GetPINBlockCallback() {
                @Override
                public void onEnterPinStart() {
                    processingDialog.dismiss();
                    //for test
                    if (Convenience.isButtonClickEnough()) {
                        String tts = "This voice is a test voice, click the PIN to cancel the voice!";
                        textToSpeech.setLanguage(Locale.US);
                        textToSpeech.speak(tts, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    final EnterPINDialog dialog = new EnterPINDialog(context);
                    usingDialogs.add(dialog);
                    dialog.show();
                    dialog.setPINCallback(new EnterPINDialog.PinDialogCallback() {
                        @Override
                        public void onStart(EnterPINDialog pinDialog, final EditText editText) {
                            GetPINBlock.getInstance().handleInputPinStart(new GetPINBlock.EnterPinCallBackExpand() {
                                final StringBuilder pinChars = new StringBuilder();

                                @Override
                                public void onAddedPinCharacter() {
                                    textToSpeech.stop();
                                    pinChars.append("*");
                                    editText.setText(pinChars.toString());
                                }

                                @Override
                                public void onClearPin() {
                                    textToSpeech.stop();
                                    pinChars.delete(0, pinChars.length());
                                    editText.setText(pinChars.toString());
                                }

                                @Override
                                public void onTouchPINPad() {
                                    textToSpeech.stop();
                                }

                            }, new FullIntegrationBase.CurrentStepCallback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFail(String code, String msg) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    public static class UpdateResourceRunner extends CmdRunner<UpdateResource.UpdateResourceRequest, UpdateResource.UpdateResourceResponse> {
        @Override
        public void run(final Context context, final UpdateResource.UpdateResourceRequest request, final CommSetting commSetting, final BasePOSLinkCallback<UpdateResource.UpdateResourceResponse> posLinkCallback) {

            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<UpdateResource.UpdateResourceResponse>() {
                @Override
                public UpdateResource.UpdateResourceResponse call() {
                    return UpdateResource.updateResource(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<UpdateResource.UpdateResourceResponse>() {
                @Override
                public void onFinish(UpdateResource.UpdateResourceResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

    public static class CheckFileRunner extends CmdRunner<CheckFile.CheckFileRequest, CheckFile.CheckFileResponse> {
        @Override
        public void run(final Context context, final CheckFile.CheckFileRequest request, final CommSetting commSetting, final BasePOSLinkCallback<CheckFile.CheckFileResponse> posLinkCallback) {

            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<CheckFile.CheckFileResponse>() {
                @Override
                public CheckFile.CheckFileResponse call() {
                    return CheckFile.checkFile(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<CheckFile.CheckFileResponse>() {
                @Override
                public void onFinish(CheckFile.CheckFileResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

    public static class InitRunner extends CmdRunner<BaseRequest, Init.InitResponse> {

        @Override
        public void run(final Context context, BaseRequest request, final CommSetting commSetting, final BasePOSLinkCallback<Init.InitResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<Init.InitResponse>() {
                @Override
                public Init.InitResponse call() {
                    return Init.init(context, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<Init.InitResponse>() {
                @Override
                public void onFinish(Init.InitResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

    public static class GetVarRunner extends CmdRunner<Variable.GetVariableRequest, Variable.GetVariableResponse> {

        @Override
        public void run(final Context context, final Variable.GetVariableRequest request,
                        final CommSetting commSetting, final BasePOSLinkCallback<Variable.GetVariableResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<Variable.GetVariableResponse>() {
                @Override
                public Variable.GetVariableResponse call() {
                    return Variable.getVariable(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<Variable.GetVariableResponse>() {
                @Override
                public void onFinish(Variable.GetVariableResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

    public static class SetVarRunner extends CmdRunner<Variable.SetVariableRequest, Variable.SetVariableResponse> {

        @Override
        public void run(final Context context, final Variable.SetVariableRequest request,
                        final CommSetting commSetting, final BasePOSLinkCallback<Variable.SetVariableResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<Variable.SetVariableResponse>() {
                @Override
                public Variable.SetVariableResponse call() {
                    return Variable.setVariable(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<Variable.SetVariableResponse>() {
                @Override
                public void onFinish(Variable.SetVariableResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

    public static class VasSetParametersRunner extends CmdRunner<VasSetMerchantParameters.VasSetMerchantParametersRequest, VasSetMerchantParameters.VasSetMerchantParametersResponse> {

        @Override
        public void run(final Context context, final VasSetMerchantParameters.VasSetMerchantParametersRequest request,
                        final CommSetting commSetting, final BasePOSLinkCallback<VasSetMerchantParameters.VasSetMerchantParametersResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<VasSetMerchantParameters.VasSetMerchantParametersResponse>() {
                @Override
                public VasSetMerchantParameters.VasSetMerchantParametersResponse call() {
                    return VasSetMerchantParameters.vasSetMerchantParameters(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<VasSetMerchantParameters.VasSetMerchantParametersResponse>() {
                @Override
                public void onFinish(VasSetMerchantParameters.VasSetMerchantParametersResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

    public static class VasPushDataRunner extends CmdRunner<VasPushData.VasPushDataRequest, VasPushData.VasPushDataResponse> {

        @Override
        public void run(final Context context, final VasPushData.VasPushDataRequest request,
                        final CommSetting commSetting, final BasePOSLinkCallback<VasPushData.VasPushDataResponse> posLinkCallback) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
            processingDialog.start("Processing...", false);
            AppThreadPool.getInstance().postTask(new Callable<VasPushData.VasPushDataResponse>() {
                @Override
                public VasPushData.VasPushDataResponse call() {
                    return VasPushData.vasPushData(context, request, commSetting);
                }
            }, new AppThreadPool.FinishInMainThreadCallback<VasPushData.VasPushDataResponse>() {
                @Override
                public void onFinish(VasPushData.VasPushDataResponse result) {
                    processingDialog.dismiss();
                    posLinkCallback.onFinish(result);
                }
            });
        }
    }

}
