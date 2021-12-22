package com.example.biketrackingapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.biketrackingapp.R
import com.example.biketrackingapp.databinding.FragmentStatisticsBinding
import com.example.biketrackingapp.other.CustomMarkerView
import com.example.biketrackingapp.other.TrakingUtility
import com.example.biketrackingapp.ui.viewModels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()

    private var _binding: FragmentStatisticsBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val view = binding.root
        setObservers()
setUpBarChart()
        return view
    }

    private fun setUpBarChart() {
        binding.BarChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.BarChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.BarChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.BarChart.apply {
            description.text = "AVG Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun setObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tvTotalTime.text = TrakingUtility.getFormattedStopWatchTime(it)
            }
        })
        viewModel.totalAvGSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tvAverageSpeed.text = "${round(it * 10f) / 10f} Km/h"
            }
        })
        viewModel.totalDistanceInMeters.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tvTotalDistance.text = "${round(it / 1000f * 10f) / 10f} Km"
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tvTotalCalories.text = "$it Kcal"
            }
        })
        viewModel.getAllRunsSortedByDate.observe(viewLifecycleOwner, Observer {
            it.let {
                val allAVGSpeed = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val barDataSet = BarDataSet(allAVGSpeed, "AVG Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                binding.BarChart.data = BarData(barDataSet)
                binding.BarChart.marker=CustomMarkerView(it,requireContext(),R.layout.marker_view)
                binding.BarChart.animateY(2000)
                binding.BarChart.invalidate()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}