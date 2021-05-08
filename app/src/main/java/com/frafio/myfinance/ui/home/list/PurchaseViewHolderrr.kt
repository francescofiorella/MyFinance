package com.frafio.myfinance.ui.home.list

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentListBinding
import com.frafio.myfinance.databinding.RecyclerViewPurchaseItemBinding

class PurchaseViewHolderrr(viewDataBinding: FragmentListBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
    lateinit var binding: RecyclerViewPurchaseItemBinding
    var itemLayout: ConstraintLayout? = null
    var dataLayout: ConstraintLayout? = null
    var dataTV: TextView? = null
    var nomeTV: TextView? = null
    var prezzoTV: TextView? = null

    init {
        itemLayout = itemView.findViewById(R.id.recView_purchaseItem_constraintLayout)
        dataLayout = itemView.findViewById(R.id.recView_purchaseItem_dataLayout)
        dataTV = itemView.findViewById(R.id.recView_purchaseItem_dataTextView)
        nomeTV = itemView.findViewById(R.id.recView_purchaseItem_nomeTextView)
        prezzoTV = itemView.findViewById(R.id.recView_purchaseItem_priceTextView)
    }
}