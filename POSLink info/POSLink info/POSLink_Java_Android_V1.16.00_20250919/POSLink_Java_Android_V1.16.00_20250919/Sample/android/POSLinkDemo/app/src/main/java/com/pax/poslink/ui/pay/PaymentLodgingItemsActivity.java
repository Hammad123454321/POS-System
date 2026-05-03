package com.pax.poslink.ui.pay;

import android.text.InputType;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.model.payment.PaymentLodgingItemsValueSetter;
import com.pax.poslink.ui.base.BaseListDataActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

/**
 * @author Justin.Z on 2021-3-3
 */
public class PaymentLodgingItemsActivity extends BaseListDataActivity<PaymentRequest.LodgingInfo.LodgingItem> {

    @Override
    protected String getMsgKey() {
        return "Payment_Lodging_Items";
    }

    @Override
    protected String getDisplayKey() {
        return "Payment_Lodging_Items_Display";
    }

    @Override
    protected PaymentRequest.LodgingInfo.LodgingItem getObject() {
        return new PaymentRequest.LodgingInfo.LodgingItem();
    }

    @Override
    protected PaymentRequest.LodgingInfo.LodgingItem getData() {
        com.pax.poslink.PaymentRequest.LodgingInfo.LodgingItem lodgingItem = new PaymentRequest.LodgingInfo.LodgingItem();
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentLodgingItemsValueSetter valueSetter = PaymentLodgingItemsValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(lodgingItem, nameValueStringEntity.getValue());

            }
        }
        return lodgingItem;
    }

    @Override
    protected String formatData(PaymentRequest.LodgingInfo.LodgingItem lodgingItem) {
        String retBuf = "";
        String ch = ",";
        retBuf += lodgingItem.ItemType;
        retBuf += ch;
        retBuf += lodgingItem.ItemAmount;
        retBuf += ch;
        retBuf += lodgingItem.ItemCode;
        return retBuf;
    }

    @Override
    protected void initList() {
        super.initList();
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingItemsValueSetter.ITEMTYPE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingItemsValueSetter.ITEMAMOUNT, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingItemsValueSetter.ITEMCODE, "", InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }
}
