package com.example.timetableapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.timetableapplication.ModelClass.User;

public class MyProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    DBHelper dbHelper;
    SharedPreferences preferences;
    TextView tvName, tvUsername, tvCourse, tvYear, tvCollege, tvTimetablesCreated, tvCourseLabel;
    LinearLayout studentInfoContainer;
    ImageView profileImage;
    Button btnChangePhoto;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_my_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("user_details", MODE_PRIVATE);

        tvName = findViewById(R.id.tv_profile_name);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvCourse = findViewById(R.id.tv_profile_course);
        tvYear = findViewById(R.id.tv_profile_year);
        tvCollege = findViewById(R.id.tv_profile_college);
        tvTimetablesCreated = findViewById(R.id.tv_timetables_created);
        tvCourseLabel = findViewById(R.id.tv_course_label);
        studentInfoContainer = findViewById(R.id.student_info_container);
        profileImage = findViewById(R.id.profile_image);
        btnChangePhoto = findViewById(R.id.btn_change_photo);

        loadProfileData();

        btnChangePhoto.setOnClickListener(v -> checkPermissionAndOpenGallery());
    }

    private void loadProfileData() {
        String username = preferences.getString("username", null);
        if (username != null) {
            User user = dbHelper.getUser(username);
            if (user != null) {
                tvName.setText(user.getName());
                tvUsername.setText("@" + user.getUsername());

                if ("Student".equals(user.getUserType())) {
                    studentInfoContainer.setVisibility(View.VISIBLE);
                    tvCourseLabel.setText("Course");
                    tvCourse.setText(user.getCourse());
                    tvYear.setText(user.getYear());
                    tvCollege.setText(user.getCollegeName());
                } else { // Teacher
                    studentInfoContainer.setVisibility(View.GONE);
                    tvCourseLabel.setText("Subjects Taught");
                    tvCourse.setText("Not specified"); 
                }

                int count = dbHelper.getTimetableCount();
                tvTimetablesCreated.setText(String.valueOf(count));

            }
        }
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            // Here you would typically save the imageUri to the database
        }
    }
}
