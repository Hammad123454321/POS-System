package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin.Z
 */
public interface PaymentCommercialTaxDetailValueSetter {

    String TAX_AMOUNT = "Tax Amount";
    String TAX_RATE = "Tax Rate";
    String MERCHANT_TAX_ID = "Merchant Tax ID";
    String CUSTOMER_TAX_ID = "Customer Tax ID";
    String VAT_INVOICE_NUMBER = "VAT Invoice Number";
    String ALTERNATE_TAX_ID = "Alternate Tax ID";

    Map<String, PaymentCommercialTaxDetailValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentCommercialTaxDetailValueSetter>() {
        {
            put(TAX_AMOUNT, new PaymentCommercialTaxDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value) {
                    taxDetail.TaxAmount = value;
                }
            });
            put(TAX_RATE, new PaymentCommercialTaxDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value) {
                    taxDetail.TaxRate = value;
                }
            });
            put(MERCHANT_TAX_ID, new PaymentCommercialTaxDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value) {
                    taxDetail.MerChantTaxID = value;
                }
            });
            put(CUSTOMER_TAX_ID, new PaymentCommercialTaxDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value) {
                    taxDetail.CustomerTaxID = value;
                }
            });
            put(VAT_INVOICE_NUMBER, new PaymentCommercialTaxDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value) {
                    taxDetail.VATInvoiceNumber = value;
                }
            });
            put(ALTERNATE_TAX_ID, new PaymentCommercialTaxDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value) {
                    taxDetail.AlternateTaxID = value;
                }
            });

        }};

    void onSet(PaymentRequest.CommercialCard.TaxDetail taxDetail, String value);


}
