package com.example.kotlinshoppingapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class ProductInsertActivity : AppCompatActivity() {

    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var spProductCategory: Spinner
    private lateinit var etProductPrice: EditText
    private lateinit var btnAddProduct: Button
    private lateinit var btnChooseImage: Button
    private lateinit var ivProductImage: ImageView

    private var filePath: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var firestore: FirebaseFirestore

    private val categories = arrayOf("SELECT", "Shoes", "Clothes")
    private var selectedCategory: String = categories[0]

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_insert)

        etProductName = findViewById(R.id.etProductName)
        etProductDescription = findViewById(R.id.etProductDescription)
        spProductCategory = findViewById(R.id.spProductCategory)
        etProductPrice = findViewById(R.id.etProductPrice)
        btnAddProduct = findViewById(R.id.btnAddProduct)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        ivProductImage = findViewById(R.id.ivProductImage)

        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProductCategory.adapter = adapter


        spProductCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        btnChooseImage.setOnClickListener {
            chooseImage()
        }

        btnAddProduct.setOnClickListener {
            uploadProduct()
        }

    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
            ivProductImage.setImageBitmap(bitmap)
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadProduct() {
        val productName = etProductName.text.toString().trim()
        val productDescription = etProductDescription.text.toString().trim()
        val productPrice = etProductPrice.text.toString().trim()

        if (productName.isNotEmpty() && productDescription.isNotEmpty() && productPrice.isNotEmpty() && filePath != null) {
            val fileReference =
                storageReference.child("product_images/${System.currentTimeMillis()}.${getFileExtension(filePath!!)}")
            val uploadTask: UploadTask = fileReference.putFile(filePath!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val product = hashMapOf(
                        "name" to productName,
                        "description" to productDescription,
                        "category" to selectedCategory,
                        "price" to productPrice,
                        "image" to downloadUri.toString()
                    )

                    firestore.collection(selectedCategory)
                        .add(product)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
//                            finish()
                            val intent = Intent(this@ProductInsertActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill in all fields and choose an image", Toast.LENGTH_SHORT).show()
        }
    }

}