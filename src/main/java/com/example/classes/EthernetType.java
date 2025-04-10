package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class EthernetType extends ClassElement {
    private boolean Ethernet;
    private boolean fastEthernet;

    public EthernetSetting getEthernetSetting() {
        return ethernetSetting;
    }

    public String getType() {
        if (Ethernet) {
            return "Ethernet";
        } else if (fastEthernet) {
            return "fastEthernet";
        } else if (tengigabitEthernet) {
            return "10gigabitEthernet";
        } else if (gigabitEthernet) {
            return "gigabitEthernet";
        } else {
            return "";
        }
    }

    public void setEthernetSetting(EthernetSetting ethernetSetting) {
        if (this.ethernetSetting != null) {
            setNodeFalseInstances(this.ethernetSetting);
            setNodeFalseInstances(ethernetSetting);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + getName() + "]と[" + ethernetSetting.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.ethernetSetting = ethernetSetting;
    }
    @JsonIgnore
    private EthernetSetting ethernetSetting;
    private boolean gigabitEthernet;

    private boolean tengigabitEthernet;//10じゃダメだった

    public boolean isEthernet() {
        return Ethernet;
    }

    public void setEthernet(boolean ethernet) {
        Ethernet = ethernet;
    }

    public boolean isFastEthernet() {
        return fastEthernet;
    }

    public void setFastEthernet(boolean fastEthernet) {
        this.fastEthernet = fastEthernet;
    }

    public boolean isGigabitEthernet() {
        return gigabitEthernet;
    }

    public void setGigabitEthernet(boolean gigabitEthernet) {
        this.gigabitEthernet = gigabitEthernet;
    }

    public boolean isTengigabitEthernet() {
        return tengigabitEthernet;
    }

    public void setTengigabitEthernet(boolean tengigabitEthernet) {
        this.tengigabitEthernet = tengigabitEthernet;
    }
}
