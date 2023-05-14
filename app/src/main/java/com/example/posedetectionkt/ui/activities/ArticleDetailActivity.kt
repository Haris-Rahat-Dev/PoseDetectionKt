package com.example.posedetectionkt.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.ActivityArticleDetailBinding
import com.example.posedetectionkt.models.article.Article
import com.example.posedetectionkt.utils.WindowManager
import com.google.gson.Gson

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowManager.statusBarManager(
            this, R.color.my_light_primary, false
        )

        val json = intent.getStringExtra("articleJson")
        val gson = Gson()
        val article = gson.fromJson(json, Article::class.java)


        Glide.with(binding.ivImage).load(article.urlToImage).into(binding.ivImage)

        binding.appToolbar.tbToolbar.title = article.title
        binding.tvAuthor.text = "Author: ${article.author}"
        binding.tvPublishedAt.text = "Published At: ${article.publishedAt}"
        binding.tvTitle.text = article.title
        binding.tvContent.text = article.content

        binding.appToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }

        binding.btnReadMore.setOnClickListener {
            val intent = Intent(this@ArticleDetailActivity, WebViewActivity::class.java)

            val gson = Gson()
            val articleJson = gson.toJson(article)

            intent.putExtra("articleJson", articleJson)

            startActivity(intent)
        }
    }
}