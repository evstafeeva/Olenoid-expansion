package com.github.evstafeeva.spaceexp.tacticalcore;

import com.github.evstafeeva.spaceexp.Geometry.Point;
import com.github.evstafeeva.spaceexp.Geometry.Position;
import com.github.evstafeeva.spaceexp.Geometry.Vector;
import com.github.evstafeeva.spaceexp.modules.Engine;
import com.github.evstafeeva.spaceexp.modules.Ship;

public class MoveProcedure {

    private Ship ship;
    private Engine engine;
    private Point target;

    public MoveProcedure(Ship ship, Engine engine, Point target) {
        this.ship = ship;
        this.engine = engine;
        this.target = target;
    }

    public void run() {
        double maxThrust = 500;
        double weight = 10;
        double distance = 0;

        do {
            Position currentPosition = ship.getPosition();
            distance = currentPosition.distanceTo(target);

            System.out.println(currentPosition.toString());

            double bestSpeed = Math.sqrt(2 * distance * maxThrust / weight);
            Vector bestVelocity = currentPosition.vectorTo(target);
            bestVelocity.setLength(bestSpeed);

            Vector dV = bestVelocity.subtract(currentPosition.getVelocity());

            engine.setThrust(new Vector(dV.getX(), dV.getY(), maxThrust), (int) (1000*dV.getLength()*weight/maxThrust)+1);

            try {
                Thread.sleep(50);
            } catch(Exception exception) {}

        } while (distance > 2);
        stop();
    }

    public void stop(){

        Position currentPosition = ship.getPosition();
        double maxThrust = 500;
        double weight = 10;
        double speed = currentPosition.getVelocity().getLength();
        engine.setThrust(new Vector(-currentPosition.getVelocity().getX(), -currentPosition.getVelocity().getY(), maxThrust),
                (int)(1000*speed*weight*2/maxThrust));
    }
}
