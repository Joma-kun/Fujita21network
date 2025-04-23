package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.Link;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

public class LinkConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        Link link = new Link();

        if (!slots.isEmpty()) {
            link.setDescription(slots.get(0).getValue());
        }

        return link;
    }
}
