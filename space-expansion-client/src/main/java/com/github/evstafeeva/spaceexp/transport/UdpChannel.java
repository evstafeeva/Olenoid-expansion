package com.github.evstafeeva.spaceexp.transport;

import com.github.evstafeeva.spaceexp.transport.IChannel;

import java.net.*;

public class UdpChannel implements IChannel {
    private final int MTU = 1500; // MTU - maximum transport unit
    private DatagramSocket m_socket;
    private SocketAddress  m_remote;
    private byte[]         m_receiveBuffer;

    public UdpChannel(String localIP, int localPort) {
        m_receiveBuffer = new byte[MTU];
        try {
            m_socket = new DatagramSocket(new InetSocketAddress(localIP, localPort));
        } catch(Exception e) {
            System.out.println("Can't create client socket: " + e.getMessage());
        }
    }

    public void setRemoteAddress(String remoteIP, int remotePort) {
        m_remote = new InetSocketAddress(remoteIP, remotePort);
    }

    public boolean write(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, m_remote);
        try {
            m_socket.send(packet);
        } catch (Exception e) {
            System.out.println("Failed to send: " + e.getMessage());
            return false;
        }
        return true;
    }

    public byte[] read() {
        DatagramPacket packet = new DatagramPacket(m_receiveBuffer, MTU);
        try {
            m_socket.receive(packet);
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
            return null;
        }
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        return data;
    }

}
