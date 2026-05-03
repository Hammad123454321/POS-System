package com.pax.poslink.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.pax.poslink.R;
import com.pax.poslink.entity.Restaurant;
import com.pax.poslink.model.RestaurantValueSetter;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.List;

import static com.pax.poslink.util.Constant.RESTAURANT_RESULT;

/**
 * Created by Justin.Z on 2020-5-25
 */
public class PaymentRestaurantActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup container;
    private String msg = "";
    private Restaurant restaurant;
    private Gson gson = new Gson();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        msg = bundle.getString("Payment_Restaurant");
        setContentView(R.layout.payment_commercial_activity);
        initView();
    }

    private void initView() {
        restaurant = TextUtils.isEmpty(msg) ? new Restaurant() : gson.fromJson(msg, Restaurant.class);
        container = findViewById(R.id.commercial_container);
        Button backBtn = (Button) findViewById(R.id.payment_extData_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button clearBtn = findViewById(R.id.payment_extData_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restaurant = new Restaurant();
                initList();
            }
        });

        Button saveBtn = (Button) findViewById(R.id.payment_extData_save);
        saveBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "";
                getRestaurant();
                Intent intent = new Intent();
                msg = gson.toJson(restaurant);
                intent.putExtra("Payment_Restaurant", msg);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        initList();
    }

    private void initList() {
        renderEntityList.clear();
        container.removeAllViews();

        renderEntityList.add(new NameValueStringEntity(RestaurantValueSetter.TABLE_NUMBER, restaurant.TableNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(RestaurantValueSetter.GUEST_NUMBER, restaurant.GuestNumber, InputType.TYPE_CLASS_TEXT, ""));
        renderEntityList.add(new NameValueStringEntity(RestaurantValueSetter.TICKET_NUMBER, restaurant.TicketNumber, InputType.TYPE_CLASS_TEXT, ""));

        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(container);
            container.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }


    private void getRestaurant() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                RestaurantValueSetter valueSetter = RestaurantValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(restaurant, nameValueStringEntity.getValue());

            }
        }
    }
}
