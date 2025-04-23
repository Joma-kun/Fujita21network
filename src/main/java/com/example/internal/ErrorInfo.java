package com.example.internal;


import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;
@JsonPropertyOrder({ "error", "errortype", "instances"})
//エラー情報をまとめるクラス。JSON形式にまとめる際にJacksonを使うために作成した。
public class ErrorInfo {


    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrortype() {
        return errortype;
    }

    public void setErrortype(String errortype) {
        this.errortype = errortype;
    }

    public Map<String, String> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, String> instances) {
        this.instances = instances;
    }


    private boolean error;//警告メッセージ
    private String errortype;//エラーの種類
    private Map<String,String> instances;//エラーの原因となる設定値

    public ErrorInfo( boolean error, String errortype, Map<String, String> instances) {
        this.error = error;
        this.errortype = errortype;
        this.instances = instances;
    }
}
