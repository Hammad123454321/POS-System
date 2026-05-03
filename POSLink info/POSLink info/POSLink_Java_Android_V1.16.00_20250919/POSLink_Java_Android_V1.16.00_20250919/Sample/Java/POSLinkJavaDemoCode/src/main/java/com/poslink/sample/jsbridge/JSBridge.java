package com.poslink.sample.jsbridge;

import com.pax.poslink.PosLink;
import com.poslink.sample.batch.BatchController;
import com.poslink.sample.batch.model.BatchRequestEntity;
import com.poslink.sample.batch.model.BatchResponseEntity;
import com.poslink.sample.common.JsonUtil;
import com.poslink.sample.main.MainController;
import com.poslink.sample.manage.ManageController;
import com.poslink.sample.manage.model.ManageRequestEntity;
import com.poslink.sample.manage.model.ManageResponseEntity;
import com.poslink.sample.payment.PaymentController;
import com.poslink.sample.payment.model.PaymentRequestEntity;
import com.poslink.sample.payment.model.PaymentResponseEntity;
import com.poslink.sample.report.ReportController;
import com.poslink.sample.report.model.ReportRequestEntity;
import com.poslink.sample.report.model.ReportResponseEntity;
import com.poslink.sample.setting.SettingController;
import com.poslink.sample.setting.model.CommSettingRequest;
import com.poslink.sample.setting.model.CommSettingResponse;
import com.poslink.sample.setting.model.LogSettingRequest;
import com.poslink.sample.setting.model.LogSettingResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import netscape.javascript.JSObject;

/**
 * Created by Leon.F on 2018/3/20.
 */

public class JSBridge {
    private static final Map<String, RequestHelper> URL_CONTROLLER_MAP = new HashMap<String, RequestHelper>() {
        {
            put("/payment", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    PaymentRequestEntity request = JsonUtil.fromJson(req, PaymentRequestEntity.class);
                    PaymentController paymentController = new PaymentController();
                    PaymentResponseEntity responseEntity = paymentController.startPayment(request);
                    return JsonUtil.toJson(responseEntity);
                }
            });
            put("/manage", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    ManageRequestEntity request = JsonUtil.fromJson(req, ManageRequestEntity.class);
                    ManageController manageController = new ManageController();
                    ManageResponseEntity responseEntity = manageController.startManage(request);
                    return JsonUtil.toJson(responseEntity);
                }
            });
            put("/batch", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    BatchRequestEntity request = JsonUtil.fromJson(req, BatchRequestEntity.class);
                    BatchController batchController = new BatchController();
                    BatchResponseEntity batchResponseEntity = batchController.startBatch(request);
                    return JsonUtil.toJson(batchResponseEntity);
                }
            });
            put("/report", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    ReportRequestEntity request = JsonUtil.fromJson(req, ReportRequestEntity.class);
                    ReportController reportController = new ReportController();
                    ReportResponseEntity responseEntity = reportController.startReport(request);
                    return JsonUtil.toJson(responseEntity);
                }
            });
            put("/log_set", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    LogSettingRequest request = JsonUtil.fromJson(req, LogSettingRequest.class);
                    SettingController settingController = new SettingController();
                    LogSettingResponse logSettingResponse = settingController.startLogSetting(request);
                    return JsonUtil.toJson(logSettingResponse);
                }
            });
            put("/get_log", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    SettingController settingController = new SettingController();
                    LogSettingResponse logSettingResponse = settingController.getLogSetting();
                    return JsonUtil.toJson(logSettingResponse);
                }
            });
            put("/comm", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    CommSettingRequest request = JsonUtil.fromJson(req, CommSettingRequest.class);
                    SettingController settingController = new SettingController();
                    CommSettingResponse commSetting = settingController.startCommSetting(request);
                    return JsonUtil.toJson(commSetting);
                }
            });
            put("/get_comm", new RequestHelper() {
                @Override
                public String doRequest(String req) {
                    SettingController settingController = new SettingController();
                    CommSettingResponse commSetting = settingController.getCommSetting();
                    return JsonUtil.toJson(commSetting);
                }
            });
        }
    };

    ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    public void doRequest(String json, JSObject callback) {
        System.out.println(json);
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                JSONRequest req = JsonUtil.fromJson(json, JSONRequest.class);
                String response = URL_CONTROLLER_MAP.get(req.getUrl()).doRequest(req.getData());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        callback.eval("this(" + response + ")");
                    }
                });
            }
        });
    }

    public void cancelRequest() {
        PosLink posLink = MainController.getInstance().getPoslink();
        if (posLink != null) {
            posLink.CancelTrans();
        }
    }

}
