package com.frafio.myfinance.ui.home.list.invoice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.databinding.LayoutInvoiceItemRvBinding

class InvoiceItemAdapter(
    options: FirestoreRecyclerOptions<InvoiceItem>,
    private val listener: InvoiceItemLongClickListener
) : FirestoreRecyclerAdapter<InvoiceItem, InvoiceItemAdapter.InvoiceItemViewHolder>(options) {

    inner class InvoiceItemViewHolder(
        val recyclerViewInvoiceItemBinding: LayoutInvoiceItemRvBinding
    ) : RecyclerView.ViewHolder(recyclerViewInvoiceItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceItemViewHolder =
        InvoiceItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_invoice_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: InvoiceItemViewHolder,
        position: Int,
        model: InvoiceItem
    ) {
        holder.recyclerViewInvoiceItemBinding.invoiceItem = model

        holder.recyclerViewInvoiceItemBinding.root.setOnLongClickListener {
            model.id = snapshots.getSnapshot(position).id
            listener.onItemLongClick(model)
            true
        }
    }

}