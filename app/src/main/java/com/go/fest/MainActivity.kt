package com.go.fest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var festivalList: ViewGroup
    private lateinit var scrollView: NestedScrollView
    private lateinit var fab: FloatingActionButton
    private lateinit var totalFestivalsTextView: TextView
    private lateinit var filterButton: Button
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
        filterButton = findViewById(R.id.btn_filter)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (!scrollView.canScrollVertically(1) && scrollY > oldScrollY && !isLoading) {
                loadMoreFestivals()
            }
        }

        fab.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }

        filterButton.setOnClickListener {
            showFilterMenu()
        }

        loadFestivalsAndFilters()
    }

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
            }
            true
        }
        popup.show()
    }

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
            when (filterType) {
                "departement" -> selectedDepartement = options[which]
                "ville" -> selectedCity = options[which]
                "discipline" -> selectedDiscipline = options[which]
            }
            Log.d("MainActivity", "Selected $filterType: ${options[which]}")
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

    @SuppressLint("SetTextI18n")
    private fun loadMoreFestivals() {
        isLoading = true
        GlobalScope.launch(Dispatchers.Main) {
            try {
                Log.d("MainActivity", "Loading festivals with filters - Region: $selectedRegion, Departement: $selectedDepartement, City: $selectedCity, Discipline: $selectedDiscipline")
                val response = ApiClient.festivalApiService.getFestivals(
                    limit,
                    offset,
                    selectedDepartement,
                    selectedCity,
                    selectedDiscipline
                )
                offset += limit
                totalFestivals += response.results.size
                updateTotalFestivalsTextView()

                response.results.forEach { festival ->
                    createFestivalCardView(festival)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading festivals: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalFestivalsTextView() {
        totalFestivalsTextView.text = "Total: $totalFestivals"
    }

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

        // Create Button
        val button = Button(this@MainActivity)
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.text = "Détails"
        button.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_style)
        button.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("FESTIVAL_ID", festival.identifiant)

            startActivity(intent)
        }

        rootLayout.addView(button)

        festivalList.addView(rootLayout)
    }

    private fun loadFestivalsAndFilters() {
        isLoading = true
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.festivalApiService.getFestivals(
                    limit,
                    offset,
                    selectedDepartement,
                    selectedCity,
                    selectedDiscipline
                )
                offset += limit
                totalFestivals += response.results.size
                updateTotalFestivalsTextView()

                // Extract unique departments, cities, and disciplines from fetched data
                departments = response.results.mapNotNull { it.departement_principal_de_deroulement }.distinct()
                cities = response.results.mapNotNull { it.commune_principale_de_deroulement }.distinct()
                disciplines = response.results.mapNotNull { it.discipline_dominante }.distinct()

                // Initially load festivals
                response.results.forEach { festival ->
                    createFestivalCardView(festival)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading festivals and filters: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}