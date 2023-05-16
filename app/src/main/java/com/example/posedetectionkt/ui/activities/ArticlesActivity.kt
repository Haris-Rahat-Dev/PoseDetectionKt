package com.example.posedetectionkt.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.example.posedetectionkt.R
import com.example.posedetectionkt.adapters.ArticleAdapter
import com.example.posedetectionkt.databinding.ActivityArtilesBinding
import com.example.posedetectionkt.models.article.ApiResponse
import com.example.posedetectionkt.models.article.Article
import com.example.posedetectionkt.utils.Loading
import com.example.posedetectionkt.utils.WindowManager


class ArticlesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtilesBinding

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articlesList: ArrayList<Article>
    private lateinit var loading: Loading


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowManager.statusBarManager(
            this, R.color.my_light_primary, false
        )

        loading = Loading(this)

        binding.appToolbar.tbToolbar.title = "Articles"
        binding.appToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }

        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        articlesList = ArrayList()

        binding.actvSearchArticle.addTextChangedListener()

        binding.actvSearchArticle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty())
                    getArticles("fitness")
                else
                    getArticles(s.toString())

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        getArticles("fitness")

    }

    private fun getArticles(searchingText: String) {
        var query: String = searchingText
        if (query == "fitness") {
            loading.show()
        }
        /*if (articlesList.isEmpty()) loading.show()*/
        AndroidNetworking.get("https://newsapi.org/v2/everything?q=$searchingText&apikey=bdd64b44460e4a238f141f941c9a8faa")
            .setPriority(Priority.HIGH).build()
            .getAsObject(ApiResponse::class.java, object : ParsedRequestListener<ApiResponse> {
                override fun onResponse(apiResponse: ApiResponse) {
                    articlesList = apiResponse.articles as ArrayList<Article>

                    articleAdapter = ArticleAdapter(articlesList)
                    binding.rvArticles.adapter = articleAdapter

                    articleAdapter.notifyDataSetChanged()

                    loading.dismiss()
                }

                override fun onError(anError: ANError) {
                    anError.printStackTrace()
                }
            })
    }
}