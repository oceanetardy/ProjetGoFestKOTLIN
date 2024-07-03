package com.go.fest

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DetailsActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var festivalName: TextView
    private lateinit var festivalDiscipline: TextView
    private lateinit var festivalAddress: TextView
    private lateinit var festivalCodeCpCommune: TextView
    private lateinit var festivalCommune: TextView
    private lateinit var festivalDepartment: TextView
    private lateinit var festivalRegion: TextView
    private lateinit var festivalSiteWeb: TextView
    private lateinit var festivalMail: TextView
    private lateinit var festivalPeriod: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_details)

        mapView = findViewById<MapView>(R.id.mapview)
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)

        festivalName = findViewById(R.id.festivalName)
        festivalDiscipline = findViewById(R.id.festivalDiscipline)
        festivalAddress = findViewById(R.id.festivalAddress)
        festivalCodeCpCommune = findViewById(R.id.festivalCodeCpCommune)
        festivalCommune = findViewById(R.id.festivalCommune)
        festivalDepartment = findViewById(R.id.festivalDepartment)
        festivalRegion = findViewById(R.id.festivalRegion)
        festivalSiteWeb = findViewById(R.id.festivalSiteWeb)
        festivalMail = findViewById(R.id.festivalMail)
        festivalPeriod = findViewById(R.id.festivalPeriod)

        val festivalId = intent.getStringExtra("FESTIVAL_ID")

        if (festivalId != null) {
            fetchFestivalDetails(festivalId)
        } else {
            Log.e("DetailsActivity", "Aucun identifiant de festival trouvé")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun fetchFestivalDetails(festivalId: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.festivalApiService.getAllFestivals()
                val festival = response.results.find { it.identifiant == festivalId }
                if (festival != null) {
                    createFestivalDetailsCardView(festival)
                    updateMapWithFestivalLocation(festival)
                } else {
                    Log.e("DetailsActivity", "Festival non trouvé pour l'ID: $festivalId")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createFestivalDetailsCardView(festival: Festival) {
        val rootLayout = LinearLayout(this@DetailsActivity)

        festivalName.text = festival.nom_du_festival
        festivalDiscipline.text = festival.discipline_dominante
        festivalAddress.text = festival.adresse_postale
        festivalCodeCpCommune.text = festival.code_postal_de_la_commune_principale_de_deroulement
        festivalCommune.text = festival.commune_principale_de_deroulement
        festivalDepartment.text = festival.departement_principal_de_deroulement
        festivalRegion.text = festival.region_principale_de_deroulement
        festivalSiteWeb.text = festival.site_internet_du_festival
        festivalMail.text = festival.adresse_e_mail
        festivalPeriod.text = festival.periode_principale_de_deroulement_du_festival


    }

    private fun updateMapWithFestivalLocation(festival: Festival) {
        val latitude = 45.4333 // TODO
        val longitude = 4.4 // TODO
        val zoomLevel = 15.0

        val mapController = mapView.controller
        mapController.setZoom(zoomLevel)
        mapController.setCenter(GeoPoint(latitude, longitude))

        // Ajouter un marqueur sur la carte
        val startMarker = Marker(mapView)
        startMarker.position = GeoPoint(latitude, longitude)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.title = festival.nom_du_festival
        mapView.overlays.add(startMarker)
    }
}
