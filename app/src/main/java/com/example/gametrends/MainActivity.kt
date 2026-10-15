package com.example.gametrends

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gametrends.data.models.Game
import com.example.gametrends.data.repository.GameRepository
import com.example.gametrends.ui.adapters.GameAdapter
import kotlinx.coroutines.launch
/*
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var toolbar: Toolbar

    private val repository = GameRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // ТЕСТ: Проверим, видит ли приложение интернет
        println("=================")
        println("ПРОВЕРКА CONSTANTS:")
        println("API_KEY: ${Constants.API_KEY}")
        println("BASE_URL: ${Constants.BASE_URL}")
        println("=================")

        loadTopGames()
    }

    private fun loadTopGames() {
        progressBar.visibility = ProgressBar.VISIBLE
        errorText.visibility = TextView.GONE
        recyclerView.visibility = RecyclerView.GONE

        lifecycleScope.launch {
            try {
                val games = repository.getTopGames()

                // ДОБАВЬ ЭТО для отладки
                println("===== ПОЛУЧЕНО ИГР: ${games.size} =====")
                if (games.isNotEmpty()) {
                    println("Первая игра: ${games[0].name}")
                }

                if (games.isNotEmpty()) {
                    showGames(games)
                } else {
                    showError("Нет данных")
                    println("!!!!! СПИСОК ИГР ПУСТ !!!!!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Ошибка: ${e.message}")
                println("!!!!! ОШИБКА: ${e.message} !!!!!")
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun showGames(games: List<Game>) {
        val adapter = GameAdapter(games)
        recyclerView.adapter = adapter
        recyclerView.visibility = RecyclerView.VISIBLE
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = TextView.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

*/