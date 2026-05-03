package com.poslink.sample.setting.model;

import com.poslink.sample.common.model.BaseResponse;

/**
 * Created by Leon.F on 2018/3/13.
 */

public class LogSettingResponse extends BaseResponse {
    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private BaseLogSetting logSetting = new BaseLogSetting();

        public BaseLogSetting getLogSetting() {
            return logSetting;
        }

        public void setLogSetting(BaseLogSetting logSetting) {
            this.logSetting = logSetting;
        }
    }
}
