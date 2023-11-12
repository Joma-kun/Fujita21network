package com.example.classes;

import com.example.element.ClassElement;

public class Vlan extends ClassElement {

	private int num = -1;

	private String name;

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}


	public String getNamd() {
		return name;
	}


	public void setNamed(String name) {
		this.name = name;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		if(this.config!=null){
			setNodeFalseInstances(this.config);
			setNodeFalseInstances(config);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+getName()+"]と[configのインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}else{
			this.config = config;
		}
	}

	private Config config;

}
