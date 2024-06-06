package com.example.comprahorro

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bntEsc: Button
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private val db = FirebaseFirestore.getInstance()
    private var favoritesListenerRegistration: ListenerRegistration? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeBasedOnPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val userId = currentUser.uid
        bottomNavigationView = findViewById(R.id.bntNavView2)
        bntEsc = findViewById(R.id.bntEsc)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)

        setupRecyclerView()
        setupFavoritesListener()

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.profile -> {
                    val intent = Intent(this, ActList::class.java)
                    startActivity(intent)
                }
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        bntEsc.setOnClickListener {
            try {
                initScanner()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error en initScanner: ${e.message}")
                Toast.makeText(this, "Error al inicializar el scanner", Toast.LENGTH_SHORT).show()
            }
            loadFavorites()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        favoritesListenerRegistration?.remove()
    }

    private fun loadFavorites() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid)
                .collection("Favorites")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val favorites = documents.toObjects(Favorite::class.java)
                        favoritesAdapter.setFavorites(favorites)
                    } else {
                        Toast.makeText(this, "No hay productos favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MainActivity", "Error getting favorites: ", exception)
                }
        }
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter { favorite ->
            openProductoDetalleActivity(favorite)
        }
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter
    }

    private fun setupFavoritesListener() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            favoritesListenerRegistration = db.collection("Users").document(currentUser.uid)
                .collection("Favorites")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("MainActivity", "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val favorites = snapshots.toObjects(Favorite::class.java)
                        favoritesAdapter.setFavorites(favorites)
                    } else {
                        favoritesAdapter.setFavorites(emptyList())
                    }
                }
        }
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

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Escanea un código de barras para continuar")

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val flashEnabled = pref.getBoolean("flash", false)

        integrator.setTorchEnabled(flashEnabled)
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            } else {
                val codigoBarras = result.contents
                fetchProductoData(codigoBarras)
            }
        } else {
            Toast.makeText(this, "Error al escanear", Toast.LENGTH_SHORT).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fetchProductoData(codigoBarras: String) {
        db.collection("Producto")
            .whereEqualTo("codigoB", codigoBarras)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val producto = documents.documents[0]
                    val productoId = producto.id
                    val nombre = producto.getString("nombre") ?: "Nombre no disponible"
                    val descripcion = producto.getString("descripcion") ?: "Descripción no disponible"
                    val imagenUrl = producto.getString("imagenUrl") ?: ""

                    fetchPrecioData(productoId, nombre, descripcion, imagenUrl)
                } else {
                    showAddProductDialog(codigoBarras)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error obteniendo el producto: ", exception)
            }
    }

    private fun showAddProductDialog(codigoBarras: String) {
        AlertDialog.Builder(this)
            .setTitle("Producto no encontrado")
            .setMessage("¿Deseas añadir este producto manualmente?")
            .setPositiveButton("Sí") { dialog, which ->
                val intent = Intent(this, AgregarProductoActivity::class.java).apply {
                    putExtra("codigoBarras", codigoBarras)
                }
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun fetchPrecioData(productoId: String, nombre: String, descripcion: String, imagenUrl: String) {
        db.collection("Precio")
            .whereEqualTo("ProductoId", productoId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val precios = documents.toObjects(Precio::class.java)
                    val precioMinimo = precios.minOf { it.Precio }
                    val supermercadoId = precios.first { it.Precio == precioMinimo }.SupermercadoId

                    fetchSupermercadoData(supermercadoId, productoId, nombre, descripcion, precioMinimo, imagenUrl)
                } else {
                    Toast.makeText(this, "Precios no encontrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error getting prices: ", exception)
            }
    }

    private fun fetchSupermercadoData(supermercadoId: String, productoId: String, nombre: String, descripcion: String, precioMinimo: Double, imagenUrl: String) {
        db.collection("Supermercado")
            .document(supermercadoId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val supermercadoNombre = document.getString("nombre") ?: "Supermercado no disponible"

                    val intent = Intent(this, ProductoDetalleActivity::class.java).apply {
                        putExtra("productoId", productoId)
                        putExtra("nombre", nombre)
                        putExtra("descripcion", descripcion)
                        putExtra("precio", precioMinimo)
                        putExtra("supermercado", supermercadoNombre)
                        putExtra("imagenUrl", imagenUrl)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Supermercado no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error getting supermarket: ", exception)
            }
    }

    private fun openProductoDetalleActivity(favorite: Favorite) {
        val intent = Intent(this, ProductoDetalleActivity::class.java).apply {
            putExtra("productoId", favorite.productoId)
            putExtra("nombre", favorite.nombre)
            putExtra("descripcion", "Descripción no disponible")
            putExtra("precio", favorite.precio)
            putExtra("supermercado", favorite.supermercado)
            putExtra("imagenUrl", favorite.imagenUrl)
        }
        startActivity(intent)
    }
}