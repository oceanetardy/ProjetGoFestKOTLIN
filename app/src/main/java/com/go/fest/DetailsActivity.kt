package com.go.fest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
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
    private lateinit var progressBar: ProgressBar

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
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        val festivalId = intent.getStringExtra("FESTIVAL_ID")

        if (festivalId != null) {
            fetchFestivalDetails(festivalId)
        } else {
            Log.e("DetailsActivity", "Aucun identifiant de festival trouvé")
            progressBar.visibility = View.GONE
        }
    }

    fun onBackButtonClicked(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    /**
     * Fetches details of festival based ID
     *
     * @param festivalId Identifier of the festival
     */
    private fun fetchFestivalDetails(festivalId: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val whereClause = "identifiant like \"%$festivalId%\""

                val response = ApiClient.festivalApiService.getOneFestival(whereClause)

                val festival = response.results.find { it.identifiant == festivalId }

                if (festival != null) {
                    createFestivalDetailsCardView(festival)
                    updateMapWithFestivalLocation(festival)
                } else {
                    Log.e("DetailsActivity", "Aucun festival trouvé pour l'ID: $festivalId")
                }
            } catch (e: Exception) {
                Log.e("DetailsActivity", "Erreur lors de la récupération des détails du festival: ${e.message}", e)
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Populates the UI components with the festival's details
     *
     * @param festival Object containing the festival's details
     */
    private fun createFestivalDetailsCardView(festival: Festival) {
        festivalIdTextView.text = festival.identifiant ?: "Identifiant du festival non renseigné"
        festivalName.text = festival.nom_du_festival ?: "Nom du festival non renseigné"
        festivalDiscipline.text = festival.discipline_dominante ?: "Discipline du festival non renseignée"
        festivalAddress.text = festival.adresse_postale ?: "Adresse du festival non renseignée"
        festivalCodeCpCommune.text = festival.code_postal_de_la_commune_principale_de_deroulement ?: "Code postal du festival non renseigné"
        festivalCommune.text = festival.commune_principale_de_deroulement ?: "Commune du festival non renseignée"
        festivalDepartment.text = festival.departement_principal_de_deroulement ?: "Département du festival non renseigné"
        festivalRegion.text = festival.region_principale_de_deroulement ?: "Région du festival non renseignée"

        if (festival.site_internet_du_festival.isNullOrBlank()) {
            festivalSiteWeb.text = "Site web du festival non renseigné"
        } else {
            val linkText = "<a href=\"${festival.site_internet_du_festival}\">${festival.site_internet_du_festival}</a>"
            festivalSiteWeb.text = Html.fromHtml(linkText, Html.FROM_HTML_MODE_LEGACY)
            festivalSiteWeb.movementMethod = LinkMovementMethod.getInstance()
        }

        festivalMail.text = festival.adresse_e_mail ?: "Contact du festival non renseigné"
        festivalPeriod.text = festival.periode_principale_de_deroulement_du_festival ?: "Période du festival non renseignée"
    }

    /**
     * Updates the map view with festival's location and add a marker
     *
     * @param festival Object containing festival's location details
     */
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

        val vectorDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.marker_fest, null)?.mutate()
        vectorDrawable?.let {
            val width = resources.getDimensionPixelSize(R.dimen.marker_width)
            val height = resources.getDimensionPixelSize(R.dimen.marker_height)
            it.setBounds(0, 0, width, height)
        }

        startMarker.icon = vectorDrawable

        mapView.overlays.add(startMarker)
        mapView.invalidate()
    }

    /**
     * Handle click event to open festival's location in Google Maps
     *
     * @param view The view that was clicked
     */
    fun openInMaps(view: View) {
        val festivalId = intent.getStringExtra("FESTIVAL_ID")

        if (festivalId != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val whereClause = "identifiant like \"%$festivalId%\""

                    val response = ApiClient.festivalApiService.getOneFestival(whereClause)

                    val festival = response.results.find { it.identifiant == festivalId }

                    if (festival != null) {
                        openInMaps(festival)
                    } else {
                        Log.e("DetailsActivity", "Aucun festival trouvé pour l'ID: $festivalId")
                    }
                } catch (e: Exception) {
                    Log.e("DetailsActivity", "Erreur lors de la récupération des détails du festival: ${e.message}", e)
                }
            }
        } else {
            Log.e("DetailsActivity", "Aucun identifiant de festival trouvé")
        }
    }

    /**
     * Opens the festival's location in Google Maps or a web browser if Google Maps is unavailable
     *
     * @param festival Object containing festival's location details
     */
    private fun openInMaps(festival: Festival) {
        val latitude = festival.geocodage_xy?.lat ?: 0.0
        val longitude = festival.geocodage_xy?.lon ?: 0.0

        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps") // Utilisation explicite de Google Maps

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Si Google Maps n'est pas trouvé, essayez d'ouvrir avec un navigateur web
            openInBrowser(uri.toString())
        }
    }

    /**
     * Opens a URL in a web browser
     *
     * @param url The URL to be opened
     */
    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Navigateur web non trouvé.", Toast.LENGTH_SHORT).show()
        }
    }
}
