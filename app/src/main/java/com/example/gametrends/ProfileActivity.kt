package com.example.gametrends

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gametrends.data.models.Game
import com.example.gametrends.data.repository.GameRepository
import com.example.gametrends.ui.adapters.GameAdapter
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesEmptyText: TextView
    private lateinit var wishlistRecyclerView: RecyclerView
    private lateinit var wishlistEmptyText: TextView
    private lateinit var loginButton: Button
    private lateinit var tabFavorites: TextView
    private lateinit var tabWishlist: TextView
    private lateinit var sectionTitle: TextView

    private val repository = GameRepository()
    private val favoriteGames = mutableListOf<Game>()
    private val wishlistGames = mutableListOf<Game>()
    private var currentTab = "favorites"
    private var wishlistLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.profile)

        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        favoritesEmptyText = findViewById(R.id.favoritesEmptyText)
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView)
        wishlistEmptyText = findViewById(R.id.wishlistEmptyText)
        loginButton = findViewById(R.id.loginButton)
        tabFavorites = findViewById(R.id.tabFavorites)
        tabWishlist = findViewById(R.id.tabWishlist)
        sectionTitle = findViewById(R.id.sectionTitle)

        userName.text = "Гость"
        userEmail.text = "guest@gametrends.com"

        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        wishlistRecyclerView.layoutManager = LinearLayoutManager(this)

        loginButton.setOnClickListener {
            Toast.makeText(this, "Функция входа будет доступна в следующей версии", Toast.LENGTH_SHORT).show()
        }

        tabFavorites.setOnClickListener {
            if (currentTab != "favorites") {
                currentTab = "favorites"
                updateTabUI()
                updateSectionTitle()
                showFavoritesTab()
            }
        }

        tabWishlist.setOnClickListener {
            if (currentTab != "wishlist") {
                currentTab = "wishlist"
                updateTabUI()
                updateSectionTitle()
                showWishlistTab()
                if (!wishlistLoaded) {
                    loadWishlistGames()
                }
            }
        }

        loadFavoriteGames()

        currentTab = "favorites"
        updateTabUI()
        updateSectionTitle()
        showFavoritesTab()
    }

    private fun updateSectionTitle() {
        if (currentTab == "favorites") {
            sectionTitle.text = "Избранное"
        } else {
            sectionTitle.text = "Список желаемого"
        }
    }

    private fun updateTabUI() {
        val accentColor = ColorManager.getAccentColor(this)
        val secondaryColor = resources.getColor(R.color.night_text_secondary)

        if (currentTab == "favorites") {
            tabFavorites.setTextColor(accentColor)
            tabWishlist.setTextColor(secondaryColor)
        } else {
            tabWishlist.setTextColor(accentColor)
            tabFavorites.setTextColor(secondaryColor)
        }
    }

    private fun showFavoritesTab() {
        favoritesRecyclerView.visibility = View.VISIBLE
        wishlistRecyclerView.visibility = View.GONE
        favoritesEmptyText.visibility = if (favoriteGames.isEmpty()) View.VISIBLE else View.GONE
        wishlistEmptyText.visibility = View.GONE
    }

    private fun showWishlistTab() {
        favoritesRecyclerView.visibility = View.GONE
        wishlistRecyclerView.visibility = View.VISIBLE
        favoritesEmptyText.visibility = View.GONE
        wishlistEmptyText.visibility = if (wishlistGames.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadFavoriteGames() {
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favoriteIds = prefs.all.keys.filter { it.startsWith("game_") && prefs.getBoolean(it, false) }
            .map { it.replace("game_", "").toIntOrNull() }
            .filterNotNull()

        if (favoriteIds.isEmpty()) {
            favoritesEmptyText.visibility = View.VISIBLE
            favoritesRecyclerView.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                favoriteGames.clear()
                for (id in favoriteIds) {
                    val game = repository.getGameDetails(id)
                    game?.let {
                        favoriteGames.add(
                            Game(
                                id = it.id,
                                name = it.name,
                                rating = it.rating,
                                backgroundImage = it.backgroundImage,
                                released = it.released,
                                genres = it.genres,
                                metacritic = it.metacritic,
                                playtime = it.playtime,
                                added = it.added,
                                addedByStatus = it.addedByStatus,
                                developers = it.developers,
                                publishers = it.publishers,
                                tags = it.tags,
                                platforms = it.platforms,
                                parentPlatforms = it.parentPlatforms,
                                esrbRating = it.esrbRating,
                                ratings = it.ratings,
                                ratingsCount = it.ratingsCount,
                                reviewsCount = null,
                                suggestionsCount = null
                            )
                        )
                    }
                }

                if (favoriteGames.isNotEmpty()) {
                    favoritesEmptyText.visibility = View.GONE
                    favoritesRecyclerView.visibility = View.VISIBLE
                    val adapter = GameAdapter(favoriteGames, { game ->
                        val intent = Intent(this@ProfileActivity, GameDetailActivity::class.java)
                        intent.putExtra("GAME_ID", game.id)
                        intent.putExtra("GAME_NAME", game.name)
                        startActivity(intent)
                    })
                    favoritesRecyclerView.adapter = adapter
                } else {
                    favoritesEmptyText.visibility = View.VISIBLE
                    favoritesRecyclerView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Ошибка загрузки избранного: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadWishlistGames() {
        wishlistLoaded = true
        val prefs = getSharedPreferences("wishlist", Context.MODE_PRIVATE)
        val wishlistIds = prefs.all.keys.filter { it.startsWith("game_") && prefs.getBoolean(it, false) }
            .map { it.replace("game_", "").toIntOrNull() }
            .filterNotNull()

        if (wishlistIds.isEmpty()) {
            wishlistEmptyText.visibility = View.VISIBLE
            wishlistRecyclerView.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                wishlistGames.clear()
                for (id in wishlistIds) {
                    val game = repository.getGameDetails(id)
                    game?.let {
                        wishlistGames.add(
                            Game(
                                id = it.id,
                                name = it.name,
                                rating = it.rating,
                                backgroundImage = it.backgroundImage,
                                released = it.released,
                                genres = it.genres,
                                metacritic = it.metacritic,
                                playtime = it.playtime,
                                added = it.added,
                                addedByStatus = it.addedByStatus,
                                developers = it.developers,
                                publishers = it.publishers,
                                tags = it.tags,
                                platforms = it.platforms,
                                parentPlatforms = it.parentPlatforms,
                                esrbRating = it.esrbRating,
                                ratings = it.ratings,
                                ratingsCount = it.ratingsCount,
                                reviewsCount = null,
                                suggestionsCount = null
                            )
                        )
                    }
                }

                if (wishlistGames.isNotEmpty()) {
                    wishlistEmptyText.visibility = View.GONE
                    wishlistRecyclerView.visibility = View.VISIBLE
                    val adapter = GameAdapter(wishlistGames, { game ->
                        val intent = Intent(this@ProfileActivity, GameDetailActivity::class.java)
                        intent.putExtra("GAME_ID", game.id)
                        intent.putExtra("GAME_NAME", game.name)
                        startActivity(intent)
                    })
                    wishlistRecyclerView.adapter = adapter
                } else {
                    wishlistEmptyText.visibility = View.VISIBLE
                    wishlistRecyclerView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Ошибка загрузки списка желаемого: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)

        loginButton.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        updateTabUI()

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(accentColor)
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
        updateSectionTitle()

        if (currentTab == "favorites") {
            showFavoritesTab()
        } else {
            showWishlistTab()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}