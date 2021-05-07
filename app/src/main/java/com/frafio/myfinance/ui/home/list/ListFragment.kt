package com.frafio.myfinance.ui.home.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.ui.store.AddActivity
import com.frafio.myfinance.R
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.list.receipt.ReceiptActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ListFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    lateinit var mWarningTV: TextView

    // utile quando si elimina un acquisto
    var totPosition = 0
    companion object {
        private val TAG: String = ListFragment::class.java.getSimpleName()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        mRecyclerView = view.findViewById(R.id.list_recyclerView)
        mWarningTV = view.findViewById(R.id.list_warningTV)

        if (PurchaseManager.getPurchaseList().isEmpty()) {
            mWarningTV.visibility = View.VISIBLE
            mRecyclerView.visibility = View.GONE
        } else {
            mWarningTV.visibility = View.GONE
            mRecyclerView.visibility = View.VISIBLE
            loadPurchasesList()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (PurchaseManager.getPurchaseList().isEmpty()) {
            mWarningTV.visibility = View.VISIBLE
            mRecyclerView.visibility = View.GONE
        } else {
            mWarningTV.visibility = View.GONE
            mRecyclerView.visibility = View.VISIBLE
        }
    }

    fun loadPurchasesList() {
        val mAdapter = PurchaseAdapter(activity, context, mRecyclerView, mWarningTV, totPosition)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    class PurchaseAdapter(val activity: Activity?, val context: Context?, val mRecyclerView: RecyclerView, val mWarningTV: TextView, var totPosition: Int) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.layout_recycler_view_purchase_item, parent, false)
            return PurchaseViewHolder(view)
        }

        override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
            val currentPurchase = PurchaseManager.getPurchaseList()[position].second
            val currentPurchaseID = PurchaseManager.getPurchaseList()[position].first
            val locale = Locale("en", "UK")
            val nf = NumberFormat.getInstance(locale)
            val formatter = nf as DecimalFormat
            formatter.applyPattern("###,###,##0.00")
            val priceString = "€ " + formatter.format(currentPurchase.price)
            holder.prezzoTV?.text = priceString

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
            }
        }

        override fun getItemCount(): Int {
            return PurchaseManager.getPurchaseList().size
        }

        class PurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
    }

    fun scrollListToTop() {
        mRecyclerView.smoothScrollToPosition(0)
    }
}