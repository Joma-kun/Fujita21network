package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class OspfInterfaceSetting extends ClassElement {

    private String ipAddress;

    private String wildcardMask;

    private int areaId = -1;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    private int priority = -1;

    public int getDeadInterval() {
        return deadInterval;
    }

    public void setDeadInterval(int deadInterval) {
        this.deadInterval = deadInterval;
    }

    public int getHelloInterval() {
        return helloInterval;
    }

    public void setHelloInterval(int helloInterval) {
        this.helloInterval = helloInterval;
    }

    public String getOspfNetworkMode() {
        return ospfNetworkMode;
    }

    public void setOspfNetworkMode(String ospfNetworkMode) {
        this.ospfNetworkMode = ospfNetworkMode;
    }

    public String getStub() {
        return stub;
    }

    public void setStub(String stub) {
        this.stub = stub;
    }

    private int deadInterval = -1;
    private int helloInterval = -1;
    private String ospfNetworkMode ;
    private String stub ;


    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getWildcardMask() {
        return wildcardMask;
    }

    public void setWildcardMask(String wildcardMask) {
        this.wildcardMask = wildcardMask;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public OspfSetting getOspfSetting() {
        return ospfSetting;
    }

    public void setOspfSetting(OspfSetting ospfSetting) {
        if (this.ospfSetting != null) {
            setNodeFalseInstances(this.ospfSetting);
            setNodeFalseInstances(ospfSetting);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + getName() + "]と[" + ospfSetting.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.ospfSetting = ospfSetting;
    }
    @JsonIgnore
    private OspfSetting ospfSetting;


}
