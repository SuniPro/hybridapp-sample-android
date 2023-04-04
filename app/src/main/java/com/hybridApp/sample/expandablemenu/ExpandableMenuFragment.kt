package com.hybridApp.sample.expandablemenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hybridApp.sample.base.BaseFragment
import com.hybridApp.sample.databinding.FragmentExpandableMenuBinding
import com.hybridApp.sample.domain.model.ExpandableMenuItem
import com.hybridApp.sample.util.DLog
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpandableMenuFragment : BaseFragment<FragmentExpandableMenuBinding>() {
//BaseFragment<FragmentExpandableMenuBinding>(FragmentExpandableMenuBinding::inflate) {


    private lateinit var menuList: ArrayList<ExpandableMenuItem>
    private val viewModel: ExpandableMenuViewModel by activityViewModels()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExpandableMenuBinding {
        return FragmentExpandableMenuBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initObservers()
        initData()
    }

    private fun initData() {
        viewModel.setEvent(ExpandableMenuContract.Event.LoadMenuList)
    }

    private fun initRecyclerView() {
        MenuInflater(
            layoutInflater,
            binding.topLayout,
            binding.subLayout,
            menuList,
            viewModel
        )

    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect {
                DLog.d("state > $it")
                when (it) {
                    is ExpandableMenuContract.State.Idle -> {}
                    is ExpandableMenuContract.State.Loading -> {}
                    is ExpandableMenuContract.State.Success -> {
                        menuList = it.menuList
                        initRecyclerView()
                    }
                    is ExpandableMenuContract.State.SelectUrl -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.effect.collect {
                when (it) {
                    is ExpandableMenuContract.Effect.ShowToast -> {
                        Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()

                        /*val toast = Toast(context)
                        toast.setGravity(Gravity.CENTER, 0, 0)

                        val textView = TextView(context)
                        textView.setBackgroundColor(Color.LTGRAY)
                        textView.setTextColor(Color.BLACK)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                        textView.text = it.msg
                        textView.setPadding(10, 10, 10, 10)

                        toast.view = textView
                        toast.show()*/
                    }
                    is ExpandableMenuContract.Effect.Exception -> {
                        Firebase.crashlytics.recordException(it.err)
                    }
                }
            }
        }
    }
}