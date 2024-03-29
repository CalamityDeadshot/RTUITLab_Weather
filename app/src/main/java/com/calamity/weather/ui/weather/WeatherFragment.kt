package com.calamity.weather.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.databinding.FragmentWeatherBinding
import com.calamity.weather.ui.adapters.WeatherAdapter
import com.calamity.weather.ui.mainactivity.MainActivity
import com.calamity.weather.utils.Variables
import com.calamity.weather.utils.notifications.NotificationHelper
import com.calamity.weather.utils.onQueryTextChanged
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.layout_list_empty.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.Calendar
import kotlin.collections.HashMap
import androidx.lifecycle.Observer


@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather), WeatherAdapter.OnItemClickListener, WeatherAdapter.OnDatePickedListener {
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        initBinding(view)
        initListeners()
        requestGpsPermission()
        setHasOptionsMenu(true)

        fab_add_city.isEnabled = Variables.isNetworkConnected

    }

    private fun initBinding(view: View) {
        val binding = FragmentWeatherBinding.bind(view)

        val manager = LinearLayoutManager(requireContext())
        val weatherAdapter = WeatherAdapter(requireContext(), getImageMap(), this, this, this, manager)


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.event.collect { event ->
                when (event) {
                    is WeatherViewModel.WeatherEvent.ShowUndoDeleteMessage -> {
                        Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.undo)) {
                                viewModel.onUndoDelete(event.weather)
                            }.show()
                    }
                }
            }
            if (Variables.isNetworkConnected) {
                viewModel.update()
            } else {
                switchTo(R.id.empty)
            }
        }


        binding.apply {
            currentWeatherRecycler.apply {
                adapter = weatherAdapter
                layoutManager = manager
                setHasFixedSize(true)
                addItemDecoration(
                    WeatherAdapter.MarginItemDecoration(
                        resources.getDimensionPixelSize(
                            R.dimen.recyclerview_margin
                        )
                    )
                )
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val weather = weatherAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onEntrySwiped(weather)
                }

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    if (weatherAdapter.currentList[viewHolder.adapterPosition].isLocationEntry ||
                        (viewHolder as WeatherAdapter.WeatherViewHolder).viewExpanded
                    ) {
                        return 0
                    }
                    return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
                }
            }).attachToRecyclerView(currentWeatherRecycler)
        }

        viewModel.weather.observe(viewLifecycleOwner, Observer { it ->
            weatherAdapter.submitList(it)
            handleEmptyList(it)
            (view.parent as ViewGroup).doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        viewModel.busy.observe(viewLifecycleOwner, Observer {
            switchTo(R.id.recycler_swipe_layout)
            recycler_swipe_layout.isRefreshing = it
        })

        Variables.isNetworkConnectedLive.observe(viewLifecycleOwner, Observer {
            fab_add_city.isEnabled = it
            if (it)
                if (weatherAdapter.itemCount != 0)
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.update()
                    }
                else getWeatherByLocation()
        })

        img_search.visibility = View.GONE
        loading.visibility = View.GONE

    }

    private fun initListeners() {
        recycler_swipe_layout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.update()
                getWeatherByLocation()
            }
        }

        btn_retry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.update()
                handleEmptyList((current_weather_recycler.adapter as WeatherAdapter).currentList)
            }
        }

        fab_add_city.setOnClickListener {
            val action = WeatherFragmentDirections.actionCurrentWeatherFragmentToSearchFragment()
            findNavController().navigate(action)
        }
    }

    // This method checks if the list is empty and decides which message to show if the list is empty for whatever reason:
    //      1. There is no Internet connection
    //      2. There is Internet connection, but user denied location access so there is no way to populate the list
    // Otherwise, the list will not be empty because an entry with weather by location will be created.
    private fun handleEmptyList(list: List<Weather>) {
        if (list.isEmpty()) {
            when (switcher.currentView.id) {
                // Case empty list is visible
                R.id.recycler_swipe_layout -> {
                    if (Variables.isNetworkConnected) {
                        if (!gpsPermissionGranted()) {
                            showDeniedPermissionMessage()
                        } else {
                            getWeatherByLocation()
                        }
                    } else {
                        showNoInternetMessage()
                    }
                    switcher.showNext()
                }
                // Case empty message is visible: required to process Retry action
                else -> {
                    if (Variables.isNetworkConnected) {
                        if (!gpsPermissionGranted()) {
                            showDeniedPermissionMessage()
                        } else {
                            getWeatherByLocation()
                            switcher.showNext()
                        }
                    }
                }
            }
        } else switchTo(R.id.recycler_swipe_layout)
    }


    @Suppress("DEPRECATION")
    fun requestGpsPermission() {
        if (gpsPermissionGranted()) return

        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (Variables.isNetworkConnected) {
                        getWeatherByLocation()
                    }

                }
            }
        }
    }

    @SuppressLint("MissingPermission") // Only called if permission is granted from onRequestPermissionsResult
    private fun getWeatherByLocation() {
        if (!gpsPermissionGranted()) return
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as MainActivity)

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_getting_location),
                    Toast.LENGTH_LONG
                ).show()
                return@addOnSuccessListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.addGpsWeather(it.latitude, it.longitude)
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }

    }

    private fun gpsPermissionGranted() =
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_weather, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getImageMap(): HashMap<String, Int> {
        val map = HashMap<String, Int>()
        map["01d"] = R.drawable.ic_01d
        map["01n"] = R.drawable.ic_01n
        map["02d"] = R.drawable.ic_02d
        map["02n"] = R.drawable.ic_02n
        map["03d"] = R.drawable.ic_03d
        map["03n"] = R.drawable.ic_03n
        map["04d"] = R.drawable.ic_04d
        map["04n"] = R.drawable.ic_04n
        map["09d"] = R.drawable.ic_09d
        map["09n"] = R.drawable.ic_09n
        map["10d"] = R.drawable.ic_10d
        map["10n"] = R.drawable.ic_10n
        map["11d"] = R.drawable.ic_11d
        map["11n"] = R.drawable.ic_11n
        map["13d"] = R.drawable.ic_13d
        map["13n"] = R.drawable.ic_13n
        map["50d"] = R.drawable.ic_50d
        map["50n"] = R.drawable.ic_50n
        return map
    }

    override fun onClick(view: View, weather: Weather) {
        when (view.id) {
            R.id.open_map_btn -> {
                val alertDialog: AlertDialog = activity.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton(getString(R.string.google)) { _, _ ->
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    constructGoogleMapsUri(
                                        weather.latitude,
                                        weather.longitude,
                                        10
                                    )
                                )
                            )
                            startActivity(browserIntent)
                        }
                        setNegativeButton(getString(R.string.yandex)) { _, _ ->
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    constructYandexMapsUri(
                                        weather.longitude,
                                        weather.latitude,
                                        10
                                    )
                                )
                            )
                            startActivity(browserIntent)
                        }
                        setNeutralButton(getString(R.string.cancel)) { _, _ ->

                        }
                    }
                    builder.setMessage(getString(R.string.choose_maps_msg))
                        .setTitle(getString(R.string.choose_maps))
                    builder.create()
                }
                alertDialog.show()
            }
            R.id.tiles_overlay -> {
                val bundle = bundleOf("id" to weather.id, "transitionName" to weather.placeId)
                findNavController().navigate(
                    R.id.action_currentWeatherFragment_to_precipitationMapFragment,
                    bundle
                )
            }
            R.id.notify -> {
                val alertDialog: AlertDialog = activity.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                            NotificationHelper.deleteScheduledNotification(requireContext(), weather)
                            viewModel.onNotificationAction(weather, false)
                        }
                        setNegativeButton(getString(android.R.string.cancel)) { _, _ ->

                        }
                    }
                    builder.setMessage(getString(R.string.notification_delete_msg))
                        .setTitle(getString(R.string.are_you_sure))
                    builder.create()
                }
                alertDialog.show()
            }
        }
    }

    override fun onDatePicked(weather: Weather, hour: Int, minute: Int) {

        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.set(Calendar.HOUR_OF_DAY, hour)
        datetimeToAlarm.set(Calendar.MINUTE, minute)
        datetimeToAlarm.set(Calendar.SECOND, 0)
        datetimeToAlarm.set(Calendar.MILLISECOND, 0)

        NotificationHelper.createNotificationChannel(
            requireContext(),
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
            false,
            weather,
            requireContext().getString(R.string.weather_notification_channel_desc, weather.cityName)
        )

        NotificationHelper.scheduleNotification(
            requireContext(),
            datetimeToAlarm,
            24 * 60* 60 * 1000, // 1 day
            weather
        )

        viewModel.onNotificationAction(weather, true)

        Toast.makeText(
            requireContext(),
            requireContext().getString(R.string.notification_scheduled),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun switchTo(id: Int) {
        if (switcher.currentView.id == id) return
        else switcher.showNext()
    }

    private fun constructGoogleMapsUri(lat: Double, lon: Double, zoom: Int): String = "${Variables.googleMapsUrl}&center=$lat,$lon&zoom=$zoom"
    private fun constructYandexMapsUri(lat: Double, lon: Double, zoom: Int): String = "${Variables.yandexMapsUrl}ll=$lat,$lon&z=$zoom&l=map"

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

    private fun clearEmptyMessage() {
        img_search.visibility = View.GONE
        loading.visibility = View.GONE
        img_no_connection.visibility = View.GONE
        img_sad.visibility = View.GONE
        img_no_gps.visibility = View.GONE
        btn_retry.visibility = View.GONE
    }

    private fun showNoInternetMessage() {
        clearEmptyMessage()
        img_no_connection.visibility = View.VISIBLE
        btn_retry.visibility = View.VISIBLE
        img_sad.visibility = View.VISIBLE
        text_empty.text = resources.getString(R.string.no_internet)
    }

    private fun showDeniedPermissionMessage() {
        clearEmptyMessage()
        img_no_gps.visibility = View.VISIBLE
        img_sad.visibility = View.VISIBLE
        text_empty.text = resources.getString(R.string.no_gps)
    }
}