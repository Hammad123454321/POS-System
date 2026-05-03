/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *
 * Module Date: 2021/12/10
 * Module Auth: Yolanda.Z
 * Description:
 *
 * Revision History:
 * Date                   Author                       Action
 * 2021/12/10            Yolanda.Z                       Create
 * ============================================================================
 */
package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yolanda.Z on 2021/12/10
 */
public interface PaymentHostCredentialInformationSetter {
    String MID = "MID";
    String SERVICEUSER = "Service User";
    String SERVICEPASSWORD = "Service Password";
    String USERNAME = "User Name";
    String PASSWORD = "Password";

    Map<String, PaymentHostCredentialInformationSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentHostCredentialInformationSetter>() {
        {
            put(MID, new PaymentHostCredentialInformationSetter() {
                @Override
                public void onSet(PaymentRequest.HostCredentialInformation hostCredentialInformation, String value) {
                    hostCredentialInformation.MID = value;
                }
            });
            put(SERVICEUSER, new PaymentHostCredentialInformationSetter() {

                @Override
                public void onSet(PaymentRequest.HostCredentialInformation hostCredentialInformation, String value) {
                    hostCredentialInformation.ServiceUser = value;
                }
            });
            put(SERVICEPASSWORD, new PaymentHostCredentialInformationSetter() {

                @Override
                public void onSet(PaymentRequest.HostCredentialInformation hostCredentialInformation, String value) {
                    hostCredentialInformation.ServicePassword = value;
                }
            });
            put(USERNAME, new PaymentHostCredentialInformationSetter() {

                @Override
                public void onSet(PaymentRequest.HostCredentialInformation hostCredentialInformation, String value) {
                    hostCredentialInformation.UserName = value;
                }
            });
            put(PASSWORD, new PaymentHostCredentialInformationSetter() {

                @Override
                public void onSet(PaymentRequest.HostCredentialInformation hostCredentialInformation, String value) {
                    hostCredentialInformation.Password = value;
                }
            });
        }
    };

    void onSet(PaymentRequest.HostCredentialInformation hostCredentialInformation, String value);

}
