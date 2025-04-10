package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Hostname extends ClassElement {


    private String hostname;
    @JsonIgnore
    private Config config;


    public void setHostName(String name) {
        this.hostname = name;
    }


    public void setConfig(Config config) {
        if (this.config != null) {
            setNodeFalseInstances(this.config);
            setNodeFalseInstances(config);
            setNodeFalseInstances(this);
            setmultiplicityErrorStatement("エラー:[" + getName() + "]と[" + config.getClassName() + "のインスタンス]の関連において、最大多重度を超えました。2個以上のオブジェクトを関連付けることはできません。");
        }
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public String getHostName() {
        return hostname;
    }
}
