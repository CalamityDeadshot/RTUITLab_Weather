package com.calamity.weather.ui.adapters

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.openweather.subclasses.onecall.DailyWeather
import com.calamity.weather.data.repository.RainViewerRepository
import com.calamity.weather.databinding.ItemWeatherBinding
import com.calamity.weather.ui.weather.TimePickerFragment
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


class WeatherAdapter(
    private val context: Context,
    private val imageMap: HashMap<String, Int>,
    private val listener: OnItemClickListener,
    private val onDatePickedListener: OnDatePickedListener,
    private val superFragment: Fragment
) : ListAdapter<Weather, WeatherAdapter.WeatherViewHolder>(DiffCallback()) {

    private val expansionsCollection = ExpansionLayoutCollection().apply { openOnlyOne(true) }
    private val repository: RainViewerRepository = RainViewerRepository()

    inner class WeatherViewHolder(
        private val binding: ItemWeatherBinding,
        private val dailyAdapter: DailyWeatherAdapter
    ) : RecyclerView.ViewHolder(
        binding.root
    ), TimePickerDialog.OnTimeSetListener, OnMapReadyCallback {

        var viewExpanded = false
        lateinit var gMap: GoogleMap
        init {
            binding.apply {
                openMapBtn.setOnClickListener {
                    listener.onClick(it, getItem(adapterPosition))
                }
                notify.setOnClickListener {
                    /*val action = WeatherFragmentDirections.actionCurrentWeatherFragmentToTimePickerDialog()
                    superFragment.findNavController().navigate(action)*/
                    // We use this instead of commented code because using NavigationComponent there is no way to set a listener in a meaningful way
                    TimePickerFragment(this@WeatherViewHolder).show(
                        superFragment.childFragmentManager,
                        "Time picker"
                    )
                }
                with(map) {
                    onCreate(null)
                    getMapAsync(this@WeatherViewHolder)
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
                temperature.text = "${weather.weather.temperature.roundToInt()}°"
                if (weather.daily.isNotEmpty()) {
                    conditions.text = weather.weather.weatherConditions[0].description.capitalize(Locale.getDefault())
                    expansionsCollection.add(expansionLayout)

                    val todayData = weather.daily[0]
                    today.text =
                        "${context.resources.getString(R.string.today)} · ${todayData.weatherConditions[0].description.capitalize(Locale.getDefault())}"
                    temperatureMinmaxToday.text =
                        "${todayData.temperature.max.roundToInt()}° / ${todayData.temperature.min.roundToInt()}°"

                    val tomorrowData = weather.daily[1]

                    tomorrow.text =
                        "${context.resources.getString(R.string.tomorrow)} · ${tomorrowData.weatherConditions[0].description.capitalize(Locale.getDefault())}"
                    temperatureMinmaxTomorrow.text =
                        "${tomorrowData.temperature.max.roundToInt()}° / ${tomorrowData.temperature.min.roundToInt()}°"


                    val afterTomorrowData = weather.daily[2]
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = weather.weather.currentTime
                    afterTomorrow.text =
                        "${getWeekDayName(calendar.get(Calendar.DAY_OF_WEEK))} · ${afterTomorrowData.weatherConditions[0].description.capitalize(Locale.getDefault())}"

                    temperatureMinmaxAfterTomorrow.text =
                        "${afterTomorrowData.temperature.max.roundToInt()}° / ${afterTomorrowData.temperature.min.roundToInt()}°"
                }

                // Handling inner recycler
                dailyWeatherRecycler.apply {
                    adapter = dailyAdapter
                    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    setHasFixedSize(true)
                    addItemDecoration(
                        DailyWeatherAdapter.MarginItemDecoration(
                            resources.getDimensionPixelSize(
                                R.dimen.recyclerview_margin
                            )
                        )
                    )
                }

                val list = ArrayList<DailyWeather>()
                for (i in 3 until weather.daily.size) {
                    list.add(weather.daily[i])
                }
                dailyAdapter.submitList(list)

                expansionLayout.addListener { expansionLayout, expanded ->
                    viewExpanded = expanded
                }


                setMapLocation()


                // Binding temperature color
                val minColor = context.getColor(
                    if (weather.weather.temperature.toInt() < 17)
                        R.color.max_temperature_cold
                    else R.color.min_temperature_warm
                )
                val maxColor = context.getColor(
                    if (weather.weather.temperature.toInt() < 17)
                        R.color.min_temperature
                    else
                        R.color.max_temperature
                )
                val color = getMappedColor(
                    weather.weather.temperature.toInt(),
                    minColor,
                    maxColor,
                    if (weather.weather.temperature.toInt() < 17) -50 else 17,
                    if (weather.weather.temperature.toInt() < 17) 17 else 50
                )
                headerLayout.backgroundTintList = ColorStateList.valueOf(color)

            }
        }

        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
            onDatePickedListener.onDatePicked(getItem(adapterPosition), hourOfDay, minute)
        }

        override fun onMapReady(googleMap: GoogleMap) {
            MapsInitializer.initialize(superFragment.activity)
            gMap = googleMap
            with(gMap.uiSettings) {
                isZoomControlsEnabled = true
                isCompassEnabled = false
                isMapToolbarEnabled = false
                isMyLocationButtonEnabled = false
                isRotateGesturesEnabled = false
                isScrollGesturesEnabled = false
                isTiltGesturesEnabled = false
                isZoomGesturesEnabled = false
            }
            gMap.setMaxZoomPreference(15f)
            binding.map.onResume()
            setMapLocation()
        }
        private fun setMapLocation() {
            if (!::gMap.isInitialized) return
            val weather = getItem(adapterPosition)
            with(gMap) {
                moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            weather.latitude,
                            weather.longitude
                        ), 10f
                    )
                )
                setMaxZoomPreference(7f)
                mapType = GoogleMap.MAP_TYPE_NORMAL
                setOnMapClickListener {
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            weather.latitude,
                            weather.longitude
                        ), 10f)
                    )
                }
            }
            superFragment.viewLifecycleOwner.lifecycleScope.launch {
                repository.getInfo { response ->
                    gMap.addTileOverlay(TileOverlayOptions().tileProvider(
                        object : UrlTileProvider(256, 256) {
                            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                                val s =
                                    "${response.host}${response.radar.past.last().path}/256/$zoom/$x/$y/1/1_1.png"
                                return URL(s)
                            }
                        }
                    ).transparency(.5F))
                }
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

    override fun onViewRecycled(holder: WeatherViewHolder) {
        holder.gMap.clear()
        holder.gMap.mapType = GoogleMap.MAP_TYPE_NONE
        super.onViewRecycled(holder)
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

    interface OnDatePickedListener {
        fun onDatePicked(weather: Weather, hour: Int, minute: Int)
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
    val TAG = "Mapping color"
    // TODO: make work
    fun getMappedColor(t: Int, minColor: Int, maxColor: Int, from: Int, to: Int): Int {

        val temp = if (t > 17) abs(t - 50) else abs(t + 50)

        val minRed = minColor shr 16 and 0xFF
        val minGreen = minColor shr 8 and 0xFF
        val minBlue = minColor and 0xFF
        val maxRed = maxColor shr 16 and 0xFF
        val maxGreen = maxColor shr 8 and 0xFF
        val maxBlue = maxColor and 0xFF
        val interval_R: Double = abs((maxRed - minRed).toDouble() / abs(from - to).toDouble())
        val interval_G: Double = abs((maxGreen - minGreen) / abs(from - to).toDouble())
        val interval_B: Double = abs((maxBlue - minBlue) / abs(from - to).toDouble())

        return Color.rgb(
            ((temp) * interval_R + minRed.coerceAtMost(maxRed)).roundToInt(),
            ((temp) * interval_G + minGreen.coerceAtMost(maxGreen)).roundToInt(),
            ((temp) * interval_B + minBlue.coerceAtMost(maxBlue)).roundToInt()
        )
    }

    private fun getWeekDayName(day: Int): String =
        context.getString(
            when (day) {
                Calendar.MONDAY -> R.string.monday
                Calendar.TUESDAY -> R.string.tuesday
                Calendar.WEDNESDAY -> R.string.wednesday
                Calendar.THURSDAY -> R.string.thursday
                Calendar.FRIDAY -> R.string.friday
                Calendar.SATURDAY -> R.string.saturday
                Calendar.SUNDAY -> R.string.sunday
                else -> R.string.language_code
            }
        )
}
