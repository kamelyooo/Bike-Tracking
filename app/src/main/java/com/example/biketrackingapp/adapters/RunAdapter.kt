package com.example.biketrackingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.biketrackingapp.R
import com.example.biketrackingapp.db.Run
import com.example.biketrackingapp.other.TrakingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter :RecyclerView.Adapter<RunAdapter.ViewHolder>() {
    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val imageView:ImageView=itemView.findViewById(R.id.ivRunImage)
        val tvDate:TextView=itemView.findViewById(R.id.tvDate)
        val tvTime:TextView=itemView.findViewById(R.id.tvTime)
        val tvDistance:TextView=itemView.findViewById(R.id.tvDistance)
        val tvAvgSpeed:TextView=itemView.findViewById(R.id.tvAvgSpeed)
        val tvCalories:TextView=itemView.findViewById(R.id.tvCalories)

    }

    val diffCallback= object :DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }
    }
    val differ=AsyncListDiffer(this,diffCallback)

    fun submitList(list:List<Run>)=differ.submitList(list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_run,parent,false))
    }
    override fun getItemCount(): Int {
      return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.imageView)
            val calender=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }
            val dateFormat=SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            holder.tvDate.text ="${dateFormat.format(calender.time)}"
            holder.tvAvgSpeed.text= "${run.avgSpeedInKMH} Km/h"
            holder.tvDistance.text="${run.distanceInMeters /1000f} Km"
            holder.tvTime.text=TrakingUtility.getFormattedStopWatchTime(run.timeInMillis)
            holder.tvCalories.text="${run.caloriesBurned} Calories"

        }
    }
}