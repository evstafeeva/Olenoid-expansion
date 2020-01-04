package com.github.evstafeeva.spaceexp.Geometry;

public class Point {
    private double x = 0;
    private double y = 0;

    public Point() { }
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(Vector vector) {
        x += vector.getX();
        y += vector.getY();
    }

    public void move(Vector vector, double k) {
        x += vector.getX() * k;
        y += vector.getY() * k;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
