package com.frafio.myfinance.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.frafio.myfinance.ui.BaseFragment
import com.frafio.myfinance.ui.features.home.dashboard.DashboardScreen
import com.frafio.myfinance.ui.theme.MyFinanceTheme

class DashboardFragment : BaseFragment() {
    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyFinanceTheme {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun scrollUp() {
        super.scrollUp()
        viewModel.scrollToTop()
    }
}
