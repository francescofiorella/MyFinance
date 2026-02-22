package com.frafio.myfinance.ui.add

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.widget.DatePickerButton
import com.frafio.myfinance.databinding.ActivityAddBinding
import com.frafio.myfinance.ui.home.expenses.CategorySheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.getCategoryIcon
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.sidesheet.SideSheetDialog

class AddActivity : AppCompatActivity(), AddListener {

    companion object {
        const val REQUEST_ADD_CODE: Int = 1
        const val REQUEST_EDIT_CODE: Int = 2
        const val REQUEST_EXPENSE_CODE: Int = 10
        const val REQUEST_INCOME_CODE: Int = 11
        const val REQUEST_CODE_KEY: String = "com.frafio.myfinance.REQUEST_CODE"
        const val EXPENSE_REQUEST_KEY: String = "com.frafio.myfinance.EXPENSE_REQUEST"
        const val EXPENSE_ID_KEY: String = "com.frafio.myfinance.EXPENSE_ID"
        const val EXPENSE_NAME_KEY: String = "com.frafio.myfinance.EXPENSE_NAME"
        const val EXPENSE_PRICE_KEY: String = "com.frafio.myfinance.EXPENSE_PRICE"
        const val EXPENSE_CATEGORY_KEY: String = "com.frafio.myfinance.EXPENSE_CATEGORY"
        const val EXPENSE_POSITION_KEY: String = "com.frafio.myfinance.EXPENSE_POSITION"
        const val EXPENSE_YEAR_KEY: String = "com.frafio.myfinance.EXPENSE_YEAR"
        const val EXPENSE_MONTH_KEY: String = "com.frafio.myfinance.EXPENSE_MONTH"
        const val EXPENSE_DAY_KEY: String = "com.frafio.myfinance.EXPENSE_DAY"
        const val ADD_RESULT_MESSAGE: String = "com.frafio.myfinance.ADD_RESULT_MESSAGE"
        const val ADD_RESULT_TOTAL_ID: String = "com.frafio.myfinance.ADD_RESULT_TOTAL_ID"
    }

    private lateinit var binding: ActivityAddBinding
    private val viewModel by viewModels<AddViewModel>()

    // custom datePicker layout
    private lateinit var datePickerBtn: DatePickerButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorPadding = (36 * resources.displayMetrics.density).toInt()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        binding.viewModel = viewModel
        viewModel.listener = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                // Apply padding
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }
        }

        intent.getIntExtra(REQUEST_CODE_KEY, 0).also { code ->
            viewModel.requestCode = code
            initLayout(code)
        }

        binding.priceTIL.findViewById<TextView>(com.google.android.material.R.id.textinput_error)
            .setPaddingRelative(errorPadding, 0, 0, 0)
        binding.dateTIL.findViewById<TextView>(com.google.android.material.R.id.textinput_error)
            .setPaddingRelative(errorPadding, 0, 0, 0)
        binding.categoryTIL.findViewById<TextView>(com.google.android.material.R.id.textinput_error)
            .setPaddingRelative(errorPadding, 0, 0, 0)

        binding.priceTIL.setStartIconDrawable(
            when (getString(R.string.currency)) {
                "€" -> R.drawable.ic_euro_filled
                "$" -> R.drawable.ic_attach_money_filled
                else -> R.drawable.ic_euro_filled
            }
        )

        binding.nameET.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank())
                binding.nameTIL.error = ""
        }

        binding.priceET.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty() && text.isNotBlank()) {
                binding.priceTIL.error = ""
                if (text.contains(".")) {
                    var lastPartOfText = text.split(".")[text.split(".").size - 1]
                    if (lastPartOfText.count() > 2) {
                        lastPartOfText = text.substring(0, text.indexOf(".") + 3)
                        val selection = binding.priceET.selectionEnd
                        binding.priceET.setText(lastPartOfText)
                        if (selection >= lastPartOfText.length)
                            binding.priceET.setSelection(lastPartOfText.length)
                        else
                            binding.priceET.setSelection(selection)
                    }
                }
            }
        }
    }

    private fun initLayout(code: Int) {
        datePickerBtn = object : DatePickerButton(
            binding.dateTIL,
            binding.dateET,
            this@AddActivity
        ) {
            override fun onStart() {
                super.onStart()
                binding.nameET.clearFocus()
                binding.priceET.clearFocus()
                hideSoftKeyboard(binding.root)
            }

            override fun onPositiveBtnClickListener() {
                super.onPositiveBtnClickListener()
                viewModel.year = year
                viewModel.month = month
                viewModel.day = day
                viewModel.dateString = dateString
            }
        }

        binding.categoryTIL.setOnClickListener(categoryInputListener)
        binding.categoryET.setOnClickListener(categoryInputListener)

        when (code) {
            REQUEST_ADD_CODE -> {
                viewModel.expenseCode = REQUEST_EXPENSE_CODE
                viewModel.category = -1
                binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                    if (checkedIds.size != 1) return@setOnCheckedStateChangeListener
                    val checkedId = checkedIds[0]
                    when (checkedId) {
                        R.id.expense_chip -> {
                            if (viewModel.expenseCode == REQUEST_INCOME_CODE) {
                                binding.categoryTIL.setStartIconDrawable(R.drawable.ic_grid_3x3_filled)
                                binding.categoryET.clearText()
                            }
                            viewModel.expenseCode = REQUEST_EXPENSE_CODE
                            binding.categoryTIL.visibility = View.VISIBLE
                            binding.divider3.visibility = View.VISIBLE
                        }

                        R.id.income_chip -> {
                            if (viewModel.expenseCode == REQUEST_EXPENSE_CODE) {
                                binding.categoryTIL.setStartIconDrawable(R.drawable.ic_grid_3x3_filled)
                                binding.categoryET.clearText()
                            }
                            viewModel.expenseCode = REQUEST_INCOME_CODE
                            binding.categoryTIL.visibility = View.GONE
                            binding.divider3.visibility = View.GONE
                        }
                    }
                }
            }

            REQUEST_EDIT_CODE -> {
                intent.apply {
                    getIntExtra(EXPENSE_REQUEST_KEY, -1).also {
                        viewModel.expenseCode = it
                    }
                    getStringExtra(EXPENSE_ID_KEY)?.let {
                        viewModel.expenseId = it
                    }
                    getStringExtra(EXPENSE_NAME_KEY)?.let {
                        viewModel.name = it
                    }
                    getDoubleExtra(EXPENSE_PRICE_KEY, 0.0).also {
                        viewModel.priceString = doubleToString(it)
                    }
                    getIntExtra(EXPENSE_CATEGORY_KEY, 0).also {
                        viewModel.category = it
                    }
                    getIntExtra(EXPENSE_POSITION_KEY, 0).also {
                        viewModel.expensePosition = it
                    }
                    getIntExtra(EXPENSE_YEAR_KEY, 0).also {
                        datePickerBtn.year = it
                    }
                    getIntExtra(EXPENSE_MONTH_KEY, 0).also {
                        datePickerBtn.month = it
                    }
                    getIntExtra(EXPENSE_DAY_KEY, 0).also {
                        datePickerBtn.day = it
                    }
                }

                if (viewModel.expenseCode == REQUEST_EXPENSE_CODE) {
                    binding.chipGroup.apply {
                        check(R.id.expense_chip)
                        binding.chipGroup.findViewById<Chip>(R.id.income_chip).isEnabled = false
                    }
                    val categories = resources.getStringArray(R.array.categories)
                    if (viewModel.category != null && viewModel.category!! >= 0 && viewModel.category!! < categories.size) {
                        binding.categoryET.setText(categories[viewModel.category!!])
                        binding.categoryTIL.setStartIconDrawable(
                            when (viewModel.category) {
                                FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_home_filled
                                FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart_filled
                                FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care_filled
                                FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy_filled
                                FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school_filled
                                FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant_filled
                                FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines_filled
                                FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_subway_filled
                                FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_grid_3x3_filled
                                else -> R.drawable.ic_grid_3x3_filled
                            }
                        )
                    }
                } else {
                    binding.chipGroup.apply {
                        check(R.id.income_chip)
                        binding.chipGroup.findViewById<Chip>(R.id.expense_chip).isEnabled = false
                    }
                    binding.divider3.visibility = View.GONE
                    binding.categoryTIL.visibility = View.GONE
                }
            }
        }

        viewModel.updateTime(datePickerBtn)
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onAddStart() {
        binding.addProgressIndicator.show()

        binding.nameTIL.error = ""
        binding.priceTIL.error = ""
        binding.categoryTIL.error = ""

        binding.addAddButton.isEnabled = false
    }

    override fun onAddSuccess(response: LiveData<FinanceResult>) {
        response.observe(this) { result ->
            if (result.code != FinanceCode.EXPENSE_ADD_SUCCESS.code) {
                binding.addProgressIndicator.hide()
                binding.addAddButton.isEnabled = true
            }

            when (result.code) {
                FinanceCode.EXPENSE_ADD_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        val totalId = evaluateExpenseTotalId(
                            viewModel.day!!,
                            viewModel.month!!,
                            viewModel.year!!
                        )
                        it.putExtra(EXPENSE_REQUEST_KEY, REQUEST_EXPENSE_CODE)
                        it.putExtra(ADD_RESULT_MESSAGE, result.message)
                        it.putExtra(ADD_RESULT_TOTAL_ID, totalId)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                FinanceCode.EXPENSE_EDIT_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(EXPENSE_REQUEST_KEY, REQUEST_EXPENSE_CODE)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                FinanceCode.INCOME_ADD_SUCCESS.code -> {
                    Intent().also {
                        it.putExtra(EXPENSE_REQUEST_KEY, REQUEST_INCOME_CODE)
                        it.putExtra(ADD_RESULT_MESSAGE, result.message)
                        it.putExtra(ADD_RESULT_TOTAL_ID, viewModel.year.toString())
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                FinanceCode.INCOME_EDIT_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(EXPENSE_REQUEST_KEY, REQUEST_INCOME_CODE)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                else -> snackBar(result.message, binding.addAddButton)
            }
        }
    }

    override fun onAddFailure(financeResult: FinanceResult) {
        binding.addProgressIndicator.hide()
        binding.addAddButton.isEnabled = true

        when (financeResult.code) {
            FinanceCode.EMPTY_NAME.code,
            FinanceCode.WRONG_NAME_TOTAL.code ->
                binding.nameTIL.error = financeResult.message

            FinanceCode.EMPTY_AMOUNT.code,
            FinanceCode.WRONG_AMOUNT.code ->
                binding.priceTIL.error = financeResult.message

            FinanceCode.EMPTY_CATEGORY.code ->
                binding.categoryTIL.error = financeResult.message
        }
    }

    private val categoryInputListener = View.OnClickListener {
        binding.nameET.clearFocus()
        binding.priceET.clearFocus()
        binding.categoryET.requestFocus()
        val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
            SideSheetDialog(this)
        } else {
            BottomSheetDialog(this)
        }
        val composeView = getCategorySheetDialogComposeView(
            onDismiss = sheetDialog::hide,
            onCategorySelected = {
                binding.categoryTIL.error = ""
                val categories = resources.getStringArray(R.array.categories)
                binding.categoryET.setText(categories[it])
                binding.categoryTIL.setStartIconDrawable(getCategoryIcon(it))
                viewModel.category = it
            }
        )
        sheetDialog.setContentView(composeView)
        sheetDialog.show()
    }

    private fun evaluateExpenseTotalId(day: Int, month: Int, year: Int): String {
        return "${day}_${month}_${year}"
    }

    private fun getCategorySheetDialogComposeView(
        onDismiss: () -> Unit,
        onCategorySelected: (Int) -> Unit
    ): ComposeView {
        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    CategorySheetDialog(
                        onCategorySelected = onCategorySelected,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}