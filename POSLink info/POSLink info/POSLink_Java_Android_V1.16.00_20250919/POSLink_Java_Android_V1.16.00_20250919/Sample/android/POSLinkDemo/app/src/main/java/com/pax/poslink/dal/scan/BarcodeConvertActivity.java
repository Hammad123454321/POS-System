package com.pax.poslink.dal.scan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.peripheries.BarcodeHelper;
import com.pax.poslink.peripheries.ScanCodeFormat;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarcodeConvertActivity extends BaseActivity {

    private static final String ORIG_FORMAT = "OrigFormat";
    private static final String RESULT_FORMAT = "ResultFormat";
    private static final String ORIGIN = "Origin";
    private NameValueSelectEntity origFormatEntity;
    private NameValueSelectEntity resultFormatEntity;
    private NameValueStringEntity origContent;
    private NameValueStringEntity resultContent;

    public static void start(Context context) {
        Intent starter = new Intent(context, BarcodeConvertActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_convert);

        List<String> formatList = Arrays.asList(ScanCodeFormat.UPC_A, ScanCodeFormat.UPC_E);
        final List<RenderEntity> renderEntityList = new ArrayList<>();
        origFormatEntity = new NameValueSelectEntity(ORIG_FORMAT, "", formatList, formatList, 0);
        renderEntityList.add(origFormatEntity);
        resultFormatEntity = new NameValueSelectEntity(RESULT_FORMAT, "", formatList, formatList, 1);
        renderEntityList.add(resultFormatEntity);
        origContent = new NameValueStringEntity(ORIGIN, "", InputType.TYPE_CLASS_TEXT, "");
        renderEntityList.add(origContent);
        resultContent = new NameValueStringEntity("Result", "", InputType.TYPE_CLASS_TEXT, "");
        renderEntityList.add(resultContent);
        final ViewGroup settingContainer = findViewById(R.id.barcode_convert_setting_container);
        View convertBtn = findViewById(R.id.barcode_convert_btn);
        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String origFormat = origFormatEntity.getItemValues().get(origFormatEntity.getSelectedItem());
                String resultFormat = resultFormatEntity.getItemValues().get(resultFormatEntity.getSelectedItem());
                if (origFormat.equals(ScanCodeFormat.UPC_A) && resultFormat.equals(ScanCodeFormat.UPC_E)) {
                    resultContent.setValue(BarcodeHelper.convertUPCAtoUPCE(origContent.getValue()));
                    refresh(renderEntityList, settingContainer);
                }

                if (origFormat.equals(ScanCodeFormat.UPC_E) && resultFormat.equals(ScanCodeFormat.UPC_A)) {
                    resultContent.setValue(BarcodeHelper.convertUPCEtoUPCA(origContent.getValue()));
                    refresh(renderEntityList, settingContainer);
                }
            }
        });

        refresh(renderEntityList, settingContainer);
    }

    private void refresh(List<RenderEntity> renderEntityList, ViewGroup settingContainer) {
        settingContainer.removeAllViews();
        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(settingContainer);
            settingContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }
}
