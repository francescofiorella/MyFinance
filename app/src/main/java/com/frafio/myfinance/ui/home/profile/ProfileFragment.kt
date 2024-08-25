package com.frafio.myfinance.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.databinding.FragmentProfileBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.utils.dateToString
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.textview.MaterialTextView

class ProfileFragment : BaseFragment(), ProfileListener {

    private val viewModel by viewModels<ProfileViewModel>()
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_profile, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.listener = this

        viewModel.user?.apply {
            binding.signupDateTV.text = getString(
                R.string.signUpDate,
                dateToString(creationDay, creationMonth, creationYear)
            )
        }

        binding.profileEditCard.setOnClickListener {
            if (resources.getBoolean(R.bool.is600dp)) {
                val sideSheetDialog = SideSheetDialog(requireContext())
                sideSheetDialog.setContentView(R.layout.layout_edit_profile_bottom_sheet)
                defineSheetInterface(
                    sideSheetDialog.findViewById(android.R.id.content)!!,
                    viewModel.user?.fullName ?: "",
                    viewModel,
                    sideSheetDialog::hide
                )
                sideSheetDialog.show()
            } else {
                val modalBottomSheet = ModalBottomSheet(
                    this,
                    viewModel.user?.fullName ?: "",
                    viewModel
                )
                modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
            }
        }

        binding.dynamicColorSwitch.also {
            it.isChecked = viewModel.isSwitchDynamicColorChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDynamicColor(isChecked)
                (activity as HomeActivity).showSnackBar(
                    getString(R.string.restart_app_changes),
                    getString(R.string.restart)
                ) {
                    (activity as HomeActivity).finish()
                }
            }
        }

        return binding.root
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.profileScrollView.apply {
            stopNestedScroll()
            scrollTo(0, 0)
        }
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
    }

    override fun onProfileUpdateComplete(response: LiveData<AuthResult>) {
        response.observe(this) { authResult ->
            when (authResult.code) {
                AuthCode.USER_DATA_UPDATED.code -> {
                    viewModel.updateLocalUser()
                    binding.invalidateAll()
                    (activity as HomeActivity).hideProgressIndicator()
                    (activity as HomeActivity).showSnackBar(authResult.message)
                }

                AuthCode.USER_DATA_NOT_UPDATED.code -> {
                    (activity as HomeActivity).hideProgressIndicator()
                    (activity as HomeActivity).showSnackBar(authResult.message)
                }

                else -> Unit
            }
        }
    }

    class ModalBottomSheet(
        private val fragment: ProfileFragment,
        private val fullName: String,
        private val viewModel: ProfileViewModel
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
                inflater.inflate(R.layout.layout_edit_profile_bottom_sheet, container, false)
            fragment.defineSheetInterface(layout, fullName, viewModel, this::dismiss)
            return layout
        }
    }

    fun defineSheetInterface(
        layout: View,
        fullName: String,
        viewModel: ProfileViewModel,
        dismissFun: () -> Unit
    ) {
        val propicLayout = layout.findViewById<LinearLayout>(R.id.propic_layout)
        val fullNameLayout = layout.findViewById<LinearLayout>(R.id.full_name_layout)
        val fullNameET = layout.findViewById<AppCompatEditText>(R.id.full_nameET)
        val fullNameBtn = layout.findViewById<Button>(R.id.full_name_btn)

        fullNameET?.setText(fullName)
        propicLayout?.setOnClickListener {
            (activity as HomeActivity).showSnackBar("Cooming soon!")
            //viewModel.uploadPropic()
            dismissFun()
        }
        fullNameLayout?.setOnClickListener {
            layout.findViewById<MaterialTextView>(R.id.full_nameTV)?.visibility = View.GONE
            fullNameBtn?.visibility = View.VISIBLE
            fullNameET?.visibility = View.VISIBLE
            fullNameLayout.isClickable = false
            fullNameET?.isFocusableInTouchMode = true
            fullNameET?.requestFocus()
            fullNameET?.setSelection(fullName.length)
        }
        fullNameET?.doOnTextChanged { text, _, _, _ ->
            val currentText = text?.trim()
            fullNameBtn?.isEnabled =
                currentText.toString().isNotEmpty() && currentText.toString() != fullName
        }
        fullNameBtn?.setOnClickListener {
            viewModel.editFullName(fullNameET?.text.toString().trim())
            dismissFun()
        }
    }
}