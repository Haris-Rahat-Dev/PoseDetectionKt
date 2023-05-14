package com.example.posedetectionkt.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.RvExploreTileBinding
import com.example.posedetectionkt.models.article.Article
import com.example.posedetectionkt.ui.activities.ArticleDetailActivity
import com.google.gson.Gson

class ArticleAdapter(private var articlesList: ArrayList<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {
    class ViewHolder(binding: RvExploreTileBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.tvTitle
        val description = binding.tvDescription
        val image = binding.ivImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvExploreTileBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount() = articlesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articlesList[position]
        holder.title.text = article.title
        holder.description.text = article.description

        Glide.with(holder.image).load(article.urlToImage).placeholder(R.drawable.baseline_sync_24)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ArticleDetailActivity::class.java)

            val gson = Gson()
            val articleJson = gson.toJson(article)

            intent.putExtra("articleJson", articleJson)

            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }
}