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
import java.util.*

class AddActivity : BaseActivity(), AddListener {

    companion object {
        const val ADD_PURCHASE_CODE: Int = 1
        const val EDIT_PURCHASE_CODE: Int = 2
    }

    private lateinit var binding: ActivityAddBinding
    private lateinit var viewModel: AddViewModel

    private val factory: AddViewModelFactory by instance()

    // custom datePicker layout
    private lateinit var datePickerBtn: DatePickerButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        viewModel = ViewModelProvider(this, factory).get(AddViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.listener = this

        intent.getIntExtra("${getString(R.string.default_path)}.REQUESTCODE", 0).also { code ->
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
        if (binding.nameEditText.text.toString() == DbPurchases.NAMES.TOTAL.value
            && binding.priceEditText.text.toString() == DbPurchases.NAMES.TOTAL_PRICE.value
        ) {
            binding.nameEditText.clearText()
            binding.nameTextInputLayout.isEnabled = true

            binding.priceEditText.clearText()
            binding.priceTextInputLayout.isEnabled = true
        } else if (binding.nameEditText.text.toString() == DbPurchases.NAMES.RENT.value) {
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
                binding.typeAutoCompleteTV.setText(getString(R.string.zero_purchase))
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

        if (code == ADD_PURCHASE_CODE) {
            viewModel.type = DbPurchases.TYPES.GENERIC.value
        } else if (code == EDIT_PURCHASE_CODE) {
            intent.also { intent ->
                intent.getStringExtra("${getString(R.string.default_path)}.PURCHASE_ID")?.let {
                    viewModel.purchaseID = it
                }
                intent.getStringExtra("${getString(R.string.default_path)}.PURCHASE_NAME")?.let {
                    viewModel.name = it
                }
                intent.getDoubleExtra("${getString(R.string.default_path)}.PURCHASE_PRICE", 0.0)
                    .also {
                        viewModel.priceString = doubleToString(it)
                        viewModel.purchasePrice = it
                    }
                intent.getIntExtra("${getString(R.string.default_path)}.PURCHASE_TYPE", 0).also {
                    viewModel.purchaseType = it
                }
                intent.getIntExtra("${getString(R.string.default_path)}.PURCHASE_POSITION", 0)
                    .also {
                        viewModel.purchasePosition = it
                    }
                intent.getIntExtra("${getString(R.string.default_path)}.PURCHASE_YEAR", 0).also {
                    datePickerBtn.year = it
                }
                intent.getIntExtra("${getString(R.string.default_path)}.PURCHASE_MONTH", 0).also {
                    datePickerBtn.month = it
                }
                intent.getIntExtra("${getString(R.string.default_path)}.PURCHASE_DAY", 0).also {
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

        viewModel.updateTime(datePickerBtn)
    }

    fun onBackClick(view: View) {
        onBackPressed()
    }

    override fun onAddStart() {
        binding.addProgressIndicator.show()
    }

    override fun onAddSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this, { result ->
            if (result.code != PurchaseCode.TOTAL_ADD_SUCCESS.code) {
                binding.addProgressIndicator.hide()
            }

            when (result.code) {
                PurchaseCode.TOTAL_ADD_SUCCESS.code -> viewModel.updateLocalList()

                PurchaseCode.PURCHASE_EDIT_SUCCESS.code -> {
                    // torna alla home
                    Intent().also {
                        it.putExtra("${getString(R.string.default_path)}.purchaseRequest", true)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {
                    // torna alla home
                    Intent().also {
                        it.putExtra("${getString(R.string.default_path)}.purchaseRequest", true)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                else -> snackBar(result.message, binding.addAddButton)
            }
        })
    }

    override fun onAddFailure(result: PurchaseResult) {
        binding.addProgressIndicator.hide()

        when (result.code) {
            PurchaseCode.EMPTY_NAME.code -> binding.nameTextInputLayout.error = result.message

            PurchaseCode.WRONG_NAME_TOTAL.code -> binding.nameTextInputLayout.error = result.message

            PurchaseCode.EMPTY_PRICE.code -> binding.priceTextInputLayout.error = result.message
        }
    }
}