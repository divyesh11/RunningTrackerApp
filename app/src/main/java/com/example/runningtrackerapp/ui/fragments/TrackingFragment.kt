package com.example.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningtrackerapp.databinding.FragmentTrackingBinding
import com.example.runningtrackerapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtrackerapp.other.Constants.MAP_ZOOM
import com.example.runningtrackerapp.other.Constants.POLYLINE_COLOR
import com.example.runningtrackerapp.other.Constants.POLYLINE_WIDTH
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.services.Polyline
import com.example.runningtrackerapp.services.TrackingService
import com.example.runningtrackerapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var viewBinding: FragmentTrackingBinding

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var currentTimeInMillis = 0L

    private var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentTrackingBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.mapView.onCreate(savedInstanceState)

        viewBinding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        viewBinding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            viewBinding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            viewBinding.btnToggleRun.text = "Start"
            viewBinding.btnFinishRun.visibility = VISIBLE
        } else {
            viewBinding.btnToggleRun.text = "Stop"
            viewBinding.btnFinishRun.visibility = GONE
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

    private fun addAllPolyLines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }

    override fun onResume() {
        super.onResume()
        viewBinding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        viewBinding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewBinding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        viewBinding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBinding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding.mapView.onSaveInstanceState(outState)
    }
}