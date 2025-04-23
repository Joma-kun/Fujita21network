package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.EthernetSetting;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isBoolean;
import static com.example.internal.converter.ConverterUtil.isInt;

public class EthernetSettingConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification ethernetSettingSpecification, ArrayList<Slots> slots) {
        EthernetSetting ethernetSetting = new EthernetSetting();

        //allowedVlanの設定だけ後で設定
        for (Slots slot : slots) {
            String attr = slot.getAttribute();
            String value = slot.getValue();

            switch (attr) {
                case "stack":
                    if (isInt(value)) ethernetSetting.setStack(Integer.parseInt(value));
                    break;
                case "slot":
                    if (isInt(value)) ethernetSetting.setSlot(Integer.parseInt(value));
                    break;
                case "port":
                    if (isInt(value)) ethernetSetting.setPort(Integer.parseInt(value));
                    break;
                case "ipAddress":
                    ethernetSetting.setIpAddress(value);
                    break;
                case "subnetMask":
                    ethernetSetting.setSubnetMask(value);
                    break;
                case "accessVlan":
                    if (isInt(value)) ethernetSetting.setAccessVlan(Integer.parseInt(value));
                    break;
                case "nativeVlan":
                    if (isInt(value)) ethernetSetting.setNativeVlan(Integer.parseInt(value));
                    break;
                case "mode":
                    ethernetSetting.setMode(value);
                    break;
                case "accessListNumber":
                    if (isInt(value)) ethernetSetting.setAccessListNumber(Integer.parseInt(value));
                    break;
                case "accessListName":
                    ethernetSetting.setAccessListName(value);
                    break;
                case "accessListInOrOut":
                    ethernetSetting.setAccessListInOrOut(value);
                    break;
                case "speed":
                    ethernetSetting.setSpeed(value);
                    break;
                case "duplex":
                    ethernetSetting.setDuplex(value);
                    break;
                case "ipVirtualReassembly":
                    if (isBoolean(value)) ethernetSetting.setIpVirtualReassembly(Boolean.parseBoolean(value));
                    break;
                case " switchportTrunkEncapsulation":
                    if (isBoolean(value)) ethernetSetting.setSwitchportTrunkEncapsulation(Boolean.parseBoolean(value));
                    break;
                case "shutdown":
                    if (isBoolean(value)) ethernetSetting.setShutdown(Boolean.parseBoolean(value));
                    break;
                case "mtu":
                    if (isInt(value)) ethernetSetting.setMtu(Integer.parseInt(value));
                    break;
                case "allowedVlan":
                    ethernetSetting.setAllowdVlanString(value);
                    break;

            }
        }

        return ethernetSetting;
    }
}
