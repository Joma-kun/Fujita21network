package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.OspfSetting;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class OspfSettingConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        OspfSetting ospfSetting = new OspfSetting();

        for (Slots slot : slots) {
            String attr = slot.getAttribute();
            String value = slot.getValue();

            switch (attr) {
                case "processId":
                    if (isInt(value)) {
                        ospfSetting.setProcessId(Integer.parseInt(value.trim()));
                    }
                    break;

                case "routerId":
                    ospfSetting.setRouterId(value);
                    break;
            }
        }
        return ospfSetting;
    }
}
