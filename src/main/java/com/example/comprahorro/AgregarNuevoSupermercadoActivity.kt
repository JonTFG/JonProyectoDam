package com.example.comprahorro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class AgregarNuevoSupermercadoActivity : AppCompatActivity() {//Esto es una mejora a futuro

    private lateinit var nombreSupermercadoEditText: EditText
    private lateinit var precioEditText: EditText
    private lateinit var guardarButton: Button
    private var productoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeBasedOnPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_nuevo_supermercado)

        nombreSupermercadoEditText = findViewById(R.id.nombreSupermercadoEditText)
        precioEditText = findViewById(R.id.precioEditText)
        guardarButton = findViewById(R.id.guardarButton)

        productoId = intent.getStringExtra("productoId")

        guardarButton.setOnClickListener {
            val nombreSupermercado = nombreSupermercadoEditText.text.toString().trim()
            val precio = precioEditText.text.toString().trim().toDoubleOrNull()

            if (nombreSupermercado.isNotEmpty() && precio != null) {
                addSupermarketAndPrice(nombreSupermercado, precio)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
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

    private fun addSupermarketAndPrice(nombreSupermercado: String, precio: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Supermercado")
            .whereEqualTo("nombre", nombreSupermercado)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val supermercadoId = documents.documents[0].id

                    val priceData = hashMapOf(
                        "Precio" to precio,
                        "ProductoId" to productoId,
                        "SupermercadoId" to supermercadoId
                    )

                    db.collection("Precio").add(priceData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Supermercado y precio añadidos exitosamente", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            resultIntent.putExtra("supermercadoId", supermercadoId)
                            resultIntent.putExtra("precio", precio)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al añadir precio", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                } else {
                    Toast.makeText(this, "Supermercado no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al buscar supermercado", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }
}




