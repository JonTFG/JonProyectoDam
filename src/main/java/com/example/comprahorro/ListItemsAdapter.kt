package com.example.comprahorro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListItemsAdapter(private val listItems: Map<String, Any>) :
    RecyclerView.Adapter<ListItemsAdapter.ListItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val item = listItems.entries.toList()[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = listItems.size

    inner class ListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val listItemTextView: TextView = itemView.findViewById(R.id.expandedListItem)

        fun bind(item: Map.Entry<String, Any>) {
            listItemTextView.text = item.key
        }
    }
}
