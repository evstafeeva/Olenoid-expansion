package com.github.evstafeeva.spaceexp.transport;

import spex.Protocol;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedList;

public class BufferedTerminal implements IProtobufTerminal {

    private LinkedList<Protocol.Message> messagesQueue;

    public BufferedTerminal() {
        messagesQueue = new LinkedList<Protocol.Message>();
    }

    public void onMessageReceived(Protocol.Message message) {
        synchronized (messagesQueue) {
            messagesQueue.push(message);
        }
    }

    public Protocol.Message getMessage() {
        synchronized (messagesQueue) {
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

}
