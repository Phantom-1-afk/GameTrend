package com.example.gametrends.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gametrends.R
import com.example.gametrends.data.models.Game
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GameAdapter(
    private val games: List<Game>,
    private val onItemClick: (Game) -> Unit,
    private val extraInfoType: String = "none"
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameImage: ImageView = itemView.findViewById(R.id.gameImage)
        private val gameName: TextView = itemView.findViewById(R.id.gameName)
        private val gameRating: TextView = itemView.findViewById(R.id.gameRating)
        private val gameYear: TextView = itemView.findViewById(R.id.gameYear)
        private val gameMetacritic: TextView? = itemView.findViewById(R.id.gameMetacritic)
        private val gameExtraInfo: TextView? = itemView.findViewById(R.id.gameExtraInfo)

        fun bind(game: Game, onItemClick: (Game) -> Unit, extraInfoType: String) {
            gameName.text = game.name
            val isUpcoming = isUpcomingGame(game)

            if (isUpcoming) {
                gameRating.text = formatReleaseDate(game.released)
                gameRating.setTextColor(Color.parseColor("#B3B3B3"))
            } else {
                val rating = game.rating ?: 0.0
                gameRating.text = String.format("⭐ %.1f", rating)
                gameRating.setTextColor(Color.parseColor("#FFD700"))
            }

            if (!game.released.isNullOrEmpty()) {
                try {
                    val date = LocalDate.parse(game.released)
                    gameYear.text = date.year.toString()
                } catch (e: Exception) {
                    gameYear.text = game.released.substring(0, 4)
                }
            } else {
                gameYear.text = "TBA"
            }

            game.metacritic?.let {
                gameMetacritic?.text = "MC: $it"
                gameMetacritic?.visibility = TextView.VISIBLE
                gameMetacritic?.setTextColor(Color.parseColor("#FFD700"))
            } ?: run {
                gameMetacritic?.visibility = TextView.GONE
            }

            if (isUpcoming) {
                game.added?.let {
                    if (it > 0) {
                        gameExtraInfo?.text = "❤️ $it"
                        gameExtraInfo?.visibility = TextView.VISIBLE
                        gameExtraInfo?.setTextColor(Color.parseColor("#FFD700"))
                    } else {
                        gameExtraInfo?.visibility = TextView.GONE
                    }
                }
            } else {
                when (extraInfoType) {
                    "wished" -> {
                        game.added?.let {
                            gameExtraInfo?.text = "❤️ $it"
                            gameExtraInfo?.visibility = TextView.VISIBLE
                            gameExtraInfo?.setTextColor(Color.parseColor("#FFD700"))
                        } ?: run {
                            gameExtraInfo?.visibility = TextView.GONE
                        }
                    }
                    "metacritic" -> {
                        game.metacritic?.let {
                            gameExtraInfo?.text = "MC: $it"
                            gameExtraInfo?.visibility = TextView.VISIBLE
                            gameExtraInfo?.setTextColor(Color.parseColor("#FFD700"))
                        } ?: run {
                            gameExtraInfo?.visibility = TextView.GONE
                        }
                    }
                    else -> {
                        gameExtraInfo?.visibility = TextView.GONE
                    }
                }
            }

            if (!game.backgroundImage.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(game.backgroundImage)
                    .placeholder(R.drawable.game_placeholder)
                    .error(R.drawable.game_placeholder)
                    .into(gameImage)
            } else {
                gameImage.setImageResource(R.drawable.game_placeholder)
            }

            itemView.setOnClickListener { onItemClick(game) }
        }

        private fun isUpcomingGame(game: Game): Boolean {
            if (game.released.isNullOrEmpty()) return false
            return try {
                LocalDate.parse(game.released).isAfter(LocalDate.now())
            } catch (e: Exception) {
                false
            }
        }

        private fun formatReleaseDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "TBA"
            return try {
                val date = LocalDate.parse(dateString)
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                "Релиз: ${date.format(formatter)}"
            } catch (e: Exception) {
                "Релиз: $dateString"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position], onItemClick, extraInfoType)
    }

    override fun getItemCount() = games.size
}