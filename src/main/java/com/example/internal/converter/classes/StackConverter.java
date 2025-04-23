package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.Stack;
import com.example.element.ClassElement;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class StackConverter implements InstanceConverter{
    @Override
    public ClassElement convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        Stack stack = new Stack();

        // スロット情報に基づいて stack の属性を設定
        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "stackMemberNumber":
                    if (isInt(slot.getValue())) {
                        int number = Integer.parseInt(slot.getValue().trim());
                        stack.setStackMemberNumber(number);
                    }
                    break;
                case "previousStackNumber":
                    if (isInt(slot.getValue())) {
                        int number = Integer.parseInt(slot.getValue().trim());
                        stack.setPreviousStackNumber(number);
                    }
                    break;
                case "stackPriority":
                    if (isInt(slot.getValue())) {
                        int number = Integer.parseInt(slot.getValue().trim());
                        stack.setStackPriority(number);
                    }
                    break;
                default:
                    // 未知の属性に関しては無視する
                    break;
            }
        }

        // Stack をそのまま返す
        return stack;
    }
}
