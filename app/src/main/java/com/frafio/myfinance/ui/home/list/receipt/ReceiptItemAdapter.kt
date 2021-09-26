package com.frafio.myfinance.ui.home.list.receipt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.ReceiptItem
import com.frafio.myfinance.databinding.LayoutReceiptItemRvBinding

class ReceiptItemAdapter(
    options: FirestoreRecyclerOptions<ReceiptItem>,
    private val listener: ReceiptItemLongClickListener
) : FirestoreRecyclerAdapter<ReceiptItem, ReceiptItemAdapter.ReceiptItemViewHolder>(options) {

    inner class ReceiptItemViewHolder(
        val recyclerViewReceiptItemBinding: LayoutReceiptItemRvBinding
    ) : RecyclerView.ViewHolder(recyclerViewReceiptItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemViewHolder =
        ReceiptItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_receipt_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: ReceiptItemViewHolder,
        position: Int,
        model: ReceiptItem
    ) {
        holder.recyclerViewReceiptItemBinding.receiptItem = model

        holder.recyclerViewReceiptItemBinding.root.setOnLongClickListener {
            model.id = snapshots.getSnapshot(position).id
            listener.onItemLongClick(model)
            true
        }
    }

}