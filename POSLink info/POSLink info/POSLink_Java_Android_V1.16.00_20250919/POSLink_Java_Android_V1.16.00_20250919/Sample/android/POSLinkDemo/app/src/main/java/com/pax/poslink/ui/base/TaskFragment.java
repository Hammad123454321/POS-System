package com.pax.poslink.ui.base;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.pax.poslink.AsyncPosLinkTask;

/**
 * Created by linhb on 2015-09-22.
 */
public abstract class TaskFragment extends BaseFragment implements Runnable {
    protected DataFragment dataFragment;
    protected AsyncPosLinkTask mTask;

    public Dialog createDialog() {
        return null;
    }

    @Override
    public void run() {

    }

    public void onTaskCompleted() {
        if (dataFragment != null) dataFragment.removeData();
    }

    public void setTask() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        dataFragment = (DataFragment) fm.findFragmentByTag(getActivity().getLocalClassName() + "Data");

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment, getActivity().getLocalClassName() + "Data").commit();
        }
        mTask = (AsyncPosLinkTask) dataFragment.getData();
        if (mTask != null) {
            mTask.setFragment(this);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTask != null) mTask.setFragment(null);
        super.onSaveInstanceState(outState);
    }
}
