package com.example.pz13valovmaximpr_23101mobil;

public class Skin {
    private final String id;
    private final String name;
    private final int drawableRes;
    private final int cost;
    private final float speedMultiplier;

    public Skin(String id, String name, int drawableRes, int cost, float speedMultiplier) {
        this.id = id;
        this.name = name;
        this.drawableRes = drawableRes;
        this.cost = cost;
        this.speedMultiplier = speedMultiplier;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getDrawableRes() { return drawableRes; }
    public int getCost() { return cost; }
    public float getSpeedMultiplier() { return speedMultiplier; }
}