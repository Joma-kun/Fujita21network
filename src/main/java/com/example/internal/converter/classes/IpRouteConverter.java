package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.IpRoute;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

public class IpRouteConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        IpRoute ipRoute = new IpRoute();

        for (Slots slot : slots) {
            String attr = slot.getAttribute();
            String value = slot.getValue();

            switch (attr) {
                case "ipAddress":
                    ipRoute.setIpAddress(value);
                    break;
                case "subnetMask":
                    ipRoute.setSubnetMask(value);
                    break;
                case "nextHopAddress":
                    ipRoute.setNetHopAddress(value);
                    break;
            }
        }

        return ipRoute;
    }
}
