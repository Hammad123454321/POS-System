package com.pax.poslink.util.adapter;

import android.view.View;

public interface CommonItemView<T extends RenderEntity> {


    void render(T renderEntity);

    View getView();
}