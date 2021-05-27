package com.frafio.myfinance.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("roundImage")
fun loadImage(view: ImageView, url: String) {
    if (url != "") {
        Glide.with(view)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }
}