package com.frafio.myfinance.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.DatePickerButton
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.databinding.ActivityAddBinding
import com.frafio.myfinance.utils.doubleToString
import com.frafio.myfinance.utils.hideSoftKeyboard
import com.frafio.myfinance.utils.snackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.sidesheet.SideSheetDialog

class AddActivity : AppCompatActivity(), AddListener {

    companion object {
        const val REQUEST_ADD_CODE: Int = 1
        const val REQUEST_EDIT_CODE: Int = 2
        const val REQUEST_PAYMENT_CODE: Int = 10
        const val REQUEST_INCOME_CODE: Int = 11
        const val REQUEST_CODE_KEY: String = "com.frafio.myfinance.REQUEST_CODE"
        const val PURCHASE_REQUEST_KEY: String = "com.frafio.myfinance.PURCHASE_REQUEST"
        const val PURCHASE_ID_KEY: String = "com.frafio.myfinance.PURCHASE_ID"
        const val PURCHASE_NAME_KEY: String = "com.frafio.myfinance.PURCHASE_NAME"
        const val PURCHASE_PRICE_KEY: String = "com.frafio.myfinance.PURCHASE_PRICE"
        const val PURCHASE_CATEGORY_KEY: String = "com.frafio.myfinance.PURCHASE_CATEGORY"
        const val PURCHASE_POSITION_KEY: String = "com.frafio.myfinance.PURCHASE_POSITION"
        const val PURCHASE_YEAR_KEY: String = "com.frafio.myfinance.PURCHASE_YEAR"
        const val PURCHASE_MONTH_KEY: String = "com.frafio.myfinance.PURCHASE_MONTH"
        const val PURCHASE_DAY_KEY: String = "com.frafio.myfinance.PURCHASE_DAY"
        const val ADD_RESULT_MESSAGE: String = "com.frafio.myfinance.ADD_RESULT_MESSAGE"
    }

    private lateinit var binding: ActivityAddBinding
    private val viewModel by viewModels<AddViewModel>()

    // custom datePicker layout
    private lateinit var datePickerBtn: DatePickerButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        binding.viewModel = viewModel
        viewModel.listener = this

        intent.getIntExtra(REQUEST_CODE_KEY, 0).also { code ->
            viewModel.requestCode = code
            initLayout(code)
        }

        binding.priceIcon.setImageResource(
            when (getString(R.string.currency)) {
                "€" -> R.drawable.ic_euro
                "$" -> R.drawable.ic_attach_money
                else -> R.drawable.ic_euro
            }
        )
    }

    private fun initLayout(code: Int) {
        datePickerBtn = object : DatePickerButton(
            binding.dateLayout,
            binding.dateET,
            this@AddActivity
        ) {
            override fun onStart() {
                super.onStart()
                binding.nameET.clearFocus()
                binding.priceET.clearFocus()
                hideSoftKeyboard(binding.root)
            }
            override fun onPositiveBtnClickListener() {
                super.onPositiveBtnClickListener()
                viewModel.year = year
                viewModel.month = month
                viewModel.day = day
                viewModel.dateString = dateString
            }
        }

        binding.categoryLayout.setOnClickListener {
            binding.nameET.clearFocus()
            binding.priceET.clearFocus()
            binding.categoryET.requestFocus()
            if (resources.getBoolean(R.bool.is600dp)) {
                val sideSheetDialog = SideSheetDialog(this)
                sideSheetDialog.setContentView(R.layout.layout_category_bottom_sheet)
                defineSheetInterface(
                    sideSheetDialog.findViewById(android.R.id.content)!!,
                    sideSheetDialog::hide
                )
                sideSheetDialog.show()
            } else {
                val modalBottomSheet = ModalBottomSheet(this)
                modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
            }
        }

        when (code) {
            REQUEST_ADD_CODE -> {
                viewModel.purchaseCode = REQUEST_PAYMENT_CODE
                viewModel.category = -1
                binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                    if (checkedIds.size != 1) return@setOnCheckedStateChangeListener
                    val checkedId = checkedIds[0]
                    when (checkedId) {
                        R.id.payment_chip -> {
                            viewModel.purchaseCode = REQUEST_PAYMENT_CODE
                            binding.categoryLayout.visibility = View.VISIBLE
                            binding.divider3.visibility = View.VISIBLE
                        }

                        R.id.income_chip -> {
                            viewModel.purchaseCode = REQUEST_INCOME_CODE
                            binding.categoryLayout.visibility = View.GONE
                            binding.divider3.visibility = View.GONE
                        }
                    }
                }
            }

            REQUEST_EDIT_CODE -> {
                binding.chipGroup.visibility = View.GONE
                intent.apply {
                    getIntExtra(PURCHASE_REQUEST_KEY, -1).also {
                        viewModel.purchaseCode = it
                    }
                    getStringExtra(PURCHASE_ID_KEY)?.let {
                        viewModel.purchaseID = it
                    }
                    getStringExtra(PURCHASE_NAME_KEY)?.let {
                        viewModel.name = it
                    }
                    getDoubleExtra(PURCHASE_PRICE_KEY, 0.0).also {
                        viewModel.priceString = doubleToString(it)
                    }
                    getIntExtra(PURCHASE_CATEGORY_KEY, 0).also {
                        viewModel.category = it
                    }
                    getIntExtra(PURCHASE_POSITION_KEY, 0).also {
                        viewModel.purchasePosition = it
                    }
                    getIntExtra(PURCHASE_YEAR_KEY, 0).also {
                        datePickerBtn.year = it
                    }
                    getIntExtra(PURCHASE_MONTH_KEY, 0).also {
                        datePickerBtn.month = it
                    }
                    getIntExtra(PURCHASE_DAY_KEY, 0).also {
                        datePickerBtn.day = it
                    }
                }

                if (viewModel.purchaseCode == REQUEST_PAYMENT_CODE) {
                    val categories = resources.getStringArray(R.array.categories)
                    if (viewModel.category != null && viewModel.category!! >= 0 && viewModel.category!! < categories.size) {
                        binding.categoryET.text = categories[viewModel.category!!]
                        binding.categoryIcon.setImageResource(
                            when (viewModel.category) {
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
                    }
                } else {
                    binding.divider3.visibility = View.GONE
                    binding.categoryLayout.visibility = View.GONE
                }
            }
        }

        viewModel.updateTime(datePickerBtn)
    }

    fun onBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onAddStart() {
        binding.addProgressIndicator.show()

        binding.nameET.error = null
        binding.priceET.error = null
        binding.categoryET.error = null

        binding.addAddButton.isEnabled = false
    }

    override fun onAddSuccess(response: LiveData<PurchaseResult>) {
        response.observe(this) { result ->
            if (result.code != PurchaseCode.PURCHASE_ADD_SUCCESS.code) {
                binding.addProgressIndicator.hide()
                binding.addAddButton.isEnabled = true
            }

            when (result.code) {
                PurchaseCode.PURCHASE_ADD_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(PURCHASE_REQUEST_KEY, REQUEST_PAYMENT_CODE)
                        it.putExtra(ADD_RESULT_MESSAGE, result.message)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                PurchaseCode.PURCHASE_EDIT_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(PURCHASE_REQUEST_KEY, REQUEST_PAYMENT_CODE)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                PurchaseCode.INCOME_ADD_SUCCESS.code -> {
                    Intent().also {
                        it.putExtra(PURCHASE_REQUEST_KEY, REQUEST_INCOME_CODE)
                        it.putExtra(ADD_RESULT_MESSAGE, result.message)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                PurchaseCode.INCOME_EDIT_SUCCESS.code -> {
                    // go back to the homepage
                    Intent().also {
                        it.putExtra(PURCHASE_REQUEST_KEY, REQUEST_INCOME_CODE)
                        setResult(RESULT_OK, it)
                        finish()
                    }
                }

                else -> snackBar(result.message, binding.addAddButton)
            }
        }
    }

    override fun onAddFailure(result: PurchaseResult) {
        binding.addProgressIndicator.hide()
        binding.addAddButton.isEnabled = true

        when (result.code) {
            PurchaseCode.EMPTY_NAME.code,
            PurchaseCode.WRONG_NAME_TOTAL.code ->
                binding.nameET.error = result.message

            PurchaseCode.EMPTY_PRICE.code ->
                binding.priceET.error = result.message

            PurchaseCode.EMPTY_CATEGORY.code ->
                binding.categoryET.error = result.message
        }
    }

    class ModalBottomSheet(
        private val activity: AddActivity
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
                inflater.inflate(R.layout.layout_category_bottom_sheet, container, false)
            activity.defineSheetInterface(
                layout,
                this::dismiss
            )
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        dismissFun: () -> Unit
    ) {
        fun selectCategory(category: Int) {
            binding.categoryET.error = null
            val categories = resources.getStringArray(R.array.categories)
            binding.categoryET.text = categories[category]
            binding.categoryIcon.setImageResource(
                when (category) {
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
            viewModel.category = category
        }

        layout.findViewById<ConstraintLayout>(R.id.purchaseDetailLayout).visibility = View.GONE
        layout.findViewById<ConstraintLayout>(R.id.categoryDetailLayout).visibility = View.VISIBLE

        layout.findViewById<MaterialButton>(R.id.purchaseCategoryIcon).icon =
            ContextCompat.getDrawable(
                applicationContext,
                when (viewModel.category) {
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

        layout.findViewById<LinearLayout>(R.id.housing_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.HOUSING.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.groceries_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.GROCERIES.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.personal_care_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.PERSONAL_CARE.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.entertainment_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.ENTERTAINMENT.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.education_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.EDUCATION.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.dining_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.DINING.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.health_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.HEALTH.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.transportation_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.TRANSPORTATION.value)
            dismissFun()
        }
        layout.findViewById<LinearLayout>(R.id.miscellaneous_layout).setOnClickListener {
            selectCategory(DbPurchases.CATEGORIES.MISCELLANEOUS.value)
            dismissFun()
        }
    }
}