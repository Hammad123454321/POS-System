package com.pax.poslink.ui;

import android.text.InputType;

import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.model.MultiMerchantValueSetter;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

/**
 * Created by Justin.Z on 2020-5-25
 */
public class MultiMerchantActivity extends BaseDataActivity<MultiMerchant> {

    @Override
    protected String getMsgKey() {
        return Constant.BUNDLE_KEY_PAYMENT_MULTI_MERCHANT;
    }

    @Override
    protected MultiMerchant getObject() {
        return new MultiMerchant();
    }

    @Override
    protected void getData() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                MultiMerchantValueSetter valueSetter = MultiMerchantValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet((MultiMerchant) object, nameValueStringEntity.getValue());

            }
        }
    }

    @Override
    protected void initList() {
        super.initList();

        renderEntityList.clear();
        container.removeAllViews();

        MultiMerchant multiMerchant = (MultiMerchant) object;
        renderEntityList.add(new NameValueStringEntity(MultiMerchantValueSetter.MM_ID, multiMerchant.Id, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(MultiMerchantValueSetter.MM_NAME, multiMerchant.Name, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }
}
