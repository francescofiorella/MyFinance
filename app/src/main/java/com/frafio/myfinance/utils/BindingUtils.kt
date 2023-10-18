package com.frafio.myfinance.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frafio.myfinance.R
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

@BindingAdapter("srcRound")
fun setImageViewRoundDrawable(view: ImageView, url: String?) {
    if (!url.isNullOrBlank()) {
        Glide.with(view)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    } else {
        view.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_user))
    }
}

@Suppress("UNUSED")
@BindingAdapter("cornerRadiusTop")
fun setBottomAppBarCornerRadius(view: BottomAppBar, cornerSize: Float) {
    (view.background as MaterialShapeDrawable).also { background ->
        background.shapeAppearanceModel = background.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
            .build()
    }
}