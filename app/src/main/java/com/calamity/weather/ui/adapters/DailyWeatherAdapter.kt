package com.calamity.weather.ui.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.subclasses.onecall.DailyWeather
import com.calamity.weather.databinding.ItemDailyBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class DailyWeatherAdapter(val context: Context, private val imageMap: HashMap<String, Int>) : ListAdapter<DailyWeather, DailyWeatherAdapter.DailyWeatherViewHolder>(
    DiffCallback()
) {

    inner class DailyWeatherViewHolder(private val binding: ItemDailyBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(weather: DailyWeather, calendar: Calendar) {
            binding.apply {
                //val calendar = Calendar.getInstance()
                calendar.timeInMillis = weather.currentTime * 1000
                dayOfWeek.text = getWeekDayName(calendar.get(Calendar.DAY_OF_WEEK))

                tempMinmax.text = "${weather.temperature.max.roundToInt()}° / ${weather.temperature.min.roundToInt()}°"

                icon.setImageResource(imageMap[weather.weatherConditions[0].icon]!!)

                iconWind.rotation = weather.windDirection
                wind.text = context.getString(R.string.wind_speed, String.format("%.1f", weather.windSpeed))
            }

        }

    }

    class DiffCallback : DiffUtil.ItemCallback<DailyWeather>() {
        override fun areItemsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean =
            oldItem.currentTime == newItem.currentTime

        override fun areContentsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean =
            oldItem == newItem

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val binding = ItemDailyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, Calendar.getInstance())
    }

    class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    left = spaceSize
                }
                top = spaceSize
                right = spaceSize
                bottom = spaceSize
            }
        }
    }

    private fun getRotatedDrawable(degree: Float): Drawable {
        val iconBitmap = (AppCompatResources.getDrawable(context, R.drawable.ic_wind_direction) as BitmapDrawable).bitmap
        val matrix = Matrix()
        matrix.postRotate(degree)
        val targetBitmap =
            Bitmap.createBitmap(iconBitmap, 0, 0, iconBitmap.width, iconBitmap.height, matrix, true)
        return BitmapDrawable(context.resources, targetBitmap)
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