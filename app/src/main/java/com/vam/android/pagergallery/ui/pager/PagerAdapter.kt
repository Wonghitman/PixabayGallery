package com.vam.android.pagergallery.ui.pager

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vam.android.pagergallery.R
import com.vam.android.pagergallery.databinding.PagerPhotoViewBinding
import com.vam.android.pagergallery.network.bean.Pixabay

//使用ListAdapter的原因是，使用PagingAdapter未能实现指哪打哪的效果，BUG未有能力解决
class PagerAdapter: ListAdapter<Pixabay.PhotoItem, RecyclerView.ViewHolder>(DiffCallback){

    object DiffCallback: DiffUtil.ItemCallback<Pixabay.PhotoItem>() {//比较器
    override fun areItemsTheSame(oldItem: Pixabay.PhotoItem, newItem: Pixabay.PhotoItem): Boolean {
        return oldItem == newItem
    }

        override fun areContentsTheSame(oldItem: Pixabay.PhotoItem, newItem: Pixabay.PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("PagerAdapter", "Item at position $position: $item")

        Glide.with(holder.itemView)
            .load(item?.fullURL)
            .placeholder(R.drawable.photo_placeholder)
            .into(holder.itemView.findViewById(R.id.pagerPhoto))
        holder.itemView.tag = item?.fullURL

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
        PagerPhotoViewBinding.inflate(LayoutInflater.from(parent.context),parent,false).apply {
            return PagerPhotoViewHolder(this)
        }
    }

}

class PagerPhotoViewHolder(binding:PagerPhotoViewBinding) : RecyclerView.ViewHolder(binding.root){


}
