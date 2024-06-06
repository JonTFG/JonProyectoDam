package com.example.comprahorro

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class ActList : AppCompatActivity() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var listTitles: List<String>
    private lateinit var listDetails: HashMap<String, MutableList<String>>
    private lateinit var listAdapter: CustomExpandableListAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeBasedOnPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_list)

        expandableListView = findViewById(R.id.expandableListView)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bntNavView2)

        listDetails = loadListData()
        listTitles = listDetails.keys.toList()
        listAdapter = CustomExpandableListAdapter(this, listTitles, listDetails)
        expandableListView.setAdapter(listAdapter)

        expandableListView.setOnItemLongClickListener { parent, view, position, id ->
            val packedPosition = expandableListView.getExpandableListPosition(position)
            val itemType = ExpandableListView.getPackedPositionType(packedPosition)
            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                val groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition)
                val childPosition = ExpandableListView.getPackedPositionChild(packedPosition)
                val item = listDetails[listTitles[groupPosition]]!!.removeAt(childPosition)

                AlertDialog.Builder(this)
                    .setTitle("Eliminar elemento")
                    .setMessage("¿Estás seguro de que deseas eliminar $item?")
                    .setPositiveButton("Sí") { dialog, which ->
                        listAdapter.notifyDataSetChanged()
                        saveListData()
                        Toast.makeText(this, "$item eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                val groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition)
                val group = listTitles[groupPosition]

                AlertDialog.Builder(this)
                    .setTitle("Eliminar lista")
                    .setMessage("¿Estás seguro de que deseas eliminar la lista $group?")
                    .setPositiveButton("Sí") { dialog, which ->
                        listDetails.remove(group)
                        listTitles = listDetails.keys.toList()
                        listAdapter.updateList(listTitles, listDetails)
                        saveListData()
                        Toast.makeText(this, "Lista $group eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            } else {
                false
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, ActList::class.java)
                    startActivity(intent)
                    true
                }
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        fetchSupermarkets()
    }

    override fun onResume() {//Actualizar
        super.onResume()
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("needsUpdate", false)) {
            listDetails = loadListData()
            listTitles = listDetails.keys.toList()
            listAdapter.updateList(listTitles, listDetails)

            val editor = sharedPreferences.edit()
            editor.putBoolean("needsUpdate", false)
            editor.apply()
        }
    }

    private fun applyThemeBasedOnPreferences() {//Tema claro/oscuro
        val isDarkMode = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("modeApp", false)
        if (isDarkMode) {
            setTheme(R.style.Theme_CompraHorro_Dark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            setTheme(R.style.Theme_CompraHorro)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun fetchSupermarkets() {
        db.collection("Supermercado")
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val supermarkets = documents.documents.map { it.getString("nombre")!! }
                    for (supermarket in supermarkets) {
                        if (!listDetails.containsKey(supermarket)) {
                            listDetails[supermarket] = mutableListOf()
                        }
                    }
                    listTitles = listDetails.keys.toList()
                    listAdapter.updateList(listTitles, listDetails)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ActList", "Error fetching supermarkets: ", exception)
            }
    }

    private fun saveListData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(listDetails)
        editor.putString("listDetails", json)
        editor.apply()
    }

    private fun loadListData(): HashMap<String, MutableList<String>> {
        val sharedPreferences: SharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("listDetails", null)
        val type = object : TypeToken<HashMap<String, MutableList<String>>>() {}.type

        return try {
            if (json != null) {
                gson.fromJson(json, type)
            } else {
                hashMapOf()
            }
        } catch (e: JsonSyntaxException) {
            hashMapOf()
        }
    }
}