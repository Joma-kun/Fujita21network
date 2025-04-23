package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.Vlan;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class VlanConverter implements InstanceConverter{
    @Override
    public Vlan convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        Vlan vlan = new Vlan();

        // スロット情報に基づいてvlanの属性を設定
        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "num":
                    if (isInt(slot.getValue())) {
                        vlan.setNum(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "name":
                    vlan.setVlanName(slot.getValue());
                    break;
                default:
                    // 未知の属性に関しては無視する
                    break;
            }
        }

        // Vlan を返す
        return vlan;
    }

}
