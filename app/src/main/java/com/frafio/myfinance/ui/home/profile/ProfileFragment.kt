package com.frafio.myfinance.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.features.home.profile.ProfileScreen
import com.frafio.myfinance.ui.home.HomeActivity
import com.frafio.myfinance.ui.theme.MyFinanceTheme

class ProfileFragment : BaseFragment(), ProfileListener {

    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.listener = this

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    ProfileScreen(
                        viewModel = viewModel,
                        onUploadProPic = {
                            (activity as HomeActivity).showSnackBar(getString(R.string.coming_soon))
                        },
                        onDynamicColorChanged = { isChecked ->
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
                    )
                }
            }
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        viewModel.scrollToTop()
    }

    override fun onStarted() {
        (activity as HomeActivity).showProgressIndicator()
    }

    override fun onProfileUpdateComplete(response: LiveData<AuthResult>) {
        response.observe(viewLifecycleOwner) { authResult ->
            when (authResult.code) {
                AuthCode.USER_DATA_UPDATED.code -> {
                    viewModel.updateLocalUser()
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
