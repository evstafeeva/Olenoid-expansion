package com.github.evstafeeva.spaceexp.transport;

import spex.Protocol;

import java.util.LinkedList;

public class BufferedTerminal implements IProtobufTerminal {
    private IProtobufChannel channel;
    private LinkedList<Protocol.Message> messagesQueue;

    public BufferedTerminal() {
        messagesQueue = new LinkedList<Protocol.Message>();
    }

    public void linkToChannel(IProtobufChannel channel) {
        this.channel = channel;
    }

    protected IProtobufChannel getChannel() { return channel; }

    protected boolean send(Protocol.Message message) {
        return channel != null && channel.send(message);
    }

    public void onMessageReceived(Protocol.Message message) {
        synchronized (messagesQueue) {
            messagesQueue.push(message);
        }
    }

    public Protocol.Message getMessage() {
        synchronized (messagesQueue) {
            if (messagesQueue.isEmpty())
                return null;
            return messagesQueue.pop();
        }
    }

    public Protocol.Message waitMessage(int timeoutMs) {
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeoutMs) {
            synchronized (messagesQueue) {
                if (!messagesQueue.isEmpty())
                    return messagesQueue.pop();
            }
            try {
                Thread.sleep(1);
            } catch (Exception exception) {}
        }
        return null;
    }

    public Protocol.ICommutator waitCommutatorMessage(int timeoutMs) {
        Protocol.Message message = waitMessage(timeoutMs);
        if (message == null)
            return null;
        if (message.getChoiceCase() != Protocol.Message.ChoiceCase.COMMUTATOR)
            return null;
        return message.getCommutator();
    }

    public Protocol.INavigation waitNavigationMessage(int timeoutMs) {
        Protocol.Message message = waitMessage(timeoutMs);
        if (message == null)
            return null;
        if (message.getChoiceCase() != Protocol.Message.ChoiceCase.NAVIGATION)
            return null;
        return message.getNavigation();
    }

}
