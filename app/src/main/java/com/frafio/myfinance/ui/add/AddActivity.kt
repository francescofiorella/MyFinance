package com.frafio.myfinance.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.ButtonTrio
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

    // custom layouts
    private lateinit var typeBtnTrio: ButtonTrio
    private lateinit var ticketBtnTrio: ButtonTrio
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
            ticketBtnTrio.isVisible = false
        }
    }

    private fun initLayout(code: Int) {
        datePickerBtn = object : DatePickerButton(
            binding.addDateLayout,
            binding.addDateTextView,
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

        ticketBtnTrio = object : ButtonTrio(
            binding.addBigliettoLayout,
            binding.addTrenitaliaTv,
            binding.addAmtabTv,
            binding.addAltroTv
        ) {
            override fun onBtn1ClickAction() {
                super.onBtn1ClickAction()
                binding.addNameEditText.setText(DbPurchases.NAMES.TRENITALIA.value)
                binding.addNameEditText.isEnabled = false
            }

            override fun onBtn2ClickAction() {
                super.onBtn2ClickAction()
                binding.addNameEditText.setText(DbPurchases.NAMES.AMTAB.value)
                binding.addNameEditText.isEnabled = false
            }

            override fun onBtn3ClickAction() {
                super.onBtn3ClickAction()
                binding.addNameEditText.clearText()
                binding.addNameEditText.isEnabled = true
            }
        }

        typeBtnTrio = object : ButtonTrio(
            binding.addTypeLayout,
            binding.addGenericoTv,
            binding.addSpesaTv,
            binding.addBigliettoTv
        ) {
            override fun onBtn1ClickAction() {
                super.onBtn1ClickAction()
                ticketBtnTrio.hide(binding.root as ViewGroup)

                if (selectedBtn == Button.BUTTON_3) {
                    binding.addNameEditText.clearText()
                }
                binding.addNameEditText.isEnabled = true

                viewModel.type = DbPurchases.TYPES.GENERIC.value
            }

            override fun onBtn2ClickAction() {
                super.onBtn2ClickAction()
                ticketBtnTrio.hide(binding.root as ViewGroup)

                if (selectedBtn == Button.BUTTON_3) {
                    binding.addNameEditText.clearText()
                }
                binding.addNameEditText.isEnabled = true

                viewModel.type = DbPurchases.TYPES.SHOPPING.value
            }

            override fun onBtn3ClickAction() {
                super.onBtn3ClickAction()
                ticketBtnTrio.show(binding.root as ViewGroup)
                ticketBtnTrio.performClick()

                viewModel.type = DbPurchases.TYPES.TICKET.value
            }
        }

        if (code == ADD_PURCHASE_CODE) {
            setTotSwitch()
        } else if (code == EDIT_PURCHASE_CODE) {
            typeBtnTrio.isEnabled = false
            setTicket()

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

            datePickerBtn.isEnabled = false

            when (viewModel.purchaseType) {
                DbPurchases.TYPES.GENERIC.value -> {
                    typeBtnTrio.selectedBtn = ButtonTrio.Button.BUTTON_1
                    typeBtnTrio.enableOnlySelectedBtn()
                }

                DbPurchases.TYPES.SHOPPING.value -> {
                    typeBtnTrio.selectedBtn = ButtonTrio.Button.BUTTON_2
                    typeBtnTrio.enableOnlySelectedBtn()
                }

                DbPurchases.TYPES.TICKET.value -> {
                    typeBtnTrio.selectedBtn = ButtonTrio.Button.BUTTON_3
                    typeBtnTrio.enableOnlySelectedBtn()
                }
            }
        }

        viewModel.updateTime(datePickerBtn)
    }

    private fun setTotSwitch() {
        binding.addTotaleSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.totChecked = isChecked
            if (isChecked) {
                binding.addNameEditText.setText(DbPurchases.NAMES.TOTALE.value)
                binding.addNameEditText.isEnabled = false

                binding.addPriceEditText.setText(DbPurchases.NAMES.TOTALE_ZERO.value)
                binding.addPriceEditText.isEnabled = false

                binding.addNameEditText.error = null
                binding.addPriceEditText.error = null

                typeBtnTrio.isEnabled = false

                ticketBtnTrio.hide(binding.root as ViewGroup)
            } else {
                binding.addNameEditText.clearText()
                binding.addNameEditText.isEnabled = true

                binding.addPriceEditText.clearText()
                binding.addPriceEditText.isEnabled = true

                typeBtnTrio.isEnabled = true

                typeBtnTrio.performClick()
            }
        }
    }

    private fun setTicket() {
        when (viewModel.name) {
            DbPurchases.NAMES.TRENITALIA.value ->
                ticketBtnTrio.selectedBtn = ButtonTrio.Button.BUTTON_1

            DbPurchases.NAMES.AMTAB.value ->
                ticketBtnTrio.selectedBtn = ButtonTrio.Button.BUTTON_2

            else -> ticketBtnTrio.selectedBtn = ButtonTrio.Button.BUTTON_3
        }
    }

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onBackClick(view: View) {
        finish()
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