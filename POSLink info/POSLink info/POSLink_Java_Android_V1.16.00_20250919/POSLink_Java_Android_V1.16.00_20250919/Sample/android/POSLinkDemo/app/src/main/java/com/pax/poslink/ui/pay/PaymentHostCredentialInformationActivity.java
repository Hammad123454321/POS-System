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
package com.pax.poslink.ui.pay;

import android.text.InputType;

import com.pax.poslink.PaymentRequest;
import com.pax.poslink.model.payment.PaymentHostCredentialInformationSetter;
import com.pax.poslink.ui.BaseDataActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_HOSTCREDENTIALINFORMATION;

/**
 * @author Yolanda.Z on 2021/12/10
 */
public class PaymentHostCredentialInformationActivity extends BaseDataActivity<PaymentRequest.HostCredentialInformation> {
    @Override
    protected String getMsgKey() {
        return BUNDLE_KEY_PAYMENT_HOSTCREDENTIALINFORMATION;
    }

    @Override
    protected PaymentRequest.HostCredentialInformation getObject() {
        return new PaymentRequest.HostCredentialInformation();
    }

    @Override
    protected void getData() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentHostCredentialInformationSetter valueSetter = PaymentHostCredentialInformationSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet((PaymentRequest.HostCredentialInformation) object, nameValueStringEntity.getValue());
            }
        }
    }

    @Override
    protected void initList() {
        super.initList();

        renderEntityList.clear();
        container.removeAllViews();
        PaymentRequest.HostCredentialInformation hostCredentialInformation = object;
        renderEntityList.add(new NameValueStringEntity(PaymentHostCredentialInformationSetter.MID, hostCredentialInformation.MID, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostCredentialInformationSetter.SERVICEUSER, hostCredentialInformation.ServiceUser, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostCredentialInformationSetter.SERVICEPASSWORD, hostCredentialInformation.ServicePassword, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostCredentialInformationSetter.USERNAME, hostCredentialInformation.UserName, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(PaymentHostCredentialInformationSetter.PASSWORD, hostCredentialInformation.Password, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }
}
