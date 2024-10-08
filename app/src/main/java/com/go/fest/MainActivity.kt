package com.go.fest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var festivalList: ViewGroup
    private lateinit var scrollView: NestedScrollView
    private lateinit var fab: FloatingActionButton
    private lateinit var totalFestivalsTextView: TextView
    private lateinit var filterButton: Button
    private lateinit var btnMapMode: Button
    private lateinit var progressBar: ProgressBar

    private var offset = 0
    private val limit = 20
    private var isLoading = false
    private var totalFestivals = 0
    private var selectedRegion: String? = null
    private var selectedDepartement: String? = null
    private var selectedCity: String? = null
    private var selectedDiscipline: String? = null

    private var departments: List<String> = emptyList()
    private var cities: List<String> = emptyList()
    private var disciplines: List<String> = emptyList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        festivalList = findViewById(R.id.festival_list)
        scrollView = findViewById(R.id.scrollView)
        fab = findViewById(R.id.fab)
        totalFestivalsTextView = findViewById(R.id.total_festivals)
        filterButton = findViewById(R.id.btn_filtre)
        btnMapMode = findViewById(R.id.btn_map_mode)
        progressBar = findViewById(R.id.progress_bar)

        // Load more festivals on scroll
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (!scrollView.canScrollVertically(1) && scrollY > oldScrollY && !isLoading) {
                loadMoreFestivals()
            }
        }

        // Scroll to top
        fab.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }

        // Show filter menu
        filterButton.setOnClickListener {
            showFilterMenu()
        }

        // Switch to map mode
        btnMapMode.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        loadFestivalsAndFilters()
    }

    /**
     * Shows a popup menu with filter options (department, city, discipline)
     */
    private fun showFilterMenu() {
        val popup = PopupMenu(this, filterButton)
        popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.filter_departement -> {
                    showFilterDialog("departement")
                }
                R.id.filter_ville -> {
                    showFilterDialog("ville")
                }
                R.id.filter_discipline -> {
                    showFilterDialog("discipline")
                }
                R.id.reset_filters -> {
                    selectedDepartement = null
                    selectedCity = null
                    selectedDiscipline = null

                    resetAndLoadFestivals()
                }
            }
            true
        }
        popup.show()
    }

    /**
     * Shows a dialog for selecting a filter option (department, city, or discipline).
     * Resets other filters when a new one is selected.
     *
     * @param filterType Type of filter (department, city, or discipline).
     */
    private fun showFilterDialog(filterType: String) {
        val options = when (filterType) {
            "departement" -> getDepartments()
            "ville" -> getCities()
            "discipline" -> getDisciplines()
            else -> emptyList()
        }

        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Select $filterType")
        builder.setItems(options.toTypedArray()) { _, which ->
            // Reset all filters when a new one is selected
            selectedDepartement = null
            selectedCity = null
            selectedDiscipline = null

            when (filterType) {
                "departement" -> selectedDepartement = options[which]
                "ville" -> selectedCity = options[which]
                "discipline" -> selectedDiscipline = options[which]
            }

            resetAndLoadFestivals()
        }

        builder.create().show()
    }

    private fun getDepartments(): List<String> {
        return departments
    }

    private fun getCities(): List<String> {
        return cities
    }

    private fun getDisciplines(): List<String> {
        return disciplines
    }

    @SuppressLint("SetTextI18n")
    private fun resetAndLoadFestivals() {
        offset = 0
        totalFestivals = 0
        festivalList.removeAllViews()
        loadMoreFestivals()
    }

    /**
     * Loads more festivals based on the current offset and applies any selected filters.
     */
    @SuppressLint("SetTextI18n")
    private fun loadMoreFestivals() {
        isLoading = true
        showLoading()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val refineParameters = mutableListOf<String>()

                if (!selectedDepartement.isNullOrEmpty()) {
                    refineParameters.add("departement_principal_de_deroulement:\"$selectedDepartement\"")
                }
                if (!selectedCity.isNullOrEmpty()) {
                    refineParameters.add("commune_principale_de_deroulement:\"$selectedCity\"")
                }
                if (!selectedDiscipline.isNullOrEmpty()) {
                    refineParameters.add("discipline_dominante:\"$selectedDiscipline\"")
                }

                val refineQuery = if (refineParameters.isNotEmpty()) {
                    refineParameters.joinToString(separator = " AND ")
                } else {
                    null
                }

                val response = ApiClient.festivalApiService.getFestivals(
                    limit = limit,
                    offset = offset,
                    refine = refineQuery
                )

                Log.d("FILTERED RESPONSE", response.toString())

                offset += limit
                totalFestivals += response.results.size
                updateTotalFestivalsTextView()

                response.results.forEach { festival ->
                    createFestivalCardView(festival)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors du chargement des festivals: ${e.message}", e)
                showErrorDialog("Erreur lors du chargement des festivals. Vérifiez votre connexion internet et réessayez.")
            } finally {
                isLoading = false
                hideLoading()
            }
        }
    }

    /**
     * Updates the TextView that displays the total number of loaded festivals.
     */
    @SuppressLint("SetTextI18n")
    private fun updateTotalFestivalsTextView() {
        totalFestivalsTextView.text = "Total: $totalFestivals"
    }

    /**
     * Creates a card view for each festival and add it to festival list.
     *
     * @param festival Festival object with data to display.
     */
    @SuppressLint("SetTextI18n")
    private fun createFestivalCardView(festival: Festival) {
        val rootLayout = LinearLayout(this@MainActivity)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        rootLayout.setPadding(20, 20, 20, 20)
        rootLayout.layoutParams = layoutParams
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER
        rootLayout.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.item_festival)

        val titleTextView = TextView(this@MainActivity)
        titleTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        titleTextView.text = festival.nom_du_festival
        titleTextView.gravity = Gravity.CENTER
        titleTextView.textSize = 24f
        titleTextView.setTextColor(Color.BLACK)
        titleTextView.setTypeface(null, Typeface.BOLD)

        rootLayout.addView(titleTextView)

        val typeTextView = TextView(this@MainActivity)
        typeTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        typeTextView.text = festival.discipline_dominante
        typeTextView.gravity = Gravity.CENTER
        typeTextView.textSize = 18f
        typeTextView.setTextColor(Color.BLACK)
        typeTextView.setTypeface(null, Typeface.BOLD_ITALIC)

        rootLayout.addView(typeTextView)

        val dateTextView = TextView(this@MainActivity)
        dateTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dateTextView.text = festival.periode_principale_de_deroulement_du_festival
        dateTextView.gravity = Gravity.CENTER
        dateTextView.textSize = 18f
        dateTextView.setTextColor(Color.BLACK)

        rootLayout.addView(dateTextView)

        val cityTextView = TextView(this@MainActivity)
        cityTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cityTextView.text = festival.commune_principale_de_deroulement
        cityTextView.gravity = Gravity.CENTER
        cityTextView.textSize = 18f
        cityTextView.setTextColor(Color.BLACK)

        rootLayout.addView(cityTextView)

        val departmentTextView = TextView(this@MainActivity)
        departmentTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        departmentTextView.text = festival.departement_principal_de_deroulement
        departmentTextView.gravity = Gravity.CENTER
        departmentTextView.textSize = 18f
        departmentTextView.setTextColor(Color.BLACK)

        rootLayout.addView(departmentTextView)

        val button = Button(this@MainActivity)
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.text = "Détails"
        button.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_style)
        button.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.buttonTextColor))

        button.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("FESTIVAL_ID", festival.identifiant)

            startActivity(intent)
        }

        rootLayout.addView(button)

        festivalList.addView(rootLayout)
    }

    /**
    * Loads the initial list of festivals and filter options.
    */
    private fun loadFestivalsAndFilters() {
        isLoading = true
        showLoading()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val refineParameters = mutableListOf<String>()

                if (!selectedDepartement.isNullOrEmpty()) {
                    refineParameters.add("departement_principal_de_deroulement:\"$selectedDepartement\"")
                }
                if (!selectedCity.isNullOrEmpty()) {
                    refineParameters.add("commune_principale_de_deroulement:\"$selectedCity\"")
                }
                if (!selectedDiscipline.isNullOrEmpty()) {
                    refineParameters.add("discipline_dominante:\"$selectedDiscipline\"")
                }

                val refineQuery = if (refineParameters.isNotEmpty()) {
                    refineParameters.joinToString(separator = " AND ")
                } else {
                    null
                }

                val response = ApiClient.festivalApiService.getFestivals(
                    limit = limit,
                    offset = offset,
                    refine = refineQuery
                )

                offset += limit
                totalFestivals += response.results.size
                updateTotalFestivalsTextView()

                departments = response.results.mapNotNull { it.departement_principal_de_deroulement }.distinct()
                cities = response.results.mapNotNull { it.commune_principale_de_deroulement }.distinct()
                disciplines = response.results.mapNotNull { it.discipline_dominante }.distinct()

                response.results.forEach { festival ->
                    createFestivalCardView(festival)
                }
            } catch (e: Exception) {
                showErrorDialog("Erreur lors du chargement des festivals. Vérifiez votre connexion internet et réessayez.")
            } finally {
                isLoading = false
                hideLoading()
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    /**
     * Shows a dialog with an error message.
     *
     * @param message Error message to display.
     */
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Erreur")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
