package com.calamity.weather.ui.currentweather

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.calamity.weather.R
import com.calamity.weather.data.api.subclasses.WeatherCondition
import com.calamity.weather.data.database.Converters
import com.calamity.weather.databinding.FragmentCurrentWeatherBinding
import com.calamity.weather.ui.adapters.CurrentWeatherAdapter
import com.calamity.weather.ui.detailedweather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrentWeatherFragment : Fragment(R.layout.fragment_current_weather) {
    private val weatherViewModel: CurrentWeatherViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCurrentWeatherBinding.bind(view)

        val weatherAdapter = CurrentWeatherAdapter()

        binding.apply {
            currentWeatherRecycler.apply {
                adapter = weatherAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(CurrentWeatherAdapter.MarginItemDecoration(resources.getDimensionPixelSize(R.dimen.recyclerview_margin)))
            }
        }

        weatherViewModel.weather.observe(viewLifecycleOwner) {
            weatherAdapter.submitList(it)
        }
    }
}