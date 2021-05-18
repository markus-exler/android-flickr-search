package com.mex.flickrsearch.searchview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mex.flickrsearch.data.model.Picture
import com.mex.flickrsearch.databinding.GridViewItemBinding
import javax.inject.Inject

class PictureGridAdapter @Inject constructor() : PagingDataAdapter<Picture, PictureGridAdapter.PictureItemViewHolder>(DiffCallback) {

    /**
     * Create new view holder (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): PictureItemViewHolder {
        return PictureItemViewHolder(GridViewItemBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: PictureItemViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    /**
     * Represents list item and is used to access it and assign the binding to the [Picture] data
     */
    class PictureItemViewHolder(private var binding: GridViewItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pic: Picture) {
            binding.picture = pic
            binding.executePendingBindings()
        }
    }

    /**
     * Used be the recyclerView to determine which items have changed when the list of [Picture]
     * has been updated
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Picture>() {
        override fun areItemsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem.id == newItem.id
        }
    }
}