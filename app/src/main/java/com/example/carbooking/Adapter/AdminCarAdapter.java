package com.example.carbooking.adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import com.example.carbooking.model.AppCar;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.carbooking.R;
import com.example.carbooking.databinding.AdminCarItemsBinding;

import java.util.List;

public class AdminCarAdapter extends RecyclerView.Adapter<AdminCarAdapter.CarViewHolder> {

    public interface OnCarActionListener {
        void onEdit(AppCar car, int position);
    }

    private List<AppCar> carList;
    private OnCarActionListener listener;

    public AdminCarAdapter(List<AppCar> carList, OnCarActionListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminCarItemsBinding binding = AdminCarItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        AppCar car = carList.get(position);

        // Load car image with placeholder and error fallback
        Glide.with(holder.itemView.getContext())
                .load(car.getImage())
                .into(holder.binding.carImage);

        holder.binding.carModel.setText(car.getModel());
        holder.binding.carPrice.setText(car.getPrice());
        holder.binding.carDescription.setText(car.getDescription());

        Log.d("CarAdapter", "Model: " + car.getModel());
        Log.d("CarAdapter", "Price: " + car.getPrice());
        Log.d("CarAdapter", "Image: " + car.getImage());

        // Dot icon click -> edit action
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(car, holder.getAdapterPosition());
            }
        });

//        // Delete icon click -> delete action (if you have one)
//        holder.deleteButton.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onDelete(car, holder.getAdapterPosition());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateCarList(List<AppCar> newList) {
        this.carList = newList;
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        private final AdminCarItemsBinding binding;
        public ImageButton editButton;
        public ImageButton deleteButton;

        public CarViewHolder(@NonNull AdminCarItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            editButton = itemView.findViewById(R.id.dot); // Rename in layout if needed
        }
    }
}
