package com.example.carbooking.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carbooking.CarDetailsActivity;
import com.example.carbooking.model.AppCar;
import com.example.carbooking.databinding.ItemCarsBinding;

import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private final List<AppCar> cars;
    public CarAdapter(){
        cars = new ArrayList<>();
    }
    @NonNull
    @Override
    public CarAdapter.CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCarsBinding binding = ItemCarsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new CarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CarAdapter.CarViewHolder holder, int position) {
        AppCar car = cars.get(position);
        Glide.with(holder.itemView.getContext())
                .load(car.getImage())
                .centerCrop()
                .into(holder.binding.ivCar);

        holder.binding.tvCarName.setText(car.getName());
        holder.binding.tvCarPrice.setText(car.getPrice());

        // Navigate to DetailExpenseActivity and pass only the expense ID
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CarDetailsActivity.class);
            intent.putExtra("carId", car.getId()); // Pass only the ID
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }
    // allow user to initialize the cars
    public void setCars(List<AppCar> newExpense) {
        cars.clear();
        cars.addAll(newExpense);
        notifyDataSetChanged();
    }

    // allow user to append new cars when receiving new car from real time database of firebase
    public void addCars(List<AppCar> newCar) {
        int startPosition = cars.size();
        cars.addAll(newCar);
        notifyItemRangeInserted(startPosition, newCar.size());
    }

    public class CarViewHolder extends RecyclerView.ViewHolder{
        private ItemCarsBinding binding;
        public CarViewHolder(@NonNull ItemCarsBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
    public boolean containsCarId(String carId) {
        for (AppCar car : cars) {
            if (car.getId().equals(carId)) {
                return true;
            }
        }
        return false;
    }

}
