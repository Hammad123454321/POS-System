package com.pax.poslink.ui.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;


public class TabAdapter extends FragmentStatePagerAdapter {
    private static final String KEY_CURRENT_INDEX_OF_FRAGMENT = "key_current_index_of_fragment";
    private int savedCurrentIndex;

    private ArrayList<String> titles ;
    private ArrayList<Fragment> fragments;

    public TabAdapter(@Nullable Bundle savedInstanceState, @NonNull FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        if (savedInstanceState != null)
            savedCurrentIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX_OF_FRAGMENT);
    }

    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }

    public int getSavedIndex() {
        return savedCurrentIndex;
    }

    public void saveIndex(@Nullable Bundle outState, int currentItem) {
        if (outState != null && currentItem >=0)
            outState.putInt(KEY_CURRENT_INDEX_OF_FRAGMENT, currentItem);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
