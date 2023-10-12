package com.example.classes;

import com.example.element.ClassElement;

import java.util.ArrayList;

public class AccessList  extends ClassElement {

	private int accessListNumber;//属性

	private String permitOrDeny;//属性

	private String accessListInfo;//属性

	private ArrayList<AccessList> accessLists = new ArrayList<>();//関連先のインスタンス

	private Config config; //関連先のインスタンス





	public String getPermitOrDeny() {
		return permitOrDeny;
	}

	public void setPermitOrDeny(String permitOrDeny) {
		this.permitOrDeny = permitOrDeny;
	}

	public String getAccessListInfo() {
		return accessListInfo;
	}

	public void setAccessListInfo(String accessListInfo) {
		this.accessListInfo = accessListInfo;
	}

	public ArrayList<AccessList> getAccessList() {
		return accessLists;
	}

	public void setAccessList(AccessList accessList) {//自己関連のために特別措置
		if(this.accessLists.size()>=2){//アクセスリストへの関連が三本あるとき
			for(AccessList access : accessLists){
				setNodeFalseInstances(access);
			}
			setNodeFalseInstances(accessList);
			setNodeFalseInstances(this);
		}
		for (AccessList access : accessLists){//同じアクセスリストのクラスに関連があるとき
			if(accessList.equals(access)) {
				setNodeFalseInstances(accessList);
				setNodeFalseInstances(this);
				setErrorStatement("エラー:["+this.getName()+"]と["+accessList.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
			}
		}
		this.accessLists.add(accessList);

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

	public int getAccessListNumber() {
		return accessListNumber;
	}

	public void setAccessListNumber(int accessListNumber) {
		this.accessListNumber = accessListNumber;
	}
}
