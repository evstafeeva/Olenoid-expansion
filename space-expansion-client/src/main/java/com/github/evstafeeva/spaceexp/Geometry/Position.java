package com.github.evstafeeva.spaceexp.Geometry;

public class Position {
    private Point position = new Point();
    private Vector velocity = new Vector();

    public Position() {}
    public Position(Point position, Vector velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public void change(Point position, Vector velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public void predict(double dt) {
        position.move(velocity, dt);
    }

    public Point getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public String toString() {
        return position.toString() + ", V: " + velocity.toString();
    }
}
