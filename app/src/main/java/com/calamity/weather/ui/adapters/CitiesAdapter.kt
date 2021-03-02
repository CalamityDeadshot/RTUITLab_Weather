package com.calamity.weather.ui.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.databinding.ItemCityToAddBinding

class CitiesAdapter : ListAdapter<PlacesPrediction, CitiesAdapter.CityViewHolder>(DiffCallback()) {

    inner class CityViewHolder(private val binding: ItemCityToAddBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(prediction : PlacesPrediction) {
            binding.apply {
                //val countryName = prediction.fullText.split(", ").last()
                val cityName = prediction.fullText.split(", ")[0]
                locationName.text = cityName
                fullName.text = prediction.fullText
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityToAddBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<PlacesPrediction>() {

        override fun areItemsTheSame(
            oldItem: PlacesPrediction,
            newItem: PlacesPrediction
        ): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(
            oldItem: PlacesPrediction,
            newItem: PlacesPrediction
        ): Boolean = oldItem == newItem
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