package com.github.evstafeeva.spaceexp.tacticalcore;

import com.github.evstafeeva.spaceexp.Geometry.Point;
import com.github.evstafeeva.spaceexp.Geometry.Position;
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

    void run() {
//        Position currentPosition = ship.getPosition();
//        while (currentPosition.distanceTo(target) > 100) {
//            engine.setThrust(10, 10);
//        }
    }
}
