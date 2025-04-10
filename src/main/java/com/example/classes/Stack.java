package com.example.classes;

import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Stack extends ClassElement {
    private int stackMemberNumber = -1;
    @JsonIgnore
    private Config config;

    public Config getConfig() {
        return config;
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

    public int getStackMemberNumber() {
        return stackMemberNumber;
    }

    public void setStackMemberNumber(int stackMemberNumber) {
        this.stackMemberNumber = stackMemberNumber;
    }

    public int getPreviousStackNumber() {
        return previousStackNumber;
    }

    public void setPreviousStackNumber(int previousStackNumber) {
        this.previousStackNumber = previousStackNumber;
    }

    public int getStackPriority() {
        return stackPriority;
    }

    public void setStackPriority(int stackPriority) {
        this.stackPriority = stackPriority;
    }

    private int previousStackNumber = -1;
    private int stackPriority = -1;
}
