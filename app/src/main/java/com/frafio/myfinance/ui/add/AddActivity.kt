package com.frafio.myfinance.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.widget.DatePickerButton
import com.frafio.myfinance.databinding.ActivityAddBinding
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
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

        intent.getIntExtra(REQUEST_CODE_KEY, 0).also { code ->
            viewModel.requestCode = code
            initLayout(code)
        }

        binding.priceTIL.findViewById<TextView>(R.id.textinput_error)
            .setPaddingRelative(errorPadding, 0, 0, 0)
        binding.dateTIL.findViewById<TextView>(R.id.textinput_error)
            .setPaddingRelative(errorPadding, 0, 0, 0)
        binding.categoryTIL.findViewById<TextView>(R.id.textinput_error)
            .setPaddingRelative(errorPadding, 0, 0, 0)

        binding.priceTIL.setStartIconDrawable(
            when (getString(R.string.currency)) {
                "€" -> R.drawable.ic_euro
                "$" -> R.drawable.ic_attach_money
                else -> R.drawable.ic_euro
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
                            viewModel.expenseCode = REQUEST_EXPENSE_CODE
                            binding.categoryTIL.visibility = View.VISIBLE
                            binding.divider3.visibility = View.VISIBLE
                        }

                        R.id.income_chip -> {
                            viewModel.expenseCode = REQUEST_INCOME_CODE
                            binding.categoryTIL.visibility = View.GONE
                            binding.divider3.visibility = View.GONE
                        }
                    }
                }
            }

            REQUEST_EDIT_CODE -> {
                binding.chipGroup.visibility = View.GONE
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
                    val categories = resources.getStringArray(R.array.categories)
                    if (viewModel.category != null && viewModel.category!! >= 0 && viewModel.category!! < categories.size) {
                        binding.categoryET.setText(categories[viewModel.category!!])
                        binding.categoryTIL.setStartIconDrawable(
                            when (viewModel.category) {
                                FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                                FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                                FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                                FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                                FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                                FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                                FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                                FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                                FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                                else -> R.drawable.ic_tag
                            }
                        )
                    }
                } else {
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
                        it.putExtra(EXPENSE_REQUEST_KEY, REQUEST_EXPENSE_CODE)
                        it.putExtra(ADD_RESULT_MESSAGE, result.message)
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

    val categoryInputListener = View.OnClickListener {
        binding.nameET.clearFocus()
        binding.priceET.clearFocus()
        binding.categoryET.requestFocus()
        if (resources.getBoolean(R.bool.is600dp)) {
            val sideSheetDialog = SideSheetDialog(this)
            sideSheetDialog.setContentView(R.layout.layout_category_bottom_sheet)
            defineSheetInterface(
                sideSheetDialog.findViewById(android.R.id.content)!!,
                sideSheetDialog::hide
            )
            sideSheetDialog.show()
        } else {
            val modalBottomSheet = ModalBottomSheet(this)
            modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
        }
    }

    class ModalBottomSheet(
        private val activity: AddActivity
    ) : BottomSheetDialogFragment() {

        companion object {
            const val TAG = "ModalBottomSheet"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val layout =
                inflater.inflate(R.layout.layout_category_bottom_sheet, container, false)
            activity.defineSheetInterface(
                layout,
                this::dismiss
            )
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        dismissFun: () -> Unit
    ) {
        fun selectCategory(category: Int) {
            binding.categoryTIL.error = ""
            val categories = resources.getStringArray(R.array.categories)
            binding.categoryET.setText(categories[category])
            binding.categoryTIL.setStartIconDrawable(
                when (category) {
                    FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                    FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                    FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                    FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                    FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                    FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                    FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                    FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                    FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                    else -> R.drawable.ic_tag
                }
            )
            viewModel.category = category
        }

        layout.findViewById<ConstraintLayout>(R.id.expenseDetailLayout).visibility =
            View.GONE
        layout.findViewById<ConstraintLayout>(R.id.categoryDetailLayout).visibility =
            View.VISIBLE

        layout.findViewById<MaterialButton>(R.id.expenseCategoryIcon).icon =
            ContextCompat.getDrawable(
                applicationContext,
                when (viewModel.category) {
                    FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                    FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                    FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                    FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                    FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                    FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                    FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                    FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                    FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                    else -> R.drawable.ic_tag
                }
            )

        layout.findViewById<LinearLayout>(R.id.housing_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.HOUSING.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.groceries_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.GROCERIES.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.personal_care_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.PERSONAL_CARE.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.entertainment_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.ENTERTAINMENT.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.education_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.EDUCATION.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.dining_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.DINING.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.health_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.HEALTH.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.transportation_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.TRANSPORTATION.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.miscellaneous_layout).setOnClickListener {
            selectCategory(FirestoreEnums.CATEGORIES.MISCELLANEOUS.value)
            dismissFun()
        }
    }
}