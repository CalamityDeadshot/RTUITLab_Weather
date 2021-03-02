package com.calamity.weather.ui.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.databinding.ItemCurrentWeatherBinding
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection


class WeatherAdapter : ListAdapter<CurrentWeather, WeatherAdapter.WeatherViewHolder>(DiffCallback()) {

    private val expansionsCollection = ExpansionLayoutCollection().apply { openOnlyOne(true) }

    inner class WeatherViewHolder(private val binding: ItemCurrentWeatherBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {


        fun bind(weather: CurrentWeather) {
            binding.apply {
                locationName.text = weather.cityName
                locationName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (weather.isLocationEntry) R.drawable.ic_location_24dp else 0,
                    0, 0, 0
                )
                conditions.text = weather.weatherConditions[0].main
                temperature.text = "${Math.round(weather.main.temp!!)}°"
                expansionsCollection.add(expansionLayout)
                today.append(" · ")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = ItemCurrentWeatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrentWeather>() {
        override fun areItemsTheSame(oldItem: CurrentWeather, newItem: CurrentWeather): Boolean =
            oldItem.db_id == newItem.db_id

        override fun areContentsTheSame(oldItem: CurrentWeather, newItem: CurrentWeather): Boolean =
            oldItem == newItem

    }


    class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    top = spaceSize
                }
                left = spaceSize
                right = spaceSize
                bottom = spaceSize
            }
        }
    }
}
