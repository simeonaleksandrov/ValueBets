package com.retdevelopment.valuebetsprotipster;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        return mFragmentList.get(position);
    }


    public void addFragment(Fragment fragment, String title)
    {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public int getCount()
    {
        return mFragmentList.size();
    }
}