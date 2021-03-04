package com.calamity.weather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.openweather.subclasses.onecall.DailyWeather
import com.calamity.weather.databinding.ItemWeatherBinding
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.roundToInt


class WeatherAdapter(
    private val context: Context,
    private val imageMap: HashMap<String, Int>,
    private val listener: OnItemClickListener
    ) : ListAdapter<Weather, WeatherAdapter.WeatherViewHolder>(DiffCallback()) {

    private val expansionsCollection = ExpansionLayoutCollection().apply { openOnlyOne(true) }

    inner class WeatherViewHolder(private val binding: ItemWeatherBinding, private val dailyAdapter: DailyWeatherAdapter) : RecyclerView.ViewHolder(
        binding.root
    ) {

        var viewExpanded = false

        init {
            binding.apply {
                openMapBtn.setOnClickListener {
                    listener.onClick(it, getItem(adapterPosition))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(weather: Weather) {
            binding.apply {
                // Binding text
                locationName.text = weather.cityName
                locationName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (weather.isLocationEntry) R.drawable.ic_location_24dp else 0,
                    0, 0, 0
                )
                conditions.text = weather.weather.weatherConditions[0].main
                temperature.text = "${weather.weather.temperature.roundToInt()}°"
                expansionsCollection.add(expansionLayout)

                val todayData = weather.daily[0]
                today.text = "${context.resources.getString(R.string.today)} · ${todayData.weatherConditions[0].main}"
                temperatureMinmaxToday.text = "${todayData.temperature.max.roundToInt()}° / ${todayData.temperature.min.roundToInt()}°"

                val tomorrowData = weather.daily[1]

                tomorrow.text = "${context.resources.getString(R.string.tomorrow)} · ${tomorrowData.weatherConditions[0].main}"
                temperatureMinmaxTomorrow.text = "${tomorrowData.temperature.max.roundToInt()}° / ${tomorrowData.temperature.min.roundToInt()}°"


                val afterTomorrowData = weather.daily[2]
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = weather.weather.currentTime
                afterTomorrow.text = "${getWeekDayName(calendar.get(Calendar.DAY_OF_WEEK))} · ${afterTomorrowData.weatherConditions[0].main}"

                temperatureMinmaxAfterTomorrow.text = "${afterTomorrowData.temperature.max.roundToInt()}° / ${afterTomorrowData.temperature.min.roundToInt()}°"

                // Handling inner recycler
                dailyWeatherRecycler.apply {
                    adapter = dailyAdapter
                    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    setHasFixedSize(true)
                    addItemDecoration(DailyWeatherAdapter.MarginItemDecoration(resources.getDimensionPixelSize(
                        R.dimen.recyclerview_margin
                    )))
                }

                val list = ArrayList<DailyWeather>()
                for (i in 3 until weather.daily.size) {
                    list.add(weather.daily[i])
                }
                dailyAdapter.submitList(list)

                expansionLayout.addListener { expansionLayout, expanded ->
                    viewExpanded = expanded
                }



                // Binding temperature color
                /*val minColor = context.getColor(R.color.min_temperature)
                val maxColor = context.getColor(R.color.max_temperature)
                val color = getMappedColor(
                    weather.weather.temperature.toInt(),
                    minColor,
                    maxColor,
                    -50,
                    50
                )

                headerLayout.backgroundTintList = ColorStateList.valueOf(color)*/

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = ItemWeatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val adapter = DailyWeatherAdapter(context, imageMap)
        return WeatherViewHolder(binding, adapter)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class DiffCallback : DiffUtil.ItemCallback<Weather>() {
        override fun areItemsTheSame(oldItem: Weather, newItem: Weather): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Weather, newItem: Weather): Boolean =
            oldItem == newItem

    }

    interface OnItemClickListener {
        fun onClick(view: View, weather: Weather)
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

    // TODO: make work
    fun getMappedColor(temp: Int, minColor: Int, maxColor: Int, from: Int, to: Int): Int {

        val minRed = minColor shr 16 and 0xFF
        val minGreen = minColor shr 8 and 0xFF
        val minBlue = minColor and 0xFF

        val maxRed = maxColor shr 16 and 0xFF
        val maxGreen = maxColor shr 8 and 0xFF
        val maxBlue = maxColor and 0xFF

        val interval_R = (maxRed - minRed) / abs(from - to)
        val interval_G = (maxGreen - minGreen) / abs(from - to)
        val interval_B = (maxBlue - minBlue) / abs(from - to)

        Log.v("Mapping color", "resulting color: ${(temp + to) * interval_R}, ${(temp + to) * interval_G}, ${(temp + to) * interval_B}")

        return Color.rgb((temp + to) * interval_R, (temp + to) * interval_G, (temp + to) * interval_B)
    }

    private fun getWeekDayName(day: Int): String =
        context.getString(when (day) {
            Calendar.MONDAY -> R.string.monday
            Calendar.TUESDAY -> R.string.tuesday
            Calendar.WEDNESDAY -> R.string.wednesday
            Calendar.THURSDAY -> R.string.thursday
            Calendar.FRIDAY -> R.string.friday
            Calendar.SATURDAY -> R.string.saturday
            Calendar.SUNDAY -> R.string.sunday
            else -> R.string.language_code
        })
}
