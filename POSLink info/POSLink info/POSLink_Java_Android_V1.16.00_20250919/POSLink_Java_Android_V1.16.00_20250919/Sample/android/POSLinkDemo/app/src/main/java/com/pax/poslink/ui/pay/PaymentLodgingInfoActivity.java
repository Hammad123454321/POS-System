package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.model.payment.PaymentLodgingInfoValueSetter;
import com.pax.poslink.ui.BaseDataActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.HashMap;
import java.util.Map;

import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_LODGING;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_LODGING_ITEMS_DISPLAY;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_LODGING_ROOMS_DISPLAY;

/**
 * @author Justin.Z on 2021-3-2
 */
public class PaymentLodgingInfoActivity extends BaseDataActivity<PaymentRequest.LodgingInfo> {

    private String roomRatesJson, lodgingItemsJson;
    private ButtonEntity roomRatesDataEntity, lodgingItemsDataEntity;
    private String roomRatesDisplay, lodgingItemsDisplay;


    private final Map<String, String> ENTITY_VALUE = new HashMap<String, String>() {
        {
            put(PaymentLodgingInfoValueSetter.ROOMRATES, roomRatesJson);
            put(PaymentLodgingInfoValueSetter.LODGINGITEMS, lodgingItemsJson);
        }
    };


    @Override
    protected String getMsgKey() {
        return BUNDLE_KEY_PAYMENT_LODGING;
    }

    @Override
    protected PaymentRequest.LodgingInfo getObject() {
        return new PaymentRequest.LodgingInfo();
    }

    @Override
    protected void getData() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentLodgingInfoValueSetter valueSetter = PaymentLodgingInfoValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (ENTITY_VALUE.containsKey(nameValueStringEntity.getName())) {
                    valueSetter.onSet((PaymentRequest.LodgingInfo) object, ENTITY_VALUE.get(nameValueStringEntity.getName()));
                } else {
                    valueSetter.onSet((PaymentRequest.LodgingInfo) object, nameValueStringEntity.getValue());
                }

            }
        }
    }

    @Override
    protected void extDataGet(Bundle bundle) {
        super.extDataGet(bundle);
        roomRatesDisplay = bundle.getString(BUNDLE_KEY_PAYMENT_LODGING_ROOMS_DISPLAY);
        lodgingItemsDisplay = bundle.getString(BUNDLE_KEY_PAYMENT_LODGING_ITEMS_DISPLAY);
    }

    @Override
    protected void extDataSave(Intent intent) {
        super.extDataSave(intent);
        intent.putExtra(BUNDLE_KEY_PAYMENT_LODGING_ROOMS_DISPLAY, roomRatesDisplay);
        intent.putExtra(BUNDLE_KEY_PAYMENT_LODGING_ITEMS_DISPLAY, lodgingItemsDisplay);
    }

    @Override
    protected void dataClear() {
        super.dataClear();
        roomRatesDisplay = "";
        lodgingItemsDisplay = "";
    }

    @Override
    protected void initList() {
        super.initList();

        roomRatesJson = gson.toJson(object.RoomRates);
        lodgingItemsJson = gson.toJson(object.LodgingItems);

        renderEntityList.clear();
        container.removeAllViews();
        PaymentRequest.LodgingInfo lodgingInfo = (PaymentRequest.LodgingInfo) object;

        roomRatesDataEntity = new ButtonEntity(PaymentLodgingInfoValueSetter.ROOMRATES, roomRatesDisplay, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "",
                new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        roomRatesJson = data;
                        ENTITY_VALUE.put(PaymentLodgingInfoValueSetter.ROOMRATES, roomRatesJson);
                        UIBusiness.notifyDataSetChangeForContainer(renderEntityList, container);
                    }
                });

                Intent intent = new Intent(PaymentLodgingInfoActivity.this, PaymentLodgingRoomRatesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Payment_Lodging_RoomRates", roomRatesJson);
                bundle.putString("Payment_Lodging_RoomRates_Display", roomRatesDisplay);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_LODGING_ROOMS_RESULT);
            }
        });

        lodgingItemsDataEntity = new ButtonEntity(PaymentLodgingInfoValueSetter.LODGINGITEMS, lodgingItemsDisplay, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        lodgingItemsJson = data;
                        ENTITY_VALUE.put(PaymentLodgingInfoValueSetter.LODGINGITEMS, lodgingItemsJson);
                        UIBusiness.notifyDataSetChangeForContainer(renderEntityList, container);
                    }
                });

                Intent intent = new Intent(PaymentLodgingInfoActivity.this, PaymentLodgingItemsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Payment_Lodging_Items", lodgingItemsJson);
                bundle.putString("Payment_Lodging_Items_Display", lodgingItemsDisplay);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_LODGING_ITEMS_RESULT);
            }
        });

        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.ROOMNUMBER, lodgingInfo.RoomNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.FOLIONUMBER, lodgingInfo.FolioNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(roomRatesDataEntity);
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.CHARGETYPE, lodgingInfo.ChargeType, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.NOSHOWFLAG, lodgingInfo.NoShowFlag, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.CHECKINDATE, lodgingInfo.CheckInDate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.CHECKOUTDATE, lodgingInfo.CheckOutDate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.SPECIALPROGRAMCODE, lodgingInfo.SpecialProgramCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentLodgingInfoValueSetter.DEPARTUREADJUSTEDAMOUNT, lodgingInfo.DepartureAdjustedAmount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(lodgingItemsDataEntity);

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.PAYMENT_LODGING_ROOMS_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        roomRatesDisplay = data.getStringExtra("Payment_Lodging_RoomRates_Display");
                        roomRatesDataEntity.setValue(roomRatesDisplay);
                        onActivityResultReceive(data.getStringExtra("Payment_Lodging_RoomRates"));
                        break;
                    default:
                        break;
                }
                break;
            case Constant.PAYMENT_LODGING_ITEMS_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        lodgingItemsDisplay = data.getStringExtra("Payment_Lodging_Items_Display");
                        lodgingItemsDataEntity.setValue(lodgingItemsDisplay);
                        onActivityResultReceive(data.getStringExtra("Payment_Lodging_Items"));
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
