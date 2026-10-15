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

class SmallGameAdapter(
    private val games: List<Game>,
    private val onItemClick: (Game) -> Unit,
    private val type: String
) : RecyclerView.Adapter<SmallGameAdapter.SmallViewHolder>() {

    class SmallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameImage: ImageView = itemView.findViewById(R.id.gameImage)
        private val gameName: TextView = itemView.findViewById(R.id.gameName)
        private val gameExtraInfo: TextView = itemView.findViewById(R.id.gameExtraInfo)

        fun bind(game: Game, onItemClick: (Game) -> Unit, type: String) {
            gameName.text = game.name
            gameName.textSize = 12f

            when (type) {
                "popular" -> {
                    game.added?.let {
                        gameExtraInfo.text = "❤️ $it"
                        gameExtraInfo.visibility = TextView.VISIBLE
                        gameExtraInfo.setTextColor(Color.parseColor("#FFD700"))
                    } ?: run {
                        gameExtraInfo.visibility = TextView.GONE
                    }
                }
                "rated" -> {
                    game.rating?.let {
                        gameExtraInfo.text = String.format("⭐ %.1f", it)
                        gameExtraInfo.visibility = TextView.VISIBLE
                        gameExtraInfo.setTextColor(Color.parseColor("#FFD700"))
                    } ?: run {
                        gameExtraInfo.visibility = TextView.GONE
                    }
                }
                "metacritic" -> {
                    game.metacritic?.let {
                        gameExtraInfo.text = "MC: $it"
                        gameExtraInfo.visibility = TextView.VISIBLE
                        gameExtraInfo.setTextColor(Color.parseColor("#FFD700"))
                    } ?: run {
                        gameExtraInfo.visibility = TextView.GONE
                    }
                }
                "least" -> {
                    game.rating?.let {
                        gameExtraInfo.text = String.format("⭐ %.1f", it)
                        gameExtraInfo.visibility = TextView.VISIBLE
                        gameExtraInfo.setTextColor(Color.parseColor("#FFD700"))
                    } ?: run {
                        gameExtraInfo.visibility = TextView.GONE
                    }
                }
                else -> gameExtraInfo.visibility = TextView.GONE
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_small, parent, false)
        return SmallViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmallViewHolder, position: Int) {
        holder.bind(games[position], onItemClick, type)
    }

    override fun getItemCount() = games.size
}