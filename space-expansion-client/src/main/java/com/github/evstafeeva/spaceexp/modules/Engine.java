package com.github.evstafeeva.spaceexp.modules;

import com.github.evstafeeva.spaceexp.Geometry.Vector;
import com.github.evstafeeva.spaceexp.transport.IProtobufChannel;
import com.github.evstafeeva.spaceexp.transport.IProtobufTerminal;
import spex.Protocol;

public class Engine implements IProtobufTerminal {

    private IProtobufChannel channel;

    public Vector getThrust() {
        return null;
    }

    public void linkToChannel(IProtobufChannel channel) {
        this.channel = channel;
    }

    public boolean setThrust(double x, double y, int duration) {
        return setThrust(new Vector(x, y), duration);
    }

    public boolean setThrust(Vector thrust, int duration) {
        System.out.println("setThrust(" + thrust + ", " + duration + ")");
        return sendEngineMessage(
                Protocol.IEngine.newBuilder().setChangeThrust(
                        Protocol.IEngine.ChangeThrust.newBuilder()
                                .setX(thrust.getX())
                                .setY(thrust.getY())
                                .setThrust((int)thrust.getLength())
                                .setDurationMs(duration)
                                .build())
                        .build()
        );
    }

    public void onMessageReceived(Protocol.Message message) {

    }

    private boolean sendEngineMessage(Protocol.IEngine message) {
        return channel.send(Protocol.Message.newBuilder().setEngine(message).build());
    }
}
