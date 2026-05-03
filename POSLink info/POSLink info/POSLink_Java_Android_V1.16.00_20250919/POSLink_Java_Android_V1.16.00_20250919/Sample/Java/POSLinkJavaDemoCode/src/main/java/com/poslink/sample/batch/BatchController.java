package com.poslink.sample.batch;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.poslink.sample.batch.model.BatchRequestEntity;
import com.poslink.sample.batch.model.BatchResponseEntity;
import com.poslink.sample.common.model.BaseResponse;
import com.poslink.sample.setting.model.CommSettingDao;

/**
 * Created by Leon.F on 2018/2/7.
 */

public class BatchController {

    public BatchResponseEntity startBatch(BatchRequestEntity requestEntity) {
        BatchResponseEntity response = new BatchResponseEntity();
        if (requestEntity != null) {
            PosLink posLink = new PosLink();
            posLink.SetCommSetting(CommSettingDao.read());
            BatchRequest batchRequest = new BatchRequest();
            batchRequest.TransType = batchRequest.ParseTransType(requestEntity.getCommandType());
            posLink.BatchRequest = batchRequest;
            ProcessTransResult processTransResult = posLink.ProcessTrans();
            if (processTransResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                response.setCode(BaseResponse.CODE_OK);
                response.setMessage(BaseResponse.MESSAGE_OK);
                BatchResponseEntity.Data data = new BatchResponseEntity.Data();
                BatchResponse batchResponse = posLink.BatchResponse;
                data.setResultCode(posLink.BatchResponse.ResultCode);
                data.setResultTxt(posLink.BatchResponse.ResultTxt);
                data.setCreditCount(posLink.BatchResponse.CreditCount);
                data.setCreditAmount(batchResponse.CreditAmount);
                data.setDebitCount(batchResponse.DebitCount);
                data.setDebitAmount(batchResponse.DebitAmount);
                data.setEBTCount(batchResponse.EBTCount);
                data.setEBTAmount(batchResponse.EBTAmount);
                data.setGiftCount(batchResponse.GiftCount);
                data.setGiftAmount(batchResponse.GiftAmount);
                data.setLoyaltyCount(batchResponse.LoyaltyCount);
                data.setLoyaltyAmount(batchResponse.LoyaltyAmount);
                data.setCashCount(batchResponse.CashCount);
                data.setCashAmount(batchResponse.CashAmount);
                data.setCheckCount(batchResponse.CHECKCount);
                data.setCheckAmount(batchResponse.CHECKAmount);
                data.setTimestamp(batchResponse.Timestamp);
                data.setTID(batchResponse.TID);
                data.setMID(batchResponse.MID);
                data.setHostTraceNum(batchResponse.HostTraceNum);
                data.setBatchNum(batchResponse.BatchNum);
                data.setAuthCode(batchResponse.AuthCode);
                data.setHostCode(batchResponse.HostCode);
                data.setHostResponse(batchResponse.HostResponse);
                data.setMessage(batchResponse.Message);
                data.setExtData(batchResponse.ExtData);
                data.setSAFTotalCount(batchResponse.SAFTotalCount);
                data.setSAFTotalAmount(batchResponse.SAFTotalAmount);
                data.setSAFUploadedCount(batchResponse.SAFUploadedCount);
                data.setSAFUploadedAmount(batchResponse.SAFUploadedAmount);
                data.setSAFFailedCount(batchResponse.SAFFailedCount);
                data.setSAFFailedTotal(batchResponse.SAFFailedTotal);
                data.setSAFDeletedCount(batchResponse.SAFDeletedCount);
                data.setBatchFailedRefNum(batchResponse.BatchFailedRefNum);
                data.setBatchFailedCount(batchResponse.BatchFailedCount);
                response.setData(data);
                return response;
            } else {
                response.setCode(BaseResponse.CODE_ERROR);
                response.setMessage(processTransResult.Msg);
                return response;
            }
        }

        response.setCode(BaseResponse.CODE_ERROR);
        response.setMessage(BaseResponse.MESSAGE_REQUEST_ERROR);
        return response;
    }
}
