package com.example.comprahorro



import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private var photoUri: Uri? = null
    private lateinit var currentPhotoPath: String

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_SELECT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeBasedOnPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        storage = FirebaseStorage.getInstance()

        val selectPhotoButton: Button = findViewById(R.id.btnSeleccionarFoto)
        selectPhotoButton.setOnClickListener {
            seleccionarImagen()
        }

        val saveProductButton: Button = findViewById(R.id.btnGuardarProducto)
        saveProductButton.setOnClickListener {
            guardarProducto()
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_SELECT) {
            photoUri = data?.data
            val imageView: ImageView = findViewById(R.id.ivProducto)
            imageView.setImageURI(photoUri)
            imageView.visibility = View.VISIBLE
        }
    }

    private fun guardarProducto() {
        val nombreEditText: EditText = findViewById(R.id.etNombreProducto)
        val descripcionEditText: EditText = findViewById(R.id.etDescripcionProducto)
        val supermercadoEditText: EditText = findViewById(R.id.etSupermercado)
        val precioEditText: EditText = findViewById(R.id.etPrecio)

        val codigoBarras = intent.getStringExtra("codigoBarras")
        val nombre = nombreEditText.text.toString().trim()
        val descripcion = descripcionEditText.text.toString().trim()
        val supermercado = supermercadoEditText.text.toString().trim()
        val precio = precioEditText.text.toString().trim().toDoubleOrNull()

        if (codigoBarras != null && nombre.isNotEmpty() && descripcion.isNotEmpty() && supermercado.isNotEmpty() && precio != null) {
            if (photoUri != null) {
                subirFotoYGuardarProducto(codigoBarras, nombre, descripcion, supermercado, precio)
            } else {
                agregarProducto(codigoBarras, nombre, descripcion, supermercado, precio, null)
            }
        } else {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirFotoYGuardarProducto(codigoBarras: String, nombre: String, descripcion: String, supermercado: String, precio: Double) {
        val storageRef = storage.reference.child("product_images/${UUID.randomUUID()}.jpg")
        storageRef.putFile(photoUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    agregarProducto(codigoBarras, nombre, descripcion, supermercado, precio, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun agregarProducto(codigoBarras: String, nombre: String, descripcion: String, supermercado: String, precio: Double, imageUrl: String?) {
        val db = FirebaseFirestore.getInstance()
        val producto = hashMapOf(
            "codigoB" to codigoBarras,
            "nombre" to nombre,
            "descripcion" to descripcion
        )
        if (imageUrl != null) {
            producto["imagenUrl"] = imageUrl
        }

        // Agregar el producto a la colección Producto
        db.collection("Producto").document(codigoBarras).set(producto)
            .addOnSuccessListener {
                // Consultar la colección Supermercado para obtener el ID del documento con el nombre especificado
                db.collection("Supermercado")
                    .whereEqualTo("nombre", supermercado)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null && !documents.isEmpty) {
                            val supermercadoId = documents.documents[0].id

                            // Ahora agrega la información de precio a la colección Precio
                            val precioData = hashMapOf(
                                "Precio" to precio,
                                "ProductoId" to codigoBarras,
                                "SupermercadoId" to supermercadoId
                            )

                            db.collection("Precio").add(precioData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Producto y precio guardados exitosamente", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar el precio", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                        } else {
                            Toast.makeText(this, "Supermercado no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al buscar el supermercado", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el producto", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
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
}