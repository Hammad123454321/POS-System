package com.poslink.sample.payment;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.poslink.sample.common.model.BaseResponse;
import com.poslink.sample.main.MainController;
import com.poslink.sample.setting.model.CommSettingDao;
import com.poslink.sample.payment.model.PaymentRequestEntity;
import com.poslink.sample.payment.model.PaymentResponseEntity;


/**
 * Created by Leon.F on 2018/2/7.
 */

public class PaymentController {

    public PaymentResponseEntity startPayment(PaymentRequestEntity requestEntity) {
        PaymentResponseEntity response = new PaymentResponseEntity();
        if (requestEntity != null) {
//            PosLink posLink = new PosLink();
            PosLink posLink = MainController.getInstance().createPosLink();
            posLink.SetCommSetting(CommSettingDao.read());
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType(requestEntity.getEdcType());
            paymentRequest.TransType = paymentRequest.ParseTransType(requestEntity.getTransType());
            paymentRequest.Amount = requestEntity.getAmount();
            paymentRequest.CashBackAmt = requestEntity.getCashBackAmt();
            paymentRequest.ECRRefNum = requestEntity.getECRRefNum();
            paymentRequest.ClerkID = requestEntity.getClerkID();
            paymentRequest.Zip = requestEntity.getZip();
            paymentRequest.TipAmt = requestEntity.getTipAmt();
            paymentRequest.TaxAmt = requestEntity.getTaxAmt();
            paymentRequest.Street = requestEntity.getStreet();
            paymentRequest.Street2 = requestEntity.getStreet2();
            paymentRequest.SurchargeAmt = requestEntity.getSurchargeAmt();
            paymentRequest.PONum = requestEntity.getPONum();
            paymentRequest.OrigECRRefNum = requestEntity.getOrigECRRefNum();
            paymentRequest.OrigRefNum = requestEntity.getOrigRefNum();
            paymentRequest.InvNum = requestEntity.getInvNum();
            paymentRequest.ECRTransID = requestEntity.getECRTransID();
            paymentRequest.AuthCode = requestEntity.getAuthCode();
            paymentRequest.FuelAmt = requestEntity.getFuelAmt();
            paymentRequest.ExtData = requestEntity.getExtData();
            posLink.PaymentRequest = paymentRequest;
            ProcessTransResult processTransResult = posLink.ProcessTrans();
            if (processTransResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                response.setCode(BaseResponse.CODE_OK);
                response.setMessage(BaseResponse.MESSAGE_OK);
                PaymentResponseEntity.Data data = new PaymentResponseEntity.Data();
                PaymentResponse paymentResponse = posLink.PaymentResponse;
                data.setResultCode(paymentResponse.ResultCode);
                data.setResultTxt(paymentResponse.ResultTxt);
                data.setApprovedAmount(paymentResponse.ApprovedAmount);
                data.setAuthCode(paymentResponse.AuthCode);
                data.setAvsResponse(paymentResponse.AvsResponse);
                data.setBogusAccountNum(paymentResponse.BogusAccountNum);
                data.setCardType(paymentResponse.CardType);
                data.setCvResponse(paymentResponse.CvResponse);
                data.setExtData(paymentResponse.ExtData);
                data.setExtraBalance(paymentResponse.ExtraBalance);
                data.setHostResponse(paymentResponse.HostResponse);
                data.setHostCode(paymentResponse.HostCode);
                data.setMessage(paymentResponse.Message);
                data.setRawResponse(paymentResponse.RawResponse);
                data.setRefNum(paymentResponse.RefNum);
                data.setRemainingBalance(paymentResponse.RemainingBalance);
                data.setRequestedAmount(paymentResponse.RequestedAmount);
                data.setSigFileName(paymentResponse.SigFileName);
                data.setSignData(paymentResponse.SignData);
                data.setTimestamp(paymentResponse.Timestamp);
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
