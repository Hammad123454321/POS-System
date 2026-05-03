package com.pax.poslink.util;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon on 2017/6/21.
 */

public class FragmentsHolder {

    public static final String KEY_CURRENT_INDEX_OF_FRAGMENT = "key_current_index_of_fragment";
    private int currentFragmentIndex;
    private List<Fragment> tabFragments = new ArrayList<>();
    private final Map<Integer, FragmentCreator> fragmentCreatorMap;

    public FragmentsHolder(Map<Integer, FragmentCreator> fragmentCreatorMap) {
        this.fragmentCreatorMap = fragmentCreatorMap;
    }

    public static void switchFragment(FragmentManager fragmentManager, Fragment hideFragment, Fragment showFragment, int containerViewId, String tag) {
        if (showFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .hide(hideFragment)
                    .show(showFragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .hide(hideFragment)
                    .add(containerViewId, showFragment, tag)
                    .commit();
        }
    }

    public int getCurrentFragmentIndex() {
        return currentFragmentIndex;
    }

    public void initFragments(Bundle savedInstanceState, FragmentManager fragmentManager, int containerViewId) {
        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            for (int i = 0; i < fragmentCreatorMap.size(); i++) {
                tabFragments.add(fragmentCreatorMap.get(i).create());
            }
            Fragment tabFragment = tabFragments.get(0);
            transaction.add(containerViewId, tabFragment, String.valueOf(0));
            transaction.commit();
        } else {
            currentFragmentIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX_OF_FRAGMENT);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            for (int i = 0; i < fragmentCreatorMap.size(); i++) {
                Fragment fragmentByTag = fragmentManager.findFragmentByTag(String.valueOf(i));
                if (fragmentByTag != null) {
                    transaction.hide(fragmentByTag);
                    tabFragments.add(fragmentByTag);
                } else {
                    tabFragments.add(fragmentCreatorMap.get(i).create());
                }
            }
            transaction.show(fragmentManager.findFragmentByTag(String.valueOf(currentFragmentIndex))).commit();
        }
    }

    public void setCurrentFragmentIndex(int currentFragmentIndex) {
        this.currentFragmentIndex = currentFragmentIndex;
    }

    public Fragment getCurrentFragment() {
        return tabFragments.get(currentFragmentIndex);
    }

    public void switchFragment(int toFragmentIndex, FragmentManager supportFragmentManager, int containerViewId) {
        Fragment hideFragment = getCurrentFragment();
        setCurrentFragmentIndex(toFragmentIndex);
        Fragment showFragment = getCurrentFragment();
        FragmentsHolder.switchFragment(supportFragmentManager, hideFragment, showFragment, containerViewId, String.valueOf(getCurrentFragmentIndex()));
    }
}
