package com.pax.poslink.dal.print;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pax.poslink.R;
import com.pax.poslink.peripheries.POSLinkBluetoothPrinter;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;
import com.pax.poslink.util.UIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrintBitmapDialog extends Dialog {

    private Button displaySign, printSign, printBitmap;
    private EditText editText;
    private ImageView imageView;
    private Bitmap signBitmap;
    private Spinner sp_alignment;

    private int cutMode;
    private int index;
    private String macAdd;
    private int printType;

    public PrintBitmapDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rootView = inflater.inflate(R.layout.dialog_print_bitmap, null);
        addContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        editText = rootView.findViewById(R.id.edit_sign_data);
        editText.setText("111,49^98,50^80,52^69,55^61,58^56,61^55,67^59,76^66,86^76,97^86,105^96,111^100,116^100,119^99,122^92,129^82,135^74,137^68,138^65,138^0,65535^130,110^132,113^135,117^139,120^141,123^141,126^141,129^140,131^0,65535^0,65535^219,113^214,112^200,110^186,110^179,111^173,114^167,121^164,129^163,135^163,140^164,144^168,147^175,148^191,143^207,136^215,129^219,125^222,121^223,121^221,127^216,142^210,160^206,172^202,182^196,194^190,204^185,212^179,216^171,220^160,221^142,216^125,206^0,65535^0,65535^145,83^145,77^144,70^143,63^142,59^143,59^144,62^145,65^0,65535^~");
        displaySign = rootView.findViewById(R.id.dialog_btn_to_sign);
        imageView = rootView.findViewById(R.id.img_sign);
        printSign = rootView.findViewById(R.id.dialog_print_btn);
        printBitmap = rootView.findViewById(R.id.dialog_print_btn_bitmap);
        sp_alignment = rootView.findViewById(R.id.sp_alignment);

        List list = Arrays.asList("left", "center", "right");
        final List valueList = Arrays.asList(0, 1, 2);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_alignment.setAdapter(arrayAdapter);
        sp_alignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String signData = editText.getText().toString();
        signBitmap = POSLinkPrinter.convertSignDataToBitmap(signData);
        imageView.setImageBitmap(signBitmap);
        displaySign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String signData = editText.getText().toString();
                signBitmap = POSLinkPrinter.convertSignDataToBitmap(signData);
                imageView.setImageBitmap(signBitmap);
            }
        });
        printBitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<OrderItem> orderSelected = new ArrayList<>();
                orderSelected.add(new OrderItem("Fish", 3, 3.3f));
                orderSelected.add(new OrderItem("Bread", 3, 6.3f));
                double total = 0;
                for (OrderItem orderItem : orderSelected) {
                    total += orderItem.getPrice() * orderItem.getNumber();
                }
                PrintController printController = new PrintController(rootView);
                printController.setCutMode(cutMode);
                printController.setPrintType(printType);
                printController.print(BitmapFactory.decodeResource(rootView.getResources(), R.drawable.img_sign), new OtherInfoEntity("1",
                        "D00201", "039761", "0019", "1220"), orderSelected, total, new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });

        printSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(getContext()));
                processingDialog.start("Printing...", false);
                if (printType == 0) {
                    POSLinkPrinter.getInstance(rootView.getContext()).print(signBitmap, cutMode, new POSLinkPrinter.PrintListener() {
                        @Override
                        public void onSuccess() {
                            processingDialog.dismiss();
                        }

                        @Override
                        public void onError(ProcessResult processResult) {
                            UIUtil.showToast(getContext(), processResult.getMessage(), Toast.LENGTH_SHORT);
                            processingDialog.dismiss();
                        }
                    }, (int) valueList.get(index));
                } else {
                    POSLinkBluetoothPrinter.getInstance(rootView.getContext())
                            .print(signBitmap, cutMode, new POSLinkPrinter.PrintListener() {
                                @Override
                                public void onSuccess() {
                                    processingDialog.dismiss();
                                }

                                @Override
                                public void onError(ProcessResult processResult) {
                                    UIUtil.showToast(getContext(), processResult.getMessage(), Toast.LENGTH_SHORT);
                                    processingDialog.dismiss();
                                }
                            }, (int) valueList.get(index));
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setMacAdd(String macAdd) {
        this.macAdd = macAdd;
    }

    public void show(int cutMode, int printType) {
        this.cutMode = cutMode;
        this.printType = printType;
        super.show();
    }
}
