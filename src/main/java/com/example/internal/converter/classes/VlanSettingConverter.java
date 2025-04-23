package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.VlanSetting;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class VlanSettingConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        VlanSetting vlanSetting = new VlanSetting();

        // スロット情報に基づいて vlanSetting の属性を設定
        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "vlanNum":
                    if (isInt(slot.getValue())) {
                        vlanSetting.setVlanNum(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "ipAddress":
                    vlanSetting.setIpAddress(slot.getValue());
                    break;
                case "subnetMask":
                    vlanSetting.setSubnetMask(slot.getValue());
                    break;
                case "accessListNumber":
                    if (isInt(slot.getValue())) {
                        vlanSetting.setAccessListNumber(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "accessListName":
                    vlanSetting.setAccessListName(slot.getValue());
                    break;
                case "accessListInOrOut":
                    vlanSetting.setAccessListInOrOut(slot.getValue());
                    break;
                case "inNatInside":
                    vlanSetting.setInNatInside(slot.getValue().equals("true"));
                    break;
                case "ipTcpAdjustMss":
                    if (isInt(slot.getValue())) {
                        vlanSetting.setIpTcpAdjustMss(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "ipVirtualReassembly":
                    vlanSetting.setIpVirtualReassembly(slot.getValue().equals("true"));
                    break;
                case "shutdown":
                    vlanSetting.setShutdown(slot.getValue().equals("true"));
                    break;
                default:
                    // 未知の属性に関しては無視する
                    break;
            }
        }

        // VlanSetting を返す
        return vlanSetting;
    }
}
