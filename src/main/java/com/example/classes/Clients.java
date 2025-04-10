package com.example.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Clients extends LinkableElement {

    private String name;

    private String ipAddress;

    private int subnetMask;

    private String defaultGateway;
    @JsonIgnore
    private Link link;


    public String getName2() {
        return name;
    }


    public void setNames(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(int subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(String defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public Link getLink() {
        return link;
    }


    public void setLink(Link link) {
        if (this.link != null) {
            setNodeFalseInstances(this.link);
            setNodeFalseInstances(link);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + this.getName() + "]と[linkのインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.link = link;
    }
}
