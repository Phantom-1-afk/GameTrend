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

class PopularGamesActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar

    private val repository = GameRepository()
    private var gamesList = mutableListOf<Game>()
    private lateinit var adapter: GameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Самые популярные"

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadPopularGames()
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

    private fun loadPopularGames() {
        progressBar.visibility = ProgressBar.VISIBLE
        errorText.visibility = TextView.GONE

        lifecycleScope.launch {
            try {
                val games = repository.getMostWishedGames(30)

                if (games.isNotEmpty()) {
                    gamesList.clear()
                    gamesList.addAll(games)
                    adapter = GameAdapter(gamesList, { game ->
                        openGameDetail(game.id, game.name, game.released)
                    })
                    recyclerView.adapter = adapter
                    errorText.visibility = TextView.GONE
                } else {
                    errorText.text = getString(R.string.no_data)
                    errorText.visibility = TextView.VISIBLE
                }
            } catch (e: Exception) {
                errorText.text = "${getString(R.string.error)}: ${e.message}"
                errorText.visibility = TextView.VISIBLE
                Toast.makeText(this@PopularGamesActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            } finally {
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