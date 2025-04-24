package com.example.carbooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.carbooking.Fragment.ExploreFragment;
import com.example.carbooking.Fragment.FavoriteFragment;
import com.example.carbooking.Fragment.HomeFragment;
import com.example.carbooking.Fragment.ProfileFragment;
import com.example.carbooking.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {
    FirebaseAuth auth;
    ActivityHomeBinding binding;

    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        auth = FirebaseAuth.getInstance();

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.bottomNav.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                LoadFragment(new HomeFragment());
            } else if (itemId == R.id.nav_explore) {
                LoadFragment(new ExploreFragment());
            } else if (itemId == R.id.nav_favorite) {
                LoadFragment(new FavoriteFragment());
            } else if (itemId == R.id.nav_profile){
                LoadFragment(new ProfileFragment());
            }
            else {
                return false; // Return false if no valid ID is matched
            }
            return true;
        });
        // To ensure the selected bottom navigation item is always visible and stay where it was when activity recreated
        if(savedInstanceState == null){
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }else{
            binding.bottomNav.setSelectedItemId(savedInstanceState.getInt("selectedItemId"));
        }
    }

    private void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fragmentContainer,fragment)
                .commit();
    }
}
