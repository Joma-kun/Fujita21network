package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class LinkableElement extends ClassElement {

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        if (this.link != null) {
            setNodeFalseInstances(this.link);
            setNodeFalseInstances(link);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + getName() + "]と[" + link.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.link = link;
    }

    @JsonIgnore
    private Link link;


}
