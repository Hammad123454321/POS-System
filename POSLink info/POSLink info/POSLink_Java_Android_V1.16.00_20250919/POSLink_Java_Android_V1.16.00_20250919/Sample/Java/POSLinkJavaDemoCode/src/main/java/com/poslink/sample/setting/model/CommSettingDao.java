package com.poslink.sample.setting.model;

import com.pax.poslink.CommSetting;
import com.poslink.sample.common.DemoUtil;
import com.poslink.sample.common.FileUtil;
import com.poslink.sample.common.JsonUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class CommSettingDao {

    private static final String SETTING_PATH = "./comm_setting.json";

    public static void save(CommSettingRequest setting) throws IOException {
        String mapJakcson = JsonUtil.toJson(setting);
        FileUtil.createFile(SETTING_PATH, mapJakcson);
    }

    public static CommSetting read() {
        if (!new File(SETTING_PATH).exists()) {
            return DemoUtil.buildConnection("192.168.1.100");
        }
        String json = FileUtil.readByLines(SETTING_PATH);

        CommSettingRequest commSettingRequest = null;
        commSettingRequest = JsonUtil.fromJson(json, CommSettingRequest.class);
        CommSetting coms = new CommSetting();
        coms.setType(commSettingRequest.getCommType());
        coms.setTimeOut(commSettingRequest.getTimeOut());
        coms.setSerialPort(commSettingRequest.getSerialPort());
        coms.setBaudRate(commSettingRequest.getBaudRate());
        coms.setDestIP(commSettingRequest.getDestIP());
        coms.setDestPort(commSettingRequest.getDestPort());
        coms.setMacAddr(commSettingRequest.getMacAddr());
        return coms;
    }

    public static CommSettingRequest readComm() {
        if (!new File(SETTING_PATH).exists()) {
            CommSetting commSetting = DemoUtil.buildConnection("192.168.1.100");
            CommSettingRequest coms = new CommSettingRequest();
            coms.setCommType(commSetting.getType());
            coms.setTimeOut(commSetting.getTimeOut());
            coms.setSerialPort(commSetting.getSerialPort());
            coms.setBaudRate(commSetting.getBaudRate());
            coms.setDestIP(commSetting.getDestIP());
            coms.setDestPort(commSetting.getDestPort());
            coms.setMacAddr(commSetting.getMacAddr());
            return coms;
        }
        String json = FileUtil.readByLines(SETTING_PATH);

        CommSettingRequest commSettingRequest = null;
        commSettingRequest = JsonUtil.fromJson(json, CommSettingRequest.class);
        return commSettingRequest;
    }
}
