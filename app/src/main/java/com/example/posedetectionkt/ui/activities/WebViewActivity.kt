package com.example.posedetectionkt.ui.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.ActivityWebViewBinding
import com.example.posedetectionkt.models.article.Article
import com.example.posedetectionkt.utils.Loading
import com.example.posedetectionkt.utils.WindowManager
import com.google.gson.Gson

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var loading: Loading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading = Loading(this)

        WindowManager.statusBarManager(
            this, R.color.my_light_primary, false
        )

        val json = intent.getStringExtra("articleJson")
        val gson = Gson()
        val article = gson.fromJson(json, Article::class.java)

        binding.appToolbar.tbToolbar.title = article.title
        binding.appToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.webView.settings.javaScriptEnabled = true

        binding.webView.loadUrl(article.url)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                loading.show()
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                loading.dismiss()
                super.onPageFinished(view, url)
            }
        }
    }
}