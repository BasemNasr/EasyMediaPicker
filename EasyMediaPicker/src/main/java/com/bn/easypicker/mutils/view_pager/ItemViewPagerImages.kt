package com.bn.easypicker.mutils.view_pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ItemViewPagerImages(fm: FragmentManager?, fragments: ArrayList<Fragment>) :
    FragmentStatePagerAdapter(fm!!) {
    private var mFragments: ArrayList<Fragment> = ArrayList()
    override fun getItem(position: Int) = mFragments[position]

    override fun getCount() = mFragments.size

    init {
        mFragments = fragments
    }
}