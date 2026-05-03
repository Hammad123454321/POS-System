package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-5-25
 */
public interface PaymentTransactionBehaviorValueSetter {

    String SIGN = "SignatureCaptureFlag";
    String TIPREQ = "TipRequestFlag";
    String SIGNUPLOAD = "SignatureUploadFlag";
    String REPORTSTATUS = "StatusReportFlag";
    String CARDTYPEBITMAP = "AcceptedCardType";
    String DISPROGPROMPTS = "ProgramPromptsFlag";
    String GETSIGN = "SignatureAcquireFlag";
    String ENTRYMODEBITMAP = "EntryMode";
    String RECEIPTPRINT = "ReceiptPrintFlag";
    String CPMODE = "CardPresentMode";
    String AMOUNTLINE = "AmountLine";
    String DEBITNETWORK = "DebitNetwork";
    String USERLANGUAGE = "UserLanguage";
    String ADDLRSPDATAREQUEST = "AddlRspDataFlag";
    String FORCECC = "ForceCC";
    String FORCEFSA = "FORCEFSA";
    String FORCE = "ForceDuplicate";
    String LASTTRANSACTION = "LastTransactionRetriveFlag";
    String ACCESSIBILITYPINPAD = "AccessibilityPinPad";
    String DISTRANSPROMPTS = "DistransPrompts";
    String COFINDICATOR = "CoFIndicator";
    String COFINITIATOR = "CoFInitiator";
    String GIFTCARDINDICATOR = "GiftCardIndicator";

    Map<String, PaymentTransactionBehaviorValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentTransactionBehaviorValueSetter>() {
        {
            put(SIGN, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.SignatureCaptureFlag = value;
                }
            });
            put(TIPREQ, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.TipRequestFlag = value;
                }
            });
            put(SIGNUPLOAD, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.SignatureUploadFlag = value;
                }
            });
            put(REPORTSTATUS, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.StatusReportFlag = value;
                }
            });
            put(CARDTYPEBITMAP, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.AcceptedCardType = value;
                }
            });
            put(DISPROGPROMPTS, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.ProgramPromptsFlag = value;
                }
            });
            put(GETSIGN, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.SignatureAcquireFlag = value;
                }
            });
            put(ENTRYMODEBITMAP, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.EntryMode = value;
                }
            });
            put(RECEIPTPRINT, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.ReceiptPrintFlag = value;
                }
            });
            put(CPMODE, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.CardPresentMode = value;
                }
            });
            put(DEBITNETWORK, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.DebitNetwork = value;
                }
            });
            put(USERLANGUAGE, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.UserLanguage = value;
                }
            });
            put(ADDLRSPDATAREQUEST, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.AddlRspDataFlag = value;
                }
            });
            put(FORCECC, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.ForceCC = value;
                }
            });
            put(FORCEFSA, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.ForceFsa = value;
                }
            });
            put(FORCE, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.ForceDuplicate = value;
                }
            });

            put(ACCESSIBILITYPINPAD, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.AccessibilityPinPad = value;
                }
            });
            put(DISTRANSPROMPTS, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.DistransPrompts = value;
                }
            });
            put(COFINDICATOR, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.CoFIndicator = value;
                }
            });
            put(COFINITIATOR, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.CoFInitiator = value;
                }
            });
            put(GIFTCARDINDICATOR, new PaymentTransactionBehaviorValueSetter() {
                @Override
                public void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value) {
                    transactionBehavior.GiftCardIndicator = value;
                }
            });
        }
    };

    void onSet(PaymentRequest.TransactionBehavior transactionBehavior, String value);
}
