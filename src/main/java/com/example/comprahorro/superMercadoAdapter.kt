package com.example.comprahorro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class SupermercadoAdapter(private val supermercados: List<Supermercado>) : RecyclerView.Adapter<SupermercadoAdapter.SupermercadoViewHolder>() {

    class SupermercadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(supermercado: Supermercado) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupermercadoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_supermercado, parent, false)
        return SupermercadoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupermercadoViewHolder, position: Int) {
        holder.bind(supermercados[position])
    }

    override fun getItemCount(): Int {
        return supermercados.size
    }
}