package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.StpSetting;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class StpSettingConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        StpSetting stpSetting = new StpSetting();

        // スロット情報に基づいてstpSettingの属性を設定
        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "bridgePriority":
                    if (isInt(slot.getValue())) {
                        stpSetting.setBridgePriority(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "vlan":
                    if (isInt(slot.getValue())) {
                        stpSetting.setVlan(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "mode":
                    stpSetting.setMode(slot.getValue());
                    break;
                case "macAddress":
                    stpSetting.setMacAddress(slot.getValue());
                    break;
                default:
                    break;
            }
        }

        return stpSetting;
    }
}
