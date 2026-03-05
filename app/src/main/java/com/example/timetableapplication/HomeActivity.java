package com.example.timetableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.timetableapplication.Fragments.HomeFragment;
import com.example.timetableapplication.Fragments.ManageFragment;
import com.example.timetableapplication.Fragments.TimetableFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        // Set the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
         if (itemId == R.id.menu_profile) {
            startActivity(new Intent(HomeActivity.this, MyProfileActivity.class));
            return true;
        } else if (itemId == R.id.menu_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.menu_about_us) {
            startActivity(new Intent(HomeActivity.this, AboutUsActivity.class));
            return true;
        } else if (itemId == R.id.menu_contact_us) {
            startActivity(new Intent(HomeActivity.this, ContactUsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_timetable) {
            selectedFragment = new TimetableFragment();
        } else if (itemId == R.id.nav_manage) {
            selectedFragment = new ManageFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
        
        return false;
    }
}
