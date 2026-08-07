package com.frafio.myfinance.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AdaptiveSheet
import com.frafio.myfinance.core.components.GridSheetDialog
import com.frafio.myfinance.core.data.model.MenuItem
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.capitalizeWords
import com.frafio.myfinance.core.utils.getCurrencyIcon
import com.frafio.myfinance.core.utils.getLocaleFromCurrency
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    currencyCode: String = "EUR"
) {
    var showAll by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (!show) {
            showAll = false
            isLoading = false
        }
    }

    val currencies = listOf(
        Pair("EUR", Locale.ITALY),
        Pair("USD", Locale.US),
        Pair("GBP", Locale.UK),
        Pair("JPY", Locale.JAPAN),
        Pair("CNY", Locale.CHINA),
        Pair("RUB", Locale.forLanguageTag("ru-RU")),
        Pair("AUD", Locale.forLanguageTag("en-AU")),
        Pair("CAD", Locale.CANADA),
        Pair("TRY", Locale.forLanguageTag("tr-TR"))
    )

    val initialCurrenciesCodes = remember { currencies.map { it.first }.toSet() }

    val currencyMenuItems = remember {
        currencies.map { pair ->
            val currency = Currency.getInstance(pair.first)
            MenuItem(
                symbol = currency.getSymbol(pair.second),
                text = currency.displayName.capitalizeWords(),
                onClick = { onCurrencySelected(pair.first) }
            )
        }
    }

    val displayItems = remember(showAll) {
        if (showAll) {
            val otherCurrenciesMenuItems = Currency.getAvailableCurrencies()
                .filter { it.currencyCode !in initialCurrenciesCodes && it.currencyCode != "XXX" }
                .sortedBy { it.currencyCode }
                .map { currency ->
                    val symbol = when (currency.currencyCode) {
                        "FRF" -> "₣"
                        else -> currency.getSymbol(getLocaleFromCurrency(currency.currencyCode))
                    }
                    MenuItem(
                        symbol = symbol,
                        text = currency.displayName.capitalizeWords(),
                        onClick = { onCurrencySelected(currency.currencyCode) }
                    )
                }
            currencyMenuItems + otherCurrenciesMenuItems
        } else {
            currencyMenuItems
        }
    }

    AdaptiveSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        GridSheetDialog(
            modifier = modifier,
            icon = getCurrencyIcon(currencyCode),
            title = stringResource(id = R.string.currency),
            label = stringResource(id = R.string.select),
            rowSize = 3,
            items = displayItems,
            onDismiss = onDismiss,
            bottomContent = {
                if (!showAll) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        onClick = { showAll = true }
                    ) {
                        Text(text = stringResource(id = R.string.show_all_currencies))
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencySheetPreview() {
    MyFinanceTheme {
        CurrencySheet(
            show = true,
            onDismiss = {},
            onCurrencySelected = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
            currencyCode = "EUR"
        )
    }
}
