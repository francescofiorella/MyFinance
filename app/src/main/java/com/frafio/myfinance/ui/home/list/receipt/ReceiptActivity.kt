package com.frafio.myfinance.ui.home.list.receipt

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.ReceiptItem
import com.frafio.myfinance.databinding.ActivityReceiptBinding
import com.frafio.myfinance.ui.BaseActivity
import com.frafio.myfinance.utils.snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.generic.instance

class ReceiptActivity : BaseActivity(), ReceiptItemLongClickListener, ReceiptListener {

    private lateinit var binding: ActivityReceiptBinding
    private lateinit var viewModel: ReceiptViewModel

    private val factory: ReceiptViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_receipt)
        viewModel = ViewModelProvider(this, factory).get(ReceiptViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.listener = this

        // toolbar
        setSupportActionBar(binding.receiptToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // retrieve purchase data from intent
        intent.getStringExtra("${getString(R.string.default_path)}.purchaseID")?.let {
            viewModel.purchaseID = it
        }
        intent.getStringExtra("${getString(R.string.default_path)}.purchaseName")?.let {
            viewModel.purchaseName = it
        }
        intent.getStringExtra("${getString(R.string.default_path)}.purchasePrice")?.let {
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
        val builder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MyFinance_AlertDialog)
        builder.setTitle(receiptItem.name)
        builder.setMessage(getString(R.string.receipt_delete_dialog))
        builder.setNegativeButton(getString(R.string.annulla), null)
        builder.setPositiveButton(R.string.elimina) { _, _ ->
            viewModel.onDeleteClick(receiptItem)
        }
        builder.show()
    }

    // receiptListener
    override fun onLoadSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this, { result ->
            when (result.code) {
                PurchaseCode.RECEIPT_ADD_SUCCESS.code -> {
                    binding.root.snackbar(result.message, binding.receiptNameEditText)
                    binding.receiptNameEditText.setText("")
                    binding.receiptPriceEditText.setText("")
                }

                else ->
                    binding.root.snackbar(result.message, binding.receiptNameEditText)
            }
        })
    }

    override fun onLoadFailure(result: PurchaseResult) {
        when (result.code) {
            PurchaseCode.EMPTY_NAME.code -> binding.receiptNameEditText.error = result.message
            PurchaseCode.EMPTY_PRICE.code -> binding.receiptPriceEditText.error = result.message
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
}