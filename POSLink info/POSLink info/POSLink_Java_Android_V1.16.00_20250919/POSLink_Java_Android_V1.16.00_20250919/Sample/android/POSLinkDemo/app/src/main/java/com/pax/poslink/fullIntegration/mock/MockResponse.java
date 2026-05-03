package com.pax.poslink.fullIntegration.mock;

import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.util.StringUtil;

/**
 * Created by Leon.F on 2018/5/24.
 */
public class MockResponse extends BaseResponse<String> {
    private String resp;


    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public void unpack(String resp) {
        this.resp = StringUtil.formatExtData(resp);
    }

    public String getResp() {
        return resp;
    }
}
