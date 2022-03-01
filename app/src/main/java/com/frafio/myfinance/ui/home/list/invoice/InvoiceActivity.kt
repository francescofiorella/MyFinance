package com.frafio.myfinance.ui.home.list.invoice

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.databinding.ActivityInvoiceBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.generic.instance

class InvoiceActivity : BaseActivity(), InvoiceItemLongClickListener, InvoiceListener {

    companion object {
        const val INTENT_PURCHASE_ID = "com.frafio.myfinance.PURCHASE_ID"
        const val INTENT_PURCHASE_NAME = "com.frafio.myfinance.PURCHASE_NAME"
        const val INTENT_PURCHASE_PRICE = "com.frafio.myfinance.PURCHASE_PRICE"
    }

    private lateinit var binding: ActivityInvoiceBinding
    private lateinit var viewModel: InvoiceViewModel

    private val factory: InvoiceViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice)
        viewModel = ViewModelProvider(this, factory)[InvoiceViewModel::class.java]
        binding.viewModel = viewModel

        viewModel.listener = this

        // retrieve purchase data from intent
        intent.getStringExtra(INTENT_PURCHASE_ID)?.let {
            viewModel.purchaseID = it
        }
        intent.getStringExtra(INTENT_PURCHASE_NAME)?.let {
            viewModel.purchaseName = it
        }
        intent.getStringExtra(INTENT_PURCHASE_PRICE)?.let {
            viewModel.purchasePrice = it
        }

        binding.invoiceRecView.also {
            it.setHasFixedSize(true)
            it.adapter = InvoiceItemAdapter(viewModel.getOptions(), this)
        }
    }

    //start&stop listening
    override fun onStart() {
        super.onStart()
        (binding.invoiceRecView.adapter as InvoiceItemAdapter).startListening()
    }

    override fun onStop() {
        super.onStop()
        (binding.invoiceRecView.adapter as InvoiceItemAdapter).stopListening()
    }

    // purchaseInteractionListener
    override fun onItemLongClick(invoiceItem: InvoiceItem) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setIcon(R.drawable.ic_delete)
        builder.setTitle(invoiceItem.name)
        builder.setMessage(getString(R.string.invoice_delete_dialog))
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.setPositiveButton(R.string.delete) { _, _ ->
            viewModel.onDeleteClick(invoiceItem)
        }
        builder.show()
    }

    override fun onLoadStarted() {
        binding.invoiceProgressIndicator.show()
        binding.invoiceNameEditText.isEnabled = false
        binding.invoicePriceEditText.isEnabled = false
    }

    override fun onLoadSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this) { result ->
            when (result.code) {
                PurchaseCode.INVOICE_ADD_SUCCESS.code -> {
                    snackBar(result.message, binding.invoiceNameEditText)
                    binding.invoiceNameEditText.also {
                        it.clearText()
                        it.isEnabled = true
                    }
                    binding.invoicePriceEditText.also {
                        it.clearText()
                        it.isEnabled = true
                    }
                    binding.invoiceProgressIndicator.hide()
                }

                else -> {
                    snackBar(result.message, binding.invoiceNameEditText)
                    binding.invoiceProgressIndicator.hide()
                }
            }
        }
    }

    override fun onLoadFailure(result: PurchaseResult) {
        when (result.code) {
            PurchaseCode.EMPTY_NAME.code -> binding.invoiceNameEditText.error = result.message

            PurchaseCode.EMPTY_PRICE.code -> binding.invoicePriceEditText.error = result.message
        }

        binding.invoiceNameEditText.isEnabled = true
        binding.invoicePriceEditText.isEnabled = true

        binding.invoiceProgressIndicator.hide()
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}