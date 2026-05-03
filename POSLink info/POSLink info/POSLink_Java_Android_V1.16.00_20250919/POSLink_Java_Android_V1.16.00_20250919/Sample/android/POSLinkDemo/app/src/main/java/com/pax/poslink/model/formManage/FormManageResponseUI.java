package com.pax.poslink.model.formManage;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.pax.poslink.formManage.DoSignatureResponse;
import com.pax.poslink.formManage.InputTextResponse;
import com.pax.poslink.formManage.ShowDialogFormResponse;
import com.pax.poslink.formManage.ShowDialogResponse;
import com.pax.poslink.formManage.ShowItemResponse;
import com.pax.poslink.formManage.ShowMessageResponse;
import com.pax.poslink.formManage.ShowTextBoxResponse;
import com.pax.poslink.ui.SigDetailActivity;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.customFormManage.GetFormListResponse;
import com.pax.poslink.customFormManage.GetVarListResponse;
import com.pax.poslink.customFormManage.RunFormResponse;
import com.pax.poslink.customFormManage.SetVarListResponse;
import com.pax.poslink.fullIntegration.ResponseUI;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;

import java.util.Arrays;
import java.util.List;

public abstract class FormManageResponseUI<T extends BaseResponse> extends ResponseUI<T> {
    public static class GetFormListUI extends FormManageResponseUI<GetFormListResponse> {
        @Override
        public List<RenderEntity> createRenderList(GetFormListResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Form List", response.getFormList())
            );
        }
    }

    public static class GetVarListUI extends FormManageResponseUI<GetVarListResponse> {
        @Override
        public List<RenderEntity> createRenderList(GetVarListResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Var List", response.getVarList())
            );
        }
    }

    public static class SetVarListUI extends FormManageResponseUI<SetVarListResponse> {

        @Override
        public List<RenderEntity> createRenderList(SetVarListResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }

    public static class RunFormUI extends FormManageResponseUI<RunFormResponse> {
        @Override
        public List<RenderEntity> createRenderList(RunFormResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Result", response.getRunFormResult())
            );
        }
    }

    public static class ShowMessageUI extends FormManageResponseUI<ShowMessageResponse> {
        @Override
        public List<RenderEntity> createRenderList(ShowMessageResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }

    public static class ShowDialogUI extends FormManageResponseUI<ShowDialogResponse> {
        @Override
        public List<RenderEntity> createRenderList(ShowDialogResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Button ID", response.getButtonNum())
            );
        }
    }

    public static class ShowItemUI extends FormManageResponseUI<ShowItemResponse> {
        @Override
        public List<RenderEntity> createRenderList(ShowItemResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }

    public static class ShowTextBoxUI extends FormManageResponseUI<ShowTextBoxResponse> {
        @Override
        public List<RenderEntity> createRenderList(ShowTextBoxResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("SignStatus", response.getSignStatus()),
                    new NameValueBrowserEntity("Sig File Name", "Detail", response.getSigFileName(), InputType.TYPE_NULL, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, NameValueStringEntity entity) {
                            if (TextUtils.isEmpty(entity.getValue())) {
                                Toast.makeText(v.getContext(), "signature file path null", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Intent intent = new Intent(v.getContext(), SigDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("SigPath", entity.getValue());
                            intent.putExtras(bundle);
                            v.getContext().startActivity(intent);
                        }
                    }),
                    new NameValueStringUnEditableEntity("Text", response.getText())
                    );
        }
    }

    public static class ShowDialogFormUI extends FormManageResponseUI<ShowDialogFormResponse> {
        @Override
        public List<RenderEntity> createRenderList(ShowDialogFormResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("LabelSelected", response.getLabelSelected())
                    );
        }
    }

    public static class DoSignatureUI extends FormManageResponseUI<DoSignatureResponse> {
        @Override
        public List<RenderEntity> createRenderList(DoSignatureResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
                    );
        }
    }

    public static class InputTextUI extends FormManageResponseUI<InputTextResponse> {
        @Override
        public List<RenderEntity> createRenderList(InputTextResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Text", response.getText())
                    );
        }
    }
}
