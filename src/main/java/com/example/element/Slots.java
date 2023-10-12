package com.example.element;

public  class Slots {
    public Slots(String attribute,String value){
        this.value = value;
        this.attribute = attribute;
    }
    private String value;//値
    private String attribute;//属性


    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }
}
