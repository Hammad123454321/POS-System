package com.pax.poslink.model.payment;

import com.pax.poslink.entity.FleetCardRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-5-25
 */
public interface PaymentFleetCardValueSetter {

    String ODOMETER = "Odometer";
    String VEHICLENUMBER = "VehicleNumber";
    String JOBNUMBER = "JobNumber";
    String DRIVERID = "DriverId";
    String EMPLOYEENUMBER = "EmployeeNumber";
    String LICENSENUMBER = "LicenseNumber";
    String JOBID = "JobId";
    String DEPARTMENTNUMBER = "DepartmentNumber";
    String CUSTOMERDATA = "CustomerData";
    String USERID = "UserId";
    String VEHICLEID = "VehicleId";
    String FLEETPROMTPCODE = "FleetPromptCode";
    String VEHICLEUSAGECODE = "VehicleUsageCode";
    String HUBOMETER = "Hubometer";
    String MAINTENANCEID = "MainTenanceId";
    String FLEETPONUMBER = "FleetPoNumber";
    String REEFERHOURS = "ReeferHours";
    String RESTRICTIONCODE = "RestRictionCode";
    String TRAILERID = "TrailerId";
    String TRIPNUMBER = "TripNumber";
    String UNITID = "UnitId";
    String ADDITIONALFLEETDATA1 = "AdditionalFleetData1";
    String ADDITIONALFLEETDATA2 = "AdditionalFleetData2";


    Map<String, PaymentFleetCardValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentFleetCardValueSetter>() {
        {
            put(ODOMETER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.Odometer = value;
                }
            });
            put(VEHICLENUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.VehicleNumber = value;
                }
            });
            put(JOBNUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.JobNumber = value;
                }
            });
            put(DRIVERID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.DriverId = value;
                }
            });
            put(EMPLOYEENUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.EmployeeNumber = value;
                }
            });
            put(LICENSENUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.LicenseNumber = value;
                }
            });
            put(JOBID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.JobId = value;
                }
            });
            put(DEPARTMENTNUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.DepartmentNumber = value;
                }
            });
            put(CUSTOMERDATA, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.CustomerData = value;
                }
            });
            put(USERID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.UserId = value;
                }
            });
            put(VEHICLEID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.VehicleId = value;
                }
            });
            put(FLEETPROMTPCODE, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.FleetPromptCode = value;
                }
            });
            put(VEHICLEUSAGECODE, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.VehicleUsageCode = value;
                }
            });
            put(HUBOMETER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.Hubometer = value;
                }
            });
            put(MAINTENANCEID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.MainTenanceId = value;
                }
            });
            put(FLEETPONUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.FleetPoNumber = value;
                }
            });
            put(REEFERHOURS, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.ReeferHours = value;
                }
            });
            put(RESTRICTIONCODE, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.RestrictionCode = value;
                }
            });
            put(TRAILERID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.TrailerId = value;
                }
            });
            put(TRIPNUMBER, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.TripNumber = value;
                }
            });
            put(UNITID, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.UnitId = value;
                }
            });
            put(ADDITIONALFLEETDATA1, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.AdditionalFleetData1 = value;
                }
            });
            put(ADDITIONALFLEETDATA2, new PaymentFleetCardValueSetter() {
                @Override
                public void onSet(FleetCardRequest fleetCard, String value) {
                    fleetCard.AdditionalFleetData2 = value;
                }
            });
        }
    };

    void onSet(FleetCardRequest fleetCard, String value);

}
