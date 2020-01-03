package com.github.evstafeeva.spaceexp.transport;

import spex.Protocol;

public class VirtualChannel implements IProtobufChannel, IProtobufTerminal {
    private IProtobufChannel  downlevel;
    private IProtobufTerminal uplevel;
    private int               tunnelId;

    public VirtualChannel(int tunnelId) {
        this.tunnelId = tunnelId;
    }

    public void linkToChannel(IProtobufChannel downlevel) {
        this.downlevel = downlevel;
    }

    @Override
    public boolean send(Protocol.Message message) {
        return downlevel.send(
                Protocol.Message.newBuilder()
                        .setEncapsulated(message)
                        .setTunnelId(tunnelId)
                        .build());
    }

    @Override
    public void linkToTerminal(IProtobufTerminal terminal) {
        uplevel = terminal;
    }

    @Override
    public void onMessageReceived(Protocol.Message message) {
        if (message.getChoiceCase() != Protocol.Message.ChoiceCase.ENCAPSULATED) {
            // Unexpected message!
            assert null == "Virtual Channel got unexpected message!";
            return;
        }

        assert message.getTunnelId() == tunnelId;
        assert uplevel != null;

        uplevel.onMessageReceived(message.getEncapsulated());
    }
}
