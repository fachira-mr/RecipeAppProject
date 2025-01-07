package com.example.recipeappproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeDetailActivity : AppCompatActivity() {

    lateinit var btnBack : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            finish()
        }

        // Ambil referensi ke TextView
        val txtRecipeName = findViewById<TextView>(R.id.txtRecipeName)
        val txtCategory = findViewById<TextView>(R.id.txtCategory)
        val txtIngredients = findViewById<TextView>(R.id.txtIngredients)
        val txtSteps = findViewById<TextView>(R.id.txtSteps)

        // Ambil data dari Intent
        val recipeId = intent.getStringExtra("id") ?: "Unknown ID"
        val recipeTitle = intent.getStringExtra("title") ?: "Tidak ada judul"
        val recipeCategory = intent.getStringExtra("category") ?: "Tidak ada kategori"
        val recipeIngredients = intent.getStringExtra("bahan") ?: "Tidak ada bahan"
        val recipeSteps = intent.getStringExtra("cara") ?: "Tidak ada cara memasak"

        // Tampilkan data ke TextView
        txtRecipeName.text = recipeTitle
        txtCategory.text = "Kategori: $recipeCategory"
        txtIngredients.text = recipeIngredients
        txtSteps.text = recipeSteps
    }
}