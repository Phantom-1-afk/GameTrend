package com.example.gametrends

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gametrends.data.repository.GameRepository
import com.example.gametrends.ui.adapters.GameAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : BaseActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var filterInfoText: TextView
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private lateinit var filterButton: ImageButton
    private lateinit var filterLayout: LinearLayout
    private lateinit var genreSpinner: Spinner
    private lateinit var platformSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var applyFilterButton: Button

    private val repository = GameRepository()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupToolbar()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)
        filterInfoText = findViewById(R.id.filterInfoText)
        toolbar = findViewById(R.id.toolbar)
        filterButton = findViewById(R.id.filterButton)
        filterLayout = findViewById(R.id.filterLayout)
        genreSpinner = findViewById(R.id.genreSpinner)
        platformSpinner = findViewById(R.id.platformSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)
        applyFilterButton = findViewById(R.id.applyFilterButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        filterLayout.visibility = android.view.View.GONE
        filterInfoText.visibility = android.view.View.GONE
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.search)
    }

    private fun setupSpinners() {
        try {
            // Кастомный адаптер для жанров с белым текстом
            val genreAdapter = android.widget.ArrayAdapter.createFromResource(
                this,
                R.array.genres_array,
                R.layout.spinner_item
            )
            genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            genreSpinner.adapter = genreAdapter

            // Кастомный адаптер для платформ
            val platformAdapter = android.widget.ArrayAdapter.createFromResource(
                this,
                R.array.platforms_array,
                R.layout.spinner_item
            )
            platformAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            platformSpinner.adapter = platformAdapter

            // Кастомный адаптер для годов
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val years = (1990..currentYear + 2).map { it.toString() }.toTypedArray()
            val yearAdapter = android.widget.ArrayAdapter(
                this,
                R.layout.spinner_item,
                years
            )
            yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            yearSpinner.adapter = yearAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        filterButton.setOnClickListener {
            if (filterLayout.visibility == android.view.View.GONE) {
                filterLayout.visibility = android.view.View.VISIBLE
            } else {
                filterLayout.visibility = android.view.View.GONE
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()

                val query = s.toString().trim()
                if (query.length >= 2) {
                    searchJob = lifecycleScope.launch {
                        delay(500)
                        performSearch(query)
                    }
                } else if (query.isEmpty()) {
                    // Если поле пустое, показываем результаты только по фильтрам
                    performSearchWithFilters("")
                } else {
                    showEmptyView(getString(R.string.search_min_chars))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        applyFilterButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            val hasFilters = genreSpinner.selectedItemPosition > 0 ||
                    platformSpinner.selectedItemPosition > 0 ||
                    yearSpinner.selectedItemPosition > 0

            if (query.isNotEmpty() || hasFilters) {
                performSearchWithFilters(query)
            } else {
                Toast.makeText(this, "Введите название или выберите фильтры", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            showLoading()
            try {
                val results = repository.searchGames(query)
                if (results.isNotEmpty()) {
                    showResults(results)
                    filterInfoText.visibility = android.view.View.GONE
                } else {
                    showEmptyView(getString(R.string.no_results))
                }
            } catch (e: Exception) {
                showEmptyView("${getString(R.string.search_error)}: ${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    private fun performSearchWithFilters(query: String) {
        val selectedGenre = genreSpinner.selectedItem.toString()
        val selectedPlatform = platformSpinner.selectedItem.toString()
        val selectedYear = yearSpinner.selectedItem.toString()

        // Формируем текст активных фильтров
        val activeFilters = mutableListOf<String>()
        if (selectedGenre != "Все жанры" && selectedGenre != "All genres") {
            activeFilters.add("жанр: $selectedGenre")
        }
        if (selectedPlatform != "Все платформы" && selectedPlatform != "All platforms") {
            activeFilters.add("платформа: $selectedPlatform")
        }
        if (selectedYear != "Все годы" && selectedYear != "All years" && selectedYear != "1990") {
            activeFilters.add("год: $selectedYear")
        }
        if (query.isNotEmpty()) {
            activeFilters.add(0, "🔍 \"$query\"")
        }

        val filterText = if (activeFilters.isNotEmpty()) {
            activeFilters.joinToString(" | ")
        } else {
            ""
        }

        filterInfoText.text = filterText
        filterInfoText.visibility = if (filterText.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        // Преобразуем в ID для API
        val genreId = if (selectedGenre != "Все жанры" && selectedGenre != "All genres") {
            getGenreId(selectedGenre)
        } else null

        val platformId = if (selectedPlatform != "Все платформы" && selectedPlatform != "All platforms") {
            getPlatformId(selectedPlatform)
        } else null

        val year = if (selectedYear != "Все годы" && selectedYear != "All years" && selectedYear.toIntOrNull() != null) {
            selectedYear.toInt()
        } else null

        lifecycleScope.launch {
            showLoading()
            try {
                val results = repository.searchGamesWithFilters(
                    query = query,
                    genre = genreId,
                    platform = platformId,
                    year = year
                )
                if (results.isNotEmpty()) {
                    showResults(results)
                } else {
                    showEmptyView("По вашему запросу ничего не найдено")
                }
            } catch (e: Exception) {
                showEmptyView("${getString(R.string.search_error)}: ${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    private fun getGenreId(genreName: String): String {
        return when (genreName.lowercase()) {
            "action", "экшен" -> "4"
            "rpg" -> "5"
            "shooter", "шутер" -> "2"
            "adventure", "приключение" -> "3"
            "puzzle", "головоломка" -> "7"
            "strategy", "стратегия" -> "10"
            "sport", "спорт" -> "14"
            else -> ""
        }
    }

    private fun getPlatformId(platformName: String): String {
        return when (platformName.lowercase()) {
            "pc" -> "4"
            "playstation" -> "2,18,187"
            "xbox" -> "1,14,186"
            "nintendo" -> "7,8,9"
            "android" -> "21"
            "ios" -> "3"
            else -> ""
        }
    }

    private fun showLoading() {
        progressBar.visibility = android.view.View.VISIBLE
        recyclerView.visibility = android.view.View.GONE
        emptyView.visibility = android.view.View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = android.view.View.GONE
    }

    private fun showResults(games: List<com.example.gametrends.data.models.Game>) {
        val adapter = GameAdapter(games, { game ->
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("GAME_ID", game.id)
            intent.putExtra("GAME_NAME", game.name)
            startActivity(intent)
        })
        recyclerView.adapter = adapter
        recyclerView.visibility = android.view.View.VISIBLE
        emptyView.visibility = android.view.View.GONE
    }

    private fun showEmptyView(message: String) {
        emptyView.text = message
        emptyView.visibility = android.view.View.VISIBLE
        recyclerView.visibility = android.view.View.GONE
        progressBar.visibility = android.view.View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun applyColorsToElements() {
        val accentColor = ColorManager.getAccentColor(this)
        applyFilterButton.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        toolbar.setTitleTextColor(accentColor)
    }

    override fun onResume() {
        super.onResume()
        applyColorsToElements()
    }
}