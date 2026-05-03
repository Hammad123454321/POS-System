package com.pax.poslink.ui.pay;

import android.text.InputType;

import com.pax.poslink.entity.FleetCardRequest;
import com.pax.poslink.model.payment.PaymentFleetCardValueSetter;
import com.pax.poslink.ui.BaseDataActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_FLEET_CARD;

/**
 * Created by Justin.Z on 2020-5-25
 */
public class PaymentFleetCardActivity extends BaseDataActivity<FleetCardRequest> {


    @Override
    protected String getMsgKey() {
        return BUNDLE_KEY_PAYMENT_FLEET_CARD;
    }

    @Override
    protected FleetCardRequest getObject() {
        return new FleetCardRequest();
    }

    @Override
    protected void getData() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentFleetCardValueSetter valueSetter = PaymentFleetCardValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet((FleetCardRequest) object, nameValueStringEntity.getValue());

            }
        }
    }

    @Override
    protected void initList() {
        super.initList();

        renderEntityList.clear();
        container.removeAllViews();

        FleetCardRequest fleetCard = (FleetCardRequest) object;
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.ODOMETER, fleetCard.Odometer, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.VEHICLENUMBER, fleetCard.VehicleNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.JOBNUMBER, fleetCard.JobNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.DRIVERID, fleetCard.DriverId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.EMPLOYEENUMBER, fleetCard.EmployeeNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.LICENSENUMBER, fleetCard.LicenseNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.JOBID, fleetCard.JobId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.DEPARTMENTNUMBER, fleetCard.DepartmentNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.CUSTOMERDATA, fleetCard.CustomerData, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.USERID, fleetCard.UserId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.VEHICLEID, fleetCard.VehicleId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.FLEETPROMTPCODE, fleetCard.FleetPromptCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.VEHICLEUSAGECODE, fleetCard.VehicleUsageCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.HUBOMETER, fleetCard.Hubometer, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.MAINTENANCEID, fleetCard.MainTenanceId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.FLEETPONUMBER, fleetCard.FleetPoNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.REEFERHOURS, fleetCard.ReeferHours, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.RESTRICTIONCODE, fleetCard.RestrictionCode, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.TRAILERID, fleetCard.TrailerId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.TRIPNUMBER, fleetCard.TripNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.UNITID, fleetCard.UnitId, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.ADDITIONALFLEETDATA1, fleetCard.AdditionalFleetData1, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentFleetCardValueSetter.ADDITIONALFLEETDATA2, fleetCard.AdditionalFleetData2, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }
}
