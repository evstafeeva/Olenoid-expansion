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

        Protocol.INavigation.GetPositionResponse positionResponse = response.getPositionResponse();
        if (positionResponse == null)
            return null;

        return new Position(new Point(positionResponse.getX(), positionResponse.getY()),
                            new Vector(positionResponse.getVx(), positionResponse.getVy()));
    }

    private boolean sendPositionRequest() {
        return sendNavigationMessage(
                Protocol.INavigation.newBuilder()
                        .setPositionRequest(Protocol.INavigation.GetPosition.newBuilder().build())
                        .build()
        );
    }

    private boolean sendNavigationMessage(Protocol.INavigation message) {
        return super.send(Protocol.Message.newBuilder().setNavigation(message).build());
    }

}
