package com.example.biketrackingapp.other

import android.content.Context
import com.example.biketrackingapp.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val Runs: List<Run>,
    c: Context,
    layOutId: Int

) : MarkerView(c, layOutId) {
    override fun getOffset(): MPPointF {
        return MPPointF(-width /1.4f, -height.toFloat())

    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val runId = e.x.toInt()
        val run = Runs[runId]

        val calender = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = "${dateFormat.format(calender.time)}"
        tvAvgSpeed.text = "${run.avgSpeedInKMH} Km/h"
        tvDistance.text = "${run.distanceInMeters / 1000f} Km"
        tvDuration.text = TrakingUtility.getFormattedStopWatchTime(run.timeInMillis)
        tvCaloriesBurned.text = "${run.caloriesBurned} Calories"

    }

}