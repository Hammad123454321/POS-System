package com.poslink.sample.setting.model;

import com.poslink.sample.common.model.BaseResponse;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class CommSettingResponse extends BaseResponse {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        BaseCommSetting commSetting = new BaseCommSetting();

        /**
         * @param commSetting the commSetting to set
         */
        public void setCommSetting(BaseCommSetting commSetting) {
            this.commSetting = commSetting;
        }

        /**
         * @return the commSetting
         */
        public BaseCommSetting getCommSetting() {
            return commSetting;
        }
    }
}
