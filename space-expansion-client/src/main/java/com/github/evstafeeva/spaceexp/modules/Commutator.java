package com.github.evstafeeva.spaceexp.modules;

import com.github.evstafeeva.spaceexp.transport.BufferedTerminal;
import com.github.evstafeeva.spaceexp.transport.IProtobufChannel;
import com.github.evstafeeva.spaceexp.transport.VirtualChannel;
import spex.Protocol;

import java.util.HashMap;

public class Commutator extends BufferedTerminal {
    private IProtobufChannel channel;
    private HashMap<Integer, VirtualChannel> tunnelsMap = new HashMap<Integer, VirtualChannel>();

    public void linkToChannel(IProtobufChannel channel) {
        this.channel = channel;
    }

    public void onMessageReceived(Protocol.Message message) {
        if (message.getChoiceCase() == Protocol.Message.ChoiceCase.ENCAPSULATED) {
            int tunnelId = message.getTunnelId();
            VirtualChannel tunnel = tunnelsMap.get(tunnelId);
            if (tunnel != null)
                tunnel.onMessageReceived(message);
        } else {
            super.onMessageReceived(message);
        }
    }

    public int getTotalSlots() {
        if (!sendGetTotalSlotsRequest()) {
            return 0;
        }

        Protocol.ICommutator response = waitCommutatorMessage();
        if (response == null) {
            return 0;
        }

        switch (response.getChoiceCase()) {
            case TOTALSLOTSRESPONSE:
                return response.getTotalSlotsResponse().getNTotalSlots();
            default:
                // Unexpected response
                return 0;
        }
    }

    public ModuleInfo getModuleInfo(int nSlotId) {
        if (!sendModuleInfoRequest(nSlotId)) {
            return null;
        }

        Protocol.ICommutator response = waitCommutatorMessage();
        if (response == null) {
            return null;
        }

        switch (response.getChoiceCase()) {
            case MODULEINFO:
                return new ModuleInfo(
                        nSlotId,
                        response.getModuleInfo().getSModuleType(),
                        response.getModuleInfo().getSModuleName());
            default:
                // Unexpected response
                return null;
        }
    }

    public VirtualChannel openTunnel(int slotId) {
        if (!sendOpenTunnelRequest(slotId)) {
            return null;
        }

        Protocol.ICommutator response = waitCommutatorMessage();
        if (response == null) {
            return null;
        }

        switch (response.getChoiceCase()) {
            case OPENTUNNELSUCCESS:
                int tunnelId = response.getOpenTunnelSuccess().getNTunnelId();
                VirtualChannel tunnel = new VirtualChannel(tunnelId);
                tunnel.linkToChannel(channel);
                tunnelsMap.put(tunnelId, tunnel);
                return tunnel;
            case OPENTUNNELFAILED:
                System.out.println("Failed to open tunnel");
                return null;
            default:
                assert false: "Unexpected message: " + response.toString();
                return null;
        }
    }

    private Protocol.ICommutator waitCommutatorMessage() {
        if (channel == null)
            return null;

        while(true) {
            Protocol.Message message = waitMessage(1000);
            if (message == null)
                return null;

            switch (message.getChoiceCase()) {
                case COMMUTATOR:
                    // Наконец-то получили сообщение для данного коммутатора, его и вернём
                    return message.getCommutator();
                case ENCAPSULATED:
                    // Получили инкапсулированное сообщение для виртуального канала
                    // Потом добавим его обработку - будем класть в очередь соответствующего виртуального канала
                    break;
                default:
                    // Неожиданное сообщение
                    break;
            }
        }
    }

    private boolean sendGetTotalSlotsRequest() {
        return sendMessage(
                Protocol.ICommutator.newBuilder().setGetTotalSlots(
                        Protocol.ICommutator.GetTotalSlots.newBuilder().build()
                ).build());
    }

    private boolean sendModuleInfoRequest(int slotId) {
        return sendMessage(
                Protocol.ICommutator.newBuilder().setGetModuleInfo(
                        Protocol.ICommutator.GetModuleInfo.newBuilder().setNSlotId(slotId).build()
                ).build());
    }

    private boolean sendOpenTunnelRequest(int slotId) {
        return sendMessage(
                Protocol.ICommutator.newBuilder().setOpenTunnel(
                        Protocol.ICommutator.OpenTunnel.newBuilder().setNSlotId(slotId).build()
                ).build());
    }

    private boolean sendMessage(Protocol.ICommutator message) {
        return channel != null && channel.send(Protocol.Message.newBuilder().setCommutator(message).build());
    }

}
