package com.example.internal.converter;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.example.element.ClassElement;
import com.example.element.LinkElement;


import java.util.ArrayList;

import static com.example.internal.converter.ChangeInstanceInformation.changeInstanceInfomation;
import static com.example.internal.converter.ChangeNodeInformation.changeNodeInformation;
import static com.example.internal.converter.SetOthersInformation.*;

//allowedVlanの検証も対処する
public class ChangeClassInformation {//astahの情報をプログラム内のクラスに変換するクラス
    //設定値が正しいことを検証した後に変換することを前提とするが、正しくない場合でも変換は行えるようにする。

    //クラスや属性を足したときの処理の仕方
    /*クラスを追加したとき
    * 1.claseesに該当するクラスと属性、セッターゲッターを追加する
    * 2.他のクラスと同じようにコンバーターを用意する
    * 3.ConverterManagerに作成したクラスに対応するものをconvertersに追加する
    * 4.changenodeinformationを編集する
    * 4.検証を追加する*/

    /*属性を追加したとき
    * 1.classesの該当クラスで属性とセッターゲッターを用意する
    * 2.converterの該当クラスでcase文を追加する*/


    public static ArrayList<ClassElement> changeAllElement(ArrayList<IPresentation> presentations, ProjectAccessor projectAccessor) throws InvalidEditingException {
        ArrayList<ClassElement> instances = new ArrayList<>();//生成したインスタンスのためのリスト
        for (IPresentation presentation : presentations) {//astahの図の情報
            ClassElement instance = changeInstanceInfomation(presentation);//astahのプレゼンテーションの情報を自クラスの情報に変換する
            if (instance != null) {
                instances.add(instance);//自分たちのinstance仕様に変換しリストに追加する
            }
        }//instancesに情報を変換して入れていある状態（関連の情報は入っていない）

        for (IPresentation presentation : presentations) {
            changeNodeInformation(presentation, instances);//関連の情報をインスタンスの情報に追加する
        }

        setConectedConfigs(instances);//EthernetSettingのEthernetSettingを格納する処理　//要編集
        setLinkConfig(instances);//隣接リスト表現でコンフィグの接続情報（物理結線情報を格納するリスト）
        setallowedVlans(instances);//allowedVlanに値をいれる。他の設定値を参照するため、他の設定値の情報が入った後に値を追加する必要があった。
        setId(instances,projectAccessor);//idを設定するメソッド

        return instances;
    }









}
