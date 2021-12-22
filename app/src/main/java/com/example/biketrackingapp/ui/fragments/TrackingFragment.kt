package com.example.biketrackingapp.ui.fragments

import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.biketrackingapp.R
import com.example.biketrackingapp.databinding.FragmentTrackingBinding
import com.example.biketrackingapp.db.Run
import com.example.biketrackingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.biketrackingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.biketrackingapp.other.Constants.ACTION_STOP_SERVICE
import com.example.biketrackingapp.other.Constants.MAP_ZOOM
import com.example.biketrackingapp.other.Constants.POLY_LINE_COLOR
import com.example.biketrackingapp.other.Constants.POLY_LINE_WIDTH
import com.example.biketrackingapp.other.TrakingUtility
import com.example.biketrackingapp.services.TrackingService
import com.example.biketrackingapp.services.polyLine

import com.example.biketrackingapp.ui.CancelAlertDialog
import com.example.biketrackingapp.ui.viewModels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TAG="CANCEL_TAG"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()
   private var addMarker: Marker?=null
    private var map: GoogleMap? = null
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var isTracking: Boolean = false
    private var pathPoints = mutableListOf<polyLine>()
    private var markerOptions: MarkerOptions? = null
    private var currentTimeInMillis= 0L
    private var menu:Menu?=null

    @set:Inject
     var weight:Float = 0.0f
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.mapView.onCreate(savedInstanceState)

        Log.i("xxx",currentTimeInMillis.toString())

        binding.btnToggleRun.setOnClickListener {
            if (isLocationEnabled())
            toggleRun()
            else{
                Toast.makeText(requireContext(), "Please Turn On The Location", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        if (savedInstanceState!=null){
            val cancelAlertDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_TAG) as CancelAlertDialog
            cancelAlertDialog.setYesListener { stopRun() }
        }
        setHasOptionsMenu(true)
        subscribeToObservers()
        binding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDB()
        }

        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tracking_fragment_menu,menu)
        this.menu=menu
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis >0L){
            menu.getItem(0)?.isVisible=true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.CancelBtn->
                showCancelAlertDialog()
        }
        return super.onOptionsItemSelected(item)

    }
    private fun showCancelAlertDialog(){
        CancelAlertDialog().apply {
            setYesListener {
                stopRun()
            }

        }.show(parentFragmentManager,CANCEL_TAG)
    }
    private fun stopRun(){
        sendActionToService(ACTION_STOP_SERVICE)
        binding.tvTimer.text="00:00:00:00"
        currentTimeInMillis=0L
        TrackingService.timeRunInMillis.postValue(0L)
        findNavController().navigateUp()
    }
    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLineToMap()
            moveCameraToUser()
            AddIcon()

        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis=it
            Log.i("xxx",currentTimeInMillis.toString())
            val formattedTime=TrakingUtility.getFormattedStopWatchTime(it,true)
            binding.tvTimer.text=formattedTime

        })
    }

    private fun AddIcon() {

        if (markerOptions == null && pathPoints.last().isNotEmpty()) {
            markerOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_iconmonstr_bicycle_1_1))
                .position(pathPoints.last().last())
            addMarker = map?.addMarker(markerOptions)

        } else if (pathPoints.last().isNotEmpty()) {
            addMarker?.position = pathPoints.last().last()
        }

    }



private fun toggleRun() {
    if (isTracking) {
       menu?.getItem(0)?.isVisible=true
        sendActionToService(ACTION_PAUSE_SERVICE)
    } else
        sendActionToService(ACTION_START_OR_RESUME_SERVICE)


}

private fun updateTracking(isTracking: Boolean) {
    this.isTracking = isTracking
    if (!isTracking&&currentTimeInMillis>0L) {
        binding.btnToggleRun.text = "Start"
        binding.btnFinishRun.visibility = View.VISIBLE
    } else if (isTracking){
        binding.btnToggleRun.text = "Stop"
       menu?.getItem(0)?.isVisible=true
        binding.btnFinishRun.visibility = View.GONE
    }
}

private fun moveCameraToUser() {
    if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(),
                MAP_ZOOM
            )
        )
    }
}
private fun zoomToSeeWholeTrack(){
    val bounds=LatLngBounds.Builder()
    for (polyLine in pathPoints){
        for (pos in polyLine){
           bounds.include(pos)
        }
    }
    if (pathPoints.isNotEmpty()){
        Log.i("xxx","sss")
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

}

    private fun endRunAndSaveToDB(){
        map?.snapshot { bmp->
            var distanceInMeters=0
            for (polyLine in pathPoints){
                distanceInMeters+=TrakingUtility.calculatePolylineLength(polyLine).toInt()
            }
            //convert distance to km and divided it on hours
            val avgSpeed= round ((distanceInMeters/1000f) /(currentTimeInMillis /1000f /60/60) *10f)/10f
            val dateTimeStamp=Calendar.getInstance().timeInMillis
            val caloriesBurned=((distanceInMeters /1000f) * weight ).toInt()
            val run=Run(bmp,dateTimeStamp, avgSpeed,distanceInMeters,currentTimeInMillis,caloriesBurned)

            viewModel.insert(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "You Track Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }
private fun addAllPolyLines() {
    for (polyLine in pathPoints) {
        val polyLineOption = PolylineOptions()
            .color(POLY_LINE_COLOR)
            .width(POLY_LINE_WIDTH)
            .addAll(polyLine)
        map?.addPolyline(polyLineOption)
    }
}

private fun addLatestPolyLineToMap() {
    if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
        val polyLineOption = PolylineOptions()
            .color(POLY_LINE_COLOR)
            .width(POLY_LINE_WIDTH)
            .add(pathPoints.last()[pathPoints.last().size - 2])
            .add(pathPoints.last().last())
        map?.addPolyline(polyLineOption)


    }
}

private fun sendActionToService(action: String) {
    Intent(requireContext(), TrackingService::class.java).also {
        it.action = action
        requireContext().startService(it)
    }
}

override fun onResume() {
    super.onResume()
    binding.mapView.onResume()
}

override fun onStop() {
    super.onStop()
    binding.mapView.onStop()
}

override fun onLowMemory() {
    super.onLowMemory()
    binding.mapView.onLowMemory()
}

override fun onStart() {
    super.onStart()
    binding.mapView.onStart()
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}

override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    binding.mapView.onSaveInstanceState(outState)
}
}