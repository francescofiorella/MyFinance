package com.frafio.myfinance.ui.home.list.invoice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.databinding.LayoutInvoiceItemRvBinding

class InvoiceItemAdapter(
    private var invoiceItems: List<InvoiceItem>,
    private val listener: InvoiceItemLongClickListener
) : RecyclerView.Adapter<InvoiceItemAdapter.InvoiceItemViewHolder>() {

    inner class InvoiceItemViewHolder(
        val recyclerViewInvoiceItemBinding: LayoutInvoiceItemRvBinding
    ) : RecyclerView.ViewHolder(recyclerViewInvoiceItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InvoiceItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_invoice_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: InvoiceItemAdapter.InvoiceItemViewHolder, position: Int) {
        holder.recyclerViewInvoiceItemBinding.invoiceItem = invoiceItems[position]

        holder.recyclerViewInvoiceItemBinding.root.setOnLongClickListener {
            listener.onItemLongClick(invoiceItems[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return invoiceItems.size
    }
}