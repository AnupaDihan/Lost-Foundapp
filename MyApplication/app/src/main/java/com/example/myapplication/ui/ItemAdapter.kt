package com.example.myapplication.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.myapplication.models.Item
import com.example.myapplication.R


class ItemAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit,
    private val onDeleteClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var filteredItems: List<Item> = items

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val locationText: TextView = itemView.findViewById(R.id.locationText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = filteredItems[position]

        // Set basic information
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
        holder.categoryText.text = "📁 ${item.category}"
        holder.locationText.text = "📍 ${item.location}"

        // Set type with appropriate styling
        when (item.type.lowercase()) {
            "lost" -> {
                holder.typeText.text = "LOST"
                holder.typeText.setBackgroundResource(R.drawable.rounded_background_lost)
                holder.typeText.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            "found" -> {
                holder.typeText.text = "FOUND"
                holder.typeText.setBackgroundResource(R.drawable.rounded_background_found)
                holder.typeText.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
        }

        // Format and display timestamp
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(item.timestamp))
        holder.timestampText.text = "🕐 $formattedDate"

        // Load image safely
        val imagePath = item.imagePath
        val imageSource = if (imagePath.isNotEmpty()) {
            if (imagePath.startsWith("content://")) {
                android.net.Uri.parse(imagePath)
            } else {
                java.io.File(imagePath)
            }
        } else {
            null
        }

        Glide.with(holder.itemView.context)
            .load(imageSource)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .centerCrop()
            .into(holder.itemImage)

        // Set click listeners
        holder.cardView.setOnClickListener {
            onItemClick(item)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = filteredItems.size

    /**
     * Filter items based on search query and category
     * @param query Search text to filter by title, description, or location
     * @param category Category to filter by (null for all categories)
     */
    fun filterItems(query: String, category: String? = null) {
        filteredItems = items.filter { item ->
            // Check if item matches search query
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.location.contains(query, ignoreCase = true)
            }

            // Check if item matches selected category
            val matchesCategory = if (category.isNullOrEmpty() || category == "All Categories") {
                true
            } else {
                item.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
        notifyDataSetChanged()
    }

    /**
     * Update the entire item list
     * @param newItems New list of items to display
     */
    fun updateItems(newItems: List<Item>) {
        this.items = newItems
        this.filteredItems = newItems
        notifyDataSetChanged()
    }

    /**
     * Get the current filtered item at position
     */
    fun getItemAtPosition(position: Int): Item {
        return filteredItems[position]
    }

    /**
     * Get the original unfiltered items list
     */
    fun getOriginalItems(): List<Item> {
        return items
    }
}