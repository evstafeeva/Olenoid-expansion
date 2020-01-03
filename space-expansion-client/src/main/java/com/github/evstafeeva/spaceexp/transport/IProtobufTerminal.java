package com.github.evstafeeva.spaceexp.transport;

import spex.Protocol;

public interface IProtobufTerminal {

    public void onMessageReceived(Protocol.Message message);

}
