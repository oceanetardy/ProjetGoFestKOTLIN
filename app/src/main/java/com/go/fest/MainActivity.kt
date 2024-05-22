package com.go.fest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var festivalList: ViewGroup
    private val idMap = mutableMapOf<String, Int>()

    @OptIn(DelicateCoroutinesApi::class)
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

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.festivalApiService.getAllFestivals()

                Log.i("Response", response.toString())

                response.results.forEach { festival ->
                    createFestivalCardView(festival)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createFestivalCardView(festival: Festival) {
        val rootLayout = LinearLayout(this@MainActivity)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        layoutParams.setMargins(10, 10, 10, 10)
        rootLayout.setPadding(20, 20, 20, 20)
        rootLayout.layoutParams = layoutParams
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER
        rootLayout.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.item_festival)

        val titleTextView = TextView(this@MainActivity)
        titleTextView.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        titleTextView.text = festival.nom_du_festival
        titleTextView.setGravity(Gravity.CENTER)
        titleTextView.textSize = 24f
        titleTextView.setTextColor(Color.BLACK)

        rootLayout.addView(titleTextView)

        val typeTextView = TextView(this@MainActivity)
        typeTextView.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        typeTextView.text = festival.discipline_dominante
        typeTextView.setGravity(Gravity.CENTER)
        typeTextView.textSize = 18f
        typeTextView.setTextColor(Color.BLACK)

        rootLayout.addView(typeTextView)

        val dateTextView = TextView(this@MainActivity)
        dateTextView.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        dateTextView.text = festival.periode_principale_de_deroulement_du_festival
        dateTextView.setGravity(Gravity.CENTER)
        dateTextView.textSize = 18f
        dateTextView.setTextColor(Color.BLACK)

        rootLayout.addView(dateTextView)

        // Create Button
        val button = Button(this@MainActivity)
        button.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        button.text = "Détails"
        button.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_style)
        button.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            startActivity(intent)
        }

        rootLayout.addView(button)

        festivalList.addView(rootLayout)
    }
}