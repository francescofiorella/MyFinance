package com.frafio.myfinance.ui.home.list

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.FragmentListBinding
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.list.receipt.ReceiptActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ListFragment : Fragment(), PurchaseInteractionListener, DeleteListener, KodeinAware {

    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ListViewModel

    override val kodein by kodein()
    private val factory: ListViewModelFactory by instance()

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest =
                data!!.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false)
            if (editRequest) {
                viewModel.getPurchases()
                (activity as HomeActivity).showSnackbar("Acquisto modificato!")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        viewModel = ViewModelProvider(this, factory).get(ListViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        viewModel.listener = this

        viewModel.getPurchases()
        viewModel.purchases.observe(viewLifecycleOwner, { purchases ->
            binding.listRecyclerView.also {
                it.setHasFixedSize(true)
                it.adapter = PurchaseAdapter(purchases, this)
            }
        })

        return binding.root
    }

    override fun onItemInteraction(
        interactionID: Int,
        purchase: Purchase,
        position: Int
    ) {
        when (interactionID) {
            1 -> {
                Intent(context, ReceiptActivity::class.java).also {
                    it.putExtra("com.frafio.myfinance.purchaseID", purchase.id)
                    it.putExtra("com.frafio.myfinance.purchaseName", purchase.name)
                    it.putExtra("com.frafio.myfinance.purchasePrice", purchase.formattedPrice)
                    activity?.startActivity(it)
                }
            }
            2 -> {
                val builder = MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_MyFinance_AlertDialog
                )
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
                            editResultLauncher.launch(it)
                        }
                    }
                }
                builder.setPositiveButton("Elimina") { _, _ ->
                    viewModel.deletePurchaseAt(position)
                }
                builder.show()
            }
        }
    }

    override fun onDeleteComplete(response: LiveData<Any>) {
        response.observe(viewLifecycleOwner, { value ->
            when (value) {
                is Triple<*, *, *> -> {
                    val message = value.first as String
                    val position = value.second as Int
                    val totPosition = value.third as Int?

                    when (message) {
                        "Totale eliminato!" -> {
                            binding.listRecyclerView.also {
                                it.removeViewAt(position)
                                it.adapter!!.notifyItemRemoved(position)
                                it.adapter!!.notifyItemRangeChanged(
                                    position,
                                    viewModel.purchaseListSize
                                )
                            }
                        }
                        "Acquisto eliminato!" -> {
                            binding.listRecyclerView.also {
                                it.removeViewAt(position)
                                it.adapter!!.notifyItemRemoved(position)
                                it.adapter!!.notifyItemRangeChanged(
                                    position,
                                    viewModel.purchaseListSize
                                )
                                totPosition?.let { index ->
                                    it.adapter!!.notifyItemChanged(index)
                                }
                            }
                        }
                    }

                    (activity as HomeActivity).showSnackbar(message)
                }

                is String -> (activity as HomeActivity).showSnackbar(value)
            }
        })
    }
}