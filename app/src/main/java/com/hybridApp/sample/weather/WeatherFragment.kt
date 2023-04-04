package com.hybridApp.sample.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hybridApp.sample.R
import com.hybridApp.sample.base.BaseFragment
import com.hybridApp.sample.data.model.Weather
import com.hybridApp.sample.databinding.FragmentWeatherBinding
import com.hybridApp.sample.util.DLog
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherFragment : BaseFragment<FragmentWeatherBinding>() {
//BaseFragment<FragmentWeatherBinding>(FragmentWeatherBinding::inflate) {

    private val viewModel: WeatherViewModel by viewModels()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWeatherBinding {
        return FragmentWeatherBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        initData()
    }

    private fun initData() {
        viewModel.setEvent(WeatherContract.Event.Load)
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WeatherContract.State.Loading -> {}
                    is WeatherContract.State.Success -> {
                        DLog.d("state > $state")
                        updateView(state.weather)
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.effect.collect {
                when (it) {
                    is WeatherContract.Effect.Exception -> {
                        Firebase.crashlytics.recordException(it.err)
                    }
                }
            }
        }
    }

    private fun updateView(weather: Weather) {
        with(binding) {
            ivSky.setImageResource(getSkyResource(weather.wfKor))
            tvZone.text = weather.zone
            tvTemp.text = getString(R.string.temp_c, String.format("%.1f", weather.temp.toFloat()))
            ivWd.rotation = getRotation(weather.wd.toFloat())
            tvWs.text = getString(R.string.wind_speed, String.format("%.1f", weather.ws.toFloat()))
            tvWd.text = weather.wdKor
            layoutWeather.visibility = View.VISIBLE
        }
    }

    private fun getSkyResource(wfKor: String): Int =
        when (wfKor) {
            "맑음" -> R.drawable.clearly
            "구름 조금" -> R.drawable.partly_cloud
            "구름 많음" -> R.drawable.mostly_cloud
            "흐림" -> R.drawable.cloud
            "비" -> R.drawable.rainy
            "눈" -> R.drawable.snow_night
            "눈/비" -> R.drawable.snow_rain
            else -> R.drawable.clearly
        }

    private fun getRotation(wd: Float): Float {
        return ((wd + 8) % 8) * 45
    }

}