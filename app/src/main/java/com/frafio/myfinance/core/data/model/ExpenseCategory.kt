package com.frafio.myfinance.core.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.theme.brownContainerDark
import com.frafio.myfinance.core.theme.brownContainerLight
import com.frafio.myfinance.core.theme.brownOnContainerDark
import com.frafio.myfinance.core.theme.brownOnContainerLight
import com.frafio.myfinance.core.theme.indigoContainerDark
import com.frafio.myfinance.core.theme.indigoContainerLight
import com.frafio.myfinance.core.theme.indigoOnContainerDark
import com.frafio.myfinance.core.theme.indigoOnContainerLight
import com.frafio.myfinance.core.theme.lightBlueContainerDark
import com.frafio.myfinance.core.theme.lightBlueContainerLight
import com.frafio.myfinance.core.theme.lightBlueOnContainerDark
import com.frafio.myfinance.core.theme.lightBlueOnContainerLight
import com.frafio.myfinance.core.theme.lightGreenContainerDark
import com.frafio.myfinance.core.theme.lightGreenContainerLight
import com.frafio.myfinance.core.theme.lightGreenOnContainerDark
import com.frafio.myfinance.core.theme.lightGreenOnContainerLight
import com.frafio.myfinance.core.theme.orangeContainerDark
import com.frafio.myfinance.core.theme.orangeContainerLight
import com.frafio.myfinance.core.theme.orangeOnContainerDark
import com.frafio.myfinance.core.theme.orangeOnContainerLight
import com.frafio.myfinance.core.theme.purpleContainerDark
import com.frafio.myfinance.core.theme.purpleContainerLight
import com.frafio.myfinance.core.theme.purpleOnContainerDark
import com.frafio.myfinance.core.theme.purpleOnContainerLight
import com.frafio.myfinance.core.theme.redContainerDark
import com.frafio.myfinance.core.theme.redContainerLight
import com.frafio.myfinance.core.theme.redOnContainerDark
import com.frafio.myfinance.core.theme.redOnContainerLight
import com.frafio.myfinance.core.theme.tealContainerDark
import com.frafio.myfinance.core.theme.tealContainerLight
import com.frafio.myfinance.core.theme.tealOnContainerDark
import com.frafio.myfinance.core.theme.tealOnContainerLight
import com.frafio.myfinance.core.theme.yellowContainerDark
import com.frafio.myfinance.core.theme.yellowContainerLight
import com.frafio.myfinance.core.theme.yellowOnContainerDark
import com.frafio.myfinance.core.theme.yellowOnContainerLight

@Immutable
data class ExpenseCategory(
    val id: Int,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val colors: CategoryColors
)

@Immutable
data class CategoryColors(
    val containerLight: Color,
    val containerDark: Color,
    val onContainerLight: Color,
    val onContainerDark: Color
) {
    fun container(isDark: Boolean): Color = if (isDark) containerDark else containerLight

    fun onContainer(isDark: Boolean): Color = if (isDark) onContainerDark else onContainerLight

    /** Text drawn on the surface: the container color in light mode, the on-container color in dark mode. */
    fun text(isDark: Boolean): Color = if (isDark) onContainerDark else containerLight
}

/** The single source of truth for the expense categories; ids are the values persisted in Room and Firestore. */
object ExpenseCategories {
    private val miscellaneous = ExpenseCategory(
        id = FirestoreEnums.CATEGORIES.MISCELLANEOUS.value,
        nameRes = R.string.miscellaneous,
        descriptionRes = R.string.miscellaneous_description,
        iconRes = R.drawable.ic_grid_3x3_filled,
        colors = CategoryColors(brownContainerLight, brownContainerDark, brownOnContainerLight, brownOnContainerDark)
    )

    val all: List<ExpenseCategory> = listOf(
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.HOUSING.value,
            nameRes = R.string.housing,
            descriptionRes = R.string.housing_description,
            iconRes = R.drawable.ic_home_filled,
            colors = CategoryColors(redContainerLight, redContainerDark, redOnContainerLight, redOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.GROCERIES.value,
            nameRes = R.string.groceries,
            descriptionRes = R.string.groceries_description,
            iconRes = R.drawable.ic_shopping_cart_filled,
            colors = CategoryColors(purpleContainerLight, purpleContainerDark, purpleOnContainerLight, purpleOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.PERSONAL_CARE.value,
            nameRes = R.string.personal_care,
            descriptionRes = R.string.personal_care_description,
            iconRes = R.drawable.ic_self_care_filled,
            colors = CategoryColors(indigoContainerLight, indigoContainerDark, indigoOnContainerLight, indigoOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.ENTERTAINMENT.value,
            nameRes = R.string.entertainment,
            descriptionRes = R.string.entertainment_description,
            iconRes = R.drawable.ic_theater_comedy_filled,
            colors = CategoryColors(lightBlueContainerLight, lightBlueContainerDark, lightBlueOnContainerLight, lightBlueOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.EDUCATION.value,
            nameRes = R.string.education,
            descriptionRes = R.string.education_description,
            iconRes = R.drawable.ic_school_filled,
            colors = CategoryColors(tealContainerLight, tealContainerDark, tealOnContainerLight, tealOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.DINING.value,
            nameRes = R.string.dining,
            descriptionRes = R.string.dining_description,
            iconRes = R.drawable.ic_restaurant_filled,
            colors = CategoryColors(lightGreenContainerLight, lightGreenContainerDark, lightGreenOnContainerLight, lightGreenOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.HEALTH.value,
            nameRes = R.string.health,
            descriptionRes = R.string.health_description,
            iconRes = R.drawable.ic_vaccines_filled,
            colors = CategoryColors(yellowContainerLight, yellowContainerDark, yellowOnContainerLight, yellowOnContainerDark)
        ),
        ExpenseCategory(
            id = FirestoreEnums.CATEGORIES.TRANSPORTATION.value,
            nameRes = R.string.transportation,
            descriptionRes = R.string.transportation_description,
            iconRes = R.drawable.ic_directions_subway_filled,
            colors = CategoryColors(orangeContainerLight, orangeContainerDark, orangeOnContainerLight, orangeOnContainerDark)
        ),
        miscellaneous
    )

    val ids: List<Int> = all.map { it.id }

    /** Returned for null, -1 ("no category") and ids that aren't a category, such as TOTAL. Not part of [all]. */
    val Unknown: ExpenseCategory = miscellaneous.copy(id = -1, nameRes = R.string.category)

    private val byId: Map<Int, ExpenseCategory> = all.associateBy { it.id }

    fun get(id: Int?): ExpenseCategory = byId[id] ?: Unknown
}
