package com.poslink.sample.main;

import com.pax.poslink.PosLink;

/**
 * Created by Chilling on 2018/2/4.
 */

public class MainController {

    public String index() {
        return "home";
    }

    public String home() {
        return "home";
    }

    private static MainController instance = null;
    private PosLink poslink;

    private MainController(){}

    public static MainController getInstance() {
        if (instance == null) {
            instance = new MainController();
        }
        return instance;
    }

    public PosLink createPosLink() {
        poslink = new PosLink();
        return poslink;
    }

    public PosLink getPoslink() {
        return poslink;
    }
}
