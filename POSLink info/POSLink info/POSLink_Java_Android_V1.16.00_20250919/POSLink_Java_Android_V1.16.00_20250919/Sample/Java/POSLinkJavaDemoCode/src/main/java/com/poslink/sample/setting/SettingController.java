package com.poslink.sample.setting;

import com.poslink.sample.common.model.BaseResponse;
import com.poslink.sample.setting.model.CommSettingDao;
import com.poslink.sample.setting.model.CommSettingRequest;
import com.poslink.sample.setting.model.CommSettingResponse;
import com.poslink.sample.setting.model.LogSettingDao;
import com.poslink.sample.setting.model.LogSettingRequest;
import com.poslink.sample.setting.model.LogSettingResponse;

import java.io.IOException;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class SettingController {

    public CommSettingResponse startCommSetting(CommSettingRequest requestEntity) {
        CommSettingResponse response = new CommSettingResponse();
        if (requestEntity != null) {
            try {
                CommSettingDao.save(requestEntity);

                response.setCode(BaseResponse.CODE_OK);
                response.setMessage(BaseResponse.MESSAGE_OK);
                CommSettingResponse.Data data = new CommSettingResponse.Data();
                data.setCommSetting(requestEntity);
                response.setData(data);
            } catch (IOException e) {
                e.printStackTrace();
                response.setCode(BaseResponse.CODE_ERROR);
                response.setMessage(BaseResponse.MESSAGE_REQUEST_ERROR);
            }
            return response;
        }
        response.setCode(BaseResponse.CODE_ERROR);
        response.setMessage(BaseResponse.MESSAGE_REQUEST_ERROR);
        return response;
    }

    public CommSettingResponse getCommSetting() {
        CommSettingResponse response = new CommSettingResponse();
        response.setCode(BaseResponse.CODE_OK);
        response.setMessage(BaseResponse.MESSAGE_OK);
        CommSettingResponse.Data data = new CommSettingResponse.Data();
        data.setCommSetting(CommSettingDao.readComm());
        response.setData(data);
        return response;
    }

    public LogSettingResponse startLogSetting(LogSettingRequest requestEntity) {
        LogSettingResponse response = new LogSettingResponse();
        if (requestEntity != null) {
            try {
                LogSettingDao.save(requestEntity);
                response.setCode(BaseResponse.CODE_OK);
                response.setMessage(BaseResponse.MESSAGE_OK);
                LogSettingResponse.Data data = new LogSettingResponse.Data();
                data.setLogSetting(requestEntity);
                response.setData(data);
            } catch (IOException e) {
                e.printStackTrace();
                response.setCode(BaseResponse.CODE_ERROR);
                response.setMessage(BaseResponse.MESSAGE_REQUEST_ERROR);
                LogSettingResponse.Data data = new LogSettingResponse.Data();
                data.setLogSetting(requestEntity);
                response.setData(data);
            }
            return response;
        }
        response.setCode(BaseResponse.CODE_ERROR);
        response.setMessage(BaseResponse.MESSAGE_REQUEST_ERROR);
        return response;
    }


    public LogSettingResponse getLogSetting() {
        LogSettingResponse response = new LogSettingResponse();
        response.setCode(BaseResponse.CODE_OK);
        response.setMessage(BaseResponse.MESSAGE_OK);
        LogSettingResponse.Data data = new LogSettingResponse.Data();
        data.setLogSetting(LogSettingDao.read());
        response.setData(data);
        return response;
    }
}
