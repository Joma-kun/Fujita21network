package com.example.classes;

import com.example.element.ClassElement;

public class OspfVirtualLink extends ClassElement {

	private int areaId;

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public String getRouterId() {
		return routerId;
	}

	public void setRouterId(String routerId) {
		this.routerId = routerId;
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

	private String routerId;

	private OspfSetting ospfSetting;

}
