package com.example.kotlinshoppingapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentCategory: String

    companion object {
        private const val CATEGORY_SHOES = "Shoes"
        private const val CATEGORY_CLOTHES = "Clothes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        firestore = FirebaseFirestore.getInstance()
        currentCategory = CATEGORY_SHOES // Initial category

        // Set up RecyclerView
        productAdapter = ProductAdapter()
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = productAdapter

        // Fetch and display products
        fetchProducts(currentCategory)

        // Set up category switch buttons
        val btnShoes = findViewById<Button>(R.id.btnShoes)
        val btnClothes = findViewById<Button>(R.id.btnClothes)

        btnShoes.setOnClickListener {
            currentCategory = CATEGORY_SHOES
            fetchProducts(currentCategory)
        }

        btnClothes.setOnClickListener {
            currentCategory = CATEGORY_CLOTHES
            fetchProducts(currentCategory)
        }

    }

    private fun fetchProducts(category: String) {
        // Query products collection based on category
        val query = firestore.collection(category).orderBy("name", Query.Direction.ASCENDING)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            val productList = mutableListOf<Product>()
            for (document in snapshot?.documents ?: emptyList()) {
                val name = document.getString("name") ?: ""
                val description = document.getString("description") ?: ""
                val price = document.get("price")
                val category = document.getString("category") ?: ""
                val image = document.getString("image") ?: ""

                val product = Product(name, description, category, getPriceDouble(price), image)
                productList.add(product)
            }

            // Update the product list in the adapter
            productAdapter.setProducts(productList)
        }
    }

    private fun getPriceDouble(price: Any?): Double {
        return when (price) {
            is Double -> price
            is String -> price.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

}