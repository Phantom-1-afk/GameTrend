package com.example.gametrends.data.repository

import com.example.gametrends.Constants
import com.example.gametrends.data.api.RetrofitClient
import com.example.gametrends.data.models.Game
import com.example.gametrends.data.models.GameDetailResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.log10
import kotlin.math.min

class GameRepository {

    private val api = RetrofitClient.apiService

    private val bannedTags = listOf(
        "nsfw", "erotic", "hentai", "adult",
        "sexual", "porn", "xxx", "18+",
        "mature", "explicit", "sex", "fuck",
        "pussy", "dick", "tits", "boobs",
        "nude", "naked", "strip", "stripclub",
        "pornography", "erotica", "lewd",
        "eroge", "adult game", "18+ game",
        "sexually", "suggestive", "provocative"
    )

    private fun isGame(game: Game): Boolean {
        val hasPlatforms = (game.platforms?.isNotEmpty() == true) || (game.parentPlatforms?.isNotEmpty() == true)
        val hasName = !game.name.isNullOrBlank()
        val hasRating = game.rating != null && game.rating > 0
        val hasDate = !game.released.isNullOrBlank()
        return hasPlatforms && hasName && (hasRating || hasDate)
    }

    private fun getGameWeight(game: Game): Double {
        var weight = 0.0
        weight += (game.rating ?: 0.0) * 10
        val ratingsCount = game.ratingsCount ?: 0
        if (ratingsCount > 0) {
            weight += minOf(ratingsCount / 100.0, 30.0)
        }
        weight += (game.metacritic ?: 0) / 5.0
        val added = game.added ?: 0
        if (added > 0) {
            weight += minOf(added / 100.0, 20.0)
        }
        return weight
    }

    private fun filterAndSortGames(games: List<Game>): List<Game> {
        val filtered = games.filter { game ->
            if (!isGame(game)) return@filter false

            val hasBannedTag = game.tags?.any { tag ->
                bannedTags.any { banned -> tag.name.contains(banned, ignoreCase = true) }
            } ?: false

            val hasBannedName = bannedTags.any { banned -> game.name.contains(banned, ignoreCase = true) }
            val hasBannedGenre = game.genres?.any { genre ->
                bannedTags.any { banned -> genre.name.contains(banned, ignoreCase = true) }
            } ?: false

            val isAdultContent = hasBannedTag || hasBannedName || hasBannedGenre
            !isAdultContent
        }
        return filtered.sortedByDescending { getGameWeight(it) }
    }

    suspend fun getTopGames(limit: Int = 20): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTopGames(Constants.API_KEY, limit * 2)
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTrendingGames(limit: Int = 20): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTrendingGames(Constants.API_KEY, limit * 2)
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUpcomingGames(limit: Int = 20): List<Game> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val nextYear = today.plusYears(1)
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val dates = "${today.format(formatter)},${nextYear.format(formatter)}"
            val response = api.getUpcomingGames(
                apiKey = Constants.API_KEY,
                dates = dates,
                pageSize = limit * 2
            )
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMostPopularGame(): Game? = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = 30,
                ordering = "-added"
            )
            filterAndSortGames(response.results).firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getMostWishedGames(limit: Int = 30): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = limit * 2,
                ordering = "-added"
            )
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLeastPopularGame(): Game? = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = 30,
                ordering = "rating"
            )
            filterAndSortGames(response.results).firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getLeastPopularGames(limit: Int = 30): List<Game> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val dates = "1900-01-01,${today.format(formatter)}"
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = limit * 3,
                ordering = "rating",
                dates = dates
            )
            val filtered = filterAndSortGames(response.results)
            filtered.filter { it.rating != null && it.rating!! < 3.0 }.take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTopMetacriticGames(limit: Int = 30): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = limit * 2,
                ordering = "-metacritic"
            )
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getGameDetails(gameId: Int): GameDetailResponse? = withContext(Dispatchers.IO) {
        try {
            api.getGameDetails(gameId, Constants.API_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllGames(page: Int = 1, limit: Int = 20): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = limit,
                ordering = "-rating",
                page = page
            )
            filterAndSortGames(response.results)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchGames(query: String, page: Int = 1): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchGames(
                apiKey = Constants.API_KEY,
                query = query,
                pageSize = 20,
                page = page
            )
            filterAndSortGames(response.results)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchGamesWithFilters(
        query: String,
        genre: String? = null,
        platform: String? = null,
        year: Int? = null,
        page: Int = 1
    ): List<Game> = withContext(Dispatchers.IO) {
        try {
            // Формируем параметр dates для фильтрации по году
            val dates = if (year != null && year > 0) {
                "$year-01-01,$year-12-31"
            } else null

            val response = api.searchGames(
                apiKey = Constants.API_KEY,
                query = query,
                pageSize = 20,
                page = page,
                genres = genre,
                platforms = platform,
                dates = dates
            )
            filterAndSortGames(response.results)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getBestGame(): Game? = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = 50,
                ordering = "-rating"
            )
            val filtered = filterAndSortGames(response.results)
            filtered.maxByOrNull { game ->
                val ratingWeight = (game.rating ?: 0.0) * 20
                val metacriticWeight = (game.metacritic ?: 0) / 2.0
                val addedWeight = (game.added ?: 0) / 1000.0
                ratingWeight + metacriticWeight + addedWeight
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getTopRatedGames(limit: Int = 30): List<Game> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = limit * 2,
                ordering = "-rating"
            )
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getComprehensiveTopGames(limit: Int = 20): List<Game> = withContext(Dispatchers.IO) {
        try {
            val allGames = mutableListOf<Game>()
            var response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = 100,
                ordering = "-rating",
                page = 1
            )
            allGames.addAll(response.results)
            response = api.getGames(
                apiKey = Constants.API_KEY,
                pageSize = 100,
                ordering = "-rating",
                page = 2
            )
            allGames.addAll(response.results)
            val filtered = filterAndSortGames(allGames)
            val minRatingsCount = 20
            val gamesWithEnoughRatings = filtered.filter { game ->
                val ratingsCount = game.ratingsCount ?: 0
                ratingsCount >= minRatingsCount
            }

            val sorted = gamesWithEnoughRatings.sortedByDescending { game ->
                val rating = game.rating ?: 0.0
                val ratingsCount = game.ratingsCount ?: 0
                val metacritic = game.metacritic ?: 0
                val added = game.added ?: 0
                val ratingWeight = rating * 10
                val ratingsCountWeight = if (ratingsCount > 0) {
                    minOf((log10(ratingsCount.toDouble()) * 50), 200.0)
                } else 0.0

                val metacriticWeight = metacritic / 3.33

                val addedWeight = if (added > 0) {
                    minOf((log10(added.toDouble()) * 12.5), 50.0)
                } else 0.0

                ratingWeight + ratingsCountWeight + metacriticWeight + addedWeight
            }

            sorted.take(limit)

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUpcomingGamesWithWishes(limit: Int = 20): List<Game> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val nextYear = today.plusYears(1)
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val dates = "${today.format(formatter)},${nextYear.format(formatter)}"
            val response = api.getUpcomingGames(
                apiKey = Constants.API_KEY,
                dates = dates,
                pageSize = limit * 2
            )
            filterAndSortGames(response.results).take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}