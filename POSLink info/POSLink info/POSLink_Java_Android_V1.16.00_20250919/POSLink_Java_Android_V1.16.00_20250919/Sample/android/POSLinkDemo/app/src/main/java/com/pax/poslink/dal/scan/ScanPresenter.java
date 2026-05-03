/*
 * COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2009-2020 PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 */

package com.pax.poslink.dal.scan;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.poslink.R;
import com.pax.poslink.peripheries.POSLinkScanDecoder;
import com.pax.poslink.peripheries.ScanCodeFormat;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonBaseAdapter;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.thread.AppThreadPool;
import com.pax.poslink.view.NameValueSelectEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Eminem.H on 2018/9/6.
 */
public class ScanPresenter implements Camera.PreviewCallback, SurfaceHolder.Callback {
    private static final String TAG = "ScanPresenter";
    private ImageButton ivLight;
    private SurfaceView surfaceViewScan;
    private boolean lighting = true;

    private Context context;

    private byte[] buffer = new byte[4 * WIDTH * HEIGHT];
    private Camera camera;
    private SurfaceHolder holder;
    Handler handler = new Handler();

    private Drawable lightOn, lightOff;

    private final static int WIDTH = isSmallPreviewSize() ? 640 : 1280, HEIGHT = isSmallPreviewSize() ? 480 : 720;

    private final TextView resultTxt;
    private CommonBaseAdapter<RenderEntity> adapter;
    private List<RenderEntity> renderEntityList;

    ScanPresenter(View view, Context context) {
        this.context = context;
        resultTxt = (TextView) view.findViewById(R.id.txt_scan_decode_result);
        surfaceViewScan = view.findViewById(R.id.sv_scan);
        surfaceViewScan.setOnClickListener(onClick);

        ivLight = (ImageButton) view.findViewById(R.id.btn_light);
        ivLight.setOnClickListener(onClick);

        if (lightOff == null) {
            lightOff = context.getResources().getDrawable(R.drawable.light_off);
        }

        if (lightOn == null) {
            lightOn = context.getResources().getDrawable(R.drawable.light_on);
        }
        initFormatList(view);
    }

    private void initFormatList(View view) {
        List<String> formatList = Arrays.asList(
                ScanCodeFormat.UPC,
                ScanCodeFormat.UPC_A,
                ScanCodeFormat.UPC_E,
                ScanCodeFormat.CODE39,
                ScanCodeFormat.CODE_128,
                ScanCodeFormat.CODE_2TO5_INTERLEAVED,
                ScanCodeFormat.CODE93,
                ScanCodeFormat.GS1_DATABAR,
                ScanCodeFormat.MSI,
                ScanCodeFormat.CODEBLOCK_F,
                ScanCodeFormat.PDF417,
                ScanCodeFormat.MICROPDF,
                ScanCodeFormat.MAXICODE,
                ScanCodeFormat.QR_CODE,
                ScanCodeFormat.DATA_MATRIX,
                ScanCodeFormat.AZTEC,
                ScanCodeFormat.HAXIN,
                ScanCodeFormat.MATRIX_25,
                ScanCodeFormat.TRIOPTIC,
                ScanCodeFormat.STRAIGHT_25,
                ScanCodeFormat.TELEPEN,
                ScanCodeFormat.C11,
                ScanCodeFormat.NEC25,
                ScanCodeFormat.CODABAR,
                ScanCodeFormat.HK25,
                ScanCodeFormat.POSTAL
        );
        ListView formatListView = (ListView) view.findViewById(R.id.enable_format_list);
        renderEntityList = new ArrayList<>();
        adapter = new CommonBaseAdapter<>(renderEntityList);
        formatListView.setAdapter(adapter);
        for (final String format : formatList) {
            final String enable = "Enabled";
            final String disable = "Disabled";
            final String convert = "Convert";
            NameValueSelectEntity.OnSelectCallback onSelectCallback = new NameValueSelectEntity.OnSelectCallback() {
                @Override
                public void onSelect(View view, String selectedValue, int position) {
                    if (convert.equals(selectedValue)) {
                        BarcodeConvertActivity.start(view.getContext());
                        return;
                    }
                    if (Boolean.valueOf(selectedValue)) {
                        POSLinkScanDecoder.getInstance(context).enableFormat(format);
                    } else {
                        POSLinkScanDecoder.getInstance(context).disableFormat(format);
                    }
                }
            };
            if (format.equals(ScanCodeFormat.UPC_E) || format.equals(ScanCodeFormat.UPC_A)) {
                renderEntityList.add(new NameValueSelectEntity(format, enable,
                        Arrays.asList(enable, disable, convert),
                        Arrays.asList(String.valueOf(true), String.valueOf(false), convert), 0, onSelectCallback));
            } else {
                renderEntityList.add(new NameValueSelectEntity(format, enable,
                        Arrays.asList(enable, disable),
                        Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, onSelectCallback));
            }
        }
    }

    public void startScan(int orientation) {
        POSLinkScanDecoder.getInstance(context).init(WIDTH, HEIGHT);
        initCamera(orientation);
        if (camera == null) {
            UIUtil.showToast(context, context.getResources().getString(R.string.camera_invalid), Toast.LENGTH_SHORT);
            return;
        }
        holder = surfaceViewScan.getHolder();
        holder.addCallback(ScanPresenter.this);
        try {
            camera.setPreviewCallback(ScanPresenter.this);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ivLight.setBackgroundDrawable(lightOn);
        setFlashLight(true);

        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity nameValueSelectEntity = (NameValueSelectEntity) renderEntity;
                if (nameValueSelectEntity.getItemValues().get(nameValueSelectEntity.getSelectedItem()).equals("Convert")) {
                    nameValueSelectEntity.setSelectedItem(0);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private SparseArray<Camera> getCamera() {
        SparseArray<Camera> array = new SparseArray<>(1);
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            try {
                Camera camera = Camera.open(i);
                if (camera != null) {
                    array.put(i, camera);
                    LogStaticWrapper.getLog().v("CameraID:" + i);
                    break;
                }
            } catch (RuntimeException e) {
                LogStaticWrapper.getLog().exceptionLog(e);
            }
        }
        return array;
    }

    /**
     * Reference <a href=https://developer.android.google.cn/reference/android/hardware/Camera.html#setDisplayOrientation(int)>
     *     make the camera image show in the same orientation as the display</a>
     * @param rotation
     * @param cameraId
     * @param camera
     */
    public static void setCameraDisplayOrientation(int rotation, int cameraId, android.hardware.Camera camera) {
        LogStaticWrapper.getLog().v("Display rotation:" + rotation);
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    private void initCamera(int orientation) {
        SparseArray<Camera> array= getCamera();
        if (array.size() < 1) return;
        camera = array.valueAt(0);
        if (camera == null) return;
        setCameraDisplayOrientation(orientation, array.keyAt(0), camera);
//        camera.setDisplayOrientation(orientation == Configuration.ORIENTATION_PORTRAIT && !isA80() ? 90 : 0);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(WIDTH, HEIGHT);
        parameters.setPictureSize(WIDTH, HEIGHT);
        if (parameters.isZoomSupported()) {
            parameters.setZoom(0);
        }
        setCameraParametersFocusMode(parameters);

        camera.cancelAutoFocus();


        // For formats besides YV12, the size of the buffer is determined by multiplying the preview image width,
        // height, and bytes per pixel. The width and height can be read from Camera.Parameters.getPreviewSize(). Bytes
        // per pixel can be computed from android.graphics.ImageFormat.getBitsPerPixel(int) / 8, using the image format
        // from Camera.Parameters.getPreviewFormat().
        float bytesPerPixel = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / (float) 8;

        handler.postDelayed(runnable, 2000);
    }

    private void setCameraParameters(Camera.Parameters parameters) {
        camera.setParameters(parameters);
    }

    private void setCameraParametersFocusMode(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (!supportedFocusModes.isEmpty()){
            parameters.setFocusMode(supportedFocusModes.get(0));
        }
        try {
            camera.setParameters(parameters);
        } catch (Exception ignore) {
        }
    }
    private static boolean isSmallPreviewSize() {
        return isA80() || isE800();
    }

    private static boolean isA80() {
        return Build.MODEL.equals("A80");
    }

    private static boolean isE800() {
        return Build.MODEL.equals("E800");
    }

    @Override
    public void onPreviewFrame(final byte[] previewData, final Camera camera) {
        AppThreadPool.getInstance().runInBackground(new Runnable() {
            @Override
            public void run() {
                POSLinkScanDecoder.DecodeResult decodeResult;
                decodeResult = POSLinkScanDecoder.getInstance(context).decode(buffer);
                if (buffer.length != previewData.length) {
                    buffer = new byte[previewData.length];
                }
                camera.addCallbackBuffer(buffer);
                if (decodeResult.getContent() != null) {
                    final StringBuilder resultSB = new StringBuilder();
                    resultSB.append("Format:").append(decodeResult.getFormat()).append("\n")
                            .append("Content:").append(decodeResult.getContent()).append("\n");
                    AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTxt.setText(resultSB.toString());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        synchronized (this) {
            if (camera != null) {
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                camera.setPreviewCallbackWithBuffer(this);
                camera.addCallbackBuffer(buffer);
                camera.startPreview();
            }
            if (camera != null) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.cancelAutoFocus();
                            doAutoFocus();
                        }
                    }
                });
            }
        }


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * Manual focus
     */
    public void doAutoFocus() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            setCameraParametersFocusMode(parameters);
            camera.cancelAutoFocus();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        Camera.Parameters curParam = camera.getParameters();
                        setCameraParametersFocusMode(curParam);
                    }
                }
            });
        }
    }

    public boolean setFlashLight(boolean open) {
        if (camera == null) {
            return false;
        }
        Camera.Parameters parameters = camera.getParameters();
        if (parameters == null) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size()) {
            // Use the screen as a flashlight (next best thing)
            return false;
        }
        String flashMode = parameters.getFlashMode();
        if (open) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                setCameraParameters(parameters);
                return true;
            } else {
                return false;
            }
        } else {
            if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                setCameraParameters(parameters);
                return true;
            } else {
                return false;
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            doAutoFocus();
            handler.postDelayed(this, 500);
        }
    };

    public void onDestroy() {
        POSLinkScanDecoder.getInstance(context).release();
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            handler.removeCallbacks(runnable);
        }
        setFlashLight(false);
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.sv_scan:
                    doAutoFocus();
                    break;
                case R.id.btn_light:
                    if (lighting) {
                        lighting = false;
                        ivLight.setBackgroundDrawable(lightOff);
                    } else {
                        lighting = true;
                        ivLight.setBackgroundDrawable(lightOn);
                    }
                    setFlashLight(lighting);

                    break;
            }

        }
    };
}
