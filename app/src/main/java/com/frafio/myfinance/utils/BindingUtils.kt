package com.frafio.myfinance.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.frafio.myfinance.R
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries

@BindingAdapter("srcRound")
fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrBlank()) {
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

@BindingAdapter("data")
fun setData(view: ValueLineChart, list: List<Pair<String, Double>>) {
    ValueLineSeries().also { series ->
        series.color = ColorUtils
            .setAlphaComponent(ContextCompat.getColor(view.context, R.color.accent), 150)

        list.forEach { pair ->
            val date: String = pair.first
            // converti to Float e arrotonda alla seconda cifra decimale
            val value: Float = pair.second.toFloat().round(2)
            series.addPoint(ValueLinePoint(date, value))
        }

        view.addSeries(series)
    }
}