package com.example.timetableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MyProfileActivity extends AppCompatActivity {

    ImageView ivMyProfilePhoto;

    Button btnSelectPhoto, btnMyProfileDelete, btnViewHistory;

    TextView tvMyProfileName, tvMyProfileMobileNO, tvMyProfileEmail, tvMyProfileUsername;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ivMyProfilePhoto.setImageURI(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        ivMyProfilePhoto = findViewById(R.id.ivMyProfilePhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnMyProfileDelete = findViewById(R.id.btnMyProfileDelete);
        tvMyProfileName = findViewById(R.id.tvMyProfileName);
        tvMyProfileMobileNO = findViewById(R.id.tvMyProfileMobileNo);
        tvMyProfileEmail = findViewById(R.id.tvMyProfileEmail);
        tvMyProfileUsername = findViewById(R.id.tvMyProfileUsername);

        loadUserProfile();

        btnSelectPhoto.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            pickImageLauncher.launch(gallery);
        });

        btnMyProfileDelete.setOnClickListener(v -> deleteAccount());

    }

    private void loadUserProfile() {
    }

    private void deleteAccount() {
        // Implement your account deletion logic here
    }


}
