package com.pax.poslink.ui.pay;

import android.text.InputType;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.model.payment.PaymentAutoRentalItemValueSetter;
import com.pax.poslink.ui.base.BaseListDataActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

/**
 * @author Justin.Z on 2021-7-14
 */
public class PaymentAutoRentalItemActivity extends BaseListDataActivity<PaymentRequest.AutoRentalInfo.ExtraChargeItem> {

    @Override
    protected String getMsgKey() {
        return "Payment_Extra_Charge_Items";
    }

    @Override
    protected String getDisplayKey() {
        return "Payment_Extra_Charge_Items_Display";
    }

    @Override
    protected PaymentRequest.AutoRentalInfo.ExtraChargeItem getObject() {
        return new PaymentRequest.AutoRentalInfo.ExtraChargeItem();
    }

    @Override
    protected PaymentRequest.AutoRentalInfo.ExtraChargeItem getData() {
        PaymentRequest.AutoRentalInfo.ExtraChargeItem extraChargeItem = new PaymentRequest.AutoRentalInfo.ExtraChargeItem();
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentAutoRentalItemValueSetter valueSetter = PaymentAutoRentalItemValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(extraChargeItem, nameValueStringEntity.getValue());

            }
        }
        return extraChargeItem;
    }

    @Override
    protected String formatData(PaymentRequest.AutoRentalInfo.ExtraChargeItem extraChargeItem) {
        String retBuf = "";
        String ch = ",";
        retBuf += extraChargeItem.ItemType;
        retBuf += ch;
        retBuf += extraChargeItem.ExtraChargeAmount;
        return retBuf;
    }

    @Override
    protected void initList() {
        super.initList();
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalItemValueSetter.ITEMTYPE, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalItemValueSetter.EXTRACHARGEAMOUNT, "", InputType.TYPE_CLASS_TEXT, ""));
        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }
}
