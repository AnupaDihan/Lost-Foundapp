package com.example.myapplication.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.DatabaseHelper
import com.example.myapplication.models.Item
import com.example.myapplication.utils.ImageUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class PostActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var selectedImageUri: Uri? = null
    private var imagePath: String = ""

    // UI Components
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioLost: RadioButton
    private lateinit var radioFound: RadioButton
    private lateinit var editName: TextInputEditText
    private lateinit var editPhone: TextInputEditText
    private lateinit var editDescription: TextInputEditText
    private lateinit var editDate: TextInputEditText
    private lateinit var editLocation: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var imagePreview: ImageView
    private lateinit var buttonSelectImage: MaterialButton
    private lateinit var buttonSave: MaterialButton

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_image_placeholder)
                .centerCrop()
                .into(imagePreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        databaseHelper = DatabaseHelper(this)

        initializeViews()
        setupToolbar()
        setupDatePicker()
        setupCategorySpinner()
        setupImagePicker()
        setupSaveButton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initializeViews() {
        radioGroupType = findViewById(R.id.radioGroupType)
        radioLost = findViewById(R.id.radioLost)
        radioFound = findViewById(R.id.radioFound)
        editName = findViewById(R.id.editName)
        editPhone = findViewById(R.id.editPhone)
        editDescription = findViewById(R.id.editDescription)
        editDate = findViewById(R.id.editDate)
        editLocation = findViewById(R.id.editLocation)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        imagePreview = findViewById(R.id.imagePreview)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSave = findViewById(R.id.buttonSave)
    }

    private fun setupDatePicker() {
        editDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editDate.setText(date)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Electronics", "Pets", "Wallets", "Documents", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupImagePicker() {
        buttonSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupSaveButton() {
        buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveItem()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = editName.text.toString().trim()
        val phone = editPhone.text.toString().trim()
        val description = editDescription.text.toString().trim()
        val date = editDate.text.toString().trim()
        val location = editLocation.text.toString().trim()

        return when {
            name.isEmpty() -> {
                editName.error = "Please enter name"
                false
            }
            phone.isEmpty() -> {
                editPhone.error = "Please enter phone number"
                false
            }
            description.isEmpty() -> {
                editDescription.error = "Please enter description"
                false
            }
            date.isEmpty() -> {
                editDate.error = "Please select date"
                false
            }
            location.isEmpty() -> {
                editLocation.error = "Please enter location"
                false
            }
            selectedImageUri == null -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveItem() {
        try {
            // Save image to internal storage
            imagePath = ImageUtils.saveImageToInternalStorage(this, selectedImageUri!!)

            if (imagePath.isEmpty()) {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                return
            }

            // Get post type
            val type = if (radioLost.isChecked) "lost" else "found"

            // Get category from spinner
            val category = spinnerCategory.selectedItem.toString()

            val item = Item(
                title = editName.text.toString().trim(),
                description = editDescription.text.toString().trim(),
                category = category,
                type = type,
                location = editLocation.text.toString().trim(),
                contactInfo = editPhone.text.toString().trim(),
                imagePath = imagePath,
                timestamp = System.currentTimeMillis(),
                date = editDate.text.toString().trim(),
                isResolved = false
            )

            databaseHelper.insertItem(item)
            Toast.makeText(this, "Advert created successfully", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving advert: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
