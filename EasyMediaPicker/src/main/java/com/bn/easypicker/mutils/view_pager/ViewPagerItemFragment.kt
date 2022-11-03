package com.bn.easypicker.mutils.view_pager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.bn.easypicker.R
import com.bumptech.glide.Glide

class ViewPagerItemFragment : Fragment() {

    private var imageModel: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = run {
        return inflater.inflate(R.layout.fragment_viewpager_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imm =
            this.requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        val imageItem = view.findViewById<AppCompatImageView>(R.id.imageItem)
        if (imageModel != null) {
            Log.d("ImageModelFragment", "init: >>> $imageModel")
            Glide.with(this)
                .load(imageModel)
                .into(imageItem)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            imageModel = requireArguments().getInt("image")
        }
    }

    companion object {
        fun getInstance(image: Int?): ViewPagerItemFragment {
            val fragment = ViewPagerItemFragment()
            if (image != null) {
                val bundle = Bundle()
                bundle.putInt("image", image)
                fragment.arguments = bundle
            }
            return fragment
        }
    }


}