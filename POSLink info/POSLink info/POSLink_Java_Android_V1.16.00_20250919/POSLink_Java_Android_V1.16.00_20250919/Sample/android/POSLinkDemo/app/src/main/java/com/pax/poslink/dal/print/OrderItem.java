package com.pax.poslink.dal.print;

/**
 * Created by Leon.F on 2018/3/23.
 */

public class OrderItem {
    private String name;
    private int number;
    private double price;

    public OrderItem(String name, int number, double price) {
        this.name = name;
        this.number = number;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public double getPrice() {
        return price;
    }
}
