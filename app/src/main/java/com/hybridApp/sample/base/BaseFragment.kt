package com.hybridApp.sample.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hybridApp.sample.util.DLog

//typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T
//abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //_binding = inflate.invoke(inflater, container, false)
        _binding = getFragmentBinding(inflater, container)
        DLog.d("binding=${_binding.toString()}")
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DLog.d("binding=${_binding.toString()}")

        _binding = null
    }
}
