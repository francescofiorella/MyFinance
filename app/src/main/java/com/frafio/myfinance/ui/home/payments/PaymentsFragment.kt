package com.frafio.myfinance.ui.home.payments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.managers.PurchaseManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.databinding.FragmentPaymentsBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.add.AddActivity
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToPrice
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textview.MaterialTextView


class PaymentsFragment : BaseFragment(), PurchaseInteractionListener, PaymentListener {

    private lateinit var binding: FragmentPaymentsBinding
    private val viewModel by viewModels<PaymentsViewModel>()
    private var isListBlocked = false
    private var maxPurchaseNumber = DEFAULT_LIMIT + 1
    private val recViewLiveData = MediatorLiveData<List<Purchase>>()
    private lateinit var localPurchasesLiveData: LiveData<List<Purchase>>

    private var editResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val editRequest = data!!.getIntExtra(AddActivity.PURCHASE_REQUEST_KEY, -1)

            if (editRequest == AddActivity.REQUEST_PAYMENT_CODE) {
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

        /*binding.listRecyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (binding.listRecyclerView.layoutManager as LinearLayoutManager)
                        .findFirstVisibleItemPosition()
                    scrollTo(position)
                }
            }
        })*/


        localPurchasesLiveData = viewModel.getLocalPurchases()
        recViewLiveData.addSource(localPurchasesLiveData) { value ->
            recViewLiveData.value = value
        }
        recViewLiveData.observe(viewLifecycleOwner) { purchases ->
            // Evaluate limit and decide if new items can be retrieved
            val limit = if (binding.listRecyclerView.adapter != null) {
                (binding.listRecyclerView.adapter as PurchaseAdapter).getLimit()
            } else {
                DEFAULT_LIMIT
            }
            // Get list with limit and update recList
            var nl = purchases.take(limit.toInt()).map { p -> p.copy() }
            nl = PurchaseStorage.addTotals(nl)
            viewModel.updatePurchasesEmpty(nl.isEmpty())
            maxPurchaseNumber = purchases.size.toLong()
            binding.listRecyclerView.also {
                if (it.adapter == null) {
                    it.adapter = PurchaseAdapter(nl, this)
                    isListBlocked = limit >= maxPurchaseNumber
                } else {
                    it.post {
                        (it.adapter as PurchaseAdapter).updateData(nl)
                        isListBlocked = limit >= maxPurchaseNumber
                    }
                }
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
                if (resources.getBoolean(R.bool.is600dp)) {
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

            ON_BUTTON_CLICK -> {
                if (resources.getBoolean(R.bool.is600dp)) {
                    val sideSheetDialog = SideSheetDialog(requireContext())
                    sideSheetDialog.setContentView(R.layout.layout_category_bottom_sheet)
                    defineSheetInterface(
                        sideSheetDialog.findViewById(android.R.id.content)!!,
                        purchase,
                        position,
                        editResultLauncher,
                        viewModel,
                        sideSheetDialog::hide,
                        true
                    )
                    sideSheetDialog.show()
                } else {
                    val modalBottomSheet = ModalBottomSheet(
                        this,
                        purchase,
                        position,
                        editResultLauncher,
                        viewModel,
                        true
                    )
                    modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
                }
            }

            ON_LOAD_MORE_REQUEST -> {
                // Increment elements limit on scroll
                if (!isListBlocked) {
                    isListBlocked = true
                    binding.listRecyclerView.adapter?.let {
                        (it as PurchaseAdapter).getLimit(true)
                    }
                    // this trigger the observer
                    recViewLiveData.removeSource(localPurchasesLiveData)
                    recViewLiveData.addSource(localPurchasesLiveData) { value ->
                        recViewLiveData.value = value
                    }
                }
            }
        }
    }

    override fun onCompleted(response: LiveData<PurchaseResult>) {
        response.observe(viewLifecycleOwner) { result ->
            when (result.code) {
                PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS.code -> {
                    val limit = if (binding.listRecyclerView.adapter != null) {
                        (binding.listRecyclerView.adapter as PurchaseAdapter).getLimit()
                    } else {
                        DEFAULT_LIMIT
                    }
                    isListBlocked = limit >= maxPurchaseNumber
                }

                PurchaseCode.PURCHASE_EDIT_SUCCESS.code -> {
                    Unit
                }

                PurchaseCode.PURCHASE_ADD_SUCCESS.code -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }

                else -> {
                    (activity as HomeActivity).showSnackBar(result.message)
                }
            }
        }
    }

    override fun onDeleteCompleted(
        response: LiveData<PurchaseResult>,
        purchase: Purchase
    ) {
        response.observe(viewLifecycleOwner) { result ->
            if (result.code == PurchaseCode.PURCHASE_DELETE_SUCCESS.code) {
                (activity as HomeActivity).showSnackBar(
                    result.message,
                    getString(R.string.cancel)
                ) {
                    viewModel.addPurchase(purchase)
                }
            } else {
                (activity as HomeActivity).showSnackBar(result.message)
            }
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.listRecyclerView.apply {
            stopScroll()
            (layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
        }
    }

    fun scrollTo(position: Int) {
        binding.listRecyclerView.apply {
            val linearSmoothScroller: LinearSmoothScroller =
                object : LinearSmoothScroller(context) {
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return 200.0f / displayMetrics.densityDpi
                    }
                }
            stopScroll()
            linearSmoothScroller.targetPosition = position
            (layoutManager as LinearLayoutManager?)?.startSmoothScroll(linearSmoothScroller)
        }
    }

    class ModalBottomSheet(
        private val fragment: PaymentsFragment,
        private val purchase: Purchase,
        private val position: Int,
        private val editResultLauncher: ActivityResultLauncher<Intent>,
        private val viewModel: PaymentsViewModel,
        private val fromCategoryIcon: Boolean = false
    ) : BottomSheetDialogFragment() {

        companion object {
            const val TAG = "ModalBottomSheet"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val layout = inflater.inflate(
                if (fromCategoryIcon) R.layout.layout_category_bottom_sheet
                else R.layout.layout_edit_purchase_bottom_sheet,
                container,
                false
            )
            fragment.defineSheetInterface(
                layout,
                purchase,
                position,
                editResultLauncher,
                viewModel,
                this::dismiss,
                fromCategoryIcon
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
        dismissFun: () -> Unit,
        fromCategoryIcon: Boolean = false
    ) {
        layout.findViewById<MaterialTextView>(R.id.nameTV).text = purchase.name
        layout.findViewById<MaterialTextView>(R.id.dateTV).text =
            dateToString(purchase.day, purchase.month, purchase.year)
        layout.findViewById<MaterialTextView>(R.id.priceTV).text =
            doubleToPrice(purchase.price ?: 0.0)
        layout.findViewById<MaterialButton>(R.id.purchaseCategoryIcon).icon =
            ContextCompat.getDrawable(
                requireContext(),
                when (purchase.category) {
                    DbPurchases.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                    DbPurchases.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                    DbPurchases.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                    DbPurchases.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                    DbPurchases.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                    DbPurchases.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                    DbPurchases.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                    DbPurchases.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                    DbPurchases.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                    else -> R.drawable.ic_tag
                }
            )

        if (fromCategoryIcon) {
            // layout_category_bottom_sheet.xml
            layout.findViewById<ConstraintLayout>(R.id.categoryDetailLayout).visibility = View.GONE
            layout.findViewById<ConstraintLayout>(R.id.purchaseDetailLayout).visibility =
                View.VISIBLE
            layout.findViewById<LinearLayout>(R.id.housing_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.HOUSING.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.groceries_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.GROCERIES.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.personal_care_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.PERSONAL_CARE.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.entertainment_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.ENTERTAINMENT.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.education_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.EDUCATION.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.dining_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.DINING.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.health_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.HEALTH.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.transportation_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.TRANSPORTATION.value)
                dismissFun()
            }
            layout.findViewById<LinearLayout>(R.id.miscellaneous_layout).setOnClickListener {
                viewModel.updateCategory(purchase, DbPurchases.CATEGORIES.MISCELLANEOUS.value)
                dismissFun()
            }
            return
        }

        // layout_edit_purchase_bottom_sheet.xml
        val editLayout = layout.findViewById<LinearLayout>(R.id.edit_layout)
        val deleteLayout = layout.findViewById<LinearLayout>(R.id.delete_layout)
        editLayout.setOnClickListener {
            Intent(context, AddActivity::class.java).also {
                it.putExtra(AddActivity.REQUEST_CODE_KEY, AddActivity.REQUEST_EDIT_CODE)
                it.putExtra(AddActivity.PURCHASE_REQUEST_KEY, AddActivity.REQUEST_PAYMENT_CODE)
                it.putExtra(AddActivity.PURCHASE_ID_KEY, purchase.id)
                it.putExtra(AddActivity.PURCHASE_NAME_KEY, purchase.name)
                it.putExtra(AddActivity.PURCHASE_PRICE_KEY, purchase.price)
                it.putExtra(AddActivity.PURCHASE_CATEGORY_KEY, purchase.category)
                it.putExtra(AddActivity.PURCHASE_POSITION_KEY, position)
                it.putExtra(AddActivity.PURCHASE_YEAR_KEY, purchase.year)
                it.putExtra(AddActivity.PURCHASE_MONTH_KEY, purchase.month)
                it.putExtra(AddActivity.PURCHASE_DAY_KEY, purchase.day)
                editResultLauncher.launch(it)
            }
            dismissFun()
        }
        deleteLayout.setOnClickListener {
            viewModel.deletePurchase(purchase)
            dismissFun()
        }
    }
}