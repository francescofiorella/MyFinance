package com.frafio.myfinance.ui.home.list

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.databinding.FragmentListBinding
import com.frafio.myfinance.databinding.RecyclerViewPurchaseItemBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class PurchaseAdapter : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    // utile quando si elimina un acquisto
    private var totPosition = 0

    inner class PurchaseViewHolder(
        val viewDataBinding: RecyclerViewPurchaseItemBinding
        ) : RecyclerView.ViewHolder(viewDataBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = RecyclerViewPurchaseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PurchaseViewHolder(binding)
        /*val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.recycler_view_purchase_item, parent, false)
        return PurchaseViewHolder(view)*/
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val currentPurchaseID = PurchaseManager.getPurchaseList()[position].first
        val currentPurchase = PurchaseManager.getPurchaseList()[position].second

        val locale = Locale("en", "UK")
        val nf = NumberFormat.getInstance(locale)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("###,###,##0.00")
        val priceString = "€ " + formatter.format(currentPurchase.price)

        holder.viewDataBinding.recViewPurchaseItemPriceTextView.text = priceString
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
        return PurchaseManager.getPurchaseList().size
    }
}