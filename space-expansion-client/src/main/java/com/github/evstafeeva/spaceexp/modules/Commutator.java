package com.github.evstafeeva.spaceexp.modules;

import com.github.evstafeeva.spaceexp.transport.BufferedTerminal;
import com.github.evstafeeva.spaceexp.transport.VirtualChannel;
import spex.Protocol;

import java.util.HashMap;

public class Commutator extends BufferedTerminal {
    private HashMap<Integer, VirtualChannel> tunnelsMap = new HashMap<Integer, VirtualChannel>();

    public void onMessageReceived(Protocol.Message message) {
        // необходимо переопределить эту функцию (исходная релизация есть в BufferedTerminal) так, чтобы
        // все инкапсулированные сообщения не попадали в очередь, а сразу прокидывались в соответствующий
        // туннель. Все остальные сообщения обрабатываются как обычно.
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

        Protocol.ICommutator response = waitCommutatorMessage(1000);
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

        Protocol.ICommutator response = waitCommutatorMessage(1000);
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

        Protocol.ICommutator response = waitCommutatorMessage(1000);
        if (response == null) {
            return null;
        }

        switch (response.getChoiceCase()) {
            case OPENTUNNELSUCCESS:
                int tunnelId = response.getOpenTunnelSuccess().getNTunnelId();
                VirtualChannel tunnel = new VirtualChannel(tunnelId);
                tunnel.linkToChannel(super.getChannel());
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

    private boolean sendGetTotalSlotsRequest() {
        return sendCommutatorMessage(
                Protocol.ICommutator.newBuilder().setGetTotalSlots(
                        Protocol.ICommutator.GetTotalSlots.newBuilder().build()
                ).build());
    }

    private boolean sendModuleInfoRequest(int slotId) {
        return sendCommutatorMessage(
                Protocol.ICommutator.newBuilder().setGetModuleInfo(
                        Protocol.ICommutator.GetModuleInfo.newBuilder().setNSlotId(slotId).build()
                ).build());
    }

    private boolean sendOpenTunnelRequest(int slotId) {
        return sendCommutatorMessage(
                Protocol.ICommutator.newBuilder().setOpenTunnel(
                        Protocol.ICommutator.OpenTunnel.newBuilder().setNSlotId(slotId).build()
                ).build());
    }

    protected boolean sendCommutatorMessage(Protocol.ICommutator message) {
        return super.send(Protocol.Message.newBuilder().setCommutator(message).build());
    }

}
