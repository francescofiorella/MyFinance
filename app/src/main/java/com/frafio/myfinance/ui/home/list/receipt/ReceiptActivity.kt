package com.frafio.myfinance.ui.home.list.receipt

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCodeIT
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.ReceiptItem
import com.frafio.myfinance.databinding.ActivityReceiptBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.utils.clearText
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.generic.instance

class ReceiptActivity : BaseActivity(), ReceiptItemLongClickListener, ReceiptListener {

    companion object {
        const val INTENT_PURCHASE_ID = "com.frafio.myfinance.PURCHASE_ID"
        const val INTENT_PURCHASE_NAME = "com.frafio.myfinance.PURCHASE_NAME"
        const val INTENT_PURCHASE_PRICE = "com.frafio.myfinance.PURCHASE_PRICE"
    }

    private lateinit var binding: ActivityReceiptBinding
    private lateinit var viewModel: ReceiptViewModel

    private val factory: ReceiptViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_receipt)
        viewModel = ViewModelProvider(this, factory).get(ReceiptViewModel::class.java)
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

        binding.receiptRecView.also {
            it.setHasFixedSize(true)
            it.adapter = ReceiptItemAdapter(viewModel.getOptions(), this)
        }
    }

    //start&stop listening
    override fun onStart() {
        super.onStart()
        (binding.receiptRecView.adapter as ReceiptItemAdapter).startListening()
    }

    override fun onStop() {
        super.onStop()
        (binding.receiptRecView.adapter as ReceiptItemAdapter).stopListening()
    }

    // purchaseInteractionListener
    override fun onItemLongClick(receiptItem: ReceiptItem) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setIcon(R.drawable.ic_delete)
        builder.setTitle(receiptItem.name)
        builder.setMessage(getString(R.string.receipt_delete_dialog))
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.setPositiveButton(R.string.delete) { _, _ ->
            viewModel.onDeleteClick(receiptItem)
        }
        builder.show()
    }

    override fun onLoadStarted() {
        binding.receiptProgressIndicator.show()
        binding.receiptNameEditText.isEnabled = false
        binding.receiptPriceEditText.isEnabled = false
    }

    override fun onLoadSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this, { result ->
            when (result.code) {
                PurchaseCodeIT.RECEIPT_ADD_SUCCESS.code -> {
                    snackBar(result.message, binding.receiptNameEditText)
                    binding.receiptNameEditText.also{
                        it.clearText()
                        it.isEnabled = true
                    }
                    binding.receiptPriceEditText.also{
                        it.clearText()
                        it.isEnabled = true
                    }
                    binding.receiptProgressIndicator.hide()
                }

                else -> {
                    snackBar(result.message, binding.receiptNameEditText)
                    binding.receiptProgressIndicator.hide()
                }
            }
        })
    }

    override fun onLoadFailure(result: PurchaseResult) {
        when (result.code) {
            PurchaseCodeIT.EMPTY_NAME.code -> binding.receiptNameEditText.error = result.message

            PurchaseCodeIT.EMPTY_PRICE.code -> binding.receiptPriceEditText.error = result.message
        }
        binding.receiptProgressIndicator.hide()
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}