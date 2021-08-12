package com.frafio.myfinance.ui.home.list.receipt

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
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
        intent.getStringExtra("com.frafio.myfinance.purchaseID")?.let {
            viewModel.setPurchaseID(it)
        }
        intent.getStringExtra("com.frafio.myfinance.purchaseName")?.let {
            viewModel.purchaseName = it
        }
        intent.getStringExtra("com.frafio.myfinance.purchasePrice")?.let {
            viewModel.purchasePrice = it
        }

        viewModel.setOptions().also { options ->
            binding.receiptRecView.also {
                it.setHasFixedSize(true)
                it.adapter = ReceiptItemAdapter(options, this)
            }
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

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemLongClick(receiptItem: ReceiptItem) {
        val builder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MyFinance_AlertDialog)
        builder.setTitle(receiptItem.name)
        builder.setMessage("Vuoi eliminare la voce selezionata?")
        builder.setNegativeButton("Annulla", null)
        builder.setPositiveButton("Elimina") { _, _ ->
            viewModel.onDeleteClick(receiptItem)
        }
        builder.show()
    }

    override fun onLoadSuccess(response: LiveData<Any>) {
        response.observe(this, { value ->
            when (value) {
                "Voce aggiunta!" -> {
                    binding.root.snackbar(value as String, binding.receiptNameEditText)
                    binding.receiptNameEditText.setText("")
                    binding.receiptPriceEditText.setText("")
                }

                is String ->
                    binding.root.snackbar(value, binding.receiptNameEditText)
            }
        })
    }

    override fun onLoadFailure(message: Any) {
        when (message) {
            1 -> binding.receiptNameEditText.error = "Inserisci il nome dell'acquisto."
            2 -> binding.receiptPriceEditText.error = "Inserisci il costo dell'acquisto."
        }
    }
}