package com.example.timetableapplication;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.behavior.HideViewOnScrollBehavior;

public class
    RegistrationActivity extends AppCompatActivity {

    EditText etRegisterName, etRegisterMobileNumber, etRegisterEmailId, etRegisterUsername, etRegisterPassword;

    CheckBox cbRegisterShowAndHidePassword;

    Button btnRegisterRegister;
    DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //DATABASE
        DBHelper dpHelper = new DBHelper(this);
        // database cheak


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db != null) {
            Toast.makeText(this, "Database created successfully", Toast.LENGTH_SHORT).show();
        }

        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterMobileNumber = findViewById(R.id.etRegisterMobileNumber);
        etRegisterEmailId = findViewById(R.id.etRegisterEmailId);
        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        cbRegisterShowAndHidePassword = findViewById(R.id.cbRegisterShowAndHidePassword);
        btnRegisterRegister = findViewById(R.id.btnRegisterRegister);

       cbRegisterShowAndHidePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
           if (isChecked)
           {
              etRegisterPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
           } else {
               etRegisterPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
           }
           etRegisterPassword.setSelection(etRegisterPassword.getText().length());
       });
       
       btnRegisterRegister.setOnClickListener(view -> {
           
           if (etRegisterName.getText().toString().isEmpty())
           {
               etRegisterName.setError("Please enter your Name");
           }
           else if (etRegisterEmailId.getText().toString().isEmpty())
           {
               etRegisterEmailId.setError("Please enter your Email id");

           }else if (etRegisterMobileNumber.getText().toString().length()<10)
           {
               etRegisterMobileNumber.setError("Please enter valid mobile number");
           }
           else if (etRegisterPassword.getText().toString().isEmpty())
           {
               etRegisterPassword.setError("Please enter your password");
           }
           else if (!etRegisterPassword.getText().toString().matches(".*[A-Z].*"))
           {
               etRegisterUsername.setError("Password must contain at least one uppercase letter");
           }
           else if (!etRegisterPassword.getText().toString().matches(".*[a-z].*"))
           {
               etRegisterPassword.setError("Password must contain at least one lowercase letter");
           }
           else if (!etRegisterPassword.getText().toString().matches(".*[0-9].*"))
           {
               etRegisterPassword.setError("Password must contain at least one number");
           }
           else if (!etRegisterPassword.getText().toString().matches(".*[@#$%^&+=!].*"))
           {
               etRegisterPassword.setError("Password must contain at least one special character (@#$%^&+=!)");
           }else {
               Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
               Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);

               startActivity(intent);
           }
               }
       );

    }
}

