package com.frafio.myfinance.ui.home.list

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.RecyclerViewPurchaseItemBinding

class PurchaseAdapter(
    private val purchases: List<Purchase>,
    private val listener: RecyclerViewInteractionListener
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    inner class PurchaseViewHolder(
        val recyclerViewPurchaseItemBinding: RecyclerViewPurchaseItemBinding
        ) : RecyclerView.ViewHolder(recyclerViewPurchaseItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.recycler_view_purchase_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val currentPurchase = purchases[position]
        holder.recyclerViewPurchaseItemBinding.purchase = currentPurchase

        holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout.setOnClickListener {
            listener.onRecyclerViewItemInteraction(1, currentPurchase, position)
        }
        holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout.setOnLongClickListener {
            listener.onRecyclerViewItemInteraction(2, currentPurchase, position)
            true
        }

        if (currentPurchase.type == 0) {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemNomeTextView
                .setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        } else {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemNomeTextView
                .setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
        /*
        if (currentPurchase.type == 0) {
            holder.itemLayout?.setOnClickListener(null)
            holder.nomeTV?.text = currentPurchase.name
            holder.nomeTV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            holder.dataLayout?.visibility = View.VISIBLE

            val dayString: String = if (currentPurchase.day!! < 10) {
                "0${currentPurchase.day}"
            } else {
                currentPurchase.day.toString()
            }
            val monthString: String = if (currentPurchase.month!! < 10) {
                "0${currentPurchase.month}"
            } else {
                currentPurchase.month.toString()
            }

            holder.dataTV?.text = "$dayString/$monthString/${currentPurchase.year}"
        } else if (currentPurchase.type == 1) {
            holder.nomeTV?.text = "   ${currentPurchase.name}"
            holder.nomeTV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            holder.dataLayout?.visibility = View.GONE

            holder.itemLayout?.setOnClickListener {
                val intent = Intent(context, ReceiptActivity::class.java)
                intent.putExtra("com.frafio.myfinance.purchaseID", currentPurchaseID)
                intent.putExtra("com.frafio.myfinance.purchaseName", currentPurchase.name)
                intent.putExtra("com.frafio.myfinance.purchasePrice", priceString)
                activity?.startActivity(intent)
            }
        } else {
            holder.itemLayout?.setOnClickListener(null)
            holder.nomeTV?.text = "   ${currentPurchase.name}"
            holder.nomeTV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            holder.dataLayout?.visibility = View.GONE
        }

        if (!(currentPurchase.name == "Totale" && currentPurchase.price != 0.0)) {
            holder.itemLayout?.isEnabled = true
            holder.itemLayout?.setOnLongClickListener {
                val builder = MaterialAlertDialogBuilder(context!!, R.style.ThemeOverlay_MyFinance_AlertDialog)
                builder.setTitle(currentPurchase.name)
                if (!(currentPurchase.name == "Totale" && currentPurchase.price == 0.0)) {
                    builder.setMessage("Vuoi modificare o eliminare l'acquisto selezionato?")
                    builder.setNegativeButton("Modifica") { dialog, which ->
                        val intent = Intent(context, AddActivity::class.java)
                        intent.putExtra("com.frafio.myfinance.REQUESTCODE", 2)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_ID", currentPurchaseID)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_NAME", currentPurchase.name)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_PRICE", currentPurchase.price)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_TYPE", currentPurchase.type)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_POSITION", position)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_YEAR", currentPurchase.year)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_MONTH", currentPurchase.month)
                        intent.putExtra("com.frafio.myfinance.PURCHASE_DAY", currentPurchase.day)
                        activity?.startActivityForResult(intent, 2)
                    }
                } else {
                    builder.setMessage("Vuoi eliminare l'acquisto selezionato?")
                }
                builder.setPositiveButton("Elimina") { dialog, which ->
                    val fStore = FirebaseFirestore.getInstance()
                    fStore.collection("purchases").document(currentPurchaseID).delete().addOnSuccessListener {
                        if (currentPurchase.name == "Totale") {
                            PurchaseManager.removePurchaseAt(position)

                            mRecyclerView.removeViewAt(position)
                            mRecyclerView.adapter?.notifyItemRemoved(position)
                            mRecyclerView.adapter?.notifyItemRangeChanged(position, PurchaseManager.getPurchaseList().size)

                            (activity as HomeActivity).showSnackbar("Totale eliminato!")

                            if (PurchaseManager.getPurchaseList().isEmpty()) {
                                mWarningTV.visibility = View.VISIBLE
                                mRecyclerView.visibility = View.GONE
                            }
                        } else if (currentPurchase.type != 3) {
                            for (i in position - 1 downTo 0) {
                                if (PurchaseManager.getPurchaseAt(i)?.second?.type == 0) {
                                    totPosition = i
                                    val newPurchase = PurchaseManager.getPurchaseAt(totPosition)!!.second
                                    newPurchase.price = newPurchase.price?.minus(
                                        PurchaseManager.getPurchaseAt(position)?.second?.price!!
                                    )
                                    PurchaseManager.updatePurchaseAt(totPosition, newPurchase)
                                    PurchaseManager.removePurchaseAt(position)
                                    val fStore1 = FirebaseFirestore.getInstance()
                                    fStore1.collection("purchases").document(PurchaseManager.getPurchaseAt(i)!!.first)
                                        .set(PurchaseManager.getPurchaseAt(i)!!.second).addOnSuccessListener {
                                            mRecyclerView.removeViewAt(position)
                                            mRecyclerView.adapter?.notifyItemRemoved(position)
                                            mRecyclerView.adapter?.notifyItemRangeChanged(position, PurchaseManager.getPurchaseList().size)
                                            mRecyclerView.adapter?.notifyItemChanged(totPosition)
                                            (activity as HomeActivity).showSnackbar("Acquisto eliminato!")
                                        }.addOnFailureListener { e ->
                                            Log.e(TAG, "Error! ${e.localizedMessage}")
                                            (activity as HomeActivity).showSnackbar("Acquisto non eliminato correttamente!")
                                        }
                                    break
                                }
                            }
                        } else {
                            PurchaseManager.removePurchaseAt(position)
                            mRecyclerView.removeViewAt(position)
                            mRecyclerView.adapter?.notifyItemRemoved(position)
                            mRecyclerView.adapter?.notifyItemRangeChanged(position, PurchaseManager.getPurchaseList().size)
                            (activity as HomeActivity).showSnackbar("Acquisto eliminato!")
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error! ${e.localizedMessage}")
                        (activity as HomeActivity).showSnackbar("Acquisto non eliminato correttamente!")
                    }
                }
                builder.show()
                true
            }
        } else {
            holder.itemLayout?.setOnLongClickListener(null)
            holder.itemLayout?.isEnabled = false
        }*/
    }

    override fun getItemCount(): Int {
        return purchases.size
    }
}