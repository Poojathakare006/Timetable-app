package com.example.timetableapplication;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.timetableapplication.Fragments.HomeFragment;
import com.example.timetableapplication.Fragments.ManageFragment;
import com.example.timetableapplication.Fragments.TimetableFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.homeBottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.bottomnavmenuHome);



        preferences = getSharedPreferences("user_details", MODE_PRIVATE);
        editor = preferences.edit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuMyProfile) {
            Intent intent = new Intent(HomeActivity.this, MyProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menusettings) {
            Toast.makeText(HomeActivity.this,"Settings item click", LENGTH_SHORT).show();
        } else if (id ==R.id.menuSharetimetable) {
            Toast.makeText(HomeActivity.this, "Share Timetable item click", LENGTH_SHORT).show();
        } else if (id == R.id.menuAboutus) {
            Toast.makeText(HomeActivity.this,"About us item Click", LENGTH_SHORT).show();
        } else if (id ==R.id.menuContactus) {
            Toast.makeText(HomeActivity.this, "Contact us item click", LENGTH_SHORT).show();
        } else if (id == R.id.menuLogout) {
            AlertDialog.Builder ad = new AlertDialog.Builder(HomeActivity.this);
            ad.setTitle("Timetable");
            ad.setMessage("Are you sure you want to logout?");

            ad.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            ad.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                 Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                 editor.putBoolean("isLogin",false).apply();
                 startActivity(intent);
                 finish();
                }
            });
            ad.create().show();
        }
        return true;
    }

    HomeFragment homeFragment = new HomeFragment();
    ManageFragment manageFragment = new ManageFragment();

    TimetableFragment timetableFragment = new TimetableFragment();
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.bottomnavmenuHome)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.homeFramelayout,homeFragment).commit();
        }
        else if (menuItem.getItemId() == R.id.bottomnavmenuManage)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.homeFramelayout,manageFragment).commit();
        }
        else if (menuItem.getItemId() == R.id.homebottomnavTimetable)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.homeFramelayout,timetableFragment).commit();
        }
        return true;
    }
}
