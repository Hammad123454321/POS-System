package com.pax.poslink.ui.base;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/**
 * Created by linhb on 2015-09-22.
 */
public class DataFragment extends Fragment
{
    private Object data;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }

    public void removeData()
    {
        data = null;
    }
}