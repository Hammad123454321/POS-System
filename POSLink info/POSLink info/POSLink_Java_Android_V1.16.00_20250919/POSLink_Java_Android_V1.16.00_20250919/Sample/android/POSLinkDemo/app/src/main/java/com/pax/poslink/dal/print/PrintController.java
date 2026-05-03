package com.pax.poslink.dal.print;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.peripheries.POSLinkBluetoothPrinter;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;
import com.pax.poslink.util.thread.AppThreadPool;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon.F on 2018/3/23.
 */

public class PrintController {

    private View rootView;

    private View printViewContainer;
    private ViewGroup orderItemContainer;
    private View orderItemHeadView;
    private TextView printPriceTxtView;
    private TextView printTotalTxtView;

    private final LinearLayout otherInfoContainer;
    private List<SimpleNameValueEntity> otherInfoList = new ArrayList<>();
    private final ImageView signatureImg;
    private final View signatureBottomLine;
    private int cutMode = POSLinkPrinter.CutMode.FULL_PAPER_CUT;
    private int printType;

    public PrintController(View rootView) {
        this.rootView = rootView;
        printViewContainer = rootView.findViewById(R.id.sv_print);
        ViewGroup.LayoutParams layoutParams = printViewContainer.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        }
        Integer recommendWidth = POSLinkPrinter.RecommendWidth.MODEL_MAP_RECOMMEND_WIDTH.get(Build.MODEL);
        recommendWidth = recommendWidth == null ? POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH : recommendWidth;
        layoutParams.width = recommendWidth;
        printViewContainer.setLayoutParams(layoutParams);
        orderItemContainer = (ViewGroup) rootView.findViewById(R.id.lv_print);
        printTotalTxtView = (TextView) rootView.findViewById(R.id.tv_print_total);
        printPriceTxtView = (TextView) rootView.findViewById(R.id.tv_print_price);
        otherInfoContainer = (LinearLayout) rootView.findViewById(R.id.print_other_info_container);
        signatureImg = (ImageView) rootView.findViewById(R.id.signature_image);
        signatureBottomLine = rootView.findViewById(R.id.signature_bottom_line);

        initOrderItemHeadView(rootView);
    }

    public void setCutMode(int cutMode) {
        this.cutMode = cutMode;
    }

    public void setPrintType(int printType) {
        this.printType = printType;
    }

    private void initOtherInfoContainer(LinearLayout otherInfoContainer, OtherInfoEntity otherInfoEntity) {
        otherInfoList.clear();

        StringBuilder transNO = new StringBuilder(otherInfoEntity.getTransNO());
        if (!TextUtils.isEmpty(transNO.toString())) {
            if (transNO.length() < 6) {
                int needZerosCnt = 6 - transNO.length();
                for (int i = 0; i < needZerosCnt; i++) {
                    transNO.insert(0, "0");
                }
            }
            addNewOtherInfoEntity(new SimpleNameValueEntity("Order Num: ", transNO.toString()));
        }
        addNewOtherInfoEntity(new SimpleNameValueEntity("Date: ", PrintUtil.getSystemDate()));
        addNewOtherInfoEntity(new SimpleNameValueEntity("Time: ", PrintUtil.getSystemTime()));
        addNewOtherInfoEntity(new SimpleNameValueEntity("Ref Num: ", otherInfoEntity.getHref()));
        addNewOtherInfoEntity(new SimpleNameValueEntity("Auth Code: ", otherInfoEntity.getAuthCode()));
        addNewOtherInfoEntity(new SimpleNameValueEntity("Card No: ", otherInfoEntity.getCardNO()));
        addNewOtherInfoEntity(new SimpleNameValueEntity("Exp Date: ", otherInfoEntity.getExpDate()));

        otherInfoContainer.removeAllViews();
        for (SimpleNameValueEntity nameValueEntity : otherInfoList) {
            SimpleNameValueItemView itemView = nameValueEntity.createView(otherInfoContainer);
            itemView.render(nameValueEntity);
            otherInfoContainer.addView(itemView.getView());
        }
    }

    private void addNewOtherInfoEntity(SimpleNameValueEntity e) {
        if (!TextUtils.isEmpty(e.getName())) {
            otherInfoList.add(e);
        }
    }

    private void initOrderItemHeadView(View rootView) {
        orderItemHeadView = LayoutInflater.from(rootView.getContext()).inflate(R.layout.layout_print_title_item, null);
        TextView tv_name = (TextView) orderItemHeadView.findViewById(R.id.tv_print_total_title);
        TextView tv_number = (TextView) orderItemHeadView.findViewById(R.id.tv_print_total);
        TextView tv_price = (TextView) orderItemHeadView.findViewById(R.id.tv_print_price);
        tv_name.setText("Name");
        tv_number.setText("Number");
        tv_price.setText("Price");
    }

    public void print(@Nullable final Bitmap signBitmap, final OtherInfoEntity otherInfoEntity, final List<OrderItem> orderSelected, final double totalPrices, final Runnable printFinish) {
        Log.i("iii", "print-------------------------------------------------------------------");
        if (orderSelected.size() > 0) {
            renderPrintView(signBitmap, true, otherInfoEntity, orderSelected, totalPrices);
            rootView.measure(View.MeasureSpec.makeMeasureSpec(printViewContainer.getLayoutParams().width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            rootView.layout(0, 0, rootView.getMeasuredWidth(), rootView.getMeasuredHeight());
            printViewContainer.requestLayout();
            printViewContainer.invalidate();
            printViewContainer.post(new Runnable() {
                @Override
                public void run() {
                    final Bitmap resultBitmap = PrintUtil.shotScrollView((ScrollView) printViewContainer);
                    Log.i("iii", "width---" + resultBitmap.getWidth());
                    printMerchantCopy(printFinish, resultBitmap);
                }
            });
        } else {
            Toast.makeText(rootView.getContext(), "Menu is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void printMerchantCopy(final Runnable printFinish, final Bitmap bitmap) {
        final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(rootView.getContext()));
        processingDialog.start("Printing", false);
        AppThreadPool.getInstance().runInBackground(new Runnable() {
            @Override
            public void run() {

                if (printType == 0) {
                    POSLinkPrinter.getInstance(rootView.getContext()).print(bitmap, cutMode, new POSLinkPrinter.PrintListener() {
                        @Override
                        public void onSuccess() {
                            dismissDialog(processingDialog, printFinish);
                        }

                        @Override
                        public void onError(ProcessResult processResult) {
                            dismissDialog(processingDialog, printFinish);
                            toastError(processResult.getMessage());
                        }
                    });
                } else {
                    POSLinkBluetoothPrinter.getInstance(rootView.getContext()).print(bitmap, cutMode, new POSLinkPrinter.PrintListener() {
                        @Override
                        public void onSuccess() {
                            dismissDialog(processingDialog, printFinish);
                        }

                        @Override
                        public void onError(ProcessResult processResult) {
                            dismissDialog(processingDialog, printFinish);
                            toastError(processResult.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void toastError(final String ret) {
        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(rootView.getContext(), "Print Error---" + ret, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void dismissDialog(final ProcessingDialog processingDialog, final Runnable printFinish) {
        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processingDialog.dismiss();
                printFinish.run();
            }
        });
    }

    private void renderPrintView(Bitmap signBitmap, boolean visibleSignature, OtherInfoEntity otherInfoEntity, List<OrderItem> orderSelected, double totalPrices) {
        renderOrderItemContainer(orderSelected, totalPrices * 0.1);
        initOtherInfoContainer(otherInfoContainer, otherInfoEntity);
        if (visibleSignature && signBitmap != null) {
            signatureImg.setImageBitmap(signBitmap);
        }
        signatureBottomLine.setVisibility(visibleSignature ? View.VISIBLE : View.GONE);
        signatureImg.setVisibility(visibleSignature ? View.VISIBLE : View.GONE);
//        Utils.setListViewHeightBasedOnChildren(orderItemContainer);
        int total = 0;
        for (OrderItem ordered : orderSelected) {
            total += ordered.getNumber();
        }
        printTotalTxtView.setText(String.valueOf(total));
        DecimalFormat df = new DecimalFormat("#.##");
        printPriceTxtView.setText(String.format("$ %s", df.format(totalPrices * (1 + 0.1))));
    }

    private void renderOrderItemContainer(List<OrderItem> orderSelected, double tax) {
        orderItemContainer.removeAllViews();
        orderItemContainer.addView(orderItemHeadView);
        for (OrderItem modelOrdered : orderSelected) {
            ViewGroup parent = orderItemContainer;
            OrderItemView itemView = new OrderItemView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ordered_print, parent, false));
            itemView.render(modelOrdered);
            orderItemContainer.addView(itemView.getView());
        }

        ViewGroup parent = orderItemContainer;
        OrderItemView itemView = new OrderItemView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ordered_print, parent, false));
        itemView.render(new OrderItem("Tax", 0, tax));
        orderItemContainer.addView(itemView.getView());

    }

}
