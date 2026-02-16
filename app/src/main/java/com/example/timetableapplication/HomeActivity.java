package com.example.timetableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

        bottomNavigationView = findViewById(R.id.homeBottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.bottomnavmenuHome);
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

    HomeFragment homeFragment = new HomeFragment();
    ManageFragment manageFragment = new ManageFragment();
    TimetableFragment timetableFragment = new TimetableFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.bottomnavmenuHome) {
            getSupportFragmentManager().beginTransaction().replace(R.id.homeFramelayout, homeFragment).commit();
            return true;
        } else if (menuItem.getItemId() == R.id.bottomnavmenuManage) {
            getSupportFragmentManager().beginTransaction().replace(R.id.homeFramelayout, manageFragment).commit();
            return true;
        } else if (menuItem.getItemId() == R.id.homebottomnavTimetable) {
            getSupportFragmentManager().beginTransaction().replace(R.id.homeFramelayout, timetableFragment).commit();
            return true;
        }
        return false;
    }
}
