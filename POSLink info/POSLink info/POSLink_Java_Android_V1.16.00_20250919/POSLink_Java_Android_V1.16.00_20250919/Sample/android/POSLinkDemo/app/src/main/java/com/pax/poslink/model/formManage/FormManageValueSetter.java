package com.pax.poslink.model.formManage;

import android.text.InputType;

import com.pax.poslink.customFormManage.GetFormListRequest;
import com.pax.poslink.customFormManage.GetVarListRequest;
import com.pax.poslink.customFormManage.RunFormRequest;
import com.pax.poslink.customFormManage.SetVarListRequest;
import com.pax.poslink.formManage.DoSignatureRequest;
import com.pax.poslink.formManage.InputTextRequest;
import com.pax.poslink.formManage.ShowDialogFormRequest;
import com.pax.poslink.formManage.ShowDialogRequest;
import com.pax.poslink.formManage.ShowItemRequest;
import com.pax.poslink.formManage.ShowMessageRequest;
import com.pax.poslink.formManage.ShowTextBoxRequest;
import com.pax.poslink.model.manage.ManageItemValueSetter;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.HashMap;
import java.util.Map;

public interface FormManageValueSetter<T>{


    void onSet(T request, String value);

    String FORM_TYPE = "FormType";
    String FORM_NAME = "FormName";
    String VAR_NAME_LIST = "VarNameList";
    String TIME_OUT = "TimeOut";
    String VAR_LIST = "VarList";
    String CONTINUOUS_SCREEN = "ContinuousScreen";



    Map<String, FormManageValueSetter> GET_FORM_LIST_MAP = new HashMap<String, FormManageValueSetter>() {
        {
            put(FormManageValueSetter.FORM_TYPE, new FormManageValueSetter<GetFormListRequest>() {
                @Override
                public void onSet(GetFormListRequest request, String value) {
                    request.setFormType(value);
                }
            });
        }
    };
    Map<String, FormManageValueSetter> GET_VAR_LIST_MAP = new HashMap<String, FormManageValueSetter>() {
        {
            put(FormManageValueSetter.VAR_NAME_LIST, new FormManageValueSetter<GetVarListRequest>() {
                @Override
                public void onSet(GetVarListRequest request, String value) {
                    request.setVarNameList(value);
                }
            });
        }
    };

    Map<String, FormManageValueSetter> SET_VAR_LIST_MAP = new HashMap<String, FormManageValueSetter>() {
        {
            put(FormManageValueSetter.VAR_LIST, new FormManageValueSetter<SetVarListRequest>() {
                @Override
                public void onSet(SetVarListRequest request, String value) {
                    request.setVarList(value);
                }
            });
        }
    };

    Map<String, FormManageValueSetter> RUN_FORM_MAP = new HashMap<String, FormManageValueSetter>() {
        {
            put(FormManageValueSetter.FORM_NAME, new FormManageValueSetter<RunFormRequest>() {
                @Override
                public void onSet(RunFormRequest request, String value) {
                    request.setFormName(value);
                }
            });

            put(FormManageValueSetter.TIME_OUT, new FormManageValueSetter<RunFormRequest>() {
                @Override
                public void onSet(RunFormRequest request, String value) {
                    request.setTimeOut(value);
                }
            });
            put(FormManageValueSetter.CONTINUOUS_SCREEN, new FormManageValueSetter<RunFormRequest>() {
                @Override
                public void onSet(RunFormRequest request, String value) {
                    request.setContinuousScreen(value);
                }
            });
        }
    };

    Map<String,FormManageValueSetter> SHOW_MESSAGE_MAP = new HashMap<String,FormManageValueSetter>() {
        {
            put(ManageItemValueSetter.TITLE, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setTitle(value);
                }
            });

            put(ManageItemValueSetter.DISPLAY_MESSAGE, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setDisplayMessage(value);

                }
            });

            put(ManageItemValueSetter.DISPLAY_MESSAGE2, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setDisplayMessage2(value);

                }
            });
            put(ManageItemValueSetter.TOPDOWN, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setTopDown(value);

                }
            });
            put(ManageItemValueSetter.TAX_LINE, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setTaxLine(value);

                }
            });
            put(ManageItemValueSetter.TOTAL_LINE, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setTotalLine(value);

                }
            });
            put(ManageItemValueSetter.IMAGE_NAME, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setImageName(value);

                }
            });
            put(ManageItemValueSetter.IMAGE_DESCRIPTION, new FormManageValueSetter<ShowMessageRequest>() {
                @Override
                public void onSet(ShowMessageRequest request, String value) {
                    request.setImageDescription(value);

                }
            });
        }
    };
    Map<String,FormManageValueSetter> SHOW_DIALOG_MAP = new HashMap<String,FormManageValueSetter>() {
        {
            put(ManageItemValueSetter.TITLE, new FormManageValueSetter<ShowDialogRequest>() {
                @Override
                public void onSet(ShowDialogRequest request, String value) {
                    request.setTitle(value);

                }
            });

            put(ManageItemValueSetter.BUTTON_1, new FormManageValueSetter<ShowDialogRequest>() {
                @Override
                public void onSet(ShowDialogRequest request, String value) {
                    request.setButton1(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_2, new FormManageValueSetter<ShowDialogRequest>() {
                @Override
                public void onSet(ShowDialogRequest request, String value) {
                    request.setButton2(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_3, new FormManageValueSetter<ShowDialogRequest>() {
                @Override
                public void onSet(ShowDialogRequest request, String value) {
                    request.setButton3(value);

                }
            });

            put(ManageItemValueSetter.BUTTON_4, new FormManageValueSetter<ShowDialogRequest>() {
                @Override
                public void onSet(ShowDialogRequest request, String value) {
                    request.setButton4(value);

                }
            });

            put(ManageItemValueSetter.TIME_OUT, new FormManageValueSetter<ShowDialogRequest>() {
                @Override
                public void onSet(ShowDialogRequest request, String value) {
                    request.setTimeOut(StringUtil.parseInt(value));

                }
            });

        }
    };
    Map<String,FormManageValueSetter> SHOW_ITEM_MAP = new HashMap<String,FormManageValueSetter>() {
        {
            put(ManageItemValueSetter.TITLE, new FormManageValueSetter<ShowItemRequest>() {
                @Override
                public void onSet(ShowItemRequest request, String value) {
                    request.setTitle(value);

                }
            });

            put(ManageItemValueSetter.TOPDOWN, new FormManageValueSetter<ShowItemRequest>() {
                @Override
                public void onSet(ShowItemRequest request, String value) {
                    request.setTopDown(value);

                }
            });

            put(ManageItemValueSetter.TAX_LINE, new FormManageValueSetter<ShowItemRequest>() {
                @Override
                public void onSet(ShowItemRequest request, String value) {
                    request.setTaxLine(value);

                }
            });

            put(ManageItemValueSetter.TOTAL_LINE, new FormManageValueSetter<ShowItemRequest>() {
                @Override
                public void onSet(ShowItemRequest request, String value) {
                    request.setTotalLine(value);

                }
            });

            put(ManageItemValueSetter.ITEMS_PASSTHRUDATA, new FormManageValueSetter<ShowItemRequest>() {
                @Override
                public void onSet(ShowItemRequest request, String value) {
                    request.setItemData(value);

                }
            });
        }
    };
    Map<String,FormManageValueSetter> SHOW_TEXTBOX_MAP = new HashMap<String,FormManageValueSetter>() {
        {

            put(ManageItemValueSetter.TITLE, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setTitle(value);

                }
            });
            put(ManageItemValueSetter.TEXT, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setText(value);

                }
            });
            put(ManageItemValueSetter.SAVE_SIG_PATH, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setSigSavePath(value);
                }
            });
            put(ManageItemValueSetter.BUTTON_1, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButton1(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_COLOR1, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButtonColor1(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_2, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButton2(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_COLOR2, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButtonColor2(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_3, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButton3(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_COLOR3, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButtonColor3(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_KEY1, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButtonKey1(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_KEY2, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButtonKey2(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_KEY3, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setButtonKey3(value);

                }
            });
            put(ManageItemValueSetter.ENABLE_HARD_KEY, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setEnableHardKey(value);

                }
            });
            put(ManageItemValueSetter.HARD_KEY_LIST, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setHardKeyList(value);

                }
            });
            put(ManageItemValueSetter.SIGNATURE_BOX, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setSignatureBox(value);

                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setTimeOut(value);

                }
            });
            put(ManageItemValueSetter.CONTINUOUS_SCREEN, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setContinuousScreen(value);
                }
            });
            put(ManageItemValueSetter.BARCODE_DATA, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setBarcodeData(value);
                }
            });
            put(ManageItemValueSetter.BARCODE_TYPE, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setBarcodeType(value);
                }
            });
            put(ManageItemValueSetter.INPUT_TEXT_TITLE, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setInputTextTitle(value);
                }
            });
            put(ManageItemValueSetter.INPUT_TEXT, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setInputText(value);
                }
            });
            put(ManageItemValueSetter.INPUT_TYPE, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setInputType(value);
                }
            });
            put(ManageItemValueSetter.MIN_LENGTH, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setMinLength(value);
                }
            });
            put(ManageItemValueSetter.MAX_LENGTH, new FormManageValueSetter<ShowTextBoxRequest>() {
                @Override
                public void onSet(ShowTextBoxRequest request, String value) {
                    request.setMaxLength(value);
                }
            });
        }
    };
    Map<String,FormManageValueSetter> SHOW_DIALOG_FORM_MAP = new HashMap<String,FormManageValueSetter>() {
        {
            put(ManageItemValueSetter.TITLE, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setTitle(value);

                }
            });
            put(ManageItemValueSetter.LABEL1, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel1(value);

                }
            });
            put(ManageItemValueSetter.LABEL1_PROPERTY, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel1Property(value);

                }
            });
            put(ManageItemValueSetter.LABEL2, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel2(value);

                }
            });
            put(ManageItemValueSetter.LABEL2_PROPERTY, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel2Property(value);

                }
            });
            put(ManageItemValueSetter.LABEL3, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel3(value);

                }
            });
            put(ManageItemValueSetter.LABEL3_PROPERTY, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel3Property(value);

                }
            });
            put(ManageItemValueSetter.LABEL4, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel4(value);

                }
            });
            put(ManageItemValueSetter.LABEL4_PROPERTY, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setLabel4Property(value);

                }
            });
            put(ManageItemValueSetter.BUTTON_TYPE, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setButtonType(value);

                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FormManageValueSetter<ShowDialogFormRequest>() {
                @Override
                public void onSet(ShowDialogFormRequest request, String value) {
                    request.setTimeOut(StringUtil.parseInt(value));

                }
            });
        }
    };
    Map<String,FormManageValueSetter> DOSIGNATURE_MAP = new HashMap<String,FormManageValueSetter>() {
        {

            put(ManageItemValueSetter.EDC_TYPE, new FormManageValueSetter<DoSignatureRequest>() {
                @Override
                public void onSet(DoSignatureRequest request, String value) {
                    request.setEdcType(value);

                }
            });
            put(ManageItemValueSetter.UPLOAD, new FormManageValueSetter<DoSignatureRequest>() {
                @Override
                public void onSet(DoSignatureRequest request, String value) {
                    request.setUpload(StringUtil.parseInt(value));

                }
            });
            put(ManageItemValueSetter.H_REF_NUM, new FormManageValueSetter<DoSignatureRequest>() {
                @Override
                public void onSet(DoSignatureRequest request, String value) {
                    request.setHRefNum(value);

                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FormManageValueSetter<DoSignatureRequest>() {
                @Override
                public void onSet(DoSignatureRequest request, String value) {
                    request.setTimeOut(StringUtil.parseInt(value));

                }
            });
        }
    };
    Map<String,FormManageValueSetter> INPUT_TEXT_MAP = new HashMap<String,FormManageValueSetter>() {
        {
            put(ManageItemValueSetter.TITLE, new FormManageValueSetter<InputTextRequest>() {
                @Override
                public void onSet(InputTextRequest request, String value) {
                    request.setTitle(value);

                }
            });
            put(ManageItemValueSetter.INPUT_TYPE, new FormManageValueSetter<InputTextRequest>() {
                @Override
                public void onSet(InputTextRequest request, String value) {
                    request.setInputType(value);

                }
            });
            put(ManageItemValueSetter.MIN_LENGTH, new FormManageValueSetter<InputTextRequest>() {
                @Override
                public void onSet(InputTextRequest request, String value) {
                    request.setMinLength(value);

                }
            });
            put(ManageItemValueSetter.MAX_LENGTH, new FormManageValueSetter<InputTextRequest>() {
                @Override
                public void onSet(InputTextRequest request, String value) {
                    request.setMaxLength(value);

                }
            });
            put(ManageItemValueSetter.DEFAULT_VALUE, new FormManageValueSetter<InputTextRequest>() {
                @Override
                public void onSet(InputTextRequest request, String value) {
                    request.setDefaultValue(value);

                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FormManageValueSetter<InputTextRequest>() {
                @Override
                public void onSet(InputTextRequest request, String value) {
                    request.setTimeOut(StringUtil.parseInt(value));

                }
            });
        }
    };
}
