package com.pax.poslink.model.payment;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin.Z on 2021-7-14
 */
public interface PaymentAutoRentalInfoValueSetter {

    String AGREEMENTNUMBER = "AgreementNumber";
    String DAILYRATE = "DailyRate";
    String RENTALDURATION = "RentalDuration";
    String INSURANCEAMOUNT = "InsuranceAmount";
    String ALLOCATEDMILES = "AllocatedMiles";
    String MILERATE = "MileRate";
    String NAME = "Name";
    String DRIVERLICENSENUMBER = "DriverLicenseNumber";
    String RENTALPROGRAMTYPE = "RentalProgramType";
    String PICKUPLOCATIONNAME = "PickupLocationName";
    String PICKUPCITY = "PickupCity";
    String PICKUPSTATE = "PickupState";
    String PICKUPCOUNTRYCODE = "PickupCountryCode";
    String PICKUPDATETIME = "PickupDatetime";
    String RETURNLOCATION = "ReturnLocation";
    String RETURNCITY = "ReturnCity";
    String RETURNSTATE = "ReturnState";
    String RETURNCOUNTRYCODE = "ReturnCountryCode";
    String RETURNDATETIME = "ReturnDatetime";
    String TOTALMILES = "TotalMiles";
    String CUSTOMERTAXID = "CustomerTaxID";
    String VEHICLECLASSID = "VehicleClassID";
    String EXTRACHARGEITEMS = "ExtraChargeItems";
    String EXTRACHARGESAMOUNT = "ExtraChargesAmount";

    Map<String, PaymentAutoRentalInfoValueSetter> VALUE_SETTER_MAP = new HashMap() {
        {
            put(AGREEMENTNUMBER, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.AgreementNumber = value;
                }
            });
            put(DAILYRATE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.DailyRate = value;
                }
            });
            put(RENTALDURATION, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.RentalDuration = value;
                }
            });

            put(INSURANCEAMOUNT, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.InsuranceAmount = value;
                }
            });

            put(ALLOCATEDMILES, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.AllocatedMiles = value;
                }
            });
            put(MILERATE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.MileRate = value;
                }
            });
            put(NAME, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.Name = value;
                }
            });
            put(DRIVERLICENSENUMBER, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.DriverLicenseNumber = value;
                }
            });
            put(RENTALPROGRAMTYPE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.RentalProgramType = value;
                }
            });
            put(PICKUPLOCATIONNAME, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.PickupLocationName = value;
                }
            });
            put(PICKUPCITY, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.PickupCity = value;
                }
            });
            put(PICKUPSTATE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.PickupState = value;
                }
            });
            put(PICKUPCOUNTRYCODE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.PickupCountryCode = value;
                }
            });
            put(PICKUPDATETIME, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.PickupDatetime = value;
                }
            });
            put(RETURNLOCATION, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.ReturnLocation = value;
                }
            });
            put(RETURNCITY, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.ReturnCity = value;
                }
            });
            put(RETURNSTATE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.ReturnState = value;
                }
            });
            put(RETURNCOUNTRYCODE, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.ReturnCountryCode = value;
                }
            });
            put(RETURNDATETIME, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.ReturnDatetime = value;
                }
            });
            put(TOTALMILES, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.TotalMiles = value;
                }
            });
            put(CUSTOMERTAXID, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.CustomerTaxID = value;
                }
            });
            put(VEHICLECLASSID, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.VehicleClassID = value;
                }
            });
            put(EXTRACHARGEITEMS, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        autoRentalInfo.ExtraChargeItems = gson.fromJson(value, new TypeToken<List<PaymentRequest.AutoRentalInfo.ExtraChargeItem>>(){}.getType());
                    }
                }
            });
            put(EXTRACHARGESAMOUNT, new PaymentAutoRentalInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value) {
                    autoRentalInfo.ExtraChargesAmount = value;
                }
            });
        }
    };

    void onSet(PaymentRequest.AutoRentalInfo autoRentalInfo, String value);
}
