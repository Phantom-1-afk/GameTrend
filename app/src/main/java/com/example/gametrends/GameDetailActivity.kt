package com.example.gametrends

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gametrends.data.models.GameDetailResponse
import com.example.gametrends.data.repository.GameRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GameDetailActivity : BaseActivity() {

    private lateinit var gameImage: ImageView
    private lateinit var gameTitle: TextView
    private lateinit var gameRating: TextView
    private lateinit var gameMetacritic: TextView
    private lateinit var gameDescription: TextView
    private lateinit var gameDevelopers: TextView
    private lateinit var gamePublishers: TextView
    private lateinit var gameReleaseDate: TextView
    private lateinit var gameWebsite: TextView
    private lateinit var gameAchievements: TextView
    private lateinit var gamePlaytime: TextView
    private lateinit var gamePlatforms: TextView
    private lateinit var gameGenres: TextView
    private lateinit var gameReddit: TextView
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private lateinit var favoriteButton: Button
    private lateinit var wishlistButton: Button

    private val repository = GameRepository()
    private var gameData: GameDetailResponse? = null
    private var gameId: Int = -1
    private var isFavorite = false
    private var isInWishlist = false
    private var releaseDateFromList: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        gameId = intent.getIntExtra("GAME_ID", -1)
        val gameName = intent.getStringExtra("GAME_NAME") ?: getString(R.string.app_name)
        releaseDateFromList = intent.getStringExtra("RELEASE_DATE")

        if (gameId == -1) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupToolbar(gameName)
        checkFavoriteStatus()
        checkWishlistStatus()

        if (!releaseDateFromList.isNullOrEmpty() && gameData == null) {
            gameData = GameDetailResponse(
                id = gameId,
                name = gameName,
                descriptionRaw = null,
                backgroundImage = null,
                backgroundImageAdditional = null,
                website = null,
                redditUrl = null,
                metacritic = null,
                rating = 0.0,
                ratingsCount = 0,
                achievementsCount = null,
                playtime = null,
                released = releaseDateFromList,
                developers = null,
                publishers = null,
                genres = null,
                platforms = null,
                parentPlatforms = null,
                esrbRating = null,
                tags = null,
                added = null,
                addedByStatus = null,
                youtubeCount = null,
                twitchCount = null,
                ratings = null,
                metacriticPlatforms = null
            )
        }

        loadGameDetails(gameId)
    }

    private fun initViews() {
        gameImage = findViewById(R.id.gameDetailImage)
        gameTitle = findViewById(R.id.gameDetailTitle)
        gameRating = findViewById(R.id.gameDetailRating)
        gameMetacritic = findViewById(R.id.gameDetailMetacritic)
        gameDescription = findViewById(R.id.gameDetailDescription)
        gameDevelopers = findViewById(R.id.gameDetailDevelopers)
        gamePublishers = findViewById(R.id.gameDetailPublishers)
        gameReleaseDate = findViewById(R.id.gameDetailReleaseDate)
        gameWebsite = findViewById(R.id.gameDetailWebsite)
        gameAchievements = findViewById(R.id.gameDetailAchievements)
        gamePlaytime = findViewById(R.id.gameDetailPlaytime)
        gamePlatforms = findViewById(R.id.gameDetailPlatforms)
        gameGenres = findViewById(R.id.gameDetailGenres)
        gameReddit = findViewById(R.id.gameDetailReddit)
        toolbar = findViewById(R.id.toolbar)
        favoriteButton = findViewById(R.id.favoriteButton)
        wishlistButton = findViewById(R.id.wishlistButton)

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        wishlistButton.setOnClickListener {
            toggleWishlist()
        }
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun checkFavoriteStatus() {
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        isFavorite = prefs.getBoolean("game_$gameId", false)
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.text = "❤️ В избранном"
        } else {
            favoriteButton.text = "🤍 В избранное"
        }
    }

    private fun toggleFavorite() {
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        isFavorite = !isFavorite
        prefs.edit().putBoolean("game_$gameId", isFavorite).apply()
        updateFavoriteButton()

        if (isFavorite) {
            Toast.makeText(this, "Игра добавлена в избранное", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Игра удалена из избранного", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkWishlistStatus() {
        val prefs = getSharedPreferences("wishlist", Context.MODE_PRIVATE)
        isInWishlist = prefs.getBoolean("game_$gameId", false)
        updateWishlistButton()
    }

    private fun updateWishlistButton() {
        if (isInWishlist) {
            wishlistButton.text = "📝 В желаемом"
        } else {
            wishlistButton.text = "📝 В желаемое"
        }
    }

    private fun toggleWishlist() {
        val prefs = getSharedPreferences("wishlist", Context.MODE_PRIVATE)
        isInWishlist = !isInWishlist
        prefs.edit().putBoolean("game_$gameId", isInWishlist).apply()
        updateWishlistButton()

        if (isInWishlist) {
            Toast.makeText(this, "Добавлено в список желаемого", Toast.LENGTH_SHORT).show()

            val releaseDate = gameData?.released ?: releaseDateFromList
            if (!releaseDate.isNullOrEmpty() && checkIfUpcoming(releaseDate)) {
                scheduleReminder()
            }
        } else {
            Toast.makeText(this, "Удалено из списка желаемого", Toast.LENGTH_SHORT).show()
            cancelReminder()
        }
    }

    private fun checkIfUpcoming(releaseDate: String): Boolean {
        try {
            val parsedDate = parseDate(releaseDate) ?: return false
            val now = Calendar.getInstance()
            return parsedDate.after(now.time)
        } catch (e: Exception) {
            return false
        }
    }

    private fun cancelReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, gameId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        val prefs = getSharedPreferences("wishlist", Context.MODE_PRIVATE)
        prefs.edit().remove("reminder_$gameId").apply()
        prefs.edit().remove("reminder_name_$gameId").apply()
    }

    private fun scheduleReminder() {
        var releaseDate = gameData?.released
        if (releaseDate.isNullOrEmpty()) {
            releaseDate = releaseDateFromList
        }

        if (releaseDate.isNullOrEmpty()) {
            Toast.makeText(this, "Дата релиза неизвестна", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val releaseCalendar = Calendar.getInstance()
            val parsedDate = parseDate(releaseDate)

            if (parsedDate == null) {
                Toast.makeText(this, "Некорректный формат даты: $releaseDate", Toast.LENGTH_SHORT).show()
                return
            }

            releaseCalendar.time = parsedDate

            val now = Calendar.getInstance()

            val releaseDay = releaseCalendar.get(Calendar.DAY_OF_YEAR)
            val releaseYear = releaseCalendar.get(Calendar.YEAR)
            val nowDay = now.get(Calendar.DAY_OF_YEAR)
            val nowYear = now.get(Calendar.YEAR)

            if (releaseYear < nowYear || (releaseYear == nowYear && releaseDay <= nowDay)) {
                Toast.makeText(this, "Игра уже вышла или выходит сегодня!", Toast.LENGTH_SHORT).show()
                return
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    Toast.makeText(this, "Разрешите приложению устанавливать напоминания в настройках", Toast.LENGTH_LONG).show()
                    return
                }
            }

            val prefs = getSharedPreferences("wishlist", Context.MODE_PRIVATE)
            prefs.edit().putLong("reminder_$gameId", releaseCalendar.timeInMillis).apply()
            prefs.edit().putString("reminder_name_$gameId", gameData?.name ?: gameTitle.text.toString()).apply()

            val intent = Intent(this, ReminderReceiver::class.java)
            intent.putExtra("game_name", gameData?.name ?: gameTitle.text.toString())
            val pendingIntent = PendingIntent.getBroadcast(
                this, gameId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                releaseCalendar.timeInMillis,
                pendingIntent
            )

            val displayDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsedDate)
            Toast.makeText(this, "Напоминание установлено на $displayDate", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Нет разрешения для установки точного напоминания", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseDate(dateString: String): java.util.Date? {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM", Locale.getDefault()),
            SimpleDateFormat("yyyy", Locale.getDefault()),
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        )

        for (format in formats) {
            try {
                return format.parse(dateString)
            } catch (e: Exception) {
                // Продолжаем пробовать другие форматы
            }
        }
        return null
    }

    private fun loadGameDetails(gameId: Int) {
        lifecycleScope.launch {
            try {
                val game = repository.getGameDetails(gameId)
                if (game != null) {
                    gameData = game
                    updateUI(game)
                } else if (gameData != null) {
                    updateUI(gameData!!)
                } else {
                    Toast.makeText(this@GameDetailActivity, getString(R.string.no_data), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (gameData != null) {
                    updateUI(gameData!!)
                } else {
                    Toast.makeText(this@GameDetailActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(game: GameDetailResponse) {
        gameTitle.text = game.name

        gameRating.text = String.format("⭐ %.1f (%d %s)",
            game.rating,
            game.ratingsCount,
            getString(R.string.ratings_count).replace("%d", "").trim()
        )
        gameRating.setTextColor(android.graphics.Color.parseColor("#FFD700"))

        game.metacritic?.let {
            gameMetacritic.text = "Metacritic: $it"
            gameMetacritic.visibility = TextView.VISIBLE
            gameMetacritic.setTextColor(android.graphics.Color.parseColor("#FFD700"))
        } ?: run {
            gameMetacritic.visibility = TextView.GONE
        }

        val releaseDate = game.released ?: releaseDateFromList
        if (!releaseDate.isNullOrEmpty()) {
            gameReleaseDate.text = "${getString(R.string.release_date)}: ${formatDateForDisplay(releaseDate)}"
            gameReleaseDate.visibility = TextView.VISIBLE
        } else {
            gameReleaseDate.visibility = TextView.GONE
        }

        // ОПИСАНИЕ БЕЗ ПЕРЕВОДА (оригинал на английском)
        if (!game.descriptionRaw.isNullOrEmpty()) {
            gameDescription.text = game.descriptionRaw
        } else {
            gameDescription.text = getString(R.string.no_description)
        }

        if (!game.developers.isNullOrEmpty()) {
            val devs = game.developers.joinToString(", ") { it.name }
            gameDevelopers.text = "${getString(R.string.developers)}: $devs"
            gameDevelopers.visibility = TextView.VISIBLE
        } else {
            gameDevelopers.visibility = TextView.GONE
        }

        if (!game.publishers.isNullOrEmpty()) {
            val pubs = game.publishers.joinToString(", ") { it.name }
            gamePublishers.text = "${getString(R.string.publishers)}: $pubs"
            gamePublishers.visibility = TextView.VISIBLE
        } else {
            gamePublishers.visibility = TextView.GONE
        }

        if (!game.genres.isNullOrEmpty()) {
            val genres = game.genres.joinToString(", ") { it.name }
            gameGenres.text = "${getString(R.string.genres)}: $genres"
            gameGenres.visibility = TextView.VISIBLE
        } else {
            gameGenres.visibility = TextView.GONE
        }

        if (!game.platforms.isNullOrEmpty()) {
            val platforms = game.platforms.joinToString(", ") { it.platform.name }
            gamePlatforms.text = "${getString(R.string.platforms)}: $platforms"
            gamePlatforms.visibility = TextView.VISIBLE
        } else {
            gamePlatforms.visibility = TextView.GONE
        }

        game.achievementsCount?.let {
            gameAchievements.text = "${getString(R.string.achievements)}: $it"
            gameAchievements.visibility = TextView.VISIBLE
        } ?: run {
            gameAchievements.visibility = TextView.GONE
        }

        game.playtime?.let {
            gamePlaytime.text = "${getString(R.string.playtime)}: $it ${getString(R.string.hours)}"
            gamePlaytime.visibility = TextView.VISIBLE
        } ?: run {
            gamePlaytime.visibility = TextView.GONE
        }

        if (!game.website.isNullOrEmpty()) {
            gameWebsite.text = "🌐 ${getString(R.string.website)}"
            gameWebsite.visibility = TextView.VISIBLE
            gameWebsite.setTextColor(android.graphics.Color.parseColor("#CF6679"))
            gameWebsite.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(game.website))
                startActivity(intent)
            }
        } else {
            gameWebsite.text = "🌐 Ссылок нет"
            gameWebsite.visibility = TextView.VISIBLE
            gameWebsite.setTextColor(android.graphics.Color.parseColor("#808080"))
            gameWebsite.setOnClickListener(null)
        }

        if (!game.redditUrl.isNullOrEmpty()) {
            gameReddit.text = "👥 ${getString(R.string.reddit)}"
            gameReddit.visibility = TextView.VISIBLE
            gameReddit.setTextColor(android.graphics.Color.parseColor("#CF6679"))
            gameReddit.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(game.redditUrl))
                startActivity(intent)
            }
        } else {
            gameReddit.text = "👥 Reddit сообщества нет"
            gameReddit.visibility = TextView.VISIBLE
            gameReddit.setTextColor(android.graphics.Color.parseColor("#808080"))
            gameReddit.setOnClickListener(null)
        }

        if (!game.backgroundImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(game.backgroundImage)
                .placeholder(R.drawable.game_placeholder)
                .error(R.drawable.game_placeholder)
                .into(gameImage)
        } else {
            gameImage.setImageResource(R.drawable.game_placeholder)
        }
    }

    private fun formatDateForDisplay(dateString: String): String {
        return try {
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM", Locale.getDefault()),
                SimpleDateFormat("yyyy", Locale.getDefault())
            )

            for (format in formats) {
                try {
                    val date = format.parse(dateString)
                    if (date != null) {
                        val outputFormat = when (format.toPattern()) {
                            "yyyy-MM-dd" -> SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            "yyyy-MM" -> SimpleDateFormat("MM.yyyy", Locale.getDefault())
                            else -> SimpleDateFormat("yyyy", Locale.getDefault())
                        }
                        return outputFormat.format(date)
                    }
                } catch (e: Exception) {
                }
            }
            dateString
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)

        favoriteButton.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        wishlistButton.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        toolbar.setTitleTextColor(accentColor)
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
    }
}