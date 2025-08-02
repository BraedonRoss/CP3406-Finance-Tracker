package com.cp3406.financetracker.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FinancialNewsApi {
    
    @GET("v2/everything")
    suspend fun getFinancialNews(
        @Query("apiKey") apiKey: String,
        @Query("q") query: String = "finance OR economy OR market",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("page") page: Int = 1
    ): Response<NewsResponse>
    
    companion object {
        const val BASE_URL = "https://newsapi.org/"
    }
}

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class Source(
    val id: String?,
    val name: String
)