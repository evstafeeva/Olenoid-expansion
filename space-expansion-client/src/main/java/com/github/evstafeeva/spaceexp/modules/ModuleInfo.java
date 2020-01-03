package com.github.evstafeeva.spaceexp.modules;

public class ModuleInfo {

    private int    nSlotId     = 0;
    private String sModuleType = "";
    private String sModuleName = "";

    ModuleInfo(int slotId, String sModuleType, String sModuleName) {
        this.nSlotId     = slotId;
        this.sModuleType = sModuleType;
        this.sModuleName = sModuleName;
    }

    public String toString() {
        return sModuleType + " '" + sModuleName + "' in slot #" + nSlotId;
    }

    public int getSlotId() {
        return nSlotId;
    }

    public String getModuleType() {
        return sModuleType;
    }

    public String getModuleName() {
        return sModuleName;
    }

}
