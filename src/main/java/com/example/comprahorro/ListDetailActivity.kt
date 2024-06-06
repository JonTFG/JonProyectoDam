package com.example.comprahorro

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListDetailActivity : AppCompatActivity() {

    private lateinit var listNameTextView: TextView
    private lateinit var listItemsRecyclerView: RecyclerView
    private lateinit var listItemsAdapter: ListItemsAdapter
    private var listName: String? = null
    private var listData: Map<String, Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_detail)

        listNameTextView = findViewById(R.id.listNameTextView)
        listItemsRecyclerView = findViewById(R.id.listItemsRecyclerView)

        listName = intent.getStringExtra("listName")
        listData = intent.getSerializableExtra("listData") as? Map<String, Any>

        listNameTextView.text = listName

        listItemsAdapter = ListItemsAdapter(listData ?: emptyMap())
        listItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        listItemsRecyclerView.adapter = listItemsAdapter
    }
}

