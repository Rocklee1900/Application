package com.example.carbooking;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carbooking.adapter.CarAdapter;
import com.example.carbooking.databinding.FragmentCarBinding;
import com.example.carbooking.model.Car;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CarFragment extends Fragment {

    private FragmentCarBinding binding;
    private RecyclerView recyclerView;
    private List<Car> carList;
    private CarAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarBinding.inflate(inflater, container, false);

        recyclerView = binding.recyclerViewCars;
        carList = new ArrayList<>();
        adapter = new CarAdapter(carList, (car, position) -> showEditDialog(car, position));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadCars();

        binding.addCarButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddCarActivity.class);
            startActivity(intent);
        });

        enableSwipeActions();

        return binding.getRoot();
    }

    private void loadCars() {
        DatabaseReference carsRef = FirebaseDatabase.getInstance().getReference("cars");
        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                carList.clear();
                for (DataSnapshot carSnap : snapshot.getChildren()) {
                    Car car = carSnap.getValue(Car.class);
                    if (car != null) {
                        car.setId(carSnap.getKey());
                        carList.add(car);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void enableSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Car car = carList.get(position);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Car")
                        .setMessage("Are you sure you want to delete this car?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteCar(car);
                            carList.remove(position);
                            adapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("No", (dialog, which) -> adapter.notifyItemChanged(position))
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                if (dX < 0) { // Swiping to the left
                    // Draw red background
                    paint.setColor(Color.parseColor("#F44336")); // Red
                    RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    c.drawRect(background, paint);

                    // Draw delete icon
                    Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.delete); // Make sure this icon exists
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = iconRight - icon.getIntrinsicWidth();

                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }


    private void deleteCar(Car car) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cars");
        ref.child(car.getId()).removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Car deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showEditDialog(Car car, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_car, null);
        builder.setView(dialogView);

        EditText modelEdit = dialogView.findViewById(R.id.editCarModel);
        EditText descEdit = dialogView.findViewById(R.id.editCarDescription);
        EditText priceEdit = dialogView.findViewById(R.id.editCarPrice);

        modelEdit.setText(car.getModel());
        descEdit.setText(car.getDescription());
        priceEdit.setText(car.getPrice());

        builder.setTitle("Edit Car")
                .setPositiveButton("Update", (dialog, which) -> {
                    car.setModel(modelEdit.getText().toString());
                    car.setDescription(descEdit.getText().toString());
                    car.setPrice(priceEdit.getText().toString());

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cars");
                    ref.child(car.getId()).setValue(car)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Car updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(position))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}