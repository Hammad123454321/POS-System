package com.pax.poslink.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pax.gl.commhelper.IBtScanner;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.pax.poslink.MainApplication;
import com.pax.poslink.R;
import com.pax.poslink.util.adapter.BlueListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin.Z on 2020-3-4
 */
public class BlueToothHelper {

    private static IBtScanner btScan;

    public interface BlueCallback {
        void onFinish(String macAddress);
    }

    public static void initBlueTooth(Context context, final BlueCallback callback) {
        final List<BlueListAdapter.BlueItem> blueItems = new ArrayList<>();
        final BlueListAdapter blueListAdapter = new BlueListAdapter(context, blueItems);
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.listview_dialog, null);
        ListView listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(blueListAdapter);
        btScan = PaxGLComm.getInstance(MainApplication.getInstance().getApplicationContext()).getBtScanner();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setOnCancelListener(null);
        builder.setTitle("Search printer...");
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                btScan.stop();
                callback.onFinish(blueItems.get(position).getMac());
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog1) {
                btScan.stop();
            }
        });

        btScan.start(new IBtScanner.IBtScannerListener() {
            @Override
            public void onFinished() {
                System.out.println("---------finish scan---------");
            }
            @Override
            public void onDiscovered(IBtScanner.IBtDevice dev) {
                if (dev.getName().contains("BP60A")){
                    BlueListAdapter.BlueItem item = new BlueListAdapter.BlueItem(dev.getName(), dev.getIdentifier());
                    blueItems.add(item);
                    blueListAdapter.notifyDataSetChanged();
                }
            }
        }, 60);
    }

    public static boolean checkPermission(Context context) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {//API LEVEL 18
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            } else {
                return true;
            }
        } else {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
