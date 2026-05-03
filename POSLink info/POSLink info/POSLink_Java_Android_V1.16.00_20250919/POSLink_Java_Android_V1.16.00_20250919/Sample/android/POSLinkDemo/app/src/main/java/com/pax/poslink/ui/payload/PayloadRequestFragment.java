package com.pax.poslink.ui.payload;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.pax.poslink.PayloadRequest;
import com.pax.poslink.R;
import com.pax.poslink.model.payload.PayloadValueSetter;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.ui.multicmd.MultiCmdViewModel;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

/**
 * @author Justin.Z on 2021-7-15
 */
public class PayloadRequestFragment extends RequestFragment<PayloadRequest> {

    private PayloadRequest payloadRequest;
    private MultiCmdViewModel model;

    public static PayloadRequestFragment newInstance() {
        return new PayloadRequestFragment();
    }

    public static PayloadRequestFragment newInstance(String processBtnName) {
        PayloadRequestFragment fragment = new PayloadRequestFragment();
        fragment.setProcessBtn(processBtnName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_payload_request;
    }

    @Override
    protected void initView(View view) {
        requestContainer = view.findViewById(R.id.payload_request_container);
        model = new ViewModelProvider(requireActivity()).get(MultiCmdViewModel.class);
        payloadRequest = model.getPayloadRequest();
        showCorrespondingRequestView();
    }

    private void showCorrespondingRequestView() {
        requestRenderEntityList.clear();
        requestContainer.removeAllViews();

        requestRenderEntityList.add(new NameValueStringEntity(PayloadValueSetter.PAYLOAD, payloadRequest.Payload, InputType.TYPE_CLASS_TEXT, ""));

//        List<RenderEntity> commandRenderList = commandMapRenderList.get(command);
//        if (commandRenderList != null) {
//            requestRenderEntityList.addAll(commandRenderList);
//        }

        for (RenderEntity renderEntity : requestRenderEntityList) {
            String name = ((NameValueEntity) renderEntity).getName();
            CommonItemView itemView = renderEntity.createView(requestContainer);
            requestContainer.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public PayloadRequest getRequest() {
        PayloadRequest request = new PayloadRequest();
        setRequest(request);
        return request;
    }

    private void setRequest(PayloadRequest request) {
        for (RenderEntity renderEntity : requestRenderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                PayloadValueSetter valueSetter = PayloadValueSetter.VALUE_SETTER_MAP.get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));

            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PayloadValueSetter valueSetter = PayloadValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (!TextUtils.isEmpty(nameValueStringEntity.getRealData())) {
                    valueSetter.onSet(request, nameValueStringEntity.getRealData());
                } else {
                    valueSetter.onSet(request, nameValueStringEntity.getValue());
                }
            }
        }
    }
}
