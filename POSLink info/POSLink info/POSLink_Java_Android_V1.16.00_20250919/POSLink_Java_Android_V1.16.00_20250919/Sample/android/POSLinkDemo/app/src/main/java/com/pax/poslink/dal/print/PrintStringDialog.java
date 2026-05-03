package com.pax.poslink.dal.print;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.peripheries.POSLinkBluetoothPrinter;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;
import com.pax.poslink.print.PrintBarcode;
import com.pax.poslink.print.PrintDataItem;
import com.pax.poslink.util.AppThread;
import com.pax.poslink.util.adapter.CommonBaseAdapter;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.SingleButtonEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Leon.F on 2018/3/30.
 */

public class PrintStringDialog extends Dialog {

    private List<RenderEntity> renderEntities = new ArrayList<>();
    private CommonBaseAdapter<RenderEntity> commonBaseAdapter = new CommonBaseAdapter<>(renderEntities);
    private int cutMode = POSLinkPrinter.CutMode.DO_NOT_CUT;
    private POSLinkPrinter.PrintDataFormatter printDataFormatter;
    private int printType;
    private String macAdd;

    public PrintStringDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        printDataFormatter = new POSLinkPrinter.PrintDataFormatter();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.dialog_print_data, null);
        addContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ListView listView = (ListView) rootView.findViewById(R.id.dialog_print_list);
        final EditText contentView = (EditText) rootView.findViewById(R.id.dialog_print_content);
        View addLineBtn = rootView.findViewById(R.id.dialog_print_add_line_btn);
        addLineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printDataFormatter.addLineSeparator();
                showContent(contentView, printDataFormatter);
            }
        });
        View printBtn = rootView.findViewById(R.id.dialog_print_btn);
        listView.setAdapter(commonBaseAdapter);
        initList(contentView);
        initContent(contentView);
        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(v.getContext()));
                processingDialog.start("Printing...", false);
                if (printType == 0) {
                    POSLinkPrinter.getInstance(v.getContext()).print(printDataFormatter.build(), cutMode,
                            SettingINI.getCommSettingFromFile(getContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME), new POSLinkPrinter.PrintListener() {
                                @Override
                                public void onSuccess() {
                                    processingDialog.dismiss();
                                }

                                @Override
                                public void onError(ProcessResult processResult) {
                                    toastError(v.getContext(), processResult.getMessage());
                                    processingDialog.dismiss();
                                }
                            });
                } else {
                    POSLinkBluetoothPrinter.getInstance(v.getContext())
                            .setMacAddress(macAdd)
                            .print(printDataFormatter.build(), cutMode, new POSLinkPrinter.PrintListener() {
                        @Override
                        public void onSuccess() {
                            processingDialog.dismiss();
                        }

                        @Override
                        public void onError(ProcessResult processResult) {
                            toastError(v.getContext(), processResult.getMessage());
                            processingDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void toastError(final Context context, final String message) {
        AppThread.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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

    private void initContent(EditText contentView) {
//        printDataFormatter
//                .addHeader()
//                .addCenterAlign().addBigFont().addContent("Menu")
//                .addLineSeparator()
//                .addContent("Name").addCenterAlign().addContent("Number").addRightAlign().addContent("Price")
//                .addLineSeparator()
//                .addSmallFont().addContent("Salad").addSmallFont().addCenterAlign().addContent("X 2").addSmallFont().addRightAlign().addContent("$5.00")
//                .addLineSeparator()
//                .addSmallFont().addContent("Fish").addSmallFont().addCenterAlign().addContent("X 3").addSmallFont().addRightAlign().addContent("$15.00")
//                .addLineSeparator()
//                .addSmallFont().addContent("Bread").addSmallFont().addCenterAlign().addContent("X 4").addSmallFont().addRightAlign().addContent("$2.50")
//                .addLineSeparator()
//                .addContent("Total").addCenterAlign().addContent("9").addRightAlign().addContent("$22.50")
//                .addLineSeparator()
//                .addLineSeparator()
//
//                .addLeftAlign().addContent("Date").addRightAlign().addDate()
//                .addLineSeparator()
//                .addLeftAlign().addContent("Time").addRightAlign().addTime()
//                .addLineSeparator()
//                .addLeftAlign().addContent("SN").addRightAlign().addSN()
//                .addLineSeparator()
//                .addDisclaimer()
//                .addTrailer();

        printDataFormatter.clear();
        printDataFormatter
                .addHeader()
                .addInvert().addSmallFont().addCenterAlign().addContent("invert")
                .addLineSeparator()
                .addInvert().addRightAlign().addContent("invert")
                .addLineSeparator()
                .addInvert().addContent("invert")
                .addLineSeparator()
                .addBarcode(PrintBarcode.BarCodeType.CODE39, 1, "12345678")
                .addLineSeparator()
                .addLeftAlign().addContent("left").addCenterAlign().addContent("center").addRightAlign().addNormalFont().addContent("right")
                .addLineSeparator()
                .addLeftAlign().addContent("left").addCenterAlign().addContent("center").addRightAlign().addNormalFont().addContent("right")
                .addLineSeparator()
                .addBarcode(PrintBarcode.BarCodeType.CODE128, 1, "56781234")
                .addLineSeparator()
                .addLeftAlign().addBigFont().addContent("left").addBigFont().addCenterAlign().addContent("center").addRightAlign().addBigFont().addContent("right")
                .addLineSeparator()
                .addLeftAlign().addBigFont().addContent("left").addBigFont().addCenterAlign().addContent("center").addRightAlign().addBigFont().addContent("right")
                .addLineSeparator()
                .addBarcode(PrintBarcode.BarCodeType.EAN13, 1, "123456789012")
                .addLineSeparator()
                .addLeftAlign().addSmallFont().addContent("left").addCenterAlign().addSmallFont().addContent("center").addSmallFont().addRightAlign().addContent("right")
                .addLineSeparator()
                .addLeftAlign().addSmallFont().addContent("left").addCenterAlign().addSmallFont().addContent("center").addSmallFont().addRightAlign().addContent("right")
                .addLineSeparator()
                .addBarcode(PrintBarcode.BarCodeType.EAN128, 1, "[3102]000035")
                .addLineSeparator()
                .addDate()
                .addLineSeparator()
                .addBarcode(PrintBarcode.BarCodeType.PDF417, 2, "abcdefghijklmnopqrstuvwxyz57682143abcdefghijklmnopqrstuvwxyz57682143")
                .addLineSeparator()
//                .addBarcode(PrintBarcode.BarCodeType.GRIDMATRIX, 4, "56781234asoi23doj3oi")
                .addLineSeparator()
                .addBarcode(PrintBarcode.BarCodeType.QRCODE, 8, "56781234abcd111243232123fds")
                .addLineSeparator()
                .addDisclaimer()
                .addTrailer();

        showContent(contentView, printDataFormatter);
        contentView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                printDataFormatter.set(s.toString());
            }
        });
    }

    private void initList(final EditText contentView) {
        renderEntities.add(new SingleButtonEntity("LEFT_ALIGN: " + PrintDataItem.LEFT_ALIGN, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addLeftAlign();
                showContent(contentView, printDataFormatter);
            }
        }));
        renderEntities.add(new SingleButtonEntity("RIGHT_ALIGN: " + PrintDataItem.RIGHT_ALIGN, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addRightAlign();
                showContent(contentView, printDataFormatter);
            }
        }));

        renderEntities.add(new SingleButtonEntity("CENTER_ALIGN: " + PrintDataItem.CENTER_ALIGN, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addCenterAlign();
                showContent(contentView, printDataFormatter);

            }
        }));

        renderEntities.add(new SingleButtonEntity("Random Content" , new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                String[] words = new String[] {
                        "word", "review", "bang", "chew", "hour",
                        "reptile", "approach", "shiver", "employ", "skeleton",
                        "bubble", "education", "crossing", "evaluate", "salmon",
                        "copyright", "principle", "folklore", "fuel", "eternal",
                        "noise", "print", "kid", "coach", "catch", "paper",
                        "folk", "thought", "first-hand"
                };
                printDataFormatter.addContent(words[new Random().nextInt(words.length)]);
                showContent(contentView, printDataFormatter);
            }
        }));
        renderEntities.add(new SingleButtonEntity("SMALL_FONT" + PrintDataItem.SMALL_FONT, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addSmallFont();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("NORMAL_FONT" + PrintDataItem.NORMAL_FONT, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addNormalFont();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("BIG_FONT" + PrintDataItem.BIG_FONT, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addBigFont();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("HEADER: " + PrintDataItem.HEADER, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addHeader();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("TRAILER: " + PrintDataItem.TRAILER, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addTrailer();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("DISCLAIMER: " + PrintDataItem.DISCLAIMER, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addDisclaimer();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("DATE: " + PrintDataItem.DATE, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addDate();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("TIME: " + PrintDataItem.TIME, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addTime();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("SN: " + PrintDataItem.SN, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addSN();
                showContent(contentView, printDataFormatter);

            }
        }));
        renderEntities.add(new SingleButtonEntity("INVERT" + PrintDataItem.INVERT, new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                printDataFormatter.addInvert();
                showContent(contentView, printDataFormatter);

            }
        }));
        commonBaseAdapter.notifyDataSetChanged();
    }

    private void showContent(EditText contentView, POSLinkPrinter.PrintDataFormatter printDataFormatter) {
        contentView.setText(printDataFormatter.build());
        contentView.setSelection(contentView.length());
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
