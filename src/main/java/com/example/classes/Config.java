package com.example.classes;

import com.example.element.ClassElement;

import java.util.ArrayList;

public class Config extends ClassElement {
	public Config(){
		vlan = new ArrayList<>();
		ethernetSetting = new ArrayList<>();
		vlanSetting = new ArrayList<>();
		accessList = new ArrayList<>();
		vlanSetting = new ArrayList<>();
		stpSetting = new ArrayList<>();
		linkedConfigs=new ArrayList<>();
	}
	private ArrayList<Config> linkedConfigs;//Linkでつながれたコンフィグを格納しておく。隣接リスト表現

	public void setLinkedConfigs(Config linkedConfig) {
		linkedConfigs.add(linkedConfig);
	}

	public ArrayList<Config> getLinkedConfigs(){
		return linkedConfigs;
		}
	private String deviceModel;

	private ArrayList<Vlan> vlan ;

	private ArrayList<EthernetSetting> ethernetSetting ;

	private Hostname hostname = null;

	private ArrayList<VlanSetting> vlanSetting ;

	private OspfSetting ospfSetting;

	private ArrayList<StpSetting> stpSetting ;

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

	public void setHostname(Hostname hostname) {

		if(this.hostname!=null){
			setNodeFalseInstances(this.hostname);
			setNodeFalseInstances(hostname);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+getName()+"]と["+hostname.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}this.hostname = hostname;
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

		if(this.ospfSetting!=null){
			setNodeFalseInstances(this.ospfSetting);
			setNodeFalseInstances(ospfSetting);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+getName()+"]と["+ospfSetting.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}
		this.ospfSetting = ospfSetting;
	}

	public ArrayList<StpSetting> getStpSetting() {
		return stpSetting;
	}

	public void setStpSetting(StpSetting s) {
		stpSetting.add(s);
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

	public void setOspfSettings(OspfSetting o) {
		ospfSettings.add(o);
	}

	private ArrayList<AccessList> accessList ;

	private ArrayList<IpRoute> ipRoute ;

	private ArrayList<OspfSetting> ospfSettings;

//	@Override
//	public void setLink(IInstancespecification_model instance) {
//		if(instance instanceof Vlan){
//			vlan.add((Vlan) instance);
//		}
//		if (instance instanceof EthernetSetting){
//			ethernetSetting.add((EthernetSetting) instance);
//		}
//		if(instance instanceof Hostname){
//			this.hostname = (Hostname) instance;
//		}
//		if(instance instanceof VlanSetting){
//			vlanSetting.add((VlanSetting) instance);
//		}
//		if(instance instanceof OspfSetting){
//			ospfSettings.add((OspfSetting) instance);
//			this.ospfSetting = (OspfSetting) instance;
//		}
//		if(instance instanceof StpSetting){
//			stpSetting.add((StpSetting) instance);
//		}
//		if(instance instanceof AccessList){
//			accessList.add((AccessList) instance);
//		}
//		if(instance instanceof IpRoute){
//			ipRoute.add((IpRoute) instance);
//		}
//
//	}
}
