package com.example.myapplication.ui

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.DatabaseHelper
import com.example.myapplication.models.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: ItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var searchView: SearchView

    private var items = mutableListOf<Item>()
    private var currentSearchQuery: String = ""
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)

        setupViews()
        setupRecyclerView()
        setupCategoryFilter()
        loadItems()

        findViewById<FloatingActionButton>(R.id.fabCreate).setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        categorySpinner = findViewById(R.id.categorySpinner)
        searchView = findViewById(R.id.searchView)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                applyFilters()
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(
            items = items,
            onItemClick = { item ->
                // Navigate to detail activity
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("item_id", item.id)
                startActivity(intent)
            },
            onDeleteClick = { item ->
                // Show confirmation dialog before deleting
                AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete") { _, _ ->
                        databaseHelper.deleteItem(item.id)
                        loadItems() // Refresh the list
                        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupCategoryFilter() {
        val categories = listOf("All Categories", "Electronics", "Pets", "Wallets", "Documents", "Other")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentCategory = if (position == 0) null else categories[position]
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                currentCategory = null
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        adapter.filterItems(currentSearchQuery, currentCategory)
    }

    private fun filterItemsByCategory(category: String?) {
        currentCategory = category
        applyFilters()
    }

    private fun loadItems() {
        items.clear()
        items.addAll(databaseHelper.getAllItems())
        adapter.updateItems(items)

        // Re-apply filters to the new data
        applyFilters()
    }

    private fun showDeleteConfirmation(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove this advert?")
            .setPositiveButton("REMOVE") { _, _ ->
                databaseHelper.deleteItem(item.id)
                loadItems()
                Toast.makeText(this, "Advert removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}