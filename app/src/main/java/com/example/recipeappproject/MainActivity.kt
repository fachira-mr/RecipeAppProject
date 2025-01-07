package com.example.recipeappproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var btnTambah: Button
    lateinit var spinner: Spinner
    lateinit var txtEmpty: TextView
    lateinit var listView: ListView
    lateinit var btnLogout: TextView
    lateinit var textView1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTambah = findViewById(R.id.btnTambah)
        spinner = findViewById(R.id.spinner)
        txtEmpty = findViewById(R.id.txtEmpty)
        listView = findViewById(R.id.listView)
        btnLogout = findViewById(R.id.btnLogout)
        textView1 = findViewById(R.id.textView1)

        // Cek apakah user telah login
        val sharedPreferences = getSharedPreferences("app_preference", MODE_PRIVATE)
        val userId = sharedPreferences.getString("id", null)
        if (userId == null) {
            // Jika belum login, kembali ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Ambil nama pengguna dari Firebase
            loadUserName(userId)
        }

        // Setup spinner untuk kategori
        val categories = arrayOf("Semua", "Makanan", "Minuman")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // Tombol tambah resep
        btnTambah.setOnClickListener {
            val intent = Intent(this, CreateRecipeActivity::class.java)
            startActivity(intent)
        }

        // Tombol logout
        btnLogout.setOnClickListener {
            logout()
        }

        // Listener untuk memilih kategori dari spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                loadData(selectedCategory) // Muat data berdasarkan kategori
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                loadData("Semua")
            }
        }

        // Klik item ListView untuk membuka RecipeDetailActivity
        listView.setOnItemClickListener { adapterView, view, position, id ->
            val item = adapterView.getItemAtPosition(position) as FoodItem

            // Kirim data ke RecipeDetailActivity
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("id", item.id)
            intent.putExtra("title", item.title)
            intent.putExtra("category", item.category)
            intent.putExtra("bahan", item.bahan)
            intent.putExtra("cara", item.cara)
            startActivity(intent)
        }

        // Long click pada item ListView untuk opsi Update/Delete
        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val item = adapterView.getItemAtPosition(position) as FoodItem

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apa yang ingin Anda lakukan dengan resep ${item.title}?")
                .setCancelable(false)
                .setPositiveButton("Update") { _, _ ->
                    // Buka CreateRecipeActivity untuk update
                    val intent = Intent(this, CreateRecipeActivity::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("title", item.title)
                    intent.putExtra("category", item.category)
                    intent.putExtra("description", item.description)
                    intent.putExtra("bahan", item.bahan)
                    intent.putExtra("cara", item.cara)
                    startActivity(intent)
                }
                .setNegativeButton("Delete") { _, _ ->
                    // Hapus resep
                    deleteItem(item.id!!)
                    loadData(spinner.selectedItem.toString())
                }
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

            val alert = builder.create()
            alert.show()

            true
        }
    }

    // Fungsi untuk mengambil nama pengguna dari Firebase
    private fun loadUserName(userId: String) {
        val db = Firebase.firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name") ?: "User"
                    textView1.text = "Hai, $userName\nMau Masak Enak apa hari ini?"
                } else {
                    textView1.text = "Hai, User\nMau Masak Enak apa hari ini?"
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Gagal mengambil nama pengguna", exception)
                textView1.text = "Hai, User\nMau Masak Enak apa hari ini?"
            }
    }

    // Fungsi logout
    private fun logout() {
        val sharedPreferences = getSharedPreferences("app_preference", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Fungsi untuk memuat data dari Firebase
    private fun loadData(category: String = "Semua") {
        val db = Firebase.firestore
        val sharedPreferences = getSharedPreferences("app_preference", MODE_PRIVATE)
        val userId = sharedPreferences.getString("id", null)

        if (userId == null) {
            logout()
            return
        }

        db.collection("recipe")
            .whereEqualTo("uid", userId) // Filter berdasarkan user ID
            .get()
            .addOnSuccessListener { result ->
                val items = ArrayList<FoodItem>()

                for (doc in result) {
                    val itemCategory = doc.data["category"].toString()
                    if (category == "Semua" || itemCategory == category) {
                        items.add(
                            FoodItem(
                                doc.id,
                                doc.data["title"].toString(),
                                itemCategory,
                                doc.data["description"].toString(),
                                doc.data["bahan"].toString(),
                                doc.data["cara"].toString()
                            )
                        )
                    }
                }

                // Tampilkan pesan jika list kosong
                if (items.isEmpty()) {
                    txtEmpty.visibility = View.VISIBLE
                    listView.visibility = View.GONE
                } else {
                    txtEmpty.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                }

                // Set adapter ke ListView
                val adapter = FoodAdapter(this, R.layout.item_food, items)
                listView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents.", exception)
            }
    }

    // Fungsi untuk menghapus data dari Firebase
    private fun deleteItem(id: String) {
        val db = Firebase.firestore
        db.collection("recipe").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Resep berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus resep", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk refresh data ketika kembali ke MainActivity
    override fun onResume() {
        super.onResume()
        loadData(spinner.selectedItem.toString()) // Refresh data berdasarkan kategori yang dipilih
    }
}