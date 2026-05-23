package com.example.myapplication.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.myapplication.R
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.myapplication.models.DatabaseHelper
import com.example.myapplication.models.Item
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity :  AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var itemId: Long = -1
    private var currentItem: Item? = null

    // UI Components
    private lateinit var statusCard: MaterialCardView
    private lateinit var statusText: TextView
    private lateinit var itemImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var categoryText: TextView
    private lateinit var typeTextDetail: TextView
    private lateinit var locationText: TextView
    private lateinit var dateText: TextView
    private lateinit var contactText: TextView
    private lateinit var buttonContact: MaterialButton
    private lateinit var buttonResolve: MaterialButton
    private lateinit var resolvedMessage: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        databaseHelper = DatabaseHelper(this)

        // Get item ID from intent
        itemId = intent.getLongExtra("item_id", -1)

        if (itemId == -1L) {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        loadItemDetails()
        setupClickListeners()
    }

    private fun initializeViews() {
        statusCard = findViewById(R.id.statusCard)
        statusText = findViewById(R.id.statusText)
        itemImage = findViewById(R.id.itemImage)
        titleText = findViewById(R.id.titleText)
        descriptionText = findViewById(R.id.descriptionText)
        categoryText = findViewById(R.id.categoryText)
        typeTextDetail = findViewById(R.id.typeTextDetail)
        locationText = findViewById(R.id.locationText)
        dateText = findViewById(R.id.dateText)
        contactText = findViewById(R.id.contactText)
        buttonContact = findViewById(R.id.buttonContact)
        buttonResolve = findViewById(R.id.buttonResolve)
        resolvedMessage = findViewById(R.id.resolvedMessage)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadItemDetails() {
        currentItem = databaseHelper.getItemById(itemId)

        if (currentItem == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayItemDetails(currentItem!!)
    }

    private fun displayItemDetails(item: Item) {
        // Set status banner color and text
        if (item.type.equals("lost", ignoreCase = true)) {
            statusText.text = "LOST ITEM - NEEDS ATTENTION"
            statusCard.setCardBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        } else {
            statusText.text = "FOUND ITEM - CLAIM AVAILABLE"
            statusCard.setCardBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
        }

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
        
        Glide.with(this)
            .load(imageSource)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(itemImage)

        // Set text fields
        titleText.text = item.title
        descriptionText.text = item.description
        categoryText.text = item.category
        typeTextDetail.text = item.type.replaceFirstChar { it.uppercase() }
        locationText.text = item.location
        contactText.text = item.contactInfo

        // Format and display timestamp
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        dateText.text = dateFormat.format(Date(item.timestamp))

        // Check if item is resolved
        if (item.isResolved) {
            buttonResolve.isEnabled = false
            buttonResolve.text = "RESOLVED"
            resolvedMessage.visibility = android.view.View.VISIBLE
        } else {
            buttonResolve.isEnabled = true
            buttonResolve.text = "MARK RESOLVED"
            resolvedMessage.visibility = android.view.View.GONE
        }
    }

    private fun setupClickListeners() {
        buttonContact.setOnClickListener {
            showContactOptions()
        }

        buttonResolve.setOnClickListener {
            showResolveConfirmationDialog()
        }

        // Make image clickable to view full screen
        itemImage.setOnClickListener {
            showFullScreenImage()
        }
    }

    private fun showContactOptions() {
        val item = currentItem ?: return

        val options = arrayOf("Send Email", "Make Phone Call", "Copy Contact Info")

        AlertDialog.Builder(this)
            .setTitle("Contact ${item.type} owner")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sendEmail(item.contactInfo)
                    1 -> makePhoneCall(item.contactInfo)
                    2 -> copyToClipboard(item.contactInfo)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendEmail(contactInfo: String) {
        // Try to extract email from contact info
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        val emailMatch = emailRegex.find(contactInfo)

        val email = emailMatch?.value ?: contactInfo

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Regarding your lost/found item")
            putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI'm interested in the item you posted on Lost and Found App.\n\nBest regards,")
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall(contactInfo: String) {
        // Try to extract phone number (simple pattern)
        val phoneRegex = Regex("\\+?[0-9\\s\\-()]{10,}")
        val phoneMatch = phoneRegex.find(contactInfo)

        val phoneNumber = phoneMatch?.value?.trim() ?: contactInfo

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Contact Info", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Contact info copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun showResolveConfirmationDialog() {
        val item = currentItem ?: return

        val message = if (item.type.equals("lost", ignoreCase = true)) {
            "Has this lost item been returned to its owner?"
        } else {
            "Has this found item been claimed by its owner?"
        }

        AlertDialog.Builder(this)
            .setTitle("Mark as Resolved")
            .setMessage(message)
            .setPositiveButton("Yes, Mark Resolved") { _, _ ->
                resolveItem()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resolveItem() {
        val item = currentItem ?: return

        // Update the item status in database
        val rowsUpdated = databaseHelper.updateItemResolved(item.id, true)

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Item marked as resolved", Toast.LENGTH_SHORT).show()
            loadItemDetails() // Refresh the UI
        } else {
            Toast.makeText(this, "Error resolving item", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFullScreenImage() {
        val item = currentItem ?: return

        // Create a simple dialog to show full screen image
        val imageView = ImageView(this)
        imageView.layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

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

        Glide.with(this)
            .load(imageSource)
            .into(imageView)

        AlertDialog.Builder(this)
            .setView(imageView)
            .setPositiveButton("Close", null)
            .show()
    }
}