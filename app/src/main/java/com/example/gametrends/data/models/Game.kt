package com.example.gametrends.data.models

import com.google.gson.annotations.SerializedName

data class GameResponse(
    val results: List<Game>
)

data class Game(
    val id: Int,
    val name: String,
    val rating: Double?,
    @SerializedName("background_image")
    val backgroundImage: String?,
    val released: String?,
    val genres: List<Genre>?,
    val metacritic: Int?,
    val playtime: Int?,
    val added: Int?,
    @SerializedName("added_by_status")
    val addedByStatus: AddedByStatus?,
    val developers: List<Developer>?,
    val publishers: List<Publisher>?,
    val tags: List<Tag>?,
    val platforms: List<PlatformInfo>?,
    @SerializedName("parent_platforms")
    val parentPlatforms: List<ParentPlatform>?,
    @SerializedName("esrb_rating")
    val esrbRating: EsrbRating?,
    val ratings: List<Rating>?,
    @SerializedName("ratings_count")
    val ratingsCount: Int?,
    @SerializedName("reviews_count")
    val reviewsCount: Int?,
    @SerializedName("suggestions_count")
    val suggestionsCount: Int?
)

data class GameDetailResponse(
    val id: Int,
    val name: String,
    @SerializedName("description_raw")
    val descriptionRaw: String?,
    @SerializedName("background_image")
    val backgroundImage: String?,
    @SerializedName("background_image_additional")
    val backgroundImageAdditional: String?,
    val website: String?,
    @SerializedName("reddit_url")
    val redditUrl: String?,
    val metacritic: Int?,
    val rating: Double,
    @SerializedName("ratings_count")
    val ratingsCount: Int,
    @SerializedName("achievements_count")
    val achievementsCount: Int?,
    val playtime: Int?,
    val released: String?,
    val developers: List<Developer>?,
    val publishers: List<Publisher>?,
    val genres: List<Genre>?,
    val platforms: List<PlatformInfo>?,
    @SerializedName("parent_platforms")
    val parentPlatforms: List<ParentPlatform>?,
    @SerializedName("esrb_rating")
    val esrbRating: EsrbRating?,
    val tags: List<Tag>?,
    val added: Int?,
    @SerializedName("added_by_status")
    val addedByStatus: AddedByStatus?,
    @SerializedName("youtube_count")
    val youtubeCount: Int?,
    @SerializedName("twitch_count")
    val twitchCount: Int?,
    val ratings: List<Rating>?,
    @SerializedName("metacritic_platforms")
    val metacriticPlatforms: List<MetacriticPlatform>?
)

data class Rating(
    val id: Int,
    val title: String,
    val count: Int,
    val percent: Double
) {
    fun getRussianTitle(): String {
        return when (title.lowercase()) {
            "exceptional" -> "Превосходно"
            "recommended" -> "Рекомендуется"
            "meh" -> "Так себе"
            "skip" -> "Пропустить"
            else -> title
        }
    }
}

data class MetacriticPlatform(
    val platform: Platform,
    val metacritic: Int?,
    val url: String?
)

data class AddedByStatus(
    val owned: Int?,
    val beaten: Int?,
    val dropped: Int?,
    val playing: Int?,
    val yet: Int?,
    val toplay: Int?
)

data class Developer(val name: String)
data class Publisher(val name: String)
data class Genre(val id: Int, val name: String)
data class Tag(val id: Int, val name: String)

data class PlatformInfo(
    val platform: Platform,
    @SerializedName("released_at")
    val releasedAt: String?,
    @SerializedName("requirements_en")
    val requirementsEn: Requirements?
)

data class Platform(val id: Int, val name: String, val slug: String)
data class ParentPlatform(val platform: Platform)
data class Requirements(val minimum: String?, val recommended: String?)

data class EsrbRating(val id: Int, val name: String) {
    fun getRussianName(): String {
        return when (name.lowercase()) {
            "everyone" -> "Для всех"
            "everyone 10+" -> "10+"
            "teen" -> "Подросткам"
            "mature" -> "17+"
            "adults only" -> "Только для взрослых"
            else -> name
        }
    }
}