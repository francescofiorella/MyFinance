package com.frafio.myfinance.ui.home.payments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.FragmentPaymentsBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LONG_CLICK
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
                val positionChanged = data.getIntExtra(AddActivity.PURCHASE_POSITION_KEY, 0)
                binding.listRecyclerView.adapter?.notifyItemChanged(positionChanged)
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
            ON_CLICK -> Unit

            ON_LONG_CLICK -> {
                if (requireActivity().findViewById<NavigationView?>(R.id.nav_drawer) != null) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_edit_purchase_bottom_sheet)
                    defineSheetInterface(
                        sideSheetDialog.findViewById(android.R.id.content)!!,
                        purchase,
                        position,
                        editResultLauncher,
                        viewModel,
                        sideSheetDialog::hide
                    )
                    sideSheetDialog.show()
                } else {
                    val modalBottomSheet = ModalBottomSheet(
                        this,
                        purchase,
                        position,
                        editResultLauncher,
                        viewModel
                    )
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
        val todayId = (binding.listRecyclerView.adapter as PurchaseAdapter).getTodayId()
        (binding.listRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            todayId,
            0
        )
    }

    class ModalBottomSheet(
        private val fragment: PaymentsFragment,
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
            val layout =
                inflater.inflate(R.layout.layout_edit_purchase_bottom_sheet, container, false)
            fragment.defineSheetInterface(
                layout,
                purchase,
                position,
                editResultLauncher,
                viewModel,
                this::dismiss
            )
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        purchase: Purchase,
        position: Int,
        editResultLauncher: ActivityResultLauncher<Intent>,
        viewModel: PaymentsViewModel,
        dismissFun: () -> Unit
    ) {
        val editLayout = layout.findViewById<ConstraintLayout>(R.id.edit_layout)
        val deleteLayout = layout.findViewById<ConstraintLayout>(R.id.delete_layout)
        layout.findViewById<MaterialTextView>(R.id.nameTV).text = purchase.name
        layout.findViewById<MaterialTextView>(R.id.dateTV).text =
            dateToString(purchase.day, purchase.month, purchase.year)
        layout.findViewById<MaterialTextView>(R.id.priceTV).text =
            doubleToPrice(purchase.price ?: 0.0)
        layout.findViewById<ImageView>(R.id.purchaseTypeIcon).setImageResource(
            when (purchase.type) {
                DbPurchases.TYPES.HOUSING.value -> R.drawable.ic_baseline_home
                DbPurchases.TYPES.GROCERIES.value -> R.drawable.ic_shopping_cart
                DbPurchases.TYPES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                DbPurchases.TYPES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                DbPurchases.TYPES.EDUCATION.value -> R.drawable.ic_school
                DbPurchases.TYPES.DINING.value -> R.drawable.ic_restaurant
                DbPurchases.TYPES.HEALTH.value -> R.drawable.ic_vaccines
                DbPurchases.TYPES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                DbPurchases.TYPES.MISCELLANEOUS.value -> R.drawable.ic_tag
                else -> R.drawable.ic_tag
            }
        )
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
            dismissFun()
        }
        deleteLayout.setOnClickListener {
            viewModel.deletePurchaseAt(position)
            dismissFun()
        }
    }
}