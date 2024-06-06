package com.example.comprahorro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Favorite(
    val productoId: String = "",
    val nombre: String = "",
    val supermercado: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = ""
)

class FavoritesAdapter(private val onItemClick: (Favorite) -> Unit) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private var favoritesList: List<Favorite> = listOf()

    fun setFavorites(favorites: List<Favorite>) {
        this.favoritesList = favorites
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(itemView, onItemClick)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favoritesList[position]
        holder.bind(favorite)
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    inner class FavoriteViewHolder(itemView: View, private val onItemClick: (Favorite) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val favoriteImageView: ImageView = itemView.findViewById(R.id.favoriteImageView)
        private val favoriteNombreTextView: TextView = itemView.findViewById(R.id.favoriteNombreTextView)
        private val favoriteSupermercadoTextView: TextView = itemView.findViewById(R.id.favoriteSupermercadoTextView)
        private val favoritePrecioTextView: TextView = itemView.findViewById(R.id.favoritePrecioTextView)
        private val likeButton: FloatingActionButton = itemView.findViewById(R.id.likeButton)

        fun bind(favorite: Favorite) {
            favoriteNombreTextView.text = favorite.nombre
            favoriteSupermercadoTextView.text = favorite.supermercado
            favoritePrecioTextView.text = "€${favorite.precio}"
            Glide.with(itemView.context)
                .load(favorite.imagenUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(favoriteImageView)

            itemView.setOnClickListener {
                onItemClick(favorite)
            }

            likeButton.setImageResource(R.drawable.ic_heart_filled)
            likeButton.setOnClickListener {
                removeFavorite(favorite.productoId)
            }
        }

        private fun removeFavorite(productoId: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("Users").document(currentUser.uid)
                    .collection("Favorites").document(productoId)
                    .delete()
                    .addOnSuccessListener {
                        favoritesList = favoritesList.filter { it.productoId != productoId }
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FavoritesAdapter", "Error removing favorite: ", exception)
                    }
            }
        }
    }
}