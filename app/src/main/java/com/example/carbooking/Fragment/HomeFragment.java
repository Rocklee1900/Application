package com.example.carbooking.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.carbooking.Adapter.CarAdapter;
import com.example.carbooking.Model.AppCar;
import com.example.carbooking.R;
import com.example.carbooking.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    private int currentPage = 1;
    private boolean isLoading = false;
    private static final int PRE_LOAD_ITEMS = 1;
    private FirebaseAuth mAuth;
    private CarAdapter carAdapter;



    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rcListCars.setLayoutManager(layoutManager);
        carAdapter = new CarAdapter();
        binding.rcListCars.setAdapter(carAdapter);
        mAuth = FirebaseAuth.getInstance();

        binding.rcListCars.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + PRE_LOAD_ITEMS)) {
                    loadTasks(false);
                }
            }
        });
        binding.rcListCars.setAdapter(carAdapter);
        return binding.getRoot();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentPage = 1;
        loadTasks(true);
    }

    private void loadTasks(boolean reset)  {
        isLoading = true;
        showProgressBar();
        DatabaseReference carRef = FirebaseDatabase.getInstance().getReference("cars");

        carRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AppCar> carList = new ArrayList<>();
                for (DataSnapshot carSnap : snapshot.getChildren()) {
                    AppCar car = carSnap.getValue(AppCar.class);
                    if (car != null) {
                        carList.add(car);
                    }
                }

                if (reset) {
                    carAdapter.setCars(carList);  // Replace data
                } else {
                    List<AppCar> uniqueCars = new ArrayList<>();
                    for (AppCar car : carList) {
                        if (!carAdapter.containsCarId(car.getId())) {
                            uniqueCars.add(car);
                        }
                    }
                    carAdapter.addCars(uniqueCars); // Only append new unique cars
                }

                hideProgressBar();
                isLoading = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load cars: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressBar();
                isLoading = false;
            }
        });
    }

    private void showProgressBar() {
        binding.carsProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.carsProgressBar.setVisibility(View.GONE);
    }
}