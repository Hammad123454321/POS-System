package com.pax.poslink.ui.pay;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.pax.poslink.R;
import com.pax.poslink.dal.print.ProcessingDialog;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;
import com.pax.poslink.print.PrintDataConverter;
import com.pax.poslink.print.PrintDataException;
import com.pax.poslink.print.PrintDataItemContainer;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.UIUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by linhb on 2015-09-05.
 */
public class PaymentReceiptActivity extends BaseActivity implements View.OnClickListener {
    private String receiptData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_receipt);
        Bundle bundle = getIntent().getExtras();
        String extData = bundle.getString("Payment_Receipt");
        receiptData = bundle.getString("Payment_Receipt_Data");

        Button m_Back = (Button) findViewById(R.id.payment_receipt_back);
        m_Back.setOnClickListener(this);

        Button m_Print = (Button) findViewById(R.id.payment_receipt_print);
        m_Print.setOnClickListener(this);

        WebView m_Receipt = (WebView) findViewById(R.id.payment_receipt);
        m_Receipt.loadDataWithBaseURL(null, extData, "text/html", "utf-8", null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payment_receipt_back:
                PaymentReceiptActivity.this.finish();
                break;
            case R.id.payment_receipt_print:
                print();
                break;
        }
    }

    private void print() {
        Bitmap bitmap = genBitmap(PaymentReceiptActivity.this, receiptData);
        if (bitmap != null) {
            final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(PaymentReceiptActivity.this));
            processingDialog.start("Printing...", false);
            POSLinkPrinter.getInstance(PaymentReceiptActivity.this).print(bitmap, POSLinkPrinter.CutMode.DO_NOT_CUT, new POSLinkPrinter.PrintListener() {
                @Override
                public void onSuccess() {
                    processingDialog.dismiss();
                    bitmap.recycle();
                }

                @Override
                public void onError(ProcessResult processResult) {
                    UIUtil.showToast(PaymentReceiptActivity.this, "PRINT ERROR---Not support error", Toast.LENGTH_SHORT);
                    processingDialog.dismiss();
                    bitmap.recycle();
                }
            });
        }
    }

    private Bitmap genBitmap(Context context, String printData) {
        Bitmap bitmap = null;
        try {
            Integer recommendWidth = POSLinkPrinter.RecommendWidth.MODEL_MAP_RECOMMEND_WIDTH.get(Build.MODEL);
            recommendWidth = recommendWidth == null ? POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH : recommendWidth;

            PrintDataItemContainer printDataItemContainer = PrintDataConverter.parse(printData);
            bitmap = PrintDataConverter.convertPrintDataToBitmap(context, printDataItemContainer,
                    recommendWidth);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(context.getExternalFilesDir(null).getPath() + "/pressed_print.png"));
        } catch (PrintDataException | PrintDataConverter.BarcodeParser.BarcodeFormatException |
                 FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
