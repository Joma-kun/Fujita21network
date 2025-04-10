package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

public class AccessList extends ClassElement {

    private int accessListNumber = -1;//属性

    private String permitOrDeny;//属性


    public String getAccessListName() {
        return accessListName;
    }

    public void setAccessListName(String accessListName) {
        this.accessListName = accessListName;
    }

    private  String accessListName;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    private int sequenceNumber = -1;
    private String protocol;
    private String sorceIpAddress;
    private String sourceWildcardMask;
    private String sourcePort;
private boolean sourceIsHost;
private boolean destIsHost;

    public boolean getSourceIsHost() {
        return sourceIsHost;
    }

    public void setSourceIsHost(boolean sourceIsHost) {
        this.sourceIsHost = sourceIsHost;
    }

    public boolean getDestIsHost() {
        return destIsHost;
    }

    public void setDestIsHost(boolean destIsHost) {
        this.destIsHost = destIsHost;
    }

    public String getSourceOperator() {
        return sourceOperator;
    }

    public void setSourceOperator(String sourceOperator) {
        this.sourceOperator = sourceOperator;
    }

    public String getDestOperator() {
        return destOperator;
    }

    public void setDestOperator(String destOperator) {
        this.destOperator = destOperator;
    }

    private String sourceOperator;
    private String destOperator;
    private String destIpAddress;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSorceIpAddress() {
        return sorceIpAddress;
    }

    public void setSorceIpAddress(String sorceIpAddress) {
        this.sorceIpAddress = sorceIpAddress;
    }

    public String getSourceWildcardMask() {
        return sourceWildcardMask;
    }

    public void setSourceWildcardMask(String sourceWildcardMask) {
        this.sourceWildcardMask = sourceWildcardMask;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestIpAddress() {
        return destIpAddress;
    }

    public void setDestIpAddress(String destIpAddress) {
        this.destIpAddress = destIpAddress;
    }

    public String getDestWildcardMask() {
        return destWildcardMask;
    }

    public void setDestWildcardMask(String destWildcardMask) {
        this.destWildcardMask = destWildcardMask;
    }

    public String getDestPort() {
        return destPort;
    }

    public void setDestPort(String destPort) {
        this.destPort = destPort;
    }

    private String destWildcardMask;

    private String destPort;

    @JsonIgnore
    private ArrayList<AccessList> accessLists = new ArrayList<>();//関連先のインスタンス
    @JsonIgnore
    private Config config; //関連先のインスタンス


    public String getPermitOrDeny() {
        return permitOrDeny;
    }

    public void setPermitOrDeny(String permitOrDeny) {
        this.permitOrDeny = permitOrDeny;
    }


    public ArrayList<AccessList> getAccessList() {
        return accessLists;
    }

    public void setAccessList(AccessList accessList) {//自己関連のために特別措置
        if (this.accessLists.size() >= 2) {//アクセスリストへの関連が三本あるとき
            for (AccessList access : accessLists) {
                setNodeFalseInstances(access);
            }
            setNodeFalseInstances(accessList);
            setNodeFalseInstances(this);
        }
        for (AccessList access : accessLists) {//同じアクセスリストのクラスに関連があるとき
            if (accessList.equals(access)) {
                setNodeFalseInstances(accessList);
                setNodeFalseInstances(this);
                setmultiplicityErrorStatement("エラー:[" + this.getName() + "]と[" + accessList.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
            }
        }
        this.accessLists.add(accessList);

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

    public int getAccessListNumber() {
        return accessListNumber;
    }

    public void setAccessListNumber(int accessListNumber) {
        this.accessListNumber = accessListNumber;
    }
}
