package com.github.evstafeeva.spaceexp.transport;

public interface IChannel {
    public boolean write(byte[] data);
    public byte[] read();
}
