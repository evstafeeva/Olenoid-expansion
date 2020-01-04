package com.github.evstafeeva.spaceexp.Geometry;

public class Vector {
    private double  x = 0;
    private double  y = 0;
    private double  length = 0;
    private boolean isLengthCorrect = true;

    public Vector() {}
    public Vector(double x, double y) {
        set(x, y);
    }
    public Vector(Point from, Point to) {
        x = to.getX() - from.getX();
        y = to.getY() - from.getY();
        isLengthCorrect = false;
    }
    public Vector(Vector other) {
        x = other.x;
        y = other.y;
        length = other.length;
        isLengthCorrect = other.isLengthCorrect;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
        isLengthCorrect = false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getLength() {
        if (!isLengthCorrect) {
            length = x * x + y * y;
            length = Math.sqrt(length);
            isLengthCorrect = true;
        }
        return length;
    }

    public Vector mult(double k) {
        Vector result = new Vector(this);
        return result.multSelf(k);
    }

    public Vector multSelf(double k) {
        x *= k;
        y *= k;
        if (isLengthCorrect) {
            length *= k;
        }
        return this;
    }

    public String toString() {
        return "{" + x + ", " + y + "}";
    }
}
