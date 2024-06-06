package com.example.comprahorro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class SupermarketPriceAdapter(
    private val onClick: (String, String, Double) -> Unit
) : RecyclerView.Adapter<SupermarketPriceAdapter.ViewHolder>() {

    private var prices: List<Precio> = listOf()
    private var productoNombre: String = ""

    fun setPrices(prices: List<Precio>, productoNombre: String) {
        this.prices = prices
        this.productoNombre = productoNombre
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val supermarketImageView: ImageView = view.findViewById(R.id.supermarketImageView)
        val supermarketNameTextView: TextView = view.findViewById(R.id.supermarketNameTextView)
        val priceTextView: TextView = view.findViewById(R.id.priceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_supermarket_price, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val price = prices[position]
        holder.priceTextView.text = price.Precio.toString()

        val db = FirebaseFirestore.getInstance()
        db.collection("Supermercado").document(price.SupermercadoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre")
                    val imagenUrl = document.getString("imagenUrl")
                    holder.supermarketNameTextView.text = nombre
                    Glide.with(holder.itemView.context)
                        .load(imagenUrl)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                        .into(holder.supermarketImageView)

                    holder.itemView.setOnClickListener {
                        onClick(nombre!!, productoNombre, price.Precio)
                    }
                }
            }
    }

    override fun getItemCount(): Int {
        return prices.size
    }
}