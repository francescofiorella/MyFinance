package com.frafio.myfinance.ui.home.list

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentListBinding
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity.Companion.EDIT_PURCHASE_CODE
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.list.PurchaseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.list.PurchaseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.ui.home.list.receipt.ReceiptActivity
import com.frafio.myfinance.utils.doubleToPrice
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.generic.instance

class ListFragment : BaseFragment(), PurchaseInteractionListener, DeleteListener {

    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ListViewModel

    private val factory: ListViewModelFactory by instance()

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

            val editRequest = data!!.getBooleanExtra(
                "${getString(R.string.default_path)}.purchaseRequest",
                false
            )

            if (editRequest) {
                viewModel.getPurchases()
                (activity as HomeActivity).showSnackbar(PurchaseCode.PURCHASE_EDIT_SUCCESS.message)
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
            ON_CLICK -> {
                Intent(context, ReceiptActivity::class.java).also {
                    it.putExtra("${getString(R.string.default_path)}.purchaseID", purchase.id)
                    it.putExtra("${getString(R.string.default_path)}.purchaseName", purchase.name)
                    it.putExtra(
                        "${getString(R.string.default_path)}.purchasePrice",
                        doubleToPrice(purchase.price!!)
                    )
                    activity?.startActivity(it)
                }
            }

            ON_LONG_CLICK -> {
                val builder = MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_MyFinance_AlertDialog
                )
                builder.setTitle(purchase.name)
                if (purchase.type == 0 && purchase.price == 0.0) {
                    builder.setMessage(getString(R.string.purchase_delete_dialog))
                } else if (purchase.type != 0) {
                    builder.setMessage(getString(R.string.purchase_edit_delete_dialog))
                    builder.setNegativeButton(getString(R.string.modifica)) { _, _ ->
                        Intent(context, AddActivity::class.java).also {
                            it.putExtra(
                                "${getString(R.string.default_path)}.REQUESTCODE",
                                EDIT_PURCHASE_CODE
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_ID",
                                purchase.id
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_NAME",
                                purchase.name
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_PRICE",
                                purchase.price
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_TYPE",
                                purchase.type
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_POSITION",
                                position
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_YEAR",
                                purchase.year
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_MONTH",
                                purchase.month
                            )
                            it.putExtra(
                                "${getString(R.string.default_path)}.PURCHASE_DAY",
                                purchase.day
                            )
                            editResultLauncher.launch(it)
                        }
                    }
                }
                builder.setPositiveButton(getString(R.string.elimina)) { _, _ ->
                    viewModel.deletePurchaseAt(position)
                }
                builder.show()
            }
        }
    }

    override fun onDeleteComplete(response: LiveData<Pair<PurchaseResult, List<Purchase>>>) {
        response.observe(viewLifecycleOwner, { value ->
            val result = value.first
            val newList = value.second

            if (result.code == PurchaseCode.PURCHASE_DELETE_SUCCESS.code) {
                (binding.listRecyclerView.adapter as PurchaseAdapter).setData(newList)
            }

            (activity as HomeActivity).showSnackbar(result.message)
        })
    }
}