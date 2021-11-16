package com.frafio.myfinance.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
import com.frafio.myfinance.utils.snackbar
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

        resources.getStringArray(R.array.types).also { items ->
            ArrayAdapter(
                applicationContext,
                R.layout.layout_type_dropdown_item,
                items
            ).also { adapter ->
                binding.typeAutoCompleteTV.also { autoCompleteTextView ->
                    autoCompleteTextView.setAdapter(adapter)
                    autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                        if (binding.addNameEditText.text.toString() == DbPurchases.NAMES.AFFITTO.value
                            || (binding.addNameEditText.text.toString() == DbPurchases.NAMES.TOTALE.value
                                    && binding.addPriceEditText.text.toString() == DbPurchases.NAMES.TOTALE_ZERO.value)
                        ) {
                            binding.addNameEditText.clearText()
                            binding.addNameEditText.isEnabled = true

                            binding.addPriceEditText.clearText()
                            binding.addPriceEditText.isEnabled = true
                        }

                        (parent.getItemAtPosition(position) as String).also { item ->
                            when (item) {
                                items[0] -> {
                                    viewModel.type = DbPurchases.TYPES.GENERIC.value
                                }

                                items[1] -> {
                                    viewModel.type = DbPurchases.TYPES.SHOPPING.value
                                }

                                items[2] -> {
                                    viewModel.type = DbPurchases.TYPES.TRANSPORT.value
                                }

                                items[3] -> {
                                    binding.addNameEditText.setText(DbPurchases.NAMES.AFFITTO.value)
                                    binding.addNameEditText.isEnabled = false
                                    viewModel.type = DbPurchases.TYPES.RENT.value
                                }

                                items[4] -> {
                                    binding.addNameEditText.setText(DbPurchases.NAMES.TOTALE.value)
                                    binding.addNameEditText.isEnabled = false

                                    binding.addPriceEditText.setText(DbPurchases.NAMES.TOTALE_ZERO.value)
                                    binding.addPriceEditText.isEnabled = false

                                    binding.addNameEditText.error = null
                                    binding.addPriceEditText.error = null

                                    viewModel.type = DbPurchases.TYPES.TOTAL.value
                                }
                            }
                        }
                    }
                }
            }
        }

        if (code == ADD_PURCHASE_CODE) {
            viewModel.type = DbPurchases.TYPES.GENERIC.value
        } else if (code == EDIT_PURCHASE_CODE) {
            binding.typeTextInputLayout.isEnabled = false

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
                    binding.typeAutoCompleteTV.setText(getString(R.string.generico))
                }

                DbPurchases.TYPES.SHOPPING.value -> {
                    binding.typeAutoCompleteTV.setText(getString(R.string.spesa))
                }

                DbPurchases.TYPES.TRANSPORT.value -> {
                    binding.typeAutoCompleteTV.setText(getString(R.string.trasporti))
                }

                DbPurchases.TYPES.RENT.value -> {
                    binding.typeAutoCompleteTV.setText(getString(R.string.affitto))
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

                else -> snackbar(result.message, binding.addAddButton)
            }
        })
    }

    override fun onAddFailure(result: PurchaseResult) {
        binding.addProgressIndicator.hide()

        when (result.code) {
            PurchaseCode.EMPTY_NAME.code -> binding.addNameEditText.error = result.message

            PurchaseCode.WRONG_NAME_TOTAL.code -> binding.addNameEditText.error = result.message

            PurchaseCode.EMPTY_PRICE.code -> binding.addPriceEditText.error = result.message
        }
    }
}