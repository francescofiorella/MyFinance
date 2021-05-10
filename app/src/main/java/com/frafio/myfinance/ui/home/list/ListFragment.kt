package com.frafio.myfinance.ui.home.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.manager.FetchListener
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.FragmentListBinding
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.list.receipt.ReceiptActivity
import com.frafio.myfinance.ui.store.AddActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ListFragment : Fragment(),RecyclerViewInteractionListener, FetchListener, KodeinAware {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mWarningTV: TextView

    private lateinit var viewModel: ListViewModel

    override val kodein by kodein()
    private val factory: ListViewModelFactory by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        viewModel = ViewModelProvider(this, factory).get(ListViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        PurchaseManager.fetchListener = this

        viewModel.getPurchases()
        viewModel.purchases.observe(viewLifecycleOwner, { purchases ->
            mRecyclerView.also {
                it.setHasFixedSize(true)
                it.adapter = PurchaseAdapter(purchases, this)
            }
        })

        mRecyclerView = binding.root.findViewById(R.id.list_recyclerView)
        mWarningTV = binding.root.findViewById(R.id.list_warningTV)

        return binding.root
    }

    override fun onRecyclerViewItemInteraction(interactionID: Int, purchase: Purchase, position: Int) {
        when (interactionID) {
            1 -> {
                if (purchase.name == "Spesa Coop") {
                    Intent(context, ReceiptActivity::class.java).also {
                        it.putExtra("com.frafio.myfinance.purchaseID", purchase.id)
                        it.putExtra("com.frafio.myfinance.purchaseName", purchase.name)
                        it.putExtra("com.frafio.myfinance.purchasePrice", purchase.formattedPrice)
                        activity?.startActivity(it)
                    }
                }
            }
            2 -> {
                if (!(purchase.type == 0 && purchase.price != 0.0)) {
                    val builder = MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MyFinance_AlertDialog)
                    builder.setTitle(purchase.name)
                    if (purchase.type == 0 && purchase.price == 0.0) {
                        builder.setMessage("Vuoi eliminare l'acquisto selezionato?")
                    } else if (purchase.type != 0) {
                        builder.setMessage("Vuoi modificare o eliminare l'acquisto selezionato?")
                        builder.setNegativeButton("Modifica") { _, _ ->
                            Intent(context, AddActivity::class.java).also {
                                it.putExtra("com.frafio.myfinance.REQUESTCODE", 2)
                                it.putExtra("com.frafio.myfinance.PURCHASE_ID", purchase.id)
                                it.putExtra("com.frafio.myfinance.PURCHASE_NAME", purchase.name)
                                it.putExtra("com.frafio.myfinance.PURCHASE_PRICE", purchase.price)
                                it.putExtra("com.frafio.myfinance.PURCHASE_TYPE", purchase.type)
                                it.putExtra("com.frafio.myfinance.PURCHASE_POSITION", position)
                                it.putExtra("com.frafio.myfinance.PURCHASE_YEAR", purchase.year)
                                it.putExtra("com.frafio.myfinance.PURCHASE_MONTH", purchase.month)
                                it.putExtra("com.frafio.myfinance.PURCHASE_DAY", purchase.day)
                                activity?.startActivityForResult(it, 2)
                            }
                        }
                    }
                    builder.setPositiveButton("Elimina") { _, _ ->
                        PurchaseManager.deleteAndUpdatePurchaseAt(position)
                    }
                    builder.show()
                }
            }
        }
    }

    override fun onFetchSuccess(message: String?) {
        when (message) {
            "Totale eliminato!" -> {
                mRecyclerView.removeViewAt(position)
                mRecyclerView.adapter?.notifyItemRemoved(position)
                mRecyclerView.adapter?.notifyItemRangeChanged(position, PurchaseManager.getPurchaseList().size)
            }
        }
        message?.let {
            (activity as HomeActivity).showSnackbar(it)
        }
    }

    override fun onFetchFailure(message: String) {
        (activity as HomeActivity).showSnackbar(message)
    }

    /*fun loadPurchasesList() {
        val mAdapter = PurchaseAdapter(activity, context, mRecyclerView, mWarningTV)
        mRecyclerView.adapter = mAdapter
    }

    fun scrollListToTop() {
        mRecyclerView.smoothScrollToPosition(0)
    }*/
}