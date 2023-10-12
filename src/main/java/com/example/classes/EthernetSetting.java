package com.example.classes;

public class EthernetSetting extends LinkableElement {

	private int slot;

	private int port;

	private String ipAddress;

	private String subnetMask;


	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public int getAccessVlan() {
		return accessVlan;
	}

	public void setAccessVlan(int accessVlan) {
		this.accessVlan = accessVlan;
	}

	public int getNativeVlan() {
		return nativeVlan;
	}

	public void setNativeVlan(int nativeVlan) {
		this.nativeVlan = nativeVlan;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
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

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getDuplex() {
		return duplex;
	}

	public void setDuplex(String duplex) {
		this.duplex = duplex;
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

	public boolean isSwitchportTrunkEncapsulation() {
		return switchportTrunkEncapsulation;
	}

	public void setSwitchportTrunkEncapsulation(boolean switchportTrunkEncapsulation) {
		this.switchportTrunkEncapsulation = switchportTrunkEncapsulation;
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
		}this.config = config;
	}

	private int accessVlan;

	private int nativeVlan;

	private String mode;

	private int accessListNumber;

	private String accessListName;

	private String accessListInOrOut;

	private String speed;

	private String duplex;

	private boolean ipVirtualReassembly;

	private String ipAccessGroup;

	private boolean switchportTrunkEncapsulation;

	private boolean shutdown;

	private Config config;





}
