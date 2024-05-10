package com.vam.android.pagergallery.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vam.android.pagergallery.databinding.GalleryFooterBinding

class FooterAdapter():LoadStateAdapter<FooterAdapter.FooterViewHolder>()
{

    override fun onBindViewHolder(holder: FooterViewHolder, loadState: LoadState) {
        holder.bindWithNetworkStatus(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): FooterViewHolder {
        return FooterViewHolder(
            GalleryFooterBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }




    inner class FooterViewHolder(private val binding: GalleryFooterBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bindWithNetworkStatus(state: LoadState) {

                when (state) {
                    is LoadState.Error -> {
                        binding.textView.text = "点击重试"
                        binding.progressBar.visibility = View.GONE
                        itemView.isClickable = true
                    }
                    is LoadState.NotLoading -> {
                        binding.textView.text = "加载完毕"
                        binding.progressBar.visibility = View.GONE
                        itemView.isClickable = false
                    }
                    is LoadState.Loading -> {
                        binding.textView.text = "正在加载"
                        binding.progressBar.visibility = View.VISIBLE
                        itemView.isClickable = false
                    }

                }

        }

    }




}