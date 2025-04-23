package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.Hostname;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

public class HostnameConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        Hostname hostname = new Hostname();

        if (!slots.isEmpty()) {
            hostname.setHostName(slots.get(0).getValue());
        }

        return hostname;
    }
}
