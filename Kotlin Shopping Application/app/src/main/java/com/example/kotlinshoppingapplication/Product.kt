package com.example.kotlinshoppingapplication

data class Product(
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Any? = null,
    val image: String = ""
) {
    fun getPriceDouble(): Double {
        return when (price) {
            is Double -> price
            is String -> price.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
