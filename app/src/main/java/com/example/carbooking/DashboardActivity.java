package com.example.carbooking;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cloudinary.android.MediaManager;
import com.example.carbooking.databinding.ActivityDashboardBinding;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map config = new HashMap();
        config.put("cloud_name", "drcicatxm");
        config.put("api_key", "738524273472178");
        config.put("secure", true);
        MediaManager.init(DashboardActivity.this, config);


        // Initialize ViewBinding
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // In your onCreate method
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");

        // Set toolbar as ActionBar
        setSupportActionBar(binding.toolbar);
        if (savedInstanceState == null) {
            loadFragment(new CarFragment());
            binding.navView.setCheckedItem(R.id.nav_car);
        }

        // Handle menu item clicks
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment()); // Load Dashboard Fragment
            } else if (id == R.id.nav_car) {
                loadFragment(new CarFragment());
            }
            binding.drawerLayout.closeDrawers();
            return true;
        });


        // Toggle Sidebar (Hamburger Menu)
        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
                R.string.open_drawer, R.string.close_drawer);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Helper method to load a fragment
    private void loadFragment(@NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
