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

@BindingAdapter("data")
fun setValueLineChartData(view: ValueLineChart, list: List<Pair<String, Double>>) {
    ValueLineSeries().also { series ->
        series.color = ColorUtils
            .setAlphaComponent(
                ContextCompat.getColor(
                    view.context,
                    R.color.md_theme_primaryContainer
                ), 200
            )

        list.forEach { pair ->
            val date: String = pair.first
            // convert to Float e round the the second decimal figure
            val value: Float = pair.second.toFloat().round(2)
            series.addPoint(ValueLinePoint(date, value))
        }

        view.addSeries(series)
    }
}