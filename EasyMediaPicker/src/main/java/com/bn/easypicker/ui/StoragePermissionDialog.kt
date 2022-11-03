package com.bn.easypicker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bn.easypicker.R
import com.bn.easypicker.listeners.OnPermissionDialogListener
import com.bn.easypicker.mutils.view_pager.ItemViewPagerImages
import com.bn.easypicker.mutils.view_pager.ViewPagerItemFragment
import com.google.android.material.tabs.TabLayout

class StoragePermissionDialog(
    layout: Int, private val mListener: OnPermissionDialogListener<String>
) : DialogFragment(layout)  {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.media_permission_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(view)
    }

    private fun initialize(view: View) {
        view.findViewById<AppCompatTextView>(R.id.tvCancel).setOnClickListener { mListener.onPermissionDialogClicked("cancel") }
        view.findViewById<AppCompatTextView>(R.id.tvSettings).setOnClickListener { mListener.onPermissionDialogClicked("setting") }

        val item = listOf(
            R.drawable.ic_storage_first,
            R.drawable.ic_storage_second,
            R.drawable.ic_storage_three
        )
        init(item, view)
    }

    private fun init(list: List<Int>, view: View) {
        val fragments: ArrayList<Fragment> = ArrayList()
        val images: List<Int> = list
        for (image in images) {
            val fragment = ViewPagerItemFragment.getInstance(image)
            fragments.add(fragment)
        }
        val pagerAdapter = ItemViewPagerImages(childFragmentManager, fragments)
        val viewPage = view.findViewById<ViewPager>(R.id.locationImages)
        viewPage.adapter = pagerAdapter
        view.findViewById<TabLayout>(R.id.tab_layout).setupWithViewPager(viewPage, false)
        viewPage.currentItem = 0
        viewPage.isSaveEnabled = false
    }

}