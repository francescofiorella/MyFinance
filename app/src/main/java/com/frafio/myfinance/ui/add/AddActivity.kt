package com.frafio.myfinance.ui.add

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.DatePickerButton
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.ActivityAddBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.generic.instance

class AddActivity : BaseActivity(), AddListener {

    companion object {
        const val INTENT_REQUEST_CODE: String = "com.frafio.myfinance.REQUEST_CODE"
        const val INTENT_REQUEST_ADD_CODE: Int = 1
        const val INTENT_REQUEST_EDIT_CODE: Int = 2
        const val INTENT_PURCHASE_REQUEST: String = "com.frafio.myfinance.PURCHASE_REQUEST"
        const val INTENT_PURCHASE_ID: String = "com.frafio.myfinance.PURCHASE_ID"
        const val INTENT_PURCHASE_NAME: String = "com.frafio.myfinance.PURCHASE_NAME"
        const val INTENT_PURCHASE_PRICE: String = "com.frafio.myfinance.PURCHASE_PRICE"
        const val INTENT_PURCHASE_TYPE: String = "com.frafio.myfinance.PURCHASE_TYPE"
        const val INTENT_PURCHASE_POSITION: String = "com.frafio.myfinance.PURCHASE_POSITION"
        const val INTENT_PURCHASE_YEAR: String = "com.frafio.myfinance.PURCHASE_YEAR"
        const val INTENT_PURCHASE_MONTH: String = "com.frafio.myfinance.PURCHASE_MONTH"
        const val INTENT_PURCHASE_DAY: String = "com.frafio.myfinance.PURCHASE_DAY"
    }

    private lateinit var binding: ActivityAddBinding
    private lateinit var viewModel: AddViewModel

    private val factory: AddViewModelFactory by instance()

    // custom datePicker layout
    private lateinit var datePickerBtn: DatePickerButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        viewModel = ViewModelProvider(this, factory)[AddViewModel::class.java]
        binding.viewModel = viewModel
        viewModel.listener = this

        intent.getIntExtra(INTENT_REQUEST_CODE, 0).also { code ->
            viewModel.requestCode = code
            initLayout(code)
        }
    }

    private val typeViewListener = View.OnClickListener {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setIcon(R.drawable.ic_numbers)
        builder.setTitle(getString(R.string.type))
        builder.setSingleChoiceItems(
            resources.getStringArray(R.array.types),
            when (viewModel.type) {
                DbPurchases.TYPES.GENERIC.value -> 0
                DbPurchases.TYPES.SHOPPING.value -> 1
                DbPurchases.TYPES.TRANSPORT.value -> 2
                DbPurchases.TYPES.RENT.value -> 3
                DbPurchases.TYPES.TOTAL.value -> 4
                else -> -1 // error, do not select a default option
            },
            typeListener
        )
        builder.show()
    }

    private val typeListener = DialogInterface.OnClickListener { dialog, selectedItem ->
        if ((binding.nameEditText.text.toString() == DbPurchases.NAMES.TOTAL.value_en
                    || binding.nameEditText.text.toString() == DbPurchases.NAMES.TOTAL.value_it)
            && (binding.priceEditText.text.toString() == DbPurchases.NAMES.TOTAL_PRICE.value_en
                    || binding.priceEditText.text.toString() == DbPurchases.NAMES.TOTAL_PRICE.value_it)
        ) {
            binding.nameEditText.clearText()
            binding.nameTextInputLayout.isEnabled = true

            binding.priceEditText.clearText()
            binding.priceTextInputLayout.isEnabled = true
        } else if (binding.nameEditText.text.toString() == DbPurchases.NAMES.RENT.value_en
            || binding.nameEditText.text.toString() == DbPurchases.NAMES.RENT.value_it) {
            binding.nameEditText.clearText()
        }

        when (selectedItem) {
            0 -> {
                binding.typeAutoCompleteTV.setText(getString(R.string.generic))
                viewModel.type = DbPurchases.TYPES.GENERIC.value
            }

            1 -> {
                binding.typeAutoCompleteTV.setText(getString(R.string.shopping))
                viewModel.type = DbPurchases.TYPES.SHOPPING.value
            }

            2 -> {
                binding.typeAutoCompleteTV.setText(getString(R.string.transport))
                viewModel.type = DbPurchases.TYPES.TRANSPORT.value
            }

            3 -> {
                binding.typeAutoCompleteTV.setText(getString(R.string.rent))
                binding.nameEditText.setText(DbPurchases.NAMES.RENT.value)
                viewModel.type = DbPurchases.TYPES.RENT.value
            }

            4 -> {
                binding.typeAutoCompleteTV.setText(getString(R.string.no_purchase))
                binding.nameEditText.setText(DbPurchases.NAMES.TOTAL.value)
                binding.nameTextInputLayout.isEnabled = false

                binding.priceEditText.setText(DbPurchases.NAMES.TOTAL_PRICE.value)
                binding.priceTextInputLayout.isEnabled = false

                binding.nameTextInputLayout.isErrorEnabled = false
                binding.nameTextInputLayout.error = null

                binding.priceTextInputLayout.isErrorEnabled = false
                binding.priceTextInputLayout.error = null

                viewModel.type = DbPurchases.TYPES.TOTAL.value
            }
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

        binding.typeAutoCompleteTV.setOnClickListener(typeViewListener)
        binding.typeTextInputLayout.setEndIconOnClickListener(typeViewListener)

        when (code) {
            INTENT_REQUEST_ADD_CODE -> {
                viewModel.type = DbPurchases.TYPES.GENERIC.value
            }

            INTENT_REQUEST_EDIT_CODE -> {
                intent.also { intent ->
                    intent.getStringExtra(INTENT_PURCHASE_ID)?.let {
                        viewModel.purchaseID = it
                    }
                    intent.getStringExtra(INTENT_PURCHASE_NAME)?.let {
                        viewModel.name = it
                    }
                    intent.getDoubleExtra(INTENT_PURCHASE_PRICE, 0.0)
                        .also {
                            viewModel.priceString = doubleToString(it)
                            viewModel.purchasePrice = it
                        }
                    intent.getIntExtra(INTENT_PURCHASE_TYPE, 0).also {
                        viewModel.purchaseType = it
                    }
                    intent.getIntExtra(INTENT_PURCHASE_POSITION, 0)
                        .also {
                            viewModel.purchasePosition = it
                        }
                    intent.getIntExtra(INTENT_PURCHASE_YEAR, 0).also {
                        datePickerBtn.year = it
                    }
                    intent.getIntExtra(INTENT_PURCHASE_MONTH, 0).also {
                        datePickerBtn.month = it
                    }
                    intent.getIntExtra(INTENT_PURCHASE_DAY, 0).also {
                        datePickerBtn.day = it
                    }
                }

                when (viewModel.purchaseType) {
                    DbPurchases.TYPES.GENERIC.value -> {
                        binding.typeAutoCompleteTV.setText(getString(R.string.generic))
                    }

                    DbPurchases.TYPES.SHOPPING.value -> {
                        binding.typeAutoCompleteTV.setText(getString(R.string.shopping))
                    }

                    DbPurchases.TYPES.TRANSPORT.value -> {
                        binding.typeAutoCompleteTV.setText(getString(R.string.transport))
                    }

                    DbPurchases.TYPES.RENT.value -> {
                        binding.typeAutoCompleteTV.setText(getString(R.string.rent))
                    }
                }
            }
        }

        viewModel.updateTime(datePickerBtn)
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressed()
    }

    override fun onAddStart() {
        binding.addProgressIndicator.show()

        binding.nameTextInputLayout.isErrorEnabled = false
        binding.priceTextInputLayout.isErrorEnabled = false
    }

    override fun onAddSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this) { result ->
            if (result.code != PurchaseCode.TOTAL_ADD_SUCCESS.code) {
                binding.addProgressIndicator.hide()
            }

            when (result.code) {
                PurchaseCode.TOTAL_ADD_SUCCESS.code -> viewModel.updateLocalList()

                PurchaseCode.PURCHASE_EDIT_SUCCESS.code,
                PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(INTENT_PURCHASE_REQUEST, true)
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

        when (result.code) {
            PurchaseCode.EMPTY_NAME.code,
            PurchaseCode.WRONG_NAME_TOTAL.code ->
                binding.nameTextInputLayout.error = result.message

            PurchaseCode.EMPTY_PRICE.code -> binding.priceTextInputLayout.error = result.message
        }
    }
}