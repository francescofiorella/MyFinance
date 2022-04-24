package com.frafio.myfinance.ui.home.list

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentListBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.list.PurchaseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.list.PurchaseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.ui.home.list.invoice.InvoiceActivity
import com.frafio.myfinance.utils.doubleToPrice
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ListFragment : BaseFragment(), PurchaseInteractionListener, DeleteListener {

    private lateinit var binding: FragmentListBinding
    private val viewModel by viewModels<ListViewModel>()

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

            val editRequest = data!!.getBooleanExtra(AddActivity.PURCHASE_REQUEST_KEY, false)

            if (editRequest) {
                viewModel.getPurchases()
                (activity as HomeActivity).refreshFragmentData(dashboard = true, menu = true)
                (activity as HomeActivity).showSnackBar(PurchaseCode.PURCHASE_EDIT_SUCCESS.message)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.listener = this

        viewModel.getPurchases()
        viewModel.purchases.observe(viewLifecycleOwner) { purchases ->
            binding.listRecyclerView.also {
                it.setHasFixedSize(true)
                it.adapter = PurchaseAdapter(purchases, this)
            }
        }

        return binding.root
    }

    override fun onItemInteraction(
        interactionID: Int,
        purchase: Purchase,
        position: Int
    ) {
        when (interactionID) {
            ON_CLICK -> {
                Intent(context, InvoiceActivity::class.java).also {
                    it.putExtra(InvoiceActivity.PURCHASE_ID_KEY, purchase.id)
                    it.putExtra(InvoiceActivity.PURCHASE_NAME_KEY, purchase.name)
                    it.putExtra(InvoiceActivity.PURCHASE_PRICE_KEY, doubleToPrice(purchase.price!!))
                    activity?.startActivity(it)
                }
            }

            ON_LONG_CLICK -> {
                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setTitle(purchase.name)
                if (purchase.type == 0 && purchase.price == 0.0) {
                    builder.setIcon(R.drawable.ic_delete)
                    builder.setMessage(getString(R.string.purchase_delete_dialog))
                } else if (purchase.type != 0) {
                    builder.setIcon(R.drawable.ic_create)
                    builder.setMessage(getString(R.string.purchase_edit_delete_dialog))
                    builder.setNegativeButton(getString(R.string.edit)) { _, _ ->
                        Intent(context, AddActivity::class.java).also {
                            it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                            it.putExtra(AddActivity.PURCHASE_ID_KEY, purchase.id)
                            it.putExtra(AddActivity.PURCHASE_NAME_KEY, purchase.name)
                            it.putExtra(AddActivity.PURCHASE_PRICE_KEY, purchase.price)
                            it.putExtra(AddActivity.PURCHASE_TYPE_KEY, purchase.type)
                            it.putExtra(AddActivity.PURCHASE_POSITION_KEY, position)
                            it.putExtra(AddActivity.PURCHASE_YEAR_KEY, purchase.year)
                            it.putExtra(AddActivity.PURCHASE_MONTH_KEY, purchase.month)
                            it.putExtra(AddActivity.PURCHASE_DAY_KEY, purchase.day)
                            editResultLauncher.launch(it)
                        }
                    }
                }
                builder.setPositiveButton(getString(R.string.delete)) { _, _ ->
                    viewModel.deletePurchaseAt(position)
                }
                builder.show()
            }
        }
    }

    override fun onDeleteComplete(response: LiveData<Triple<PurchaseResult, List<Purchase>, Int?>>) {
        response.observe(viewLifecycleOwner) { value ->
            val result = value.first
            val newList = value.second
            val totPosition = value.third

            if (result.code != PurchaseCode.PURCHASE_DELETE_FAILURE.code) {
                totPosition?.let {
                    binding.listRecyclerView.adapter!!.notifyItemChanged(it)
                }
                (binding.listRecyclerView.adapter as PurchaseAdapter).updateData(newList)
                (activity as HomeActivity).refreshFragmentData(dashboard = true, menu = true)
            }

            (activity as HomeActivity).showSnackBar(result.message)
        }
    }

    fun refreshListData() {
        (binding.listRecyclerView.adapter as PurchaseAdapter).updateData(viewModel.getPurchaseList())
        binding.listRecyclerView.scrollToPosition(0)
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.listRecyclerView.scrollToPosition(0)
    }
}