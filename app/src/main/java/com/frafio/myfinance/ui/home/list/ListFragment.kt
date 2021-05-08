package com.frafio.myfinance.ui.home.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.databinding.FragmentListBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ListFragment : Fragment(), KodeinAware {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mWarningTV: TextView

    private lateinit var viewModel: ListViewModel

    override val kodein by kodein()
    private val factory: ListViewModelFactory by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        viewModel = ViewModelProvider(this, factory).get(ListViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        mRecyclerView = binding.root.findViewById(R.id.list_recyclerView)
        mWarningTV = binding.root.findViewById(R.id.list_warningTV)

        return binding.root
    }

    fun loadPurchasesList() {
        val mAdapter = PurchaseAdapter(activity, context, mRecyclerView, mWarningTV)
        mRecyclerView.adapter = mAdapter
    }

    fun scrollListToTop() {
        mRecyclerView.smoothScrollToPosition(0)
    }
}