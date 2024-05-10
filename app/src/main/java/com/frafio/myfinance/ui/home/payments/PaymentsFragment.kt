package com.frafio.myfinance.ui.home.payments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentPaymentsBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.ui.home.payments.invoice.InvoiceActivity
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToPrice
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textview.MaterialTextView

class PaymentsFragment : BaseFragment(), PurchaseInteractionListener, DeleteListener {

    private lateinit var binding: FragmentPaymentsBinding
    private val viewModel by viewModels<PaymentsViewModel>()

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payments, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.listener = this

        viewModel.getPurchases()
        viewModel.updateListSize()
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
                if (requireActivity().findViewById<NavigationView?>(R.id.nav_drawer) != null) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_menu_bottom_sheet)
                    sideSheetDialog.show()
                    val editLayout =
                        sideSheetDialog.findViewById<ConstraintLayout>(R.id.edit_layout)
                    val deleteLayout =
                        sideSheetDialog.findViewById<ConstraintLayout>(R.id.delete_layout)
                    sideSheetDialog.findViewById<MaterialTextView>(R.id.nameTV)?.text =
                        purchase.name
                    sideSheetDialog.findViewById<MaterialTextView>(R.id.dateTV)?.text =
                        dateToString(purchase.day, purchase.month, purchase.year)
                    sideSheetDialog.findViewById<MaterialTextView>(R.id.priceTV)?.text =
                        doubleToPrice(purchase.price ?: 0.0)
                    if (purchase.type == 0 && purchase.price == 0.0) {
                        editLayout?.visibility = View.GONE
                    } else if (purchase.type != 0) {
                        editLayout?.setOnClickListener {
                            Intent(context, AddActivity::class.java).also {
                                it.putExtra(
                                    AddActivity.REQUEST_CODE_KEY,
                                    AddActivity.REQUEST_EDIT_CODE
                                )
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
                            sideSheetDialog.hide()
                        }
                    }
                    deleteLayout?.setOnClickListener {
                        viewModel.deletePurchaseAt(position)
                        sideSheetDialog.hide()
                    }
                } else {
                    val modalBottomSheet =
                        ModalBottomSheet(purchase, position, editResultLauncher, viewModel)
                    modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
                }
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
            viewModel.updateListSize()

            (activity as HomeActivity).showSnackBar(result.message)
        }
    }

    fun refreshListData() {
        (binding.listRecyclerView.adapter as PurchaseAdapter).updateData(viewModel.getPurchaseList())
        viewModel.updateListSize()
        scrollUp()
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.listRecyclerView.scrollToPosition(0)
    }

    class ModalBottomSheet(
        private val purchase: Purchase,
        private val position: Int,
        private val editResultLauncher: ActivityResultLauncher<Intent>,
        private val viewModel: PaymentsViewModel
    ) : BottomSheetDialogFragment() {

        companion object {
            const val TAG = "ModalBottomSheet"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val layout = inflater.inflate(R.layout.layout_menu_bottom_sheet, container, false)
            val editLayout = layout.findViewById<ConstraintLayout>(R.id.edit_layout)
            val deleteLayout = layout.findViewById<ConstraintLayout>(R.id.delete_layout)
            layout.findViewById<MaterialTextView>(R.id.nameTV).text = purchase.name
            layout.findViewById<MaterialTextView>(R.id.dateTV).text =
                dateToString(purchase.day, purchase.month, purchase.year)
            layout.findViewById<MaterialTextView>(R.id.priceTV).text =
                doubleToPrice(purchase.price ?: 0.0)
            if (purchase.type == 0 && purchase.price == 0.0) {
                editLayout.visibility = View.GONE
            } else if (purchase.type != 0) {
                editLayout.setOnClickListener {
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
                    this.dismiss()
                }
            }
            deleteLayout.setOnClickListener {
                viewModel.deletePurchaseAt(position)
                this.dismiss()
            }
            return layout
        }
    }
}