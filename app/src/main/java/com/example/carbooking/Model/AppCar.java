package com.example.carbooking.Model;

public class AppCar {
    String id;

    private String name;
    private String model;
    private String price; // match Firebase key
    private String description;
    private String image; // match Firebase key

    public AppCar() {}

    public AppCar(String id,String name, String model, String price, String description, String image) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.price = price;
        this.description = description;
        this.image = image;
    }

    public String getId() { return id;}

    public String getName() { return name; }
    public String getModel() { return model; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
}
