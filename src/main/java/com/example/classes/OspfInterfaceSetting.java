package com.example.classes;

import com.example.element.ClassElement;

public class OspfInterfaceSetting extends ClassElement {

	private String ipAddress;

	private String wildcardMask;

	private int areaId;

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
		if(this.ospfSetting!=null){
			setNodeFalseInstances(this.ospfSetting);
			setNodeFalseInstances(ospfSetting);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+getName()+"]と["+ospfSetting.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}this.ospfSetting = ospfSetting;
	}

	private OspfSetting ospfSetting;



}
