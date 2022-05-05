package com.frafio.myfinance.ui.home.list.invoice

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.databinding.ActivityInvoiceBinding
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InvoiceActivity : AppCompatActivity(), InvoiceItemLongClickListener, InvoiceListener {

    companion object {
        const val PURCHASE_ID_KEY = "com.frafio.myfinance.PURCHASE_ID"
        const val PURCHASE_NAME_KEY = "com.frafio.myfinance.PURCHASE_NAME"
        const val PURCHASE_PRICE_KEY = "com.frafio.myfinance.PURCHASE_PRICE"
    }

    private lateinit var binding: ActivityInvoiceBinding
    private val viewModel by viewModels<InvoiceViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice)
        binding.viewModel = viewModel

        viewModel.listener = this

        // retrieve purchase data from intent
        intent.getStringExtra(PURCHASE_ID_KEY)?.let {
            viewModel.purchaseID = it
        }
        intent.getStringExtra(PURCHASE_NAME_KEY)?.let {
            viewModel.purchaseName = it
        }
        intent.getStringExtra(PURCHASE_PRICE_KEY)?.let {
            viewModel.purchasePrice = it
        }

        viewModel.getInvoiceItems().observe(this) { invoiceItems ->
            binding.invoiceRecView.also {
                it.setHasFixedSize(true)
                it.adapter = InvoiceItemAdapter(invoiceItems, this)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PURCHASE_ID_KEY, viewModel.purchaseID)
        outState.putString(PURCHASE_NAME_KEY, viewModel.purchaseName)
        outState.putString(PURCHASE_PRICE_KEY, viewModel.purchasePrice)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString(PURCHASE_ID_KEY)?.let {
            viewModel.purchaseID = it
        }
        savedInstanceState.getString(PURCHASE_NAME_KEY)?.let {
            viewModel.purchaseName = it
        }
        savedInstanceState.getString(PURCHASE_PRICE_KEY)?.let {
            viewModel.purchasePrice = it
        }
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
        binding.invoiceAddBtn.isEnabled = false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onLoadSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this) { result ->
            when (result.code) {
                PurchaseCode.INVOICE_ADD_SUCCESS.code -> {
                    snackBar(result.message, binding.invoiceNameEditText)
                    (binding.invoiceRecView.adapter as InvoiceItemAdapter).notifyDataSetChanged()

                    binding.invoiceNameEditText.also {
                        it.clearText()
                        it.isEnabled = true
                    }
                    binding.invoicePriceEditText.also {
                        it.clearText()
                        it.isEnabled = true
                    }
                }

                PurchaseCode.INVOICE_DELETE_SUCCESS.code -> {
                    snackBar(result.message, binding.invoiceNameEditText)
                    (binding.invoiceRecView.adapter as InvoiceItemAdapter).notifyDataSetChanged()
                }

                else -> snackBar(result.message, binding.invoiceNameEditText)
            }

            binding.invoiceProgressIndicator.hide()
            binding.invoiceAddBtn.isEnabled = true
        }
    }

    override fun onLoadFailure(result: PurchaseResult) {
        when (result.code) {
            PurchaseCode.EMPTY_NAME.code -> binding.invoiceNameEditText.error = result.message

            PurchaseCode.EMPTY_PRICE.code -> binding.invoicePriceEditText.error = result.message
        }

        binding.invoiceNameEditText.isEnabled = true
        binding.invoicePriceEditText.isEnabled = true
        binding.invoiceAddBtn.isEnabled = true

        binding.invoiceProgressIndicator.hide()
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}