package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.OspfVirtualLink;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class OspfVirtualLinkConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        OspfVirtualLink ospfVirtualLink = new OspfVirtualLink();


        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "areaId":
                    if (isInt(slot.getValue())) {
                        ospfVirtualLink.setAreaId(Integer.parseInt(slot.getValue().trim()));
                    }
                    break;
                case "routerId":
                    ospfVirtualLink.setRouterId(slot.getValue());
                    break;
            }
        }

        return ospfVirtualLink;
    }
}
