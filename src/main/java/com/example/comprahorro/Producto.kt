package com.example.comprahorro

data class Producto(
    val productoId: Int,
    val nombre: String,
    val descripcion: String,
    val codigoB: String
) {
    override fun toString(): String {
        return "Producto($productoId, $nombre, $descripcion, $codigoB)"
    }
}