package com.example.internal.converter.classes;

import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.example.classes.AccessList;
import com.example.element.Slots;

import java.util.ArrayList;

import static com.example.internal.converter.ConverterUtil.isInt;

public class AccessListConverter implements InstanceConverter {

    @Override
    public AccessList convert(IInstanceSpecification instanceSpecification, ArrayList<Slots> slots) {
        AccessList accessList = new AccessList();

        // スロット情報に基づいてaccessListの属性を設定
        for (Slots slot : slots) {
            switch (slot.getAttribute()) {
                case "accessListNumber":
                    if (isInt(slot.getValue())) {
                        accessList.setAccessListNumber(Integer.parseInt(slot.getValue()));
                    }
                    break;
                case "accessListName":
                    accessList.setAccessListName(slot.getValue());
                    break;
                case "sequenceNumber":
                    if (isInt(slot.getValue())) {
                        accessList.setSequenceNumber(Integer.parseInt(slot.getValue()));
                    }
                    break;
                case "permitOrDeny":
                    accessList.setPermitOrDeny(slot.getValue());
                    break;
                case "protocol":
                    accessList.setProtocol(slot.getValue());
                    break;
                case "sourceIpAddress":
                    accessList.setSorceIpAddress(slot.getValue());
                    break;
                case "sourceWildcardMask":
                    accessList.setSourceWildcardMask(slot.getValue());
                    break;
                case "sourceOperator":
                    accessList.setSourceOperator(slot.getValue());
                    break;
                case "sourcePort":
                    accessList.setSourcePort(slot.getValue());
                    break;
                case "destIpAddress":
                    accessList.setDestIpAddress(slot.getValue());
                    break;
                case "destWildcardMask":
                    accessList.setDestWildcardMask(slot.getValue());
                    break;
                case "destOperator":
                    accessList.setDestOperator(slot.getValue());
                    break;
                case "destPort":
                    accessList.setDestPort(slot.getValue());
                    break;
                case "sourceIsHost":
                    accessList.setSourceIsHost(slot.getValue().equals("true"));
                    break;
                case "destIsHost":
                    accessList.setDestIsHost(slot.getValue().equals("true"));
                    break;
                default:
                    // 未知の属性に関しては無視する
                    break;
            }
        }

        // ここでAccessListをそのまま返す
        return accessList;
    }
}
