package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.DatabaseHelper;
import com.example.myapplication.models.Item;
import com.example.myapplication.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "PostActivity";

    private DatabaseHelper databaseHelper;
    private Uri selectedImageUri = null;

    // UI Components
    private RadioGroup radioGroupType;
    private RadioButton radioLost;
    private RadioButton radioFound;
    private TextInputEditText editName;
    private TextInputEditText editPhone;
    private TextInputEditText editDescription;
    private TextInputEditText editDate;
    private TextInputEditText editLocation;
    private Spinner spinnerCategory;
    private ImageView imagePreview;
    private MaterialButton buttonSelectImage;
    private MaterialButton buttonSave;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(imagePreview);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupToolbar();
        setupDatePicker();
        setupCategorySpinner();
        setupImagePicker();
        setupSaveButton();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void initializeViews() {
        radioGroupType = findViewById(R.id.radioGroupType);
        radioLost = findViewById(R.id.radioLost);
        radioFound = findViewById(R.id.radioFound);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        editLocation = findViewById(R.id.editLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imagePreview = findViewById(R.id.imagePreview);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSave = findViewById(R.id.buttonSave);
    }

    private void setupDatePicker() {
        editDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editDate.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupCategorySpinner() {
        String[] categories = {"Electronics", "Pets", "Wallets", "Documents", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupImagePicker() {
        buttonSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void setupSaveButton() {
        buttonSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            if (validateInputs()) {
                saveItem();
            }
        });
    }

    private boolean validateInputs() {
        if (!radioLost.isChecked() && !radioFound.isChecked()) {
            Toast.makeText(this, "Please select Lost or Found", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!isFieldValid(editName, "name")) return false;
        if (!isFieldValid(editPhone, "phone number")) return false;
        if (!isFieldValid(editDescription, "description")) return false;
        if (!isFieldValid(editDate, "date")) return false;
        if (!isFieldValid(editLocation, "location")) return false;

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isFieldValid(TextInputEditText field, String fieldName) {
        if (field.getText() == null || field.getText().toString().trim().isEmpty()) {
            field.setError("Please enter " + fieldName);
            field.requestFocus();
            return false;
        }
        return true;
    }

    private void saveItem() {
        try {
            Log.d(TAG, "Attempting to save item");
            String imagePath = ImageUtils.saveImageToInternalStorage(this, selectedImageUri);

            if (imagePath == null || imagePath.isEmpty()) {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = radioLost.isChecked() ? "lost" : "found";
            String category = spinnerCategory.getSelectedItem().toString();

            Item item = new Item(
                    editName.getText().toString().trim(),
                    editDescription.getText().toString().trim(),
                    category,
                    type,
                    editLocation.getText().toString().trim(),
                    editPhone.getText().toString().trim(),
                    editDate.getText().toString().trim(),
                    imagePath
            );

            long id = databaseHelper.insertItem(item);
            if (id != -1) {
                Log.d(TAG, "Item saved with ID: " + id);
                Toast.makeText(this, "Advert created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Failed to insert item into database");
                Toast.makeText(this, "Error: Could not save to database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while saving item: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
