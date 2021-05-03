package com.frafio.myfinance.ui.fragments

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
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.HomeActivity.Companion.PURCHASE_ID_LIST
import com.frafio.myfinance.ui.home.HomeActivity.Companion.PURCHASE_LIST
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.home.ReceiptActivity
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

        if (PURCHASE_LIST.isEmpty()) {
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
        if (PURCHASE_LIST.isEmpty()) {
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
            val locale = Locale("en", "UK")
            val nf = NumberFormat.getInstance(locale)
            val formatter = nf as DecimalFormat
            formatter.applyPattern("###,###,##0.00")
            val priceString = "€ " + formatter.format(HomeActivity.PURCHASE_LIST[position].price)
            holder.prezzoTV?.text = priceString

            if (PURCHASE_LIST[position].type == 0) {
                holder.itemLayout?.setOnClickListener(null)
                holder.nomeTV?.text = PURCHASE_LIST[position].name
                holder.nomeTV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                holder.dataLayout?.visibility = View.VISIBLE

                val dayString: String = if (PURCHASE_LIST[position].day!! < 10) {
                    "0${PURCHASE_LIST[position].day}"
                } else {
                    PURCHASE_LIST[position].day.toString()
                }
                val monthString: String = if (PURCHASE_LIST[position].month!! < 10) {
                    "0${PURCHASE_LIST[position].month}"
                } else {
                    PURCHASE_LIST[position].month.toString()
                }

                holder.dataTV?.text = "$dayString/$monthString/${PURCHASE_LIST[position].year}"
            } else if (PURCHASE_LIST[position].type == 1) {
                holder.nomeTV?.text = "   ${PURCHASE_LIST[position].name}"
                holder.nomeTV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                holder.dataLayout?.visibility = View.GONE

                holder.itemLayout?.setOnClickListener {
                    val intent = Intent(context, ReceiptActivity::class.java)
                    intent.putExtra("com.frafio.myfinance.purchaseID", PURCHASE_ID_LIST[position])
                    intent.putExtra("com.frafio.myfinance.purchaseName", PURCHASE_LIST[position].name)
                    intent.putExtra("com.frafio.myfinance.purchasePrice", priceString)
                    activity?.startActivity(intent)
                }
            } else {
                holder.itemLayout?.setOnClickListener(null)
                holder.nomeTV?.text = "   ${PURCHASE_LIST[position].name}"
                holder.nomeTV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                holder.dataLayout?.visibility = View.GONE
            }

            if (!(PURCHASE_LIST[position].name == "Totale" && PURCHASE_LIST[position].price != 0.0)) {
                holder.itemLayout?.isEnabled = true
                holder.itemLayout?.setOnLongClickListener {
                    val builder = MaterialAlertDialogBuilder(context!!, R.style.ThemeOverlay_MyFinance_AlertDialog)
                    builder.setTitle(PURCHASE_LIST[position].name)
                    if (!(PURCHASE_LIST[position].name == "Totale" && PURCHASE_LIST[position].price == 0.0)) {
                        builder.setMessage("Vuoi modificare o eliminare l'acquisto selezionato?")
                        builder.setNegativeButton("Modifica") { dialog, which ->
                            val intent = Intent(context, AddActivity::class.java)
                            intent.putExtra("com.frafio.myfinance.REQUESTCODE", 2)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_ID", PURCHASE_ID_LIST[position])
                            intent.putExtra("com.frafio.myfinance.PURCHASE_NAME", PURCHASE_LIST[position].name)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_PRICE", PURCHASE_LIST[position].price)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_TYPE", PURCHASE_LIST[position].type)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_POSITION", position)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_YEAR", PURCHASE_LIST[position].year)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_MONTH", PURCHASE_LIST[position].month)
                            intent.putExtra("com.frafio.myfinance.PURCHASE_DAY", PURCHASE_LIST[position].day)
                            activity?.startActivityForResult(intent, 2)
                        }
                    } else {
                        builder.setMessage("Vuoi eliminare l'acquisto selezionato?")
                    }
                    builder.setPositiveButton("Elimina") { dialog, which ->
                        val fStore = FirebaseFirestore.getInstance()
                        fStore.collection("purchases").document(PURCHASE_ID_LIST[position]).delete().addOnSuccessListener {
                                if (PURCHASE_LIST[position].name == "Totale") {
                                    PURCHASE_ID_LIST.removeAt(position)
                                    PURCHASE_LIST.removeAt(position)
                                    mRecyclerView.removeViewAt(position)
                                    mRecyclerView.adapter?.notifyItemRemoved(position)
                                    mRecyclerView.adapter?.notifyItemRangeChanged(position, PURCHASE_LIST.size)
                                    (activity as HomeActivity).showSnackbar("Totale eliminato!")
                                    if (PURCHASE_LIST.isEmpty()) {
                                        mWarningTV.visibility = View.VISIBLE
                                        mRecyclerView.visibility = View.GONE
                                    }
                                } else if (PURCHASE_LIST[position].type != 3) {
                                    for (i in position - 1 downTo 0) {
                                        if (PURCHASE_LIST[i].type == 0) {
                                            totPosition = i
                                            PURCHASE_LIST[totPosition].price = PURCHASE_LIST[totPosition].price!! - PURCHASE_LIST[position].price!!
                                            PURCHASE_ID_LIST.removeAt(position)
                                            PURCHASE_LIST.removeAt(position)
                                            val fStore1 = FirebaseFirestore.getInstance()
                                            fStore1.collection("purchases").document(PURCHASE_ID_LIST[i])
                                                .set(PURCHASE_LIST[i]).addOnSuccessListener {
                                                    mRecyclerView.removeViewAt(position)
                                                    mRecyclerView.adapter?.notifyItemRemoved(position)
                                                    mRecyclerView.adapter?.notifyItemRangeChanged(position, HomeActivity.PURCHASE_LIST.size)
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
                                    PURCHASE_ID_LIST.removeAt(position)
                                    PURCHASE_LIST.removeAt(position)
                                    mRecyclerView.removeViewAt(position)
                                    mRecyclerView.adapter?.notifyItemRemoved(position)
                                    mRecyclerView.adapter?.notifyItemRangeChanged(position, PURCHASE_LIST.size)
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
            return PURCHASE_LIST.size
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