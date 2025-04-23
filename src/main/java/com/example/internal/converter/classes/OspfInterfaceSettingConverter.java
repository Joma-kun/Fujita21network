package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.OspfInterfaceSetting;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class OspfInterfaceSettingConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        OspfInterfaceSetting ospfInterfaceSetting = new OspfInterfaceSetting();
        for (Slots slot : slots) {
            String attr = slot.getAttribute();

            String value = slot.getValue();

            switch (attr) {
                case "ipAddress":
                    ospfInterfaceSetting.setIpAddress(value);
                    break;

                case "wildcardMask":
                    ospfInterfaceSetting.setWildcardMask(value);
                    break;

                case "areaId":
                    if (isInt(value)) {
                        ospfInterfaceSetting.setAreaId(Integer.parseInt(value.trim()));
                    }
                    break;

                case "helloInterval":
                    if (isInt(value)) {
                        ospfInterfaceSetting.setHelloInterval(Integer.parseInt(value.trim()));
                    }
                    break;

                case "deadInterval":
                    if (isInt(value)) {
                        ospfInterfaceSetting.setDeadInterval(Integer.parseInt(value.trim()));
                    }
                    break;

                case "ospfNetworkMode":
                    ospfInterfaceSetting.setOspfNetworkMode(value);
                    break;

                case "stub":
                    ospfInterfaceSetting.setStub(value);
                    break;

                case "priority":
                    if (isInt(value)) {
                        ospfInterfaceSetting.setPriority(Integer.parseInt(value.trim()));
                    }
                    break;
            }
        }
        return ospfInterfaceSetting;
    }
}
