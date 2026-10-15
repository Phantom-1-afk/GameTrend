package com.example.gametrends

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gametrends.data.models.Game
import com.example.gametrends.data.repository.GameRepository
import com.example.gametrends.ui.adapters.SmallGameAdapter
import kotlinx.coroutines.launch

class MainScreenActivity : BaseActivity() {

    private lateinit var bestGameImage: ImageView
    private lateinit var bestGameName: TextView
    private lateinit var bestGameRating: TextView
    private lateinit var bestGameMetacritic: TextView
    private lateinit var bestGameAdded: TextView
    private lateinit var cardBestGame: androidx.cardview.widget.CardView

    private lateinit var horizontalPopularRecyclerView: RecyclerView
    private lateinit var horizontalTopRatedRecyclerView: RecyclerView
    private lateinit var horizontalMetacriticRecyclerView: RecyclerView
    private lateinit var horizontalLeastPopularRecyclerView: RecyclerView

    private lateinit var btnViewAllGames: Button
    private lateinit var btnTopGames: Button
    private lateinit var btnUpcomingGames: Button

    private val repository = GameRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        initViews()
        setupToolbar()
        setupClickListeners()
        setupArrowListeners()
        loadAllSections()
        requestNotificationPermission()
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.overflowIcon?.setTint(Color.WHITE)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)

        btnViewAllGames.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        btnTopGames.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        btnUpcomingGames.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(accentColor)
    }

    private fun initViews() {
        bestGameImage = findViewById(R.id.bestGameImage)
        bestGameName = findViewById(R.id.bestGameName)
        bestGameRating = findViewById(R.id.bestGameRating)
        bestGameMetacritic = findViewById(R.id.bestGameMetacritic)
        bestGameAdded = findViewById(R.id.bestGameAdded)
        cardBestGame = findViewById(R.id.cardBestGame)

        horizontalPopularRecyclerView = findViewById(R.id.horizontalPopularRecyclerView)
        horizontalTopRatedRecyclerView = findViewById(R.id.horizontalTopRatedRecyclerView)
        horizontalMetacriticRecyclerView = findViewById(R.id.horizontalMetacriticRecyclerView)
        horizontalLeastPopularRecyclerView = findViewById(R.id.horizontalLeastPopularRecyclerView)

        btnViewAllGames = findViewById(R.id.btnViewAllGames)
        btnTopGames = findViewById(R.id.btnTopGames)
        btnUpcomingGames = findViewById(R.id.btnUpcomingGames)

        horizontalPopularRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalTopRatedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalMetacriticRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalLeastPopularRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        findViewById<TextView>(R.id.bestGameTitle).text = getString(R.string.best_game)
        findViewById<TextView>(R.id.mostPopularTitle).text = getString(R.string.most_popular)
        findViewById<TextView>(R.id.topRatedTitle).text = getString(R.string.top_rated)
        findViewById<TextView>(R.id.topMetacriticTitle).text = getString(R.string.top_metacritic)
        findViewById<TextView>(R.id.leastPopularTitle).text = getString(R.string.least_popular)

        btnViewAllGames.text = getString(R.string.view_all_games)
        btnTopGames.text = getString(R.string.top_games)
        btnUpcomingGames.text = getString(R.string.upcoming_games)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupClickListeners() {
        cardBestGame.setOnClickListener {}

        btnViewAllGames.setOnClickListener {
            val intent = Intent(this, GamesListActivity::class.java)
            intent.putExtra("type", "all")
            startActivity(intent)
        }

        btnTopGames.setOnClickListener {
            val intent = Intent(this, GamesListActivity::class.java)
            intent.putExtra("type", "top")
            startActivity(intent)
        }

        btnUpcomingGames.setOnClickListener {
            val intent = Intent(this, GamesListActivity::class.java)
            intent.putExtra("type", "upcoming")
            startActivity(intent)
        }
    }

    private fun setupArrowListeners() {
        findViewById<ImageView>(R.id.btnMostPopular).setOnClickListener {
            startActivity(Intent(this, PopularGamesActivity::class.java))
        }

        findViewById<ImageView>(R.id.btnTopRated).setOnClickListener {
            val intent = Intent(this, GamesListActivity::class.java)
            intent.putExtra("type", "top")
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.btnTopMetacritic).setOnClickListener {
            startActivity(Intent(this, MetacriticGamesActivity::class.java))
        }

        findViewById<ImageView>(R.id.btnLeastPopular).setOnClickListener {
            startActivity(Intent(this, UnpopularGamesActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadAllSections() {
        lifecycleScope.launch {
            try {
                val bestGame = repository.getBestGame()
                val popularGames = repository.getMostWishedGames(6)
                val topRatedGames = repository.getTopRatedGames(6)
                val metacriticGames = repository.getTopMetacriticGames(6)
                val leastPopularGames = repository.getLeastPopularGames(6)

                updateBestGameSection(bestGame)
                updatePopularSection(popularGames)
                updateTopRatedSection(topRatedGames)
                updateMetacriticSection(metacriticGames)
                updateLeastPopularSection(leastPopularGames)

            } catch (e: Exception) {
                Toast.makeText(this@MainScreenActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBestGameSection(game: Game?) {
        if (game != null) {
            bestGameName.text = game.name
            bestGameRating.text = String.format("⭐ %.1f", game.rating ?: 0.0)
            bestGameMetacritic.text = String.format("MC: %d", game.metacritic ?: 0)
            bestGameAdded.text = String.format("❤️ %d", game.added ?: 0)
            bestGameAdded.setTextColor(Color.parseColor("#FFD700"))

            if (!game.backgroundImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(game.backgroundImage)
                    .placeholder(R.drawable.game_placeholder)
                    .error(R.drawable.game_placeholder)
                    .into(bestGameImage)
            } else {
                bestGameImage.setImageResource(R.drawable.game_placeholder)
            }

            cardBestGame.setOnClickListener {
                openGameDetail(game.id, game.name)
            }
        }
    }

    private fun updatePopularSection(games: List<Game>) {
        if (games.isNotEmpty()) {
            val adapter = SmallGameAdapter(games, { game ->
                openGameDetail(game.id, game.name)
            }, "popular")
            horizontalPopularRecyclerView.adapter = adapter
        }
    }

    private fun updateTopRatedSection(games: List<Game>) {
        if (games.isNotEmpty()) {
            val adapter = SmallGameAdapter(games, { game ->
                openGameDetail(game.id, game.name)
            }, "rated")
            horizontalTopRatedRecyclerView.adapter = adapter
        }
    }

    private fun updateMetacriticSection(games: List<Game>) {
        if (games.isNotEmpty()) {
            val adapter = SmallGameAdapter(games, { game ->
                openGameDetail(game.id, game.name)
            }, "metacritic")
            horizontalMetacriticRecyclerView.adapter = adapter
        }
    }

    private fun updateLeastPopularSection(games: List<Game>) {
        if (games.isNotEmpty()) {
            val adapter = SmallGameAdapter(games, { game ->
                openGameDetail(game.id, game.name)
            }, "least")
            horizontalLeastPopularRecyclerView.adapter = adapter
        }
    }

    private fun openGameDetail(gameId: Int, gameName: String) {
        val intent = Intent(this, GameDetailActivity::class.java)
        intent.putExtra("GAME_ID", gameId)
        intent.putExtra("GAME_NAME", gameName)
        startActivity(intent)
    }
}