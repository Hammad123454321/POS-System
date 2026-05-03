package com.poslink.sample.report;

import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.ReportResponse;
import com.poslink.sample.common.model.BaseResponse;
import com.poslink.sample.report.model.ReportRequestEntity;
import com.poslink.sample.report.model.ReportResponseEntity;
import com.poslink.sample.setting.model.CommSettingDao;

/**
 * Created by Leon.F on 2018/4/10.
 */

public class ReportController {

    public ReportResponseEntity startReport(ReportRequestEntity requestEntity) {
        ReportResponseEntity response = new ReportResponseEntity();
        if (requestEntity != null) {
            PosLink posLink = new PosLink();
            posLink.SetCommSetting(CommSettingDao.read());
            ReportRequest reportRequest = new ReportRequest();
            reportRequest.TransType = reportRequest.ParseTransType(requestEntity.getTransType());
            reportRequest.EDCType = reportRequest.ParseEDCType(requestEntity.getEdcType());
            reportRequest.CardType = reportRequest.ParseCardType(requestEntity.getCardType());
            posLink.ReportRequest = reportRequest;
            ProcessTransResult processTransResult = posLink.ProcessTrans();
            if (processTransResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                response.setCode(BaseResponse.CODE_OK);
                response.setMessage(BaseResponse.MESSAGE_OK);
                ReportResponseEntity.Data data = new ReportResponseEntity.Data();
                ReportResponse reportResponse = posLink.ReportResponse;
                data.setResultCode(posLink.ReportResponse.ResultCode);
                data.setResultTxt(posLink.ReportResponse.ResultTxt);
                data.setCreditCount(posLink.ReportResponse.CreditCount);
                data.setCreditAmount(reportResponse.CreditAmount);
                data.setDebitCount(reportResponse.DebitCount);
                data.setDebitAmount(reportResponse.DebitAmount);
                data.setEBTCount(reportResponse.EBTCount);
                data.setEBTAmount(reportResponse.EBTAmount);
                data.setGiftCount(reportResponse.GiftCount);
                data.setGiftAmount(reportResponse.GiftAmount);
                data.setLoyaltyCount(reportResponse.LoyaltyCount);
                data.setLoyaltyAmount(reportResponse.LoyaltyAmount);
                data.setCashCount(reportResponse.CashCount);
                data.setCashAmount(reportResponse.CashAmount);
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
