package com.calamity.weather.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
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
import com.calamity.weather.ui.MainActivity
import com.calamity.weather.ui.adapters.WeatherAdapter
import com.calamity.weather.utils.Variables
import com.calamity.weather.utils.onQueryTextChanged
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBinding(view)
        initListeners()

        requestGpsPermission()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            weatherViewModel.event.collect { event ->
                when (event) {
                    is WeatherViewModel.WeatherEvent.ShowUndoDeleteMessage -> {
                        Snackbar.make(requireView(), "Deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                weatherViewModel.onUndoDelete(event.weather)
                            }.show()
                    }
                }
            }
        }

        setHasOptionsMenu(true)

        viewLifecycleOwner.lifecycleScope.launch {
            weatherViewModel.update()
            getWeatherByLocation()
        }
    }

    private fun initBinding(view: View) {
        val binding = FragmentWeatherBinding.bind(view)

        val weatherAdapter = WeatherAdapter()

        binding.apply {
            currentWeatherRecycler.apply {
                adapter = weatherAdapter
                layoutManager = LinearLayoutManager(requireContext())
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
                    weatherViewModel.onEntrySwiped(weather)
                }

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    if (weatherAdapter.currentList[viewHolder.adapterPosition].isLocationEntry) {
                        return 0
                    }
                    return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
                }
            }).attachToRecyclerView(currentWeatherRecycler)
        }

        weatherViewModel.weather.observe(viewLifecycleOwner) {
            weatherAdapter.submitList(it)
            handleEmptyList(it)
        }
    }

    private fun initListeners() {
        recycler_swipe_layout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                weatherViewModel.update()
                getWeatherByLocation()
                recycler_swipe_layout.isRefreshing = false
            }
        }

        btn_retry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                weatherViewModel.update()
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
                    if (isInternetAvailable()) {
                        if (!gpsPermissionGranted()) {
                            empty_img_no_connection.visibility = View.GONE
                            empty_img_no_gps.visibility = View.VISIBLE
                            btn_retry.visibility = View.GONE
                            text_empty.text = resources.getString(R.string.no_gps)
                        } else {
                            getWeatherByLocation()
                        }
                    } else {
                        empty_img_no_connection.visibility = View.VISIBLE
                        empty_img_no_gps.visibility = View.GONE
                        btn_retry.visibility = View.VISIBLE
                        text_empty.text = resources.getString(R.string.no_internet)
                    }
                    switcher.showNext()
                }
                // Case empty message is visible: required to process Retry action
                else -> {
                    if (isInternetAvailable()) {
                        if (!gpsPermissionGranted()) {
                            empty_img_no_connection.visibility = View.GONE
                            empty_img_no_gps.visibility = View.VISIBLE
                            btn_retry.visibility = View.GONE
                            text_empty.text = resources.getString(R.string.no_gps)
                        } else {
                            getWeatherByLocation()
                            switcher.showNext()
                        }
                    }
                }
            }
        } else if (switcher.currentView.id == R.id.empty)
            switcher.showNext()
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

                    getWeatherByLocation()

                } else {
                    Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Error getting location. Add cities manually", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                weatherViewModel.addGpsWeather(it.latitude, it.longitude)
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }

    }

    private fun isInternetAvailable(): Boolean = Variables.isNetworkConnected

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
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            weatherViewModel.searchQuery.value = it
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
}