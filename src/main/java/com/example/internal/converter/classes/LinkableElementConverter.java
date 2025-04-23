package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.LinkableElement;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

public class LinkableElementConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        return new LinkableElement();
    }
}
