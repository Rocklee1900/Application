package com.example.carbooking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carbooking.cloudinary.UploadImageToCloudinary;
import com.example.carbooking.databinding.ActivityAddCarBinding;
import com.example.carbooking.model.AppCar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.UUID;

public class AddCarActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityAddCarBinding binding;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddCarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Open image picker
        binding.carImageButton.setOnClickListener(v -> openFileChooser());

        // Submit form
        binding.submitButton.setOnClickListener(v -> {
            if (imageUri != null) {
                UploadImageToCloudinary.upload(this, imageUri, new UploadImageToCloudinary.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        addCarToFirebase(imageUrl);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(AddCarActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Launch gallery to pick image
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Set selected image to ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.carImagePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Add car details to Firebase Realtime Database
    private void addCarToFirebase(String imageUrl) {
        String name = binding.carNameEdittext.getText().toString().trim();
        String model = binding.carModelEdittext.getText().toString().trim();
        String price = binding.carPriceEdittext.getText().toString().trim();
        String description = binding.carDescriptionEdittext.getText().toString().trim();

        if (name.isEmpty() || model.isEmpty() || price.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String carId = UUID.randomUUID().toString();
        AppCar car = new AppCar(carId, name, model, price, description, imageUrl);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cars");


        ref.child(carId).setValue(car).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.carImagePreview.setImageResource(R.drawable.dash_border); // reset to placeholder
                binding.carNameEdittext.setText("");
                binding.carModelEdittext.setText("");
                binding.carDescriptionEdittext.setText("");
                binding.carPriceEdittext.setText("");
                Toast.makeText(this, "Car added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add car", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
