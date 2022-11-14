package com.bn.easypicker.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import com.bn.easypicker.R
import com.bn.easypicker.listeners.OnAttachmentTypeSelected
import com.google.android.material.bottomsheet.BottomSheetDialog

class SelectAttachmentsTypeSheet(
    private val mContext: Context,
    private val itemClicked: OnAttachmentTypeSelected,
    private val cameraIcon: Int,
    private val galleryIcon: Int,
    private var backgroundColor: Int,
    private val btnBackground: Int,
    private val textColor: Int,
) : BottomSheetDialog(mContext) {



    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_attatchment_sheet)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window!!.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                mContext.resources,
                android.R.color.transparent,
                null
            )
        )

        setCancelable(true)
        setCanceledOnTouchOutside(true)
        dismissWithAnimation = true

        findViewById<LinearLayoutCompat>(R.id.llCamera)?.setOnClickListener {
            itemClicked.onAttachSelected(0)
            dismiss()
        }
        findViewById<LinearLayoutCompat>(R.id.llCamera)?.background = ContextCompat.getDrawable(mContext,btnBackground)
        findViewById<LinearLayoutCompat>(R.id.llSendImage)?.background = ContextCompat.getDrawable(mContext,btnBackground)
        findViewById<AppCompatImageView>(R.id.ivCamera)?.setImageResource(cameraIcon)
        findViewById<AppCompatImageView>(R.id.ivGallery)?.setImageResource(galleryIcon)
        findViewById<LinearLayoutCompat>(R.id.sheetContainer)?.setBackgroundColor(ContextCompat.getColor(mContext,backgroundColor))
        findViewById<AppCompatTextView>(R.id.tvCamera)?.setTextColor(ContextCompat.getColor(mContext,textColor))
        findViewById<AppCompatTextView>(R.id.tvGallery)?.setTextColor(ContextCompat.getColor(mContext,textColor))

        findViewById<LinearLayoutCompat>(R.id.llSendImage)?.setOnClickListener {
            itemClicked.onAttachSelected(1)
            dismiss()
        }

    }


}