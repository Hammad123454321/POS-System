package com.pax.poslink.model;

import com.pax.poslink.entity.Restaurant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-5-25
 */
public interface RestaurantValueSetter {

    String TABLE_NUMBER = "Table Number";
    String GUEST_NUMBER = "Guest Number";
    String TICKET_NUMBER = "Ticket Number";

    Map<String, RestaurantValueSetter> VALUE_SETTER_MAP = new HashMap<String, RestaurantValueSetter>() {
        {
            put(TABLE_NUMBER, new RestaurantValueSetter() {
                @Override
                public void onSet(Restaurant request, String value) {
                    request.TableNumber = value;
                }
            });
            put(GUEST_NUMBER, new RestaurantValueSetter() {
                @Override
                public void onSet(Restaurant request, String value) {
                    request.GuestNumber = value;
                }
            });
            put(TICKET_NUMBER, new RestaurantValueSetter() {
                @Override
                public void onSet(Restaurant request, String value) {
                    request.TicketNumber = value;
                }
            });
        }
    };

    void onSet(Restaurant request, String value);
}
