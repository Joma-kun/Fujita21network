package com.example.classes;

import com.example.element.ClassElement;

public class VlanSetting extends ClassElement {

	private int vlanNum = -1;

	private String ipAddress;

	private String subnetMask;

	private int accessListNumber = -1;

	private String accessListName;

	private String accessListInOrOut;

	private boolean inNatInside;

	private int ipTcpAdjustMss = -1;

	private boolean ipVirtualReassembly;

	private String ipAccessGroup;

	public int getVlanNum() {
		return vlanNum;
	}

	public void setVlanNum(int vlanNum) {
		this.vlanNum = vlanNum;
	}

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

	public int getAccessListNumber() {
		return accessListNumber;
	}

	public void setAccessListNumber(int accessListNumber) {
		this.accessListNumber = accessListNumber;
	}

	public String getAccessListName() {
		return accessListName;
	}

	public void setAccessListName(String accessListName) {
		this.accessListName = accessListName;
	}

	public String getAccessListInOrOut() {
		return accessListInOrOut;
	}

	public void setAccessListInOrOut(String accessListInOrOut) {
		this.accessListInOrOut = accessListInOrOut;
	}

	public boolean isInNatInside() {
		return inNatInside;
	}

	public void setInNatInside(boolean inNatInside) {
		this.inNatInside = inNatInside;
	}

	public int getIpTcpAdjustMss() {
		return ipTcpAdjustMss;
	}

	public void setIpTcpAdjustMss(int ipTcpAdjustMss) {
		this.ipTcpAdjustMss = ipTcpAdjustMss;
	}

	public boolean isIpVirtualReassembly() {
		return ipVirtualReassembly;
	}

	public void setIpVirtualReassembly(boolean ipVirtualReassembly) {
		this.ipVirtualReassembly = ipVirtualReassembly;
	}

	public String getIpAccessGroup() {
		return ipAccessGroup;
	}

	public void setIpAccessGroup(String ipAccessGroup) {
		this.ipAccessGroup = ipAccessGroup;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
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

	private boolean shutdown;

	private Config config;

}
