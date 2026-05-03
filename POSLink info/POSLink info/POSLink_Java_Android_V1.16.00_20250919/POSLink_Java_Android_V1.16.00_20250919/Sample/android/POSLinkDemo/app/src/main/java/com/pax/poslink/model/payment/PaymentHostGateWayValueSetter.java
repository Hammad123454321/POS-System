package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-5-25
 */
public interface PaymentHostGateWayValueSetter {

    String HREF = "HRef";
    String GATEWAYID = "GatewayId";
    String TOKENREQUEST = "TokenRequestFlag";
    String TOKEN = "Token";
    String CARDTYPE = "CardType";
    String PASSTHRUDATA = "PassThruData";
    String RETURNREASON = "ReturnReason";
    String STATIONNO = "StationId";
    String GLOBALUID = "GlobalUid";
    String CUSTOMIZEDATA1 = "CUSTOMIZEDATA1";
    String CUSTOMIZEDATA2 = "CUSTOMIZEDATA2";
    String CUSTOMIZEDATA3 = "CUSTOMIZEDATA3";
    String EWICDISCOUNTAMOUNT = "EwicDiscountAmount";
    String TOKENSERIALNUMBER = "TOKENSERIALNUMBER";
    String STATEMENTDESCRIPTOR = "STATEMENTDESCRIPTOR";


    Map<String, PaymentHostGateWayValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentHostGateWayValueSetter>() {
        {
            put(HREF, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.HRef = value;
                }
            });
            put(GATEWAYID, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.GatewayId = value;
                }
            });
            put(TOKENREQUEST, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.TokenRequestFlag = value;
                }
            });
            put(TOKEN, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.Token = value;
                }
            });
            put(CARDTYPE, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.CardType = value;
                }
            });
            put(PASSTHRUDATA, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.PassThruData = value;
                }
            });
            put(RETURNREASON, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.ReturnReason = value;
                }
            });
            put(STATIONNO, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.StationId = value;
                }
            });
            put(GLOBALUID, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.GlobalUid = value;
                }
            });
            put(CUSTOMIZEDATA1, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.CustomizeData1 = value;
                }
            });
            put(CUSTOMIZEDATA2, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.CustomizeData2 = value;
                }
            });
            put(CUSTOMIZEDATA3, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.CustomizeData3 = value;
                }
            });
            put(EWICDISCOUNTAMOUNT, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.EwicDiscountAmount = value;
                }
            });
            put(TOKENSERIALNUMBER, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.TokenSerialNum = value;
                }
            });
            put(STATEMENTDESCRIPTOR, new PaymentHostGateWayValueSetter() {
                @Override
                public void onSet(PaymentRequest.HostGateWay hostGateWay, String value) {
                    hostGateWay.StatementDescriptor = value;
                }
            });
        }
    };


    void onSet(PaymentRequest.HostGateWay hostGateWay, String value);
}
