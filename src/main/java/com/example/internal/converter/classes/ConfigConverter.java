package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.Config;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;
import java.util.List;

public class ConfigConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        Config config = new Config();

        if (!slots.isEmpty()) {
            config.setDeviceModel(slots.get(0).getValue());
        }

        return config;
    }
}
