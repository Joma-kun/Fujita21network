package com.example.classes;

import com.example.element.ClassElement;

import java.util.ArrayList;

public class Config extends ClassElement {
    public Config() {
        vlan = new ArrayList<>();
        ethernetSetting = new ArrayList<>();
        vlanSetting = new ArrayList<>();
        accessList = new ArrayList<>();
        vlanSetting = new ArrayList<>();
        stpSetting = new ArrayList<>();
        linkedConfigs = new ArrayList<>();
    }

    private ArrayList<Config> linkedConfigs;//Linkでつながれたコンフィグを格納しておく。隣接リスト表現 属性になし

    private String deviceModel;

    private ArrayList<Vlan> vlan;

    private Stack stack;
    private ArrayList<AccessList> accessList;

    private ArrayList<IpRoute> ipRoute;

    private ArrayList<OspfSetting> ospfSettings;


    private ArrayList<EthernetSetting> ethernetSetting;

    private Hostname hostname = null;

    private ArrayList<VlanSetting> vlanSetting;

    private OspfSetting ospfSetting;

    private ArrayList<StpSetting> stpSetting;

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public ArrayList<Vlan> getVlan() {
        return vlan;
    }

    public void setVlan(Vlan v) {
        vlan.add(v);
    }

    public ArrayList<EthernetSetting> getEthernetSetting() {
        return ethernetSetting;
    }

    public void setEthernetSetting(EthernetSetting e) {
        ethernetSetting.add(e);
    }

    public Hostname getHostname() {
        return hostname;
    }

    public void setLinkedConfigs(Config linkedConfig) {
        linkedConfigs.add(linkedConfig);
    }

    public ArrayList<Config> getLinkedConfigs() {
        return linkedConfigs;
    }

    public void setHostname(Hostname hostname) {

        if (this.hostname != null) {
            setNodeFalseInstances(this.hostname);
            setNodeFalseInstances(hostname);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + getName() + "]と[" + hostname.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.hostname = hostname;
    }

    public ArrayList<VlanSetting> getVlanSetting() {
        return vlanSetting;
    }

    public void setVlanSetting(VlanSetting v) {
        vlanSetting.add(v);
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

    public ArrayList<StpSetting> getStpSetting() {
        return stpSetting;
    }

    public void setStpSetting(StpSetting s) {
        stpSetting.add(s);
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }

    public ArrayList<AccessList> getAccessList() {
        return accessList;
    }

    public void setAccessList(AccessList a) {
        accessList.add(a);
    }

    public ArrayList<IpRoute> getIpRoute() {
        return ipRoute;
    }

    public void setIpRoute(IpRoute i) {
        ipRoute.add(i);
    }

    public ArrayList<OspfSetting> getOspfSettings() {
        return ospfSettings;
    }

    //Config-eth-link-eth2-anotherconfigとつながれていたらethをかえす
    public void setOspfSettings(OspfSetting o) {
        ospfSettings.add(o);
    }

    public  EthernetSetting getEthernetSettingOne(Config anotherconfig){
        for(EthernetSetting eth : this.getEthernetSetting()){
            if(eth.getConectedThing() instanceof EthernetSetting){
                if(((EthernetSetting) eth.getConectedThing()).getConfig()==anotherconfig){
                    return eth;
                }
            }
        }
        return null;
    }
    //Config-eth-link-eth2-anotherconfigとつながれていたらeth２をかえす
    public  EthernetSetting getanoterEthernetSettingOne(Config anotherconfig){
        for(EthernetSetting eth : this.getEthernetSetting()){
            if(eth.getConectedThing() instanceof EthernetSetting){
                if(((EthernetSetting) eth.getConectedThing()).getConfig()==anotherconfig){
                    return (EthernetSetting) eth.getConectedThing();
                }
            }
        }
        return null;
    }

}
