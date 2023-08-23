package com.dicoding.tourismapp.maps

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dicoding.tourismapp.core.data.Resource
import com.dicoding.tourismapp.core.domain.model.Tourism
import com.dicoding.tourismapp.detail.DetailTourismActivity
import com.dicoding.tourismapp.maps.databinding.ActivityMapsBinding
import com.google.gson.Gson
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class MapsActivity : AppCompatActivity() {

    private val mapsViewModel: MapsViewModel by viewModel()
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapboxMap: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.maps_public_token)) // get instance of Mapbox object

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadKoinModules(mapsModule) // call `mapsModule` manually (to prevent inverted dependency injection caused by "dynamic feature")

        supportActionBar?.title = "Tourism Map"

        binding.mapView.onCreate(savedInstanceState) // initialize mapview on lifecycles-create with saved state
        binding.mapView.getMapAsync { mapboxMap -> // get mapboxMap object with async call
            this.mapboxMap = mapboxMap // assign synced mapboxMap to this class's mapboxMap
            getTourismData() // call custom function
        }
    }

    private fun getTourismData() {
        mapsViewModel.tourism.observe(this) { tourism ->
            if (tourism != null) {
                when (tourism) {
                    is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        showMarker(tourism.data) // call custom function by passing `tourism.data`
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = tourism.message
                    }
                }
            }
        }
    }

    private fun showMarker(dataTourism: List<Tourism>?) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style -> // set mapbox style to streets
            style.addImage(ICON_ID, BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default)) // add custom marker
            val latLngBoundsBuilder = LatLngBounds.Builder() // create object for bounding box respective to the markers

            val symbolManager = SymbolManager(binding.mapView, mapboxMap, style) // initialize symbolManager
            symbolManager.iconAllowOverlap = true // allow icon to overlap

            val options = ArrayList<SymbolOptions>() // store options of each symbols
            dataTourism?.forEach { data ->
                latLngBoundsBuilder.include(LatLng(data.latitude, data.longitude)) // includes lat-lng to bounding box
                options.add( // add SymbolsOptions to symbol ArrayList
                    SymbolOptions()
                        .withLatLng(LatLng(data.latitude, data.longitude)) // specify marker position
                        .withIconImage(ICON_ID) // set icon image
                        .withData(Gson().toJsonTree(data)) // set data for the symbol
                )
            }
            symbolManager.create(options) // adds all marker in `options` to the map

            val latLngBounds = latLngBoundsBuilder.build() // GPT says this is redundant
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000) // add padding 50px to bounds with 5s of animation

            symbolManager.addClickListener { symbol -> // set click actions for all markers
                val data = Gson().fromJson(symbol.data, Tourism::class.java)
                val intent = Intent(this, DetailTourismActivity::class.java)
                intent.putExtra(DetailTourismActivity.EXTRA_DATA, data)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    companion object {
        private const val ICON_ID = "ICON_ID"
    }
}