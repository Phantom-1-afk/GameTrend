package com.example.gametrends.data.api

import com.example.gametrends.data.models.GameDetailResponse
import com.example.gametrends.data.models.GameResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GameApiService {

    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("ordering") ordering: String = "-rating",
        @Query("dates") dates: String? = null,
        @Query("page") page: Int = 1,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("metacritic") metacritic: String? = null
    ): GameResponse

    @GET("games")
    suspend fun getTopGames(
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("ordering") ordering: String = "-rating"
    ): GameResponse

    @GET("games")
    suspend fun getTrendingGames(
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("ordering") ordering: String = "-added"
    ): GameResponse

    @GET("games")
    suspend fun getUpcomingGames(
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("dates") dates: String,
        @Query("ordering") ordering: String = "-added"
    ): GameResponse

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): GameDetailResponse

    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String,
        @Query("search") query: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("dates") dates: String? = null
    ): GameResponse
}