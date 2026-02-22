package com.frafio.myfinance.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.databinding.FragmentProfileBinding
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.features.home.profile.EditFullNameSheet
import com.frafio.myfinance.ui.features.home.profile.EditProfileSheet
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.setRoundDrawableFromUrl
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetDialog

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
                getCreationDataString()
            )
            binding.profilePropicIv.setRoundDrawableFromUrl(photoUrl)
        }

        binding.profileEditCard.setOnClickListener {
            val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
                SideSheetDialog(requireContext())
            } else {
                BottomSheetDialog(requireContext())
            }
            val composeView = getEditProfileSheetDialogComposeView(sheetDialog::hide)
            sheetDialog.setContentView(composeView)
            sheetDialog.show()
        }

        binding.dynamicColorSwitch.also {
            it.isChecked = viewModel.isSwitchDynamicColorChecked

            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDynamicColor(isChecked)
                (activity as HomeActivity).showSnackBar(
                    getString(R.string.restart_app_changes),
                    getString(R.string.restart)
                ) {
                    (activity as HomeActivity).applicationContext.also { ctx ->
                        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
                        val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
                        ctx.startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }
                }
            }
        }

        return binding.root
    }

    private fun getEditProfileSheetDialogComposeView(onDismiss: () -> Unit): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    EditProfileSheet(
                        onDismiss = onDismiss,
                        onUploadProPic = {
                            (activity as HomeActivity).showSnackBar(getString(R.string.coming_soon))
                        },
                        onEditFullName = {
                            val sheetDialog = if (resources.getBoolean(R.bool.is600dp)) {
                                SideSheetDialog(requireContext())
                            } else {
                                BottomSheetDialog(requireContext())
                            }
                            val composeView =
                                getEditFullNameSheetDialogComposeView(sheetDialog::hide)
                            sheetDialog.setContentView(composeView)
                            onDismiss()
                            sheetDialog.show()
                        }
                    )
                }
            }
        }
    }

    private fun getEditFullNameSheetDialogComposeView(onDismiss: () -> Unit): ComposeView {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    EditFullNameSheet(
                        fullName = viewModel.user?.fullName ?: "",
                        onDismiss = onDismiss,
                        onEditFullName = { viewModel.editFullName(it) }

                    )
                }
            }
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        binding.profileScrollView.apply {
            fling(0)
            smoothScrollTo(0, 0)
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
}
