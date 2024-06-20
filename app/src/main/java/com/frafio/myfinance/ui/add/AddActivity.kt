package com.frafio.myfinance.ui.add

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.DatePickerButton
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.ActivityAddBinding
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddActivity : AppCompatActivity(), AddListener {

    companion object {
        const val REQUEST_ADD_CODE: Int = 1
        const val REQUEST_EDIT_CODE: Int = 2
        const val REQUEST_CODE_KEY: String = "com.frafio.myfinance.REQUEST_CODE"
        const val PURCHASE_REQUEST_KEY: String = "com.frafio.myfinance.PURCHASE_REQUEST"
        const val PURCHASE_ID_KEY: String = "com.frafio.myfinance.PURCHASE_ID"
        const val PURCHASE_NAME_KEY: String = "com.frafio.myfinance.PURCHASE_NAME"
        const val PURCHASE_PRICE_KEY: String = "com.frafio.myfinance.PURCHASE_PRICE"
        const val PURCHASE_CATEGORY_KEY: String = "com.frafio.myfinance.PURCHASE_CATEGORY"
        const val PURCHASE_POSITION_KEY: String = "com.frafio.myfinance.PURCHASE_POSITION"
        const val PURCHASE_YEAR_KEY: String = "com.frafio.myfinance.PURCHASE_YEAR"
        const val PURCHASE_MONTH_KEY: String = "com.frafio.myfinance.PURCHASE_MONTH"
        const val PURCHASE_DAY_KEY: String = "com.frafio.myfinance.PURCHASE_DAY"
        const val ADD_RESULT_MESSAGE: String = "com.frafio.myfinance.ADD_RESULT_MESSAGE"
    }

    private lateinit var binding: ActivityAddBinding
    private val viewModel by viewModels<AddViewModel>()

    // custom datePicker layout
    private lateinit var datePickerBtn: DatePickerButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        binding.viewModel = viewModel
        viewModel.listener = this

        intent.getIntExtra(REQUEST_CODE_KEY, 0).also { code ->
            viewModel.requestCode = code
            initLayout(code)
        }

        binding.priceTextInputLayout.setStartIconDrawable(
            when (getString(R.string.currency)) {
                "€" -> R.drawable.ic_euro
                "$" -> R.drawable.ic_attach_money
                else -> R.drawable.ic_euro
            }
        )
    }

    private val categoryViewListener = View.OnClickListener {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setIcon(R.drawable.ic_tag)
        builder.setTitle(getString(R.string.category))
        builder.setSingleChoiceItems(
            resources.getStringArray(R.array.categories),
            when (viewModel.category) {
                DbPurchases.CATEGORIES.HOUSING.value -> 0
                DbPurchases.CATEGORIES.GROCERIES.value -> 1
                DbPurchases.CATEGORIES.PERSONAL_CARE.value -> 2
                DbPurchases.CATEGORIES.ENTERTAINMENT.value -> 3
                DbPurchases.CATEGORIES.EDUCATION.value -> 4
                DbPurchases.CATEGORIES.DINING.value -> 5
                DbPurchases.CATEGORIES.HEALTH.value -> 6
                DbPurchases.CATEGORIES.TRANSPORTATION.value -> 7
                DbPurchases.CATEGORIES.MISCELLANEOUS.value -> 8
                else -> -1 // error, do not select a default option
            },
            categoryListener
        )
        builder.show()
    }

    private val categoryListener = DialogInterface.OnClickListener { dialog, selectedItem ->
        val categories = resources.getStringArray(R.array.categories)
        if (selectedItem >= 0 && selectedItem < categories.size) {
            binding.categoryAutoCompleteTV.setText(categories[selectedItem])
            binding.categoryTextInputLayout.setStartIconDrawable(
                when (selectedItem) {
                    DbPurchases.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                    DbPurchases.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                    DbPurchases.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                    DbPurchases.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                    DbPurchases.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                    DbPurchases.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                    DbPurchases.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                    DbPurchases.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                    DbPurchases.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                    else -> R.drawable.ic_tag
                }
            )
            viewModel.category = selectedItem
        } else {
            viewModel.category = -1
        }

        dialog.dismiss()
    }

    private fun initLayout(code: Int) {
        datePickerBtn = object : DatePickerButton(
            binding.datePickerTextInputLayout,
            binding.dateAutoCompleteTV,
            this@AddActivity
        ) {
            override fun onPositiveBtnClickListener() {
                super.onPositiveBtnClickListener()
                viewModel.year = year
                viewModel.month = month
                viewModel.day = day
                viewModel.dateString = dateString
            }
        }

        binding.categoryAutoCompleteTV.setOnClickListener(categoryViewListener)
        binding.categoryTextInputLayout.setEndIconOnClickListener(categoryViewListener)

        when (code) {
            REQUEST_ADD_CODE -> {
                viewModel.category = -1
            }

            REQUEST_EDIT_CODE -> {
                intent.also { intent ->
                    intent.getStringExtra(PURCHASE_ID_KEY)?.let {
                        viewModel.purchaseID = it
                    }
                    intent.getStringExtra(PURCHASE_NAME_KEY)?.let {
                        viewModel.name = it
                    }
                    intent.getDoubleExtra(PURCHASE_PRICE_KEY, 0.0)
                        .also {
                            viewModel.priceString = doubleToString(it)
                            viewModel.purchasePrice = it
                        }
                    intent.getIntExtra(PURCHASE_CATEGORY_KEY, 0).also {
                        viewModel.category = it
                    }
                    intent.getIntExtra(PURCHASE_POSITION_KEY, 0)
                        .also {
                            viewModel.purchasePosition = it
                        }
                    intent.getIntExtra(PURCHASE_YEAR_KEY, 0).also {
                        datePickerBtn.year = it
                    }
                    intent.getIntExtra(PURCHASE_MONTH_KEY, 0).also {
                        datePickerBtn.month = it
                    }
                    intent.getIntExtra(PURCHASE_DAY_KEY, 0).also {
                        datePickerBtn.day = it
                    }
                }

                val categories = resources.getStringArray(R.array.categories)
                if (viewModel.category != null && viewModel.category!! >= 0 && viewModel.category!! < categories.size) {
                    binding.categoryAutoCompleteTV.setText(categories[viewModel.category!!])
                    binding.categoryTextInputLayout.setStartIconDrawable(
                        when (viewModel.category) {
                            DbPurchases.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                            DbPurchases.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                            DbPurchases.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                            DbPurchases.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                            DbPurchases.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                            DbPurchases.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                            DbPurchases.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                            DbPurchases.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                            DbPurchases.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                            else -> R.drawable.ic_tag
                        }
                    )
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

        binding.nameTextInputLayout.isErrorEnabled = false
        binding.priceTextInputLayout.isErrorEnabled = false
        binding.categoryTextInputLayout.isErrorEnabled = false

        binding.addAddButton.isEnabled = false
    }

    override fun onAddSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this) { result ->
            if (result.code != PurchaseCode.PURCHASE_ADD_SUCCESS.code) {
                binding.addProgressIndicator.hide()
                binding.addAddButton.isEnabled = true
            }

            when (result.code) {
                PurchaseCode.PURCHASE_ADD_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        val payload = result.message.split("&")
                        it.putExtra(PURCHASE_REQUEST_KEY, true)
                        it.putExtra(ADD_RESULT_MESSAGE, payload[0])
                        it.putExtra(PURCHASE_POSITION_KEY, payload[1].toInt())
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                PurchaseCode.PURCHASE_EDIT_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(PURCHASE_REQUEST_KEY, true)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                else -> snackBar(result.message, binding.addAddButton)
            }
        }
    }

    override fun onAddFailure(result: PurchaseResult) {
        binding.addProgressIndicator.hide()
        binding.addAddButton.isEnabled = true

        when (result.code) {
            PurchaseCode.EMPTY_NAME.code,
            PurchaseCode.WRONG_NAME_TOTAL.code ->
                binding.nameTextInputLayout.error = result.message

            PurchaseCode.EMPTY_PRICE.code ->
                binding.priceTextInputLayout.error = result.message

            PurchaseCode.EMPTY_CATEGORY.code ->
                binding.categoryTextInputLayout.error = result.message
        }
    }
}