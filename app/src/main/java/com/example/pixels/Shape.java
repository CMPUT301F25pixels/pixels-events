package com.example.pixels;

public abstract class Shape {
    protected int x;
    protected int y;
    String color = "green";

    public Shape(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
