package com.go.fest

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
    private lateinit var festivalIdTextView: TextView
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

        mapView = findViewById(R.id.mapview)
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)

        festivalIdTextView = findViewById(R.id.festivalId)
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
                val whereClause = "identifiant like \"%$festivalId%\""

                val response = ApiClient.festivalApiService.getOneFestival(whereClause)

                val festival = response.results.find { it.identifiant == festivalId }

                if (festival != null) {
                    Log.d("DetailsActivity", "Festival trouvé: $festival")

                    createFestivalDetailsCardView(festival)
                    updateMapWithFestivalLocation(festival)
                } else {
                    Log.e("DetailsActivity", "Aucun festival trouvé pour l'ID: $festivalId")
                }
            } catch (e: Exception) {
                Log.e("DetailsActivity", "Erreur lors de la récupération des détails du festival: ${e.message}", e)
            }
        }
    }

    private fun createFestivalDetailsCardView(festival: Festival) {
        festivalIdTextView.text = festival.identifiant ?: "Identifiant du festival non renseigné"
        festivalName.text = festival.nom_du_festival ?: "Nom du festival non renseigné"
        festivalDiscipline.text = festival.discipline_dominante ?: "Discipline du festival non renseignée"
        festivalAddress.text = festival.adresse_postale ?: "Adresse du festival non renseignée"
        festivalCodeCpCommune.text = festival.code_postal_de_la_commune_principale_de_deroulement ?: "Code postal du festival non renseigné"
        festivalCommune.text = festival.commune_principale_de_deroulement ?: "Commune du festival non renseignée"
        festivalDepartment.text = festival.departement_principal_de_deroulement ?: "Département du festival non renseigné"
        festivalRegion.text = festival.region_principale_de_deroulement ?: "Région du festival non renseignée"
        festivalSiteWeb.text = festival.site_internet_du_festival ?: "Site web du festival non renseigné"
        festivalMail.text = festival.adresse_e_mail ?: "Contact du festival non renseigné"
        festivalPeriod.text = festival.periode_principale_de_deroulement_du_festival ?: "Période du festival non renseignée"
    }

    private fun updateMapWithFestivalLocation(festival: Festival) {
        val latitude = festival.geocodage_xy?.lat ?: 0.0
        val longitude = festival.geocodage_xy?.lon ?: 0.0
        val zoomLevel = 15.0

        val mapController = mapView.controller
        mapController.setZoom(zoomLevel)
        mapController.setCenter(GeoPoint(latitude, longitude))

        val startMarker = Marker(mapView)
        startMarker.position = GeoPoint(latitude, longitude)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.title = festival.nom_du_festival ?: "Nom du festival non renseigné"
        mapView.overlays.add(startMarker)
    }
}
