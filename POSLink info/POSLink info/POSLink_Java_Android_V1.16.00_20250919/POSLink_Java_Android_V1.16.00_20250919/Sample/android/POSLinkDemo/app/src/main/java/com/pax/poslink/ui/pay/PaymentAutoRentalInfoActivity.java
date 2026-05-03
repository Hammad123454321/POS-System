package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.model.payment.PaymentAutoRentalInfoValueSetter;
import com.pax.poslink.model.payment.PaymentLodgingInfoValueSetter;
import com.pax.poslink.ui.BaseDataActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.NameValueStringEntity;

import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_AUTO_RENTAL;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_AUTO_RENTAL_ITEMS_DISPLAY;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_LODGING_ITEMS_DISPLAY;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_LODGING_ROOMS_DISPLAY;

/**
 * @author Justin.Z on 2021-7-14
 */
public class PaymentAutoRentalInfoActivity extends BaseDataActivity<PaymentRequest.AutoRentalInfo> {

    private String extraChargeItemsJson;
    private String extraChargeItemsDisplay;
    private ButtonEntity extraChargeItemsEntity;

    @Override
    protected String getMsgKey() {
        return BUNDLE_KEY_PAYMENT_AUTO_RENTAL;
    }

    @Override
    protected PaymentRequest.AutoRentalInfo getObject() {
        return new PaymentRequest.AutoRentalInfo();
    }

    @Override
    protected void getData() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentAutoRentalInfoValueSetter valueSetter = PaymentAutoRentalInfoValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (PaymentAutoRentalInfoValueSetter.EXTRACHARGEITEMS.equals(nameValueStringEntity.getName())) {
                    valueSetter.onSet((PaymentRequest.AutoRentalInfo) object, extraChargeItemsJson);
                } else {
                    valueSetter.onSet((PaymentRequest.AutoRentalInfo) object, nameValueStringEntity.getValue());
                }

            }
        }
    }

    @Override
    protected void extDataGet(Bundle bundle) {
        super.extDataGet(bundle);
        extraChargeItemsDisplay = bundle.getString(BUNDLE_KEY_PAYMENT_AUTO_RENTAL_ITEMS_DISPLAY);
    }

    @Override
    protected void extDataSave(Intent intent) {
        super.extDataSave(intent);
        intent.putExtra(BUNDLE_KEY_PAYMENT_AUTO_RENTAL_ITEMS_DISPLAY, extraChargeItemsDisplay);
    }

    @Override
    protected void dataClear() {
        super.dataClear();
        extraChargeItemsDisplay = "";
    }


    @Override
    protected void initList() {
        super.initList();

        extraChargeItemsJson = gson.toJson(object.ExtraChargeItems);

        renderEntityList.clear();
        container.removeAllViews();
        PaymentRequest.AutoRentalInfo autoRentalInfo = (PaymentRequest.AutoRentalInfo) object;

        extraChargeItemsEntity = new ButtonEntity(PaymentAutoRentalInfoValueSetter.EXTRACHARGEITEMS, extraChargeItemsDisplay, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        extraChargeItemsJson = data;
                        UIBusiness.notifyDataSetChangeForContainer(renderEntityList, container);
                    }
                });

                Intent intent = new Intent(PaymentAutoRentalInfoActivity.this, PaymentAutoRentalItemActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Payment_Extra_Charge_Items", extraChargeItemsJson);
                bundle.putString("Payment_Extra_Charge_Items_Display", extraChargeItemsDisplay);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.PAYMENT_AUTO_RENTAL_ITEMS_RESULT);
            }
        });

        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.AGREEMENTNUMBER, autoRentalInfo.AgreementNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.DAILYRATE, autoRentalInfo.DailyRate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RENTALDURATION, autoRentalInfo.RentalDuration, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.INSURANCEAMOUNT, autoRentalInfo.InsuranceAmount, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.ALLOCATEDMILES, autoRentalInfo.AllocatedMiles, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.MILERATE, autoRentalInfo.MileRate, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.NAME, autoRentalInfo.Name, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.DRIVERLICENSENUMBER, autoRentalInfo.DriverLicenseNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RENTALPROGRAMTYPE, autoRentalInfo.RentalProgramType, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.PICKUPLOCATIONNAME, autoRentalInfo.PickupLocationName, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.PICKUPCITY, autoRentalInfo.PickupCity, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.PICKUPSTATE, autoRentalInfo.PickupState, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.PICKUPCOUNTRYCODE, autoRentalInfo.PickupCountryCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.PICKUPDATETIME, autoRentalInfo.PickupDatetime, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RETURNLOCATION, autoRentalInfo.ReturnLocation, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RETURNCITY, autoRentalInfo.ReturnCity, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RETURNSTATE, autoRentalInfo.ReturnState, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RETURNCOUNTRYCODE, autoRentalInfo.ReturnCountryCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.RETURNDATETIME, autoRentalInfo.ReturnDatetime, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.TOTALMILES, autoRentalInfo.TotalMiles, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.CUSTOMERTAXID, autoRentalInfo.CustomerTaxID, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.VEHICLECLASSID, autoRentalInfo.VehicleClassID, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(extraChargeItemsEntity);
        renderEntityList.add(new NameValueStringEntity(PaymentAutoRentalInfoValueSetter.EXTRACHARGESAMOUNT, autoRentalInfo.ExtraChargesAmount, InputType.TYPE_CLASS_TEXT, ""));

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
            case Constant.PAYMENT_AUTO_RENTAL_ITEMS_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        extraChargeItemsDisplay = data.getStringExtra("Payment_Extra_Charge_Items_Display");
                        extraChargeItemsEntity.setValue(extraChargeItemsDisplay);
                        onActivityResultReceive(data.getStringExtra("Payment_Extra_Charge_Items"));
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
