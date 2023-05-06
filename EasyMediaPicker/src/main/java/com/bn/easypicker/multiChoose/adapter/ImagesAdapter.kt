package com.bn.easypicker.multiChoose.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bn.easypicker.R
import com.bn.easypicker.databinding.LayoutImageItemBinding
import com.bumptech.glide.Glide
import com.bn.easypicker.multiChoose.model.ImageModel

abstract class ImagesAdapter(
    private val context: Context,
    private val maxSelectionLimit: Int,
    private val list: MutableList<ImageModel> = mutableListOf()
) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutImageItemBinding = LayoutImageItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = layoutInflater.inflate(R.layout.layout_image_item, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageModel = list[position]

        holder.binding.let {
            Glide.with(context)
                .load(imageModel.uri)
                .into(it.imgPreview)

            if (imageModel.isSelected) {
                it.imgSelected.visibility = View.VISIBLE
            } else {
                it.imgSelected.visibility = View.GONE
            }

            it.layoutRoot.setOnClickListener { _ ->
                if (imageModel.isSelected) {
                    imageModel.isSelected = false
                    it.imgSelected.visibility = View.GONE
                    updateNextButton()
                } else {
                    if (getSelectedImageCount() < maxSelectionLimit) {
                        imageModel.isSelected = true
                        it.imgSelected.visibility = View.VISIBLE
                        updateNextButton()
                    } else {
                        showMessageForMaxSelection()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    abstract fun updateNextButton()
    abstract fun showMessageForMaxSelection()

    fun getSelectedImageCount(): Int {
        return list.filter { it.isSelected }.size
    }

    fun getSelectedUris(): MutableList<Uri> {
        return list.filter { it.isSelected }.map { it.uri }.toMutableList()
    }

    fun updateList(newList: MutableList<ImageModel>) {
        val oldSize = list.size
        list.clear()
        notifyItemRangeRemoved(0, oldSize)
        list.addAll(newList)
        notifyItemRangeInserted(0, list.size)
    }

}