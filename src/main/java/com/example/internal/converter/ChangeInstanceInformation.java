package com.example.internal.converter;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.example.element.ClassElement;
import com.example.element.Slots;
import com.example.internal.converter.classes.InstanceConverter;

import java.util.ArrayList;

public class ChangeInstanceInformation {
    //自分たちのinstance情報に変換するためのメソッド
    public static ClassElement changeInstanceInfomation(IPresentation nodePresentation) throws InvalidEditingException {
        if (!(nodePresentation instanceof INodePresentation)) {//ノードの情報ではないならnullを返す
            return null;
        }

        IElement model = nodePresentation.getModel();



        if (!(model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification)) {//インスタンス情報じゃなければnullを返す
            return null;
        }
        com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (IInstanceSpecification) model;
        if(instanceSpecification ==null) return null;
        if(instanceSpecification.getClassifier() == null) {
            return  null;
        }

        String className = instanceSpecification.getClassifier().getName();//クラスの名前
        ISlot[] slot = instanceSpecification.getAllSlots();//astahのインスタンスの属性値を取得してslotとする
        ArrayList<Slots> slots = new ArrayList<>();//自プロジェクトのslotsを用意する
        for (ISlot s : slot) {
            try {
                IPresentation[] presentations = s.getPresentations();
                for (IPresentation p : presentations) {
                    p.setProperty(PresentationPropertyConstants.Key.FILL_COLOR, "#ff0000");
                }
            } catch (InvalidUsingException e) {
                throw new RuntimeException(e);
            }
            slots.add(new Slots(s.getDefiningAttribute().getName(), s.getValue()));//astah属性の値などを自プロジェクトのslotsに格納する
        }
        //クラス名から対応するコンバーター取得
        InstanceConverter converter = ConverterManager.getConverter(className);
        if (converter == null) {
            System.out.println("未対応のクラスです");
            return null;
        }

        // 変換処理を呼び出す
        ClassElement instance = converter.convert(instanceSpecification, slots);

        if (instance != null) {
            instance.setPresentation(nodePresentation);
            instance.setClassName(instanceSpecification.getClassifier().getName());//クラスの名前（AccessListを登録する）
            instance.setName(instanceSpecification.getName());//インスタンスの名前を登録する(例：Acc_1など ）
            instance.setSlots(slots);//instance のスロットに格納する
            instance.setElement(model);//astahのインスタンス情報と自インスタンスを関連付ける
        } else {
            System.out.println("変換に失敗しました: " + instanceSpecification.getName());
        }
        return instance;
    }
}
