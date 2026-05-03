package com.pax.poslink.ui.pay;

import android.text.InputType;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.model.payment.PaymentLodgingRoomRatesValueSetter;
import com.pax.poslink.ui.base.BaseListDataActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

/**
 * @author Justin.Z on 2021-3-2
 */
public class PaymentLodgingRoomRatesActivity extends BaseListDataActivity<PaymentRequest.LodgingInfo.RoomRates> {

    @Override
    protected String getMsgKey() {
        return "Payment_Lodging_RoomRates";
    }

    @Override
    protected String getDisplayKey() {
        return "Payment_Lodging_RoomRates_Display";
    }

    @Override
    protected PaymentRequest.LodgingInfo.RoomRates getObject() {
        return new PaymentRequest.LodgingInfo.RoomRates();
    }

    @Override
    protected PaymentRequest.LodgingInfo.RoomRates getData() {
        com.pax.poslink.PaymentRequest.LodgingInfo.RoomRates roomRates = new PaymentRequest.LodgingInfo.RoomRates();
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentLodgingRoomRatesValueSetter valueSetter = PaymentLodgingRoomRatesValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(roomRates, nameValueStringEntity.getValue());

            }
        }
        return roomRates;
    }

    @Override
    protected String formatData(PaymentRequest.LodgingInfo.RoomRates roomRates) {
        String retBuf = "";
        String ch = ",";
        retBuf += roomRates.StayDuration;
        retBuf += ch;
        retBuf += roomRates.RoomRateAmount;
        retBuf += ch;
        retBuf += roomRates.RoomRateTax;
        return retBuf;
    }

    @Override
    protected void initList() {
        super.initList();

        renderEntityList.add(new NameValueStringEntity(PaymentLodgingRoomRatesValueSetter.STAYDURATION, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingRoomRatesValueSetter.ROOMRATEAMOUNT, "", InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingRoomRatesValueSetter.ROOMRATETAX, "", InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }


    }
}
