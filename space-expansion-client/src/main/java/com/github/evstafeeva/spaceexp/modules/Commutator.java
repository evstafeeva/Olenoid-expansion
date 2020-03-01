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
            case TOTAL_SLOTS:
                return response.getTotalSlots();
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
            case MODULE_INFO:
                return new ModuleInfo(
                        nSlotId,
                        response.getModuleInfo().getModuleType(),
                        response.getModuleInfo().getModuleName());
            default:
                // Unexpected response
                return null;
        }
    }

    public VirtualChannel openTunnelTo(String moduleName, String expectedType) {
        int totalSlots = getTotalSlots();
        for (int slotId = 0; slotId < totalSlots; slotId++) {
            ModuleInfo module = getModuleInfo(slotId);
            if (module.getModuleName().equals(moduleName)) {
                assert module.getModuleType().equals(expectedType) : "Unexpected type";
                return openTunnel(module.getSlotId());
            }
        }
        return null;
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
            case OPEN_TUNNEL_REPORT:
                int tunnelId = response.getOpenTunnelReport();
                VirtualChannel tunnel = new VirtualChannel(tunnelId);
                tunnel.linkToChannel(super.getChannel());
                tunnelsMap.put(tunnelId, tunnel);
                return tunnel;
            case OPEN_TUNNEL_FAILED:
                System.out.println("Failed to open tunnel");
                return null;
            default:
                assert false: "Unexpected message: " + response.toString();
                return null;
        }
    }

    private boolean sendGetTotalSlotsRequest() {
        return sendCommutatorMessage(
                Protocol.ICommutator.newBuilder().setTotalSlotsReq(true).build());
    }

    private boolean sendModuleInfoRequest(int slotId) {
        return sendCommutatorMessage(
                Protocol.ICommutator.newBuilder().setModuleInfoReq(slotId).build());
    }

    private boolean sendOpenTunnelRequest(int slotId) {
        return sendCommutatorMessage(
                Protocol.ICommutator.newBuilder().setOpenTunnel(slotId).build());
    }

    protected boolean sendCommutatorMessage(Protocol.ICommutator message) {
        return super.send(Protocol.Message.newBuilder().setCommutator(message).build());
    }

}
