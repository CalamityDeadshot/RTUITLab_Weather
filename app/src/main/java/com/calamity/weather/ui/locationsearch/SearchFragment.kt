package com.calamity.weather.ui.locationsearch

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.calamity.weather.R
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.databinding.FragmentSearchBinding
import com.calamity.weather.ui.adapters.CitiesAdapter
import com.calamity.weather.utils.Variables
import com.calamity.weather.utils.onQueryTextChanged
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weather.switcher
import kotlinx.android.synthetic.main.layout_list_empty.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), CitiesAdapter.OnItemClickListener {

    private val viewModel: SearchViewModel by viewModels()

    lateinit var placesClient: PlacesClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        Places.initialize(requireContext(), Variables.googleApiKey)
        placesClient = Places.createClient(requireContext())
        viewModel.provideClient(placesClient)

        initBinding(view)
    }

    override fun onAddClick(place: PlacesPrediction, isAdded: Boolean) =
        viewModel.onAddPlace(place, isAdded)


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

        viewModel.predictions.observe(viewLifecycleOwner, Observer {
            citiesAdapter.submitList(it)
            handleEmptyList(it)
        })

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.event.collect { event ->
                when (event) {
                    is SearchViewModel.UpdateEvent.UpdateList -> {
                        citiesAdapter.submitList(event.list)
                        handleEmptyList(event.list)
                    }
                }
            }
        }

        if (!Variables.isNetworkConnected) showNoInternetMessage()
        Variables.isNetworkConnectedLive.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (viewModel.hasQuery.value!!) {
                    switchTo(R.id.cities_recycler)
                    viewModel.searchQuery.value = viewModel.searchQuery.value
                }
                else
                    showStartEmptyMessage()
            }
            else {
                switchTo(R.id.empty)
                showNoInternetMessage()
            }
        })

        viewModel.busy.observe(viewLifecycleOwner, Observer {
            if (!Variables.isNetworkConnected) return@Observer
            if (it) {
                if (viewModel.hasQuery.value!!) showLoadingEmptyMessage()
            } else {
                if (!viewModel.hasQuery.value!!) {
                    showStartEmptyMessage()
                } else {
                    showNoResultsEmptyMessage()
                }
            }
        })

        img_no_gps.visibility = View.GONE

    }

    private fun handleEmptyList(list: List<PlacesPrediction>) {
        if (list.isEmpty()) {
            if (switcher.currentView.id == R.id.cities_recycler) {
                switcher.showNext()
            }

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

    private fun switchTo(id: Int) {
        if (switcher.currentView.id == id) return
        else switcher.showNext()
    }

    private fun clearEmptyMessage() {
        img_search.visibility = View.GONE
        loading.visibility = View.GONE
        img_no_connection.visibility = View.GONE
        img_sad.visibility = View.GONE
        btn_retry.visibility = View.GONE
    }

    private fun showNoInternetMessage() {
        clearEmptyMessage()
        img_no_connection.visibility = View.VISIBLE
        btn_retry.visibility = View.VISIBLE
        img_sad.visibility = View.VISIBLE
        text_empty.text = resources.getString(R.string.no_internet)
    }

    private fun showLoadingEmptyMessage() {
        clearEmptyMessage()
        text_empty.text = getString(R.string.loading)
        loading.visibility = View.VISIBLE
    }

    private fun showNoResultsEmptyMessage() {
        clearEmptyMessage()
        img_sad.visibility = View.VISIBLE
        img_search.visibility = View.VISIBLE
        text_empty.text = getString(R.string.search_nothing_found)
    }

    private fun showStartEmptyMessage() {
        clearEmptyMessage()
        img_search.visibility = View.VISIBLE
        text_empty.text = getString(R.string.search_empty_text)
    }

}