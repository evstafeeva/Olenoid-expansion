package com.github.evstafeeva.spaceexp.transport;

import com.google.protobuf.InvalidProtocolBufferException;
import spex.Protocol;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProtobufChannel implements IProtobufChannel {

    private IChannel channel;
    private IProtobufTerminal terminal;
    private AtomicBoolean stopFlag = new AtomicBoolean(false);

    @Override
    public boolean send(Protocol.Message message) {
        return channel.write(message.toByteArray());
    }

    public void run() {
        while (!stopFlag.get()) {
            try {
                byte[] data = channel.read();
                Protocol.Message message = Protocol.Message.parseFrom(data);
                synchronized (this.terminal) {
                    terminal.onMessageReceived(message);
                }
            } catch (InvalidProtocolBufferException e) {
                System.out.println("Proval: " + e.getMessage());
                return;
            }
        }
        return;
    }

    public void stop() {
        stopFlag.set(true);
    }

    public void linkToChannel(IChannel channel) {
        this.channel = channel;
    }
    public void linkToTerminal(IProtobufTerminal terminal) {
        if (this.terminal != null) {
            synchronized (this.terminal) {
                this.terminal = terminal;
            }
        } else {
            this.terminal = terminal;
        }
    }
}
