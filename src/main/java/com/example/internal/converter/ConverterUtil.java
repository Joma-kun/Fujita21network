package com.example.internal.converter;

public class ConverterUtil {
    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static boolean isBoolean(String value){
        if(value.equals("true")) return true;
        else return false;
    }
}
