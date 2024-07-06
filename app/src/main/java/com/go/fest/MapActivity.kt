package com.go.fest

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        supportActionBar?.title = "Les festivals en carte"

        val webView: WebView = findViewById(R.id.webview)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://data.culture.gouv.fr/explore/embed/dataset/festivals-global-festivals-_-pl/map/?location=2,17.99267,51.6211&static=false&datasetcard=false&scrollWheelZoom=false")
    }
}
