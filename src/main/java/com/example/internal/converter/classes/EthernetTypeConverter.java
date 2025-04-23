package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.EthernetType;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

public class EthernetTypeConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        EthernetType ethernetType = new EthernetType();

        // スロット情報に基づいて ethernetType の属性を設定
        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "Ethernet":
                    ethernetType.setEthernet(slot.getValue().equals("true"));
                    break;
                case "fastEthernet":
                    ethernetType.setFastEthernet(slot.getValue().equals("true"));
                    break;
                case "gigabitEthernet":
                    ethernetType.setGigabitEthernet(slot.getValue().equals("true"));
                    break;
                case "10gigabitEthernet":
                    ethernetType.setTengigabitEthernet(slot.getValue().equals("true"));
                    break;
                default:
                    // 未知の属性に関しては無視する
                    break;
            }
        }

        // EthernetType をそのまま返す
        return ethernetType;
    }

}
