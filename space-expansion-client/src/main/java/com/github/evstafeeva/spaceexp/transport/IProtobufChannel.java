package com.github.evstafeeva.spaceexp.transport;

import spex.Protocol;

public interface IProtobufChannel{
    public boolean send(Protocol.Message message);

    public void linkToTerminal(IProtobufTerminal terminal);
}
