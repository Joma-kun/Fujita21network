package com.example.classes;

import com.example.element.ClassElement;

public class IpRoute extends ClassElement
{

	private String network;

	private String addressPrefix;

	private String netHopAddress;

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getAddressPrefix() {
		return addressPrefix;
	}

	public void setAddressPrefix(String addressPrefix) {
		this.addressPrefix = addressPrefix;
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
		if(this.config!=null){
			setNodeFalseInstances(this.config);
			setNodeFalseInstances(config);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+getName()+"]と["+config.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}
		this.config = config;
	}

	private Config config;

}
