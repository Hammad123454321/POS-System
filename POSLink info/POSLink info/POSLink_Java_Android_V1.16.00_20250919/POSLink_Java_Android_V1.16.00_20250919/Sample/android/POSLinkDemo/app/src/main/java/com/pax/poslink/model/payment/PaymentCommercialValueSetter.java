package com.pax.poslink.model.payment;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin.Z
 */
public interface PaymentCommercialValueSetter {

    String PO_NUMBER = "PO Number";
    String CUSTOMER_CODE = "Customer Code";
    String TAX_EXEMPT = "Tax Exempt";
    String TAX_EXEMPT_ID = "Tax Exempt ID";
    String MERCHANT_TAX_ID = "Merchant Tax ID";
    String DESTINATION_ZIP_CODE = "Destination Zip Code";
    String PRODUCT_DESCRIPTION = "Product Description";
    String SHIP_FROM_ZIP_CODE = "Ship From Zip Code";
    String DESTINATION_COUNTRY_CODE = "Destination Country Code";
    String TAX_DETAIL = "TaxDetail";
    String SUMMARY_COMMODITY_CODE = "Summary Commodity Code";
    String DISCOUNT_AMOUNT = "Discount Amount";
    String FREIGHT_AMOUNT = "Freight Amount";
    String DUTY_AMOUNT = "Duty Amount";
    String ORDER_DATE = "Order Date";
    String LINEITEMDETAIL = "LineItemDetail";
    String SHIPPING_COMPANY = "Shipping Company";
    String SHIPPING_TRACKING_NUMBER = "Shipping Tracking Number";
    String ADDITIONAL_FEES = "Additional Fees";

    Map<String, PaymentCommercialValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentCommercialValueSetter>() {
        {
            put(PO_NUMBER, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.PONumber = value;
                }
            });
            put(CUSTOMER_CODE, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.CustomerCode = value;
                }
            });
            put(TAX_EXEMPT, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.TaxExempt = value;
                }
            });
            put(TAX_EXEMPT_ID, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.TaxExemptID = value;
                }
            });
            put(MERCHANT_TAX_ID, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.MerchantTaxID = value;
                }
            });
            put(DESTINATION_ZIP_CODE, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.DestinationZipCode = value;
                }
            });
            put(PRODUCT_DESCRIPTION, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.ProductDescription = value;
                }
            });
            put(SHIP_FROM_ZIP_CODE, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.ShipFromZipCode = value;
                }
            });
            put(DESTINATION_COUNTRY_CODE, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.DestinationCountryCode = value;
                }
            });
            put(TAX_DETAIL, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
//                    commercialCard.PONumber = value;
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        commercialCard.TaxDetails = gson.fromJson(value,
                                new TypeToken<List<PaymentRequest.CommercialCard.TaxDetail>>(){}.getType());
                    }
                }
            });
            put(SUMMARY_COMMODITY_CODE, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.SummaryCommodityCode = value;
                }
            });
            put(DISCOUNT_AMOUNT, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.DiscountAmount = value;
                }
            });
            put(FREIGHT_AMOUNT, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.FreightAmount = value;
                }
            });
            put(DUTY_AMOUNT, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.DutyAmount = value;
                }
            });
            put(ORDER_DATE, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.OrderDate = value;
                }
            });
            put(LINEITEMDETAIL, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
//                    commercialCard.DutyAmount = value;
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        commercialCard.LineItemDetails = gson.fromJson(value,
                                new TypeToken<List<PaymentRequest.CommercialCard.LineItemDetail>>(){}.getType());
                    }
                }
            });
            put(SHIPPING_COMPANY, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.ShippingCompany = value;
                }
            });
            put(SHIPPING_TRACKING_NUMBER, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.ShippingTrackingNumber = value;
                }
            });
            put(ADDITIONAL_FEES, new PaymentCommercialValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard commercialCard, String value) {
                    commercialCard.AdditionalFees = value;
                }
            });
        }};

    void onSet(PaymentRequest.CommercialCard commercialCard, String value);
}
