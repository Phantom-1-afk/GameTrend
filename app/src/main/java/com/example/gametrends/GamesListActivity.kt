package com.example.gametrends

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gametrends.data.models.Game
import com.example.gametrends.data.repository.GameRepository
import com.example.gametrends.ui.adapters.GameAdapter
import kotlinx.coroutines.launch

class GamesListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar

    private val repository = GameRepository()
    private var currentPage = 1
    private var isLoading = false
    private var gamesList = mutableListOf<Game>()
    private lateinit var adapter: GameAdapter
    private var currentType = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentType = intent.getStringExtra("type") ?: "all"
        supportActionBar?.title = when (currentType) {
            "all" -> getString(R.string.view_all_games)
            "top" -> getString(R.string.top_games)
            "upcoming" -> getString(R.string.upcoming_games)
            else -> getString(R.string.view_all_games)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Бесконечная прокрутка только для "Все игры"
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItem >= totalItemCount - 5 && currentType == "all") {
                    currentPage++
                    loadGames(currentType, currentPage)
                }
            }
        })

        loadGames(currentType, 1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)
        toolbar.setTitleTextColor(accentColor)
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
    }

    private fun loadGames(type: String, page: Int) {
        isLoading = true
        if (page == 1) {
            progressBar.visibility = ProgressBar.VISIBLE
            errorText.visibility = TextView.GONE
        }

        lifecycleScope.launch {
            try {
                val games = when (type) {
                    "all" -> repository.getAllGames(page, 20)
                    "top" -> repository.getComprehensiveTopGames(20)
                    "upcoming" -> repository.getUpcomingGamesWithWishes(20)
                    else -> repository.getAllGames(page, 20)
                }

                if (games.isNotEmpty()) {
                    if (page == 1) {
                        gamesList.clear()
                        gamesList.addAll(games)
                        adapter = GameAdapter(gamesList, { game ->
                            openGameDetail(game.id, game.name, game.released)
                        })
                        recyclerView.adapter = adapter
                    } else {
                        val positionStart = gamesList.size
                        gamesList.addAll(games)
                        adapter.notifyItemRangeInserted(positionStart, games.size)
                    }
                    errorText.visibility = TextView.GONE
                } else if (page == 1) {
                    errorText.text = getString(R.string.no_data)
                    errorText.visibility = TextView.VISIBLE
                }
            } catch (e: Exception) {
                if (page == 1) {
                    errorText.text = "${getString(R.string.error)}: ${e.message}"
                    errorText.visibility = TextView.VISIBLE
                }
                Toast.makeText(this@GamesListActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun openGameDetail(gameId: Int, gameName: String, releaseDate: String?) {
        val intent = Intent(this, GameDetailActivity::class.java)
        intent.putExtra("GAME_ID", gameId)
        intent.putExtra("GAME_NAME", gameName)
        intent.putExtra("RELEASE_DATE", releaseDate ?: "")
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}