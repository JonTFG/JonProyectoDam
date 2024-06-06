package com.example.comprahorro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListsAdapter(
    private val onItemClick: (String, List<HashMap<String, Any>>) -> Unit
) : RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    private val lists = mutableMapOf<String, List<HashMap<String, Any>>>()

    fun updateLists(newLists: Map<String, List<HashMap<String, Any>>>) {
        lists.clear()
        lists.putAll(newLists)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listId = lists.keys.elementAt(position)
        val products = lists[listId] ?: emptyList()
        holder.bind(listId, products)
    }

    override fun getItemCount(): Int = lists.size

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(listId: String, products: List<HashMap<String, Any>>) {
            textView.text = listId
            itemView.setOnClickListener {
                onItemClick(listId, products)
            }
        }
    }
}
