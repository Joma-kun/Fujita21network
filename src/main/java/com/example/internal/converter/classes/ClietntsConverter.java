package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.Clients;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class ClietntsConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        Clients client = new Clients();
        for (Slots slot : slots) {
            String attribute = slot.getAttribute();
            String value = slot.getValue();

            switch (attribute) {
                case "name":
                    client.setNames(value);
                    break;
                case "ipAddress":
                    client.setIpAddress(value);
                    break;
                case "subnetMask":
                    if (isInt(value)) {
                        client.setSubnetMask(Integer.parseInt(value));
                    }
                    break;
                case "defaultGateway":
                    client.setDefaultGateway(value);
                    break;
            }
        }

        return client;
    }




}
