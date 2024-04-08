package com.vam.android.pagergallery.ui.gallery

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("ClickableViewAccessibility")
fun RecyclerView.setAdapterWithDefaultFooter(adapter: GalleryAdapter) {
    this.adapter = adapter.withLoadStateFooter(FooterAdapter())
    var downEventY = 0f
    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downEventY = event.rawY
            }

            MotionEvent.ACTION_UP -> {
                if (downEventY - event.rawY > 20) {
                    adapter.retry()
                }
            }
        }
        false
    }
}