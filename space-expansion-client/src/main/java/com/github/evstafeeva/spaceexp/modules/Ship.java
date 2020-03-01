package com.github.evstafeeva.spaceexp.modules;

import com.github.evstafeeva.spaceexp.Geometry.Point;
import com.github.evstafeeva.spaceexp.Geometry.Position;
import com.github.evstafeeva.spaceexp.Geometry.Vector;
import spex.Protocol;

public class Ship extends Commutator {

    public Position getPosition() {
        if (!sendPositionRequest()) {
            return null;
        }

        Protocol.INavigation response = waitNavigationMessage(1000);
        if (response == null) {
            return null;
        }

        Protocol.Position position = response.getPosition();
        if (position == null)
            return null;

        return new Position(new Point(position.getX(), position.getY()),
                            new Vector(position.getVx(), position.getVy()));
    }

    private boolean sendPositionRequest() {
        return sendNavigationMessage(
                Protocol.INavigation.newBuilder().setPositionReq(true).build()
        );
    }

    private boolean sendNavigationMessage(Protocol.INavigation message) {
        return super.send(Protocol.Message.newBuilder().setNavigation(message).build());
    }

}
