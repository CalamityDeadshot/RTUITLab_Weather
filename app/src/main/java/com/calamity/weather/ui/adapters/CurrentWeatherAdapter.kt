package com.calamity.weather.ui.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calamity.weather.data.api.CurrentWeather
import com.calamity.weather.databinding.ItemCurrentWeatherBinding

class CurrentWeatherAdapter : ListAdapter<CurrentWeather, CurrentWeatherAdapter.WeatherViewHolder>(DiffCallback()) {

    class WeatherViewHolder(private val binding: ItemCurrentWeatherBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weather: CurrentWeather) {
            binding.apply {
                locationName.text = weather.cityName
                conditions.text = weather.weatherConditions[0].main
                temperature.text = "${Math.round(weather.main.temp!!)}°"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = ItemCurrentWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
