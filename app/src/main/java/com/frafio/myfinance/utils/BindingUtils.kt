package com.frafio.myfinance.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

@BindingAdapter("srcRound")
fun loadImage(view: ImageView, url: String) {
    if (url != "") {
        Glide.with(view)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }
}

@BindingAdapter("cornerRadiusTop")
fun setCornerRadius(view: BottomAppBar, cornerSize: Float) {
    (view.background as MaterialShapeDrawable).also { background ->
        background.shapeAppearanceModel = background.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
            .build()
    }
}