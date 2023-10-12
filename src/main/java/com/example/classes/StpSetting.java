package com.example.classes;

import com.example.element.ClassElement;

public class StpSetting extends ClassElement {

	private int bridgePriority;

	private int vlan;

	public int getBridgePriority() {
		return bridgePriority;
	}

	public void setBridgePriority(int bridgePriority) {
		this.bridgePriority = bridgePriority;
	}

	public int getVlan() {
		return vlan;
	}

	public void setVlan(int vlan) {
		this.vlan = vlan;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(int macAddress) {
		this.macAddress = macAddress;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		if(this.config!=null){
			setNodeFalseInstances(this.config);
			setNodeFalseInstances(config);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+getName()+"]と["+config.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}
		this.config = config;
	}

	private int mode;

	private int macAddress;

	private Config config;

}
