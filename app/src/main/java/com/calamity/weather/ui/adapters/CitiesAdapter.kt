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

class CitiesAdapter(private val listener: OnItemClickListener) : ListAdapter<PlacesPrediction, CitiesAdapter.CityViewHolder>(DiffCallback()) {

    inner class CityViewHolder(private val binding: ItemCityToAddBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                btnAdd.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val place = getItem(position)
                        btnAdd.animate().rotation(if (place.isAdded) 45f else 0f).start()
                        listener.onAddClick(place, !place.isAdded)
                    }
                }
            }
        }

        fun bind(prediction : PlacesPrediction) {
            binding.apply {
                val cityName = prediction.fullText.split(", ")[0]
                locationName.text = cityName
                fullName.text = prediction.fullText
                btnAdd.animate().rotation(if (prediction.isAdded) 45f else 0f).start()
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

    interface OnItemClickListener {
        fun onAddClick(place: PlacesPrediction, isAdded: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<PlacesPrediction>() {

        override fun areItemsTheSame(
            oldItem: PlacesPrediction,
            newItem: PlacesPrediction
        ): Boolean = oldItem.placeId == newItem.placeId//oldItem.id == newItem.id
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