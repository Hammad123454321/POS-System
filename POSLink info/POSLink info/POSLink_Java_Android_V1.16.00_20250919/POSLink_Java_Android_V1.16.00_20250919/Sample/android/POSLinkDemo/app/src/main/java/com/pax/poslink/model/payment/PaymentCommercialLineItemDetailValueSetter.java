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
public interface PaymentCommercialLineItemDetailValueSetter {

    String ITEM_SEQUENCE_NUMBER = "Item Sequence Number";
    String PRODUCT_CODE = "Product Code";
    String ITEM_COMMODITY_CODE = "Item Commodity Code";
    String ITEM_DESCRIPTION = "Item Description";
    String ITEM_QUANTITY = "Item Quantity";
    String ITEM_MEASUREMENT_UNIT = "Item Measurement Unit";
    String ITEM_UNIT_PRICE = "Item Unit Price";
    String ITEM_DISCOUNT_AMOUNT = "Item Discount Amount";
    String ITEM_DISCOUNT_RATE = "Item Discount Rate";
    String TAX_DETAIL = "Tax Detail";
    String LINE_ITEM_TOTAL = "LineItem Total";

    Map<String, PaymentCommercialLineItemDetailValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentCommercialLineItemDetailValueSetter>() {
        {
            put(ITEM_SEQUENCE_NUMBER, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemSequenceNumber = value;
                }
            });
            put(PRODUCT_CODE, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ProductCode = value;
                }
            });
            put(ITEM_COMMODITY_CODE, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemCommodityCode = value;
                }
            });
            put(ITEM_DESCRIPTION, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemDescription = value;
                }
            });
            put(ITEM_QUANTITY, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemQuantity = value;
                }
            });
            put(ITEM_MEASUREMENT_UNIT, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemMeasurementUnit = value;
                }
            });
            put(ITEM_UNIT_PRICE, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemUnitPrice = value;
                }
            });
            put(ITEM_DISCOUNT_AMOUNT, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemDiscountAmount = value;
                }
            });
            put(ITEM_DISCOUNT_RATE, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.ItemDiscountRate = value;
                }
            });
            put(TAX_DETAIL, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
//                    lineItemDetail.ItemDiscountRate = value;
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        lineItemDetail.TaxDetails = gson.fromJson(value,
                                new TypeToken<List<PaymentRequest.CommercialCard.TaxDetail>>(){}.getType());
                    }
                }
            });
            put(LINE_ITEM_TOTAL, new PaymentCommercialLineItemDetailValueSetter() {
                @Override
                public void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value) {
                    lineItemDetail.LineItemTotal = value;
                }
            });
        }
    };

    void onSet(PaymentRequest.CommercialCard.LineItemDetail lineItemDetail, String value);
}
