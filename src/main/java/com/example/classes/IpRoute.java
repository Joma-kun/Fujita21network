package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class IpRoute extends ClassElement {

    private String ipAddress;

    private String subnetMask;

    private String netHopAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getNetHopAddress() {
        return netHopAddress;
    }

    public void setNetHopAddress(String ipAddress) {
        this.netHopAddress = ipAddress;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        if (this.config != null) {
            setNodeFalseInstances(this.config);
            setNodeFalseInstances(config);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + getName() + "]と[" + config.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.config = config;
    }
    @JsonIgnore
    private Config config;

}
