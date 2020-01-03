package com.github.evstafeeva.spaceexp;
import java.io.*;
import java.net.*;

public class EchoUdpServer {

    private DatagramSocket m_socket;

    public EchoUdpServer(int port) {
        try {
            m_socket = new DatagramSocket(port);
        } catch (Exception e) {
            System.out.println("Failed to create socket!");
        }
    }

    public void run() {
        final int MTU = 1500;   // MTU - maximum transport unit

        byte[] body = new byte[MTU];
        DatagramPacket packet = new DatagramPacket(body, MTU);
        while(true) {
            try {
                m_socket.receive(packet);
                System.out.println("Got " + packet.getLength() + " byte message from " +
                                    packet.getAddress().toString() + ": \"" + packet.toString() + "\"");
                m_socket.send(packet);
            } catch (Exception e) {
                System.out.println("Something went wrong: " + e.getMessage());
                return;
            }
        }
    }

}
