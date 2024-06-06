package com.example.comprahorro

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class ProductoDetalleActivity : AppCompatActivity() {

    private lateinit var productoImageView: ImageView
    private lateinit var productoNombreTextView: TextView
    private lateinit var supermercadoNombreTextView: TextView
    private lateinit var precioTextView: TextView
    private lateinit var likeButton: FloatingActionButton
    private lateinit var supermarketPricesRecyclerView: RecyclerView
    private lateinit var supermarketPriceAdapter: SupermarketPriceAdapter
    private var isLiked: Boolean = false
    private var db = FirebaseFirestore.getInstance()

    private var productoId: String? = null
    private var nombre: String? = null
    private var descripcion: String? = null
    private var precio: Double? = null
    private var supermercado: String? = null
    private var imagenUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeBasedOnPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_detalle)
        db = FirebaseFirestore.getInstance()
        productoImageView = findViewById(R.id.productoImageView)
        productoNombreTextView = findViewById(R.id.productoNombreTextView)
        supermercadoNombreTextView = findViewById(R.id.supermercadoNombreTextView)
        precioTextView = findViewById(R.id.precioTextView)
        likeButton = findViewById(R.id.likeButton)
        supermarketPricesRecyclerView = findViewById(R.id.supermarketPricesRecyclerView)

        productoId = intent.getStringExtra("productoId")
        nombre = intent.getStringExtra("nombre")
        descripcion = intent.getStringExtra("descripcion")
        precio = intent.getDoubleExtra("precio", 0.0)
        supermercado = intent.getStringExtra("supermercado")
        imagenUrl = intent.getStringExtra("imagenUrl")
        Glide.with(this).load(imagenUrl).into(productoImageView)
        productoNombreTextView.text = nombre
        supermercadoNombreTextView.text = supermercado
        precioTextView.text = getString(R.string.precio_format, precio)
        Glide.with(this)
            .load(imagenUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(productoImageView)
        supermarketPriceAdapter = SupermarketPriceAdapter { supermercado, nombre, precio ->
            addToSupermarketList(supermercado, nombre, precio)
        }
        supermarketPricesRecyclerView.layoutManager = LinearLayoutManager(this)
        supermarketPricesRecyclerView.adapter = supermarketPriceAdapter

        likeButton.setOnClickListener {
            isLiked = if (isLiked) {
                if (productoId != null) {
                    removeFromFavorites(productoId!!)
                }
                likeButton.setImageResource(R.drawable.ic_heart_outline)
                false
            } else {
                if (productoId != null) {
                    addToFavorites(productoId!!, nombre, supermercado, precio, imagenUrl)
                }
                likeButton.setImageResource(R.drawable.ic_heart_filled)
                true
            }
        }

        checkIfLiked(productoId)
        fetchSupermarketPrices(productoId, nombre!!)
    }
    private fun applyThemeBasedOnPreferences() {
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
    private fun fetchSupermarketPrices(productoId: String?, productoNombre: String) {
        db.collection("Precio")
            .whereEqualTo("ProductoId", productoId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val prices = documents.toObjects(Precio::class.java)
                    supermarketPriceAdapter.setPrices(prices, productoNombre)
                } else {
                    Toast.makeText(this, "No se encontraron precios", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProductoDetalleActivity", "Error obteniendo precios: ", exception)
            }
    }


    private fun getSupermarketDetails(supermarketId: String, callback: (String, String) -> Unit) {
        db.collection("Supermercado").document(supermarketId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("nombre") ?: "Desconocido"
                    val imageUrl = document.getString("imagenUrl") ?: ""
                    callback(name, imageUrl)
                } else {
                    callback("Desconocido", "")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProductoDetalleActivity", "Error obteniendo detalles del supermercado: ", e)
                callback("Desconocido", "")
            }
    }
    private fun addToSupermarketList(supermercado: String?, nombre: String?, precio: Double?) {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("listDetails", null)
        val type = object : TypeToken<HashMap<String, MutableList<String>>>() {}.type
        val listDetails: HashMap<String, MutableList<String>> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            hashMapOf()
        }

        val productList = listDetails[supermercado] ?: mutableListOf()
        productList.add(nombre!!)
        listDetails[supermercado!!] = productList

        val editor = sharedPreferences.edit()
        val newJson = gson.toJson(listDetails)
        editor.putString("listDetails", newJson)
        editor.putBoolean("needsUpdate", true)
        editor.apply()

        Toast.makeText(this, "$nombre añadido a la lista de $supermercado", Toast.LENGTH_SHORT).show()
    }

    private fun checkIfLiked(productoId: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && productoId != null) {
            db.collection("Users").document(currentUser.uid)
                .collection("Favorites").document(productoId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        isLiked = true
                        likeButton.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        isLiked = false
                        likeButton.setImageResource(R.drawable.ic_heart_outline)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProductoDetalleActivity", "Error checking favorite: ", exception)
                }
        }
    }

    private fun addToFavorites(
        productoId: String,
        nombre: String?,
        supermercado: String?,
        precio: Double?,
        imagenUrl: String?
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val favorite = hashMapOf(
                "productoId" to productoId,
                "nombre" to nombre,
                "supermercado" to supermercado,
                "precio" to precio,
                "imagenUrl" to imagenUrl
            )
            db.collection("Users").document(currentUser.uid)
                .collection("Favorites").document(productoId).set(favorite)
                .addOnSuccessListener {
                    Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ProductoDetalleActivity", "Error adding to favorites", e)
                }
        }
    }

    private fun removeFromFavorites(productoId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid)
                .collection("Favorites").document(productoId).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ProductoDetalleActivity", "Error removing from favorites", e)
                }
        }
    }
}