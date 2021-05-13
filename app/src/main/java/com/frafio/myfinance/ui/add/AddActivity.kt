package com.frafio.myfinance.ui.add

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.ActivityAddBinding
import com.frafio.myfinance.util.snackbar
import com.google.android.material.datepicker.MaterialDatePicker
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class AddActivity : AppCompatActivity(), AddListener, KodeinAware {

    private val interpolator = OvershootInterpolator()

    private lateinit var binding: ActivityAddBinding
    private lateinit var viewModel: AddViewModel

    override val kodein by kodein()
    private val factory: AddViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        viewModel = ViewModelProvider(this, factory).get(AddViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.listener = this

        // toolbar
        setSupportActionBar(binding.addToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // stabilisci se bisogna creare un nuovo evento (1) o modificarne uno esistente (2)
        intent.getIntExtra("com.frafio.myfinance.REQUESTCODE", 0).also {
            viewModel.requestCode = it
            binding.addBigliettoLayout.alpha = 0f
            initLayout(it)
            viewModel.updateDateTV(it)
        }
    }

    private fun initLayout(code: Int) {
        if (code == 1) {
            binding.addGenericoTv.isSelected = true
            viewModel.type = 2

            setTypeButton()
            setTotSwitch()

            setDatePicker()
        } else if (code == 2) {
            intent.also { intent ->
                intent.getStringExtra("com.frafio.myfinance.PURCHASE_ID")?.let {
                    viewModel.purchaseID = it
                }
                intent.getStringExtra("com.frafio.myfinance.PURCHASE_NAME")?.let {
                    viewModel.name = it
                }
                intent.getDoubleExtra("com.frafio.myfinance.PURCHASE_PRICE", 0.0).also {
                    val locale = Locale("en", "UK")
                    val nf = NumberFormat.getInstance(locale)
                    val formatter = nf as DecimalFormat
                    formatter.applyPattern("###,###,##0.00")
                    viewModel.priceString = formatter.format(it)
                    viewModel.purchasePrice = it
                }
                intent.getIntExtra("com.frafio.myfinance.PURCHASE_TYPE", 0).also {
                    viewModel.purchaseType = it
                }
                intent.getIntExtra("com.frafio.myfinance.PURCHASE_POSITION", 0).also {
                    viewModel.purchasePosition = it
                }
                intent.getIntExtra("com.frafio.myfinance.PURCHASE_YEAR", 0).also {
                    viewModel.year = it
                }
                intent.getIntExtra("com.frafio.myfinance.PURCHASE_MONTH", 0).also {
                    viewModel.month = it
                }
                intent.getIntExtra("com.frafio.myfinance.PURCHASE_DAY", 0).also {
                    viewModel.day = it
                }
            }

            when (viewModel.purchaseType) {
                1 -> {
                    binding.addGenericoTv.isEnabled = false
                    binding.addSpesaTv.isSelected = true
                    binding.addBigliettoTv.isEnabled = false
                }
                2 -> {
                    binding.addGenericoTv.isSelected = true
                    binding.addSpesaTv.isEnabled = false
                    binding.addBigliettoTv.isEnabled = false
                }
                3 -> {
                    binding.addGenericoTv.isEnabled = false
                    binding.addSpesaTv.isEnabled = false
                    binding.addBigliettoTv.isSelected = true
                    setBigliettoLayout()
                }
            }

            viewModel.updateDateTV(null)

            binding.addDateTextView.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.disabled_text
                )
            )
        }
    }

    private fun setDatePicker() {
        // date picker
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Seleziona una data")
        builder.setSelection(today)
        builder.setTheme(R.style.ThemeOverlay_MyFinance_DatePicker)
        val materialDatePicker = builder.build()

        binding.addDateLayout.setOnClickListener {
            showDatePicker(materialDatePicker)
        }
    }

    private fun showDatePicker(materialDatePicker: MaterialDatePicker<*>) {
        if (!materialDatePicker.isAdded) {
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
            materialDatePicker.addOnPositiveButtonClickListener { selection ->
                // get selected date
                val date = Date(selection.toString().toLong())
                val calendar = Calendar.getInstance()

                calendar.time = date

                viewModel.year = calendar[Calendar.YEAR]
                viewModel.month = calendar[Calendar.MONTH] + 1
                viewModel.day = calendar[Calendar.DAY_OF_MONTH]

                binding.addDateTextView.text = viewModel.updateDateTV(null)
            }
        }
    }

    private fun setTypeButton() {
        binding.addGenericoTv.setOnClickListener {
            if (!binding.addGenericoTv.isSelected) {
                closeTicketBtn()

                if (binding.addBigliettoTv.isSelected) {
                    binding.addNameEditText.setText("")
                }
                binding.addNameEditText.isEnabled = true

                binding.addGenericoTv.isSelected = true
                binding.addSpesaTv.isSelected = false
                binding.addBigliettoTv.isSelected = false
            }
            viewModel.type = 2
        }

        binding.addSpesaTv.setOnClickListener {
            if (!binding.addSpesaTv.isSelected) {
                closeTicketBtn()

                if (binding.addBigliettoTv.isSelected) {
                    binding.addNameEditText.setText("")
                }
                binding.addNameEditText.isEnabled = true

                binding.addGenericoTv.isSelected = false
                binding.addSpesaTv.isSelected = true
                binding.addBigliettoTv.isSelected = false
            }
            viewModel.type = 1
        }

        binding.addBigliettoTv.setOnClickListener {
            if (!binding.addBigliettoTv.isSelected) {
                binding.addGenericoTv.isSelected = false
                binding.addSpesaTv.isSelected = false
                binding.addBigliettoTv.isSelected = true

                setBigliettoLayout()
            }
            viewModel.type = 3
        }
    }

    private fun setBigliettoLayout() {
        if (binding.addBigliettoTv.isSelected) {
            openTicketBtn()

            binding.addTrenitaliaTv.setOnClickListener {
                if (!binding.addTrenitaliaTv.isSelected) {
                    binding.addTrenitaliaTv.isSelected = true
                    binding.addAmtabTv.isSelected = false
                    binding.addAltroTv.isSelected = false

                    binding.addNameEditText.setText("Biglietto TrenItalia")
                    binding.addNameEditText.isEnabled = false
                }
            }

            binding.addAmtabTv.setOnClickListener {
                if (!binding.addAmtabTv.isSelected) {
                    binding.addTrenitaliaTv.isSelected = false
                    binding.addAmtabTv.isSelected = true
                    binding.addAltroTv.isSelected = false

                    binding.addNameEditText.setText("Biglietto Amtab")
                    binding.addNameEditText.isEnabled = false
                }
            }

            binding.addAltroTv.setOnClickListener {
                if (!binding.addAltroTv.isSelected) {
                    binding.addTrenitaliaTv.isSelected = false
                    binding.addAmtabTv.isSelected = false
                    binding.addAltroTv.isSelected = true

                    binding.addNameEditText.setText("")
                    binding.addNameEditText.isEnabled = true
                }
            }

            if (viewModel.requestCode == 2) {
                when (viewModel.name) {
                    "Biglietto TrenItalia" -> binding.addTrenitaliaTv.performClick()

                    "Biglietto Amtab" -> binding.addAmtabTv.performClick()

                    else -> binding.addAltroTv.performClick()
                }
            } else {
                binding.addTrenitaliaTv.performClick()
            }
        } else {
            closeTicketBtn()
        }
    }

    private fun openTicketBtn() {
        if (binding.addBigliettoLayout.visibility == View.GONE) {
            binding.addBigliettoLayout.animate().setInterpolator(interpolator).alpha(1f)
                .setDuration(1500)
                .start()
            binding.addBigliettoLayout.visibility = View.VISIBLE

            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            val transition = AutoTransition()
            transition.duration = 2000
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
        }
    }

    private fun closeTicketBtn() {
        if (binding.addBigliettoLayout.visibility == View.VISIBLE) {
            binding.addBigliettoLayout.animate().setInterpolator(interpolator).alpha(0f)
                .setDuration(1500)
                .start()
            binding.addBigliettoLayout.visibility = View.GONE

            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            val transition = AutoTransition()
            transition.duration = 2000
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
        }
    }

    private fun setTotSwitch() {
        binding.addTotaleSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.totChecked = isChecked
            if (isChecked) {
                binding.addNameEditText.setText("Totale")
                binding.addNameEditText.isEnabled = false

                binding.addPriceEditText.setText("0.00")
                binding.addPriceEditText.isEnabled = false

                binding.addNameEditText.error = null
                binding.addPriceEditText.error = null

                binding.addGenericoTv.isEnabled = false
                binding.addSpesaTv.isEnabled = false
                binding.addBigliettoTv.isEnabled = false

                closeTicketBtn()
            } else {
                binding.addNameEditText.setText("")
                binding.addNameEditText.isEnabled = true

                binding.addPriceEditText.setText("")
                binding.addPriceEditText.isEnabled = true

                binding.addGenericoTv.isEnabled = true
                binding.addSpesaTv.isEnabled = true
                binding.addBigliettoTv.isEnabled = true

                setBigliettoLayout()
            }
        }
    }

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAddStart() {
        binding.addProgressIndicator.show()
    }

    override fun onAddSuccess(response: LiveData<Any>) {
        response.observe(this, { value ->
            if (value != 1) {
                binding.addProgressIndicator.hide()
            }

            when (value) {
                1 -> viewModel.updateLocalList()

                "List updated" ->
                    // torna alla home
                    Intent().also {
                        it.putExtra("com.frafio.myfinance.purchaseRequest", true)
                        setResult(RESULT_OK, it)
                        finish()
                    }

                is String -> binding.root.snackbar(value, binding.addAddButton)
            }
        })
    }

    override fun onAddFailure(errorCode: Int) {
        binding.addProgressIndicator.hide()

        when (errorCode) {
            1 -> binding.addNameEditText.error = "Inserisci il nome dell'acquisto."
            2 -> binding.addNameEditText.error = "L'acquisto non può chiamarsi 'Totale'."
            3 -> binding.addPriceEditText.error = "Inserisci il costo dell'acquisto."
        }
    }
}