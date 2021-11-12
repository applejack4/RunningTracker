package com.example.runningapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp.databinding.ItemRunBinding
import com.example.runningapp.db.Run
import com.example.runningapp.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(view : ItemRunBinding) : RecyclerView.ViewHolder(view.root){
        val tvDate = view.tvDate
        val tvAverageSpeed = view.tvAvgSpeed
        val tvDistance = view.tvDistance
        val tvTime = view.tvTime
        val tvCalories = view.tvCalories
        val image = view.ivRunImage
    }

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding : ItemRunBinding = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.image).into(holder.image)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            holder.tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            holder.tvAverageSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            holder.tvDistance.text = distanceInKm

            holder.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timestamp)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            holder.tvCalories.text = caloriesBurned
        }
    }
}


