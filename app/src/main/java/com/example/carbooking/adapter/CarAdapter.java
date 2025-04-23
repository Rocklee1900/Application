package com.example.carbooking.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carbooking.R;
import com.example.carbooking.databinding.CarItemsBinding;
import com.example.carbooking.model.Car;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnCarActionListener {
        void onEdit(Car car, int position);
    }

    private List<Car> carList;
    private OnCarActionListener listener;

    public CarAdapter(List<Car> carList, OnCarActionListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CarItemsBinding binding = CarItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(car.getImage())
                .into(holder.binding.carImage);

        holder.binding.carModel.setText(car.getModel());
        holder.binding.carPrice.setText(car.getPrice());
        holder.binding.carDescription.setText(car.getDescription());

        Log.d("CarAdapter", "Model: " + car.getModel());
        Log.d("CarAdapter", "Price: " + car.getPrice());
        Log.d("CarAdapter", "Image: " + car.getImage());

        // Dot icon click -> edit dialog
        holder.dot.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(car, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        private final CarItemsBinding binding;
        public ImageButton dot;

        public CarViewHolder(@NonNull CarItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            dot = itemView.findViewById(R.id.dot);
        }
    }
}
