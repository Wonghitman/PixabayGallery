package com.vam.android.pagergallery.ui.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vam.android.pagergallery.R
import com.vam.android.pagergallery.databinding.GalleryCellBinding
import com.vam.android.pagergallery.network.bean.Pixabay


class GalleryAdapter() : PagingDataAdapter<Pixabay.PhotoItem, RecyclerView.ViewHolder>
    (PHOTO_COMPARATOR) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return PhotoViewHolder(
            GalleryCellBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).also { photoViewHolder ->
            photoViewHolder.itemView.setOnClickListener {

                val position = photoViewHolder.bindingAdapterPosition
                val photoItem = getItem(position)

                Bundle().apply {
                    putInt("PHOTO_POSITION", position)
                    photoItem?.let {
                        photoViewHolder.itemView.findNavController()
                            .navigate(R.id.action_galleryFragment_to_pagerFragment,this)
                    }
                }


            }
        }


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val photoItem = getItem(position) ?: return
        (holder as PhotoViewHolder).bindWithPhotoItem(photoItem)

    }

    class PhotoViewHolder(private val binding: GalleryCellBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindWithPhotoItem(photoItem: Pixabay.PhotoItem) {
            with(itemView) {
                binding.shimmerLayoutCell.apply {
                    setShimmerColor(0x55FFFFFF)
                    setShimmerAngle(0)
                    startShimmerAnimation()
                }
                binding.apply {
                    textViewUser.text = photoItem.photoUser
                    textViewLikes.text = photoItem.likesNum.toString()
                    textViewFavorites.text = photoItem.downloadsNum.toString()
                    imageView.layoutParams.height = photoItem.photoHeight
                }

            }

            Glide.with(itemView)
                .load(photoItem.previewURL)
                .placeholder(R.drawable.photo_placeholder)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false.also { binding.shimmerLayoutCell.stopShimmerAnimation() }
                    }

                })
                .into(binding.imageView)
        }
    }

    companion object {

        private val PHOTO_COMPARATOR = object : DiffUtil.ItemCallback<Pixabay.PhotoItem>() {
            override fun areItemsTheSame(
                oldItem: Pixabay.PhotoItem,
                newItem: Pixabay.PhotoItem
            ): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(
                oldItem: Pixabay.PhotoItem,
                newItem: Pixabay.PhotoItem
            ): Boolean =
                oldItem.photoId == newItem.photoId
        }

    }


}