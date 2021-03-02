package com.calamity.weather.ui.locationsearch

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.calamity.weather.R
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.databinding.FragmentSearchBinding
import com.calamity.weather.ui.adapters.CitiesAdapter
import com.calamity.weather.ui.weather.CurrentWeatherViewModel
import com.calamity.weather.utils.PlacesApi
import com.calamity.weather.utils.onQueryTextChanged
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), CitiesAdapter.OnItemClickListener {

    private val viewModel: SearchViewModel by viewModels()

    lateinit var placesClient: PlacesClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        Places.initialize(requireContext(), PlacesApi.API_KEY)
        placesClient = Places.createClient(requireContext())
        viewModel.provideClient(placesClient)

        initBinding(view)
    }

    override fun onAddClick(place: PlacesPrediction, isAdded: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.onAddPlace(place, isAdded)
        }
        //viewModel.searchQuery.value = viewModel.searchQuery.value
        println("Parameter passed to onClick: $isAdded, placeId = ${place.placeId}")
        Toast.makeText(requireContext(), place.placeId, Toast.LENGTH_LONG).show()
    }

    private fun initBinding(view: View) {
        val binding = FragmentSearchBinding.bind(view)

        val citiesAdapter = CitiesAdapter(this)

        binding.apply {
            citiesRecycler.apply {
                adapter = citiesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(
                    CitiesAdapter.MarginItemDecoration(
                        resources.getDimensionPixelSize(
                            R.dimen.recyclerview_margin
                        )
                    )
                )
            }
        }

        viewModel.predictions.observe(viewLifecycleOwner) {
            citiesAdapter.submitList(it)
            handleEmptyList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.event.collect { event ->
                when (event) {
                    is SearchViewModel.UpdateEvent.UpdateList -> {
                        citiesAdapter.submitList(event.list)
                    }
                }
            }
        }
    }

    private fun handleEmptyList(list: List<PlacesPrediction>) {
        if (list.isEmpty()) {
            if (switcher.currentView.id == R.id.cities_recycler)
                switcher.showNext()

        } else if (switcher.currentView.id == R.id.empty)
            switcher.showNext()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_weather, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(false)

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onStop() {
        viewModel.onDestroy()
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }

}