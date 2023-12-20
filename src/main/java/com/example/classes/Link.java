package com.example.classes;

import com.example.element.ClassElement;

import java.util.ArrayList;

public class Link extends ClassElement {

	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<LinkableElement> getLinkableElement() {
		return linkableElements;
	}

	//linkableElementがつながっているLinkableElementを見つけるための処理
	public LinkableElement getAnotherLinkableElement(LinkableElement link1){
		for(LinkableElement linked: this.getLinkableElement()){
			if(linked!=link1){
				return linked;
			}
		}
		return null;
	}


	private ArrayList<LinkableElement> linkableElements = new ArrayList<>();

	public void setLinkableElement(LinkableElement linkableElement){
		if(this.linkableElements.size()>=2){
			for(LinkableElement linkable : linkableElements){
				setNodeFalseInstances(linkable);
//				System.out.println(linkable.getName());
			}
//			System.out.println(this.getName()+"---"+linkableElements.size());
			setNodeFalseInstances(linkableElement);
			setNodeFalseInstances(this);
			setErrorStatement("エラー:["+this.getName()+"]と["+linkableElement.getClassName()+"のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
		}
		this.linkableElements.add(linkableElement);
	}

}
