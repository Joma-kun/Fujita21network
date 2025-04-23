package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

public interface InstanceConverter {
    ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots);
}
