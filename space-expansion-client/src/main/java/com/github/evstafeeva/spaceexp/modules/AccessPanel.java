package com.github.evstafeeva.spaceexp.modules;

import com.github.evstafeeva.spaceexp.transport.BufferedTerminal;
import com.github.evstafeeva.spaceexp.transport.IProtobufTerminal;
import com.github.evstafeeva.spaceexp.transport.ProtobufChannel;
import spex.Protocol;

public class AccessPanel extends BufferedTerminal {
    private ProtobufChannel channel;

    public class LoginStatus {
        LoginStatus(int port) {
            this.success = true;
            this.port    = port;
        }
        LoginStatus(String error) {
            this.success = false;
            this.port    = 0xFFFF;
            this.problem = error;
        }

        public boolean success;
        public int     port;
        public String  problem;
    }

    public void linkToChannel(ProtobufChannel channel) {
        this.channel = channel;
    }

    public LoginStatus login(String login, String password, String localIP, int localPort) {
        if (!sendLoginRequest(login, password, localIP, localPort)) {
            return new LoginStatus("Failed to send request");
        }

        Protocol.Message response = waitMessage(1000);
        if (response == null) {
            return new LoginStatus("Got invalid response!");
        }

        if (response.getChoiceCase() != Protocol.Message.ChoiceCase.ACCESSPANEL) {
            return new LoginStatus("Got non AccessPanel message!");
        }

        Protocol.IAccessPanel accessPanelResponse = response.getAccessPanel();

        switch (accessPanelResponse.getChoiceCase()) {
            case ACCESS_REJECTED:
                return new LoginStatus("Access rejected: " + accessPanelResponse.getAccessRejected());
            case ACCESS_GRANTED:
                return new LoginStatus(accessPanelResponse.getAccessGranted());
            default:
                return new LoginStatus("Got unexpected AccessPanel message!");
        }
    }

    private boolean sendLoginRequest(String login, String password, String localIP, int localPort)
    {
        Protocol.IAccessPanel.LoginRequest request =
                Protocol.IAccessPanel.LoginRequest.newBuilder()
                        .setLogin(login)
                        .setPassword(password)
                        .setIp(localIP)
                        .setPort(localPort)
                        .build();
        return send(Protocol.IAccessPanel.newBuilder().setLogin(request).build());
    }

    private boolean send(Protocol.IAccessPanel body) {
        return channel != null && channel.send(Protocol.Message.newBuilder().setAccessPanel(body).build());
    }

}
