package com.example.internal;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.example.classes.*;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
import com.example.element.Slots;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class ChangeClassInformation {
    //変換と同時に構文のチェックを行う　エラーが出ても格納は行う．
    //正規表現
    static Pattern twoDigits = Pattern.compile("^([0-9]{1,2})$");//二桁の数字
    static Pattern fourDigits = Pattern.compile("^([0-9]{1,4})$");//四桁の数字
    static Pattern fiveDigits = Pattern.compile("^([0-9]{1,5})$");//5桁の数字
    static Pattern subnetmask = Pattern.compile("^(?:[0-9]|[1-2][0-9]|3[0-2])$");//サブネットマスク




    static Pattern space = Pattern.compile("[\\w.,-]+\\s+[\\w.,-]+"); //半角スペース

    static Pattern ipAddress = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");//IPアドレス

    static Pattern macAddress = Pattern.compile("\\b(?:[0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}\\b|\\b(?:[0-9a-fA-F]{4}\\.){2}[0-9a-fA-F]{4}\\b");
    static Pattern macAddress2 = Pattern.compile("\b(?:[0-9a-fA-F]{4}\\.){2}[0-9a-fA-F]{4}\b");
    static Pattern linkPattern = Pattern.compile("^\\S+ \\S+ and \\S+ \\S+$|^\\S+ and \\S+ \\S+$|^\\S+ \\S+ and \\S+$");

    static List<String> protocolName = Arrays.asList("ip","udp","tcp","icmp");
    static String red = "#ff0000";//エラーの色
    public static ArrayList<ClassElement> changeAllElement(ArrayList<IPresentation> presentations, TextArea textArea,ArrayList<String> formatErrorStatements,ArrayList<ClassElement> errorInstances) throws InvalidEditingException {
        ArrayList<ClassElement> instances = new ArrayList<>();//生成したインスタンスのためのリスト
        for (IPresentation presentation : presentations) {//astahの図の情報
            ClassElement instance = changeInstanceInfomation(presentation,textArea,formatErrorStatements,errorInstances);
            if (instance != null) {
                instances.add(instance);//自分たちのinstance仕様に変換しリストに追加する
            }
        }//instancesに情報を変換して入れていある状態（関連の情報は入っていない）


        for (IPresentation presentation : presentations) {
            changeNodeInformation(presentation, instances);//関連の情報をインスタンスの情報に追加する
        }

//        System.out.println("s");
//        for(ClassElement instan :instances){
//            System.out.println(instan.getName());
//        }
        setLinkConfig(instances);
        setallowedVlans(instances);

        return instances;
    }

    //configのリンクでつながれたConfigをConfigクラスのlinkedConfig属性に格納する処理。隣接リスト表現
    public static void setLinkConfig(ArrayList<ClassElement> instances) {
        for (ClassElement instance : instances) {
            if (instance instanceof Config) {
                ArrayList<EthernetSetting> ethernetSettings = ((Config) instance).getEthernetSetting();

                for (EthernetSetting ethernetSetting : ethernetSettings) {

                    Link link = ethernetSetting.getLink();
                    if(link!=null) {
                        EthernetSetting ethernetSettingTarget = null;
                        if (link.getLinkableElement() != null) {
                            for (LinkableElement linked : link.getLinkableElement()) {
                                if (linked != ethernetSetting) {
                                    if (linked instanceof EthernetSetting) {
                                        ethernetSettingTarget = (EthernetSetting) linked;
                                        Config conf = ethernetSettingTarget.getConfig();
                                        ((Config) instance).setLinkedConfigs(conf);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    public static ArrayList<LinkElement> changeLinkInformation(ArrayList<IPresentation> presentations, ArrayList<ClassElement> instances) {
        ArrayList<LinkElement> links = new ArrayList<>();
        for (IPresentation presentation : presentations) {
            if (presentation instanceof ILinkPresentation) {
                LinkElement link = new LinkElement();
                link.setLinkPresentation((ILinkPresentation) presentation);
                for (ClassElement instance : instances) {
                    if (instance.getPresentation() == ((ILinkPresentation) presentation).getSourceEnd()) {
                        link.setSourceEnd(instance);
                    } else if (instance.getPresentation() == ((ILinkPresentation) presentation).getTargetEnd()) {
                        link.setTargetEnd(instance);
                    }
                }
                links.add(link);
            }
        }
        return links;
    }

    public static boolean isFullWidth(char c) {
        // Unicodeの範囲による判定
        // 全角文字のUnicode範囲：\uFF01-\uFF5E、\u3040-\u309F、\u30A0-\u30FF
        return (c >= '\uFF01' && c <= '\uFF5E') || (c >= '\u3040' && c <= '\u309F') || (c >= '\u30A0' && c <= '\u30FF');
    }
    public static void setallowedVlans(ArrayList<ClassElement> instances) {
        for (ClassElement instance : instances) {
            if (instance instanceof EthernetSetting) {
                if(((EthernetSetting) instance).getLink() != null) {

                    try {
                        ((EthernetSetting) instance).setConectedThing(((EthernetSetting) instance).getLink().getAnotherLinkableElement((LinkableElement) instance));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        for (ClassElement classElement : instances) {
            if (classElement instanceof EthernetSetting) {
                ((EthernetSetting) classElement).setAllowedVlan();
//                    System.out.println(classElement.getName()+"  "+((EthernetSetting) classElement).getAllowedVlans());
            }
        }
    }

    public static ClassElement changeInstanceInfomation(IPresentation nodepresentation,TextArea textArea,ArrayList<String> formatErrorStatements,ArrayList<ClassElement> errorInstances) throws InvalidEditingException {//自分たちのinstance情報に変換するためのメソッド
        if (nodepresentation instanceof INodePresentation) {//インスタンスの名前、スロットの処理
            IElement model = nodepresentation.getModel();
            if (model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;

                ClassElement instance = null;


                ISlot[] slot = instanceSpecification.getAllSlots();//astahのインスタンスの属性値を取得してslotとする
                ArrayList<Slots> slots = new ArrayList<>();//自プロジェクトのslotsを用意する
                for (ISlot s : slot) {
                    try {
                        IPresentation[] presentations = s.getPresentations();
                        for(IPresentation p : presentations){
                            p.setProperty(PresentationPropertyConstants.Key.FILL_COLOR, "#ff0000");
                        }
                    } catch (InvalidUsingException e) {
                        throw new RuntimeException(e);
                    }

                    Slots sl = new Slots(s.getDefiningAttribute().getName(), s.getValue());
                    slots.add(sl);//astah属性の値などを自プロジェクトのslotsに格納する
                }


                //インスタンスの名前によって識別し、自分のインスタンス情報に変換する
                if (instanceSpecification.getClassifier().getName().equals("AccessList") || instanceSpecification.getClassifier().getName().equals("CiscoAccessList")) {
                    instance = new AccessList();//自分たちのインスタンスのアクセスリストを作る
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());//クラスの名前（AccessListを登録する）
                    instance.setName(instanceSpecification.getName());//インスタンスの名前を登録する(例：Acc_1など ）
                    instance.setSlots(slots);//instance のスロットに格納する
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String accName = slots.get(slotnumber).getAttribute();
                            if (accName.equals("accessListNumber")) {
                                if (isInt(slots.get(slotnumber).getValue())) {//int型に変換してAccesslistの属性に入れる処理
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue());
                                    ((AccessList) instance).setAccessListNumber(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。 入力に半角スペースが含まれています");


                                        errorInstances.add(instance);
                                        //色の切り替え

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。整数値を入力してください");


                                        errorInstances.add(instance);
                                        //色の切り替え

                                    }
                                    Matcher accessListNumberM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!accessListNumberM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");


                                        errorInstances.add(instance);

                                        //色の切り替え

                                    }
                                }
                            }
                            if(accName.equals("accessListName")){
                                ((AccessList) instance).setAccessListName(slots.get(slotnumber).getValue());
                            }
                        if (accName.equals("sequenceNumber")) {
                            if (isInt(slots.get(slotnumber).getValue())) {//int型に変換してAccesslistの属性に入れる処理
                                int number = Integer.parseInt(slots.get(slotnumber).getValue());
                                ((AccessList) instance).setSequenceNumber(number);
                            }
                            if(!slots.get(slotnumber).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のsequenceNumberの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のsequenceNumberの値は無効です。 入力に半角スペースが含まれています");


                                    errorInstances.add(instance);
                                    //色の切り替え

                                }
                                if (!isInt(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のsequenceNumberの値は無効です。整数値を入力してください");


                                    errorInstances.add(instance);
                                    //色の切り替え

                                }
                                Matcher sequenceNumberM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (!sequenceNumberM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のsequenceNumberの値は無効です。4桁以内の整数値を入力してください");


                                    errorInstances.add(instance);

                                    //色の切り替え

                                }
                            }
                        }
                            if (accName.equals("permitOrDeny")) {
                                ((AccessList) instance).setPermitOrDeny(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のpermitOrDenyの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のpermitOrDenyの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("permit") || slots.get(slotnumber).getValue().equals("deny") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のpermitOrDenyの値は無効です。'permit' または 'deny' のいずれかを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (accName.equals("protocol")) {
                                ((AccessList) instance).setProtocol(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のprotocolの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のprotocolの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!protocolName.contains(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のprotocolの値は無効です。 正しいプロトコル名を記入してください");
                                        errorInstances.add(instance);

                                    }

                                }
                            }
                            if (accName.equals("sourceIpAddress")) {
                                ((AccessList) instance).setSorceIpAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsourceIpAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsourceIpAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher ipAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!ipAddressM.matches() && !slots.get(slotnumber).getValue().equals("any")) {
                                        formatErrorStatements.add(instance.getName() + "のsourceIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (accName.equals("sourceWildcardMask")) {
                                ((AccessList) instance).setSourceWildcardMask(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsourceWildcardMaskの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsourceWildcardMaskの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher ipAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!ipAddressM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsourceWildcardMaskの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (accName.equals("sourceOperator")) {
                                ((AccessList) instance).setSourceOperator(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsourceOperatorの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsourceOperatorの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("eq") || slots.get(slotnumber).getValue().equals("neq") || slots.get(slotnumber).getValue().equals("gt") || slots.get(slotnumber).getValue().equals("lt") || slots.get(slotnumber).getValue().equals("range"))) {
                                        formatErrorStatements.add(instance.getName() + "のsourceOperatorの値は無効です。'eq','neq','gt','lt',range のいずれかを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (accName.equals("sourcePort")) {
                                ((AccessList) instance).setSourcePort(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsourcePortの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のsourcePortの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                Matcher fiveDigit = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (!fiveDigit.matches() || isInt(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のsourcePortの値は無効です。有効なポートを入力してください．");
                                    errorInstances.add(instance);
                                }}
                            }
                            if (accName.equals("destIpAddress")) {
                                ((AccessList) instance).setDestIpAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のdestIpAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のdestIpAddressの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                Matcher ipAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                if (!ipAddressM.matches() && !slots.get(slotnumber).getValue().equals("any")) {
                                    formatErrorStatements.add(instance.getName() + "のdestIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                    formatErrorStatements.add(((AccessList) instance).getDestIpAddress() + "のdestIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                    errorInstances.add(instance);
//
                                }}
                            }
                            if (accName.equals("destWildcardMask")) {
                                ((AccessList) instance).setDestWildcardMask(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のdestWildcardMaskの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のdestWildcardMaskの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher ipAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!ipAddressM.matches() && !slots.get(slotnumber).getValue().equals("any")) {
                                        formatErrorStatements.add(instance.getName() + "のdestWildcardMaskの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (accName.equals("destOperator")) {
                                ((AccessList) instance).setDestOperator(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のdestOperatorの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のdestOperatorの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("eq") || slots.get(slotnumber).getValue().equals("neq") || slots.get(slotnumber).getValue().equals("gt") || slots.get(slotnumber).getValue().equals("lt") || slots.get(slotnumber).getValue().equals("range"))) {
                                        formatErrorStatements.add(instance.getName() + "のdestOperatorの値は無効です。'eq','neq','gt','lt',range のいずれかを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (accName.equals("destPort")) {
                                ((AccessList) instance).setDestPort(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のdestPortの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のdestPortの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher fiveDigit = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!fiveDigit.matches() && isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のdestPortの値は無効です。有効なポートを入力してください．");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                        if (accName.equals("sourceIsHost")) {
                            ((AccessList) instance).setSourceIsHost(slots.get(slotnumber).getValue().equals("true"));
                            if (!slots.get(slotnumber).getValue().isEmpty()) {
                                if (zenkakuCheck(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のsourceIsHostの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のsourceIsHostの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                    formatErrorStatements.add(instance.getName() + "のsourceIsHostの値は無効です。trueまたはfalseを入力してください");
                                    errorInstances.add(instance);

                                }
                            }

                        }
                        if (accName.equals("destIsHost")) {
                            ((AccessList) instance).setDestIsHost(slots.get(slotnumber).getValue().equals("true"));
                            if (!slots.get(slotnumber).getValue().isEmpty()) {
                                if (zenkakuCheck(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のdestIsHostの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のdestIsHostの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                    formatErrorStatements.add(instance.getName() + "のdestIsHostの値は無効です。trueまたはfalseを入力してください");
                                    errorInstances.add(instance);

                                }
                            }

                        }

                        }


                } else if (instanceSpecification.getClassifier().getName().equals("Client")) {
                    instance = new Clients();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String cliName = slots.get(slotnumber).getAttribute();

                            if (cliName.equals("name")) {
                                ((Clients) instance).setNames(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
//                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
//                                        formatErrorStatements.add(instance.getName() + "のnameの値は無効です。入力に全角が含まれています");
//                                        errorInstances.add(instance);
//                                    }
//                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
//                                    if (spaceM.matches()) {
//                                        formatErrorStatements.add(instance.getName() + "のnameの値は無効です。 入力に半角スペースが含まれています");
//                                        errorInstances.add(instance);
//
//                                    }
                                }
                            }
                            if (cliName.equals("ipAddress")) {
                                ((Clients) instance).setIpAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher IpAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!IpAddressM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (cliName.equals("subnetMask")) {
                                if (isInt(slots.get(slotnumber).getValue())) {//int型に変換してAccesslistの属性に入れる処理
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue());
                                    ((Clients) instance).setSubnetMask(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                        //色の切り替え

                                    }
                                    Matcher subnetMaskM = subnetmask.matcher(slots.get(slotnumber).getValue());
                                    if (!subnetMaskM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (cliName.equals("defaultGateway")) {
                                ((Clients) instance).setDefaultGateway(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のdefaultGatawayの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のdefaultGatawayの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher defaultGateWayM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!defaultGateWayM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のdefaultGateWayの値は無効です。有効なデフォルトゲートウェイ形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }


//
                    }


                } else if (instanceSpecification.getClassifier().getName().equals("Config")) {

                    instance = new Config();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    instance.setName(instanceSpecification.getName());
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                        ((Config) instance).setDeviceModel(slots.get(0).getValue());
//                        if(!slots.get(0).getValue().isEmpty()) {
//                            if(zenkakuCheck(slots.get(0).getValue())){
//                                formatErrorStatements.add(instance.getName() + "のConfigの値は無効です。入力に全角が含まれています");
//                                errorInstances.add(instance);
//                            }
//                        Matcher spaceM = space.matcher(String.valueOf(slots.get(0).getValue()));
//                        if (spaceM.matches()) {
//                            formatErrorStatements.add(instance.getName() + "のConfigの値は無効です。 入力に半角スペースが含まれています");
//                            errorInstances.add(instance);
//
//                        }
//                    }

                } else if (instanceSpecification.getClassifier().getName().equals("CiscoEthernetSetting" ) || instanceSpecification.getClassifier().getName().equals("EthernetSetting") ){//"EthernetSetting"
                    instance = new EthernetSetting();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);

                    for (int e = 0; e < slots.size(); e++) {
                        String ethName = slots.get(e).getAttribute();
                            if (ethName.equals("stack")) {

                                if (isInt(slots.get(e).getValue())) {
                                    int number = Integer.parseInt(slots.get(e).getValue().trim());
                                }
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のstackの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のstackの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);
                                    }
                                    if (!isInt(slots.get(e).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のstackの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                    Matcher stackM = twoDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!stackM.matches()) {//半角数値二桁のみ
                                        formatErrorStatements.add(instance.getName() + "のstackの値は無効です。1桁または2桁の整数を入力してください");//エラー文
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("slot")) {
                                if (isInt(slots.get(e).getValue())) {
                                    int number = Integer.parseInt(slots.get(e).getValue().trim());
                                    ((EthernetSetting) instance).setSlot(number);
                                }
                                if(zenkakuCheck(slots.get(e).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のslotの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                if(!slots.get(e).getValue().isEmpty()) {
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のslotの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(e).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のslotの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                    Matcher slotM = twoDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!slotM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のslotの値は無効です。1桁または2桁の整数を入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }

                            if (ethName.equals("port")) {
                                                                if (isInt(slots.get(e).getValue())) {
                                    int number = Integer.parseInt(slots.get(e).getValue().trim());



                                    ((EthernetSetting) instance).setPort(number);
                                }
                                if(zenkakuCheck(slots.get(e).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のportの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                if(!slots.get(e).getValue().isEmpty()) {
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のportの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(e).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のportの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                    Matcher portM = twoDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!portM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のportの値は無効です。1桁または2桁の整数を入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("ipAddress")) {
                                ((EthernetSetting) instance).setIpAddress(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }

                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher ipAddressM = ipAddress.matcher(slots.get(e).getValue());
                                    if (!ipAddressM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("subnetMask")) {
                                ((EthernetSetting) instance).setSubnetMask(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher subnetMaskM = ipAddress.matcher(slots.get(e).getValue());
                                    if (!subnetMaskM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("accessVlan")) {
                                if (isInt(slots.get(e).getValue())) {
                                    int number = Integer.parseInt(slots.get(e).getValue().trim());
                                    ((EthernetSetting) instance).setAccessVlan(number);
                                }
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessVlanの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessVlanの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(e).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のaccessVlanの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                    Matcher accessVlanM = fourDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!accessVlanM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessVlanの値は無効です。4桁以内の整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("nativeVlan")) {
                                if (isInt(slots.get(e).getValue())) {
                                    int number = Integer.parseInt(slots.get(e).getValue().trim());
                                    ((EthernetSetting) instance).setNativeVlan(number);
                                }
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のnativeVlanの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnativeVlanの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(e).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のnativeVlanの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                    Matcher nativeVlanM = fourDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!nativeVlanM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnativeVlanの値は無効です。4桁以内の整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("mode")) {
                                ((EthernetSetting) instance).setMode(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のmodeの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のmodeの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("access") || slots.get(e).getValue().equals("trunk"))) {
                                        formatErrorStatements.add(instance.getName() + "のmodeの値は無効です。'access' または 'trunk' のいずれかを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }}
                            if (ethName.equals("accessListNumber")) {
                                if (isInt(slots.get(e).getValue())) {
                                    int number = Integer.parseInt(slots.get(e).getValue().trim());
                                    ((EthernetSetting) instance).setAccessListNumber(number);
                                }
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(e).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                    Matcher accessListNumberM = fourDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!accessListNumberM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("accessListName")) {
                                ((EthernetSetting) instance).setAccessListName(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListNameの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNameの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("accessListInOrOut")) {
                                ((EthernetSetting) instance).setAccessListInOrOut(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListInOrOutの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListInOrOutの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("in") || slots.get(e).getValue().equals("out"))) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListInOrOutの値は無効です。'in' または 'out' のいずれかを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("speed")) {
                                ((EthernetSetting) instance).setSpeed(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty())                                 {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のspeedの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のspeedの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("auto") || slots.get(e).getValue().equals("10") || slots.get(e).getValue().equals("100") || slots.get(e).getValue().equals("1000")|| slots.get(e).getValue().equals("10000"))) {
                                        formatErrorStatements.add(instance.getName() + "のspeedの値は無効です。'auto','10','100','1000'のいずれかを入力してください");
                                        errorInstances.add(instance);

                                    }

                                }
                            }
                            if (ethName.equals("duplex")) {
                                ((EthernetSetting) instance).setDuplex(slots.get(e).getValue());
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のduplexの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のduplexの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("auto") || slots.get(e).getValue().equals("full") || slots.get(e).getValue().equals("half"))) {
                                        formatErrorStatements.add(instance.getName() + "のduplexの値は無効です。'full' または 'half' のいずれかを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("ipVirtualReassembly")) {
                                ((EthernetSetting) instance).setIpVirtualReassembly(slots.get(e).getValue().equals("true"));
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipVirtualReassemblyの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipVirtualReassemblyの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("true") || slots.get(e).getValue().equals("false") || slots.get(e).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のipVirtualReassemblyの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }

                            }

                            if (ethName.equals("switchportTrunkEncapsulation")) {
                                ((EthernetSetting) instance).setSwitchportTrunkEncapsulation(slots.get(e).getValue().equals("true"));
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のswitchportTrunkEncapsulationの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のswitchportTrunkEncapsulationの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("true") || slots.get(e).getValue().equals("false") || slots.get(e).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のswitchportTrunkEncapsulationの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (ethName.equals("shutdown")) {
                                ((EthernetSetting) instance).setShutdown(slots.get(e).getValue().equals("true"));
                                if(!slots.get(e).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(e).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のshutdownの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のshutdownの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(e).getValue().equals("true") || slots.get(e).getValue().equals("false") || slots.get(e).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のshutdownの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }

                            }

                            if (ethName.equals("allowedVlan")) {
                                ((EthernetSetting) instance).setAllowdVlanString(slots.get(e).getValue());
                                if (!slots.get(e).getValue().isEmpty()) {
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                    if (!isInt(slots.get(e).getValue())) {
                                        if(zenkakuCheck(slots.get(e).getValue())){
                                            formatErrorStatements.add(instance.getName() + "のallowedVlanの値は無効です。入力に全角が含まれています");
                                            errorInstances.add(instance);
                                        }
                                        if (spaceM.matches()) {
                                            formatErrorStatements.add(instance.getName() + "のallowedVlanの値は無効です。 入力に半角スペースが含まれています");
                                            errorInstances.add(instance);

                                        }
                                        String[] splits = slots.get(e).getValue().split(",");
                                        for (String string : splits) {
                                            if (string.matches("^[0-9]*$")) {
                                            } else if (string.matches("^[0-9]*-[0-9]*$")) {
                                            } else if(string.matches(".*")){//田島さんのための処置　1,2, 10, 20みたいにスペースつきよう

                                            }else {
                                                formatErrorStatements.add(instance.getName() + "のallowedVlanの値は無効です。正しい形式で入力してください");
                                                errorInstances.add(instance);
                                            }
                                        }
                                    }
                                }
                            }
                        if (ethName.equals("mtu")) {

                            int number=-1;
                            if (isInt(slots.get(e).getValue())) {
                                number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setMtu(number);
                            }
                            if(!slots.get(e).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(e).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のmtuの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(e).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のmtuの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);
                                }
                                if (!isInt(slots.get(e).getValue())) {//文字列の時
                                    formatErrorStatements.add(instance.getName() + "のmtuの値は無効です。整数値を入力してください");
                                    errorInstances.add(instance);

                                }
                                Matcher mtuM = twoDigits.matcher(String.valueOf(slots.get(e).getValue()));
                                if (!(number == -1) && number<832 && number>=1500 ) {//半角数値二桁のみ 範囲は832~1500
                                    formatErrorStatements.add(instance.getName() + "のmtuの値は無効です。832～1500までの整数を入力してください");//エラー文
                                    errorInstances.add(instance);

                                }
                            }
                        }


                    }

                } else if (instanceSpecification.getClassifier().getName().equals("CiscoHostname") || (instanceSpecification.getClassifier().getName().equals("Hostname"))) {
                    instance = new Hostname();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    ((Hostname) instance).setHostName(slots.get(0).getValue());
                    if(!slots.get(0).getValue().isEmpty()){
                        if(zenkakuCheck(slots.get(0).getValue())){
                            formatErrorStatements.add(instance.getName() + "のhostNameの値は無効です。入力に全角が含まれています");
                            errorInstances.add(instance);
                        }
                        Matcher spaceM = space.matcher(String.valueOf(slots.get(0).getValue()));
                        if (spaceM.matches()) {
                            formatErrorStatements.add(instance.getName() + "のhostNameの値は無効です。 入力に半角スペースが含まれています");
                            errorInstances.add(instance);

                        }
                    }


                } else if (instanceSpecification.getClassifier().getName().equals("IpRoute")) {
                    instance = new IpRoute();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String iprName = slots.get(slotnumber).getAttribute();

                            if (iprName.equals("ipAddress")) {
                                ((IpRoute) instance).setIpAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のnetworkの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnetworkの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher networkM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!networkM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnetworkの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (iprName.equals("subnetMask")) {
                                ((IpRoute) instance).setSubnetMask(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaddressPrefixの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaddressPrefixの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher addressPrefixM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!addressPrefixM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaddressPrefixの値は無効です。有効なサブネットマスク形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }

                            }
                            if (iprName.equals("nextHopAddress")) {
                                ((IpRoute) instance).setNetHopAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のnextHopAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnextHopAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher nextHopAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!nextHopAddressM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnextHopAddressの値は無効です。有効なゲートウェイアドレス形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                    }


                } else if (instanceSpecification.getClassifier().getName().equals("Link")) {
                    instance = new Link();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    ((Link) instance).setDescription(slots.get(0).getValue());

                    if(!slots.get(0).getValue().isEmpty()) {

//                        Matcher descriptionM = linkPattern.matcher(slots.get(0).getValue());
//                        if(zenkakuCheck(slots.get(0).getValue())){
//                            formatErrorStatements.add(instance.getName() + "のdescriptionの値は無効です。入力に全角が含まれています");
//                            errorInstances.add(instance);
//                        }
//                        if (!descriptionM.matches()) {
//                            formatErrorStatements.add(instance.getName() + "のdescriptionの値は無効です。有効なdescription形式で入力してください");
//                            errorInstances.add(instance);
////
//
//                                //色の切り替え
//
//                        }
                    }


                } else if (instanceSpecification.getClassifier().getName().equals("LinkableElement")) {
                    instance = new LinkableElement();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);

                } else if (instanceSpecification.getClassifier().getName().equals("OspfInterfaceSetting") || instanceSpecification.getClassifier().getName().equals("CiscoOspfInterfaceSetting")) {
                    instance = new OspfInterfaceSetting();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String ospName = slots.get(slotnumber).getAttribute();

                            if (ospName.equals("ipAddress")) {
                                ((OspfInterfaceSetting) instance).setIpAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher ipAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!ipAddressM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (ospName.equals("wildcardMask")) {
                                ((OspfInterfaceSetting) instance).setWildcardMask(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のwildcardMaskの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のwildcardMaskの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher wildcardMaskM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!wildcardMaskM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のwildcardMaskの値は無効です。有効なワイルドカードマスク形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (ospName.equals("areaId")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((OspfInterfaceSetting) instance).setAreaId(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher areaIdM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!areaIdM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。4桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                        if (ospName.equals("helloInterval")) {
                            if (isInt(slots.get(slotnumber).getValue())) {
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((OspfInterfaceSetting) instance).setHelloInterval(number);
                            }
                            if(!slots.get(slotnumber).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のhelloIntervalの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のhelloIntervalの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                if (!isInt(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のhelloIntervalの値は無効です。整数値を入力してください");
                                    errorInstances.add(instance);
                                }
                                Matcher areaIdM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (!areaIdM.matches()) {//個々は考え直す必要あり
                                    formatErrorStatements.add(instance.getName() + "のhelloIntervalの値は無効です。5桁までの整数を入力してください");
                                    errorInstances.add(instance);
                                }
                            }
                        }

                        if (ospName.equals("deadInterval")) {
                            if (isInt(slots.get(slotnumber).getValue())) {
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((OspfInterfaceSetting) instance).setDeadInterval(number);
                            }
                            if(!slots.get(slotnumber).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のdeadIntervalの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のdeadIntervalの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                if (!isInt(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のdeadIntervalの値は無効です。整数値を入力してください");
                                    errorInstances.add(instance);
                                }
                                Matcher areaIdM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (!areaIdM.matches()) {//個々は考え直す必要あり
                                    formatErrorStatements.add(instance.getName() + "のdeadIntervalの値は無効です。5桁までの整数を入力してください");
                                    errorInstances.add(instance);
                                }
                            }
                        }
                        if (ospName.equals("ospfNetworkMode")) {
                            ((OspfInterfaceSetting) instance).setOspfNetworkMode(slots.get(slotnumber).getValue());
                            if(!slots.get(slotnumber).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のospfNetworkModeの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
//                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
//                                if (spaceM.matches()) {
//                                    formatErrorStatements.add(instance.getName() + "のospfNetworkModeの値は無効です。 入力に半角スペースが含まれています");
//                                    errorInstances.add(instance);
//
//                                }
                                if (!(slots.get(slotnumber).getValue().equals("broadcast") || slots.get(slotnumber).getValue().equals("point-to-point") || slots.get(slotnumber).getValue().equals("non-broadcast") ||slots.get(slotnumber).getValue().equals("point-to-multipoint") || slots.get(slotnumber).getValue().equals("point-to-multipoint nonbroadcast"))) {
                                    formatErrorStatements.add(instance.getName() + "のospfNetworkModeの値は無効です。'broadcast','point-to-point','non-broadcast','point-to-multipoint','point-to-multipoint nonbroadcast' のいずれかを入力してください");
                                    errorInstances.add(instance);
                                }
                            }
                        }

                        if (ospName.equals("stub")) {
                            ((OspfInterfaceSetting) instance).setStub(slots.get(slotnumber).getValue());
                            if(!slots.get(slotnumber).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のstubの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
//                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
//                                if (spaceM.matches()) {
//                                    formatErrorStatements.add(instance.getName() + "のstubの値は無効です。 入力に半角スペースが含まれています");
//                                    errorInstances.add(instance);
//
//                                }
                                if (!(slots.get(slotnumber).getValue().equals("stub") || slots.get(slotnumber).getValue().equals("stub no-summary") || slots.get(slotnumber).getValue().equals("normal") || slots.get(slotnumber).getValue().equals("nssa") ||slots.get(slotnumber).getValue().equals("nssa no-smmary") )) {
                                    formatErrorStatements.add(instance.getName() + "のstubの値は無効です。'stub','stub no-summary','nssa','nssa no-smmary'のいずれかを入力してください");
                                    errorInstances.add(instance);
                                }
                            }
                        }
                        if (ospName.equals("priority")) {
                            if (isInt(slots.get(slotnumber).getValue())) {
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((OspfInterfaceSetting) instance).setPriority(number);
                            }
                            if(!slots.get(slotnumber).getValue().isEmpty()) {
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のpriorityの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のpriorityの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                if (!isInt(slots.get(slotnumber).getValue())) {
                                    formatErrorStatements.add(instance.getName() + "のpriorityの値は無効です。整数値を入力してください");
                                    errorInstances.add(instance);
                                }

                                if (0>Integer.parseInt(slots.get(slotnumber).getValue().trim()) || 255<Integer.parseInt(slots.get(slotnumber).getValue().trim())) {
                                    formatErrorStatements.add(instance.getName() + "のpriorityの値は無効です。0~255までの整数を入力してください");
                                    errorInstances.add(instance);
                                }
                            }
                        }

                    }


                } else if (instanceSpecification.getClassifier().getName().equals("OspfSetting") || instanceSpecification.getClassifier().getName().equals("CiscoOspfSetting")) {
                    instance = new OspfSetting();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String ospName = slots.get(slotnumber).getAttribute();

                            if (ospName.equals("processId")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((OspfSetting) instance).setProcessId(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のprocessIdの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のprocessIdの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のprocessIdの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher processIdM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!processIdM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のprocessIdの値は無効です。5桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (ospName.equals("routerId")) {
                                ((OspfSetting) instance).setRouterId(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のrouterIdの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のrouterIdの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher routerIdM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!routerIdM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のrouterIdの値は無効です。有効なルーターID形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                    }


                } else if (instanceSpecification.getClassifier().getName().equals("OspfVirtualLink") || instanceSpecification.getClassifier().getName().equals("CiscoOspfVirtualLink")) {
                    instance = new OspfVirtualLink();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String ospName = slots.get(slotnumber).getAttribute();

                            if (ospName.equals("areaId")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((OspfVirtualLink) instance).setAreaId(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher arealdM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!arealdM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のareaIdの値は無効です。4桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (ospName.equals("routerId")) {
                                ((OspfVirtualLink) instance).setRouterId(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のrouterIdの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のrouterIdの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher routerIdM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!routerIdM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のrouterIdの値は無効です。有効なルーターID形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                    }


                } else if (instanceSpecification.getClassifier().getName().equals("StpSetting") ||instanceSpecification.getClassifier().getName().equals("CiscoStpSetting")) {
                    instance = new StpSetting();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String stpName = slots.get(slotnumber).getAttribute();

                            if (stpName.equals("bridgePriority")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((StpSetting) instance).setBridgePriority(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のbridgePriorityの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のbridgePriorityの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のbridgePriorityの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher bredgePriorityM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!bredgePriorityM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "bridgePriorityの値は無効です。5桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (stpName.equals("vlan")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((StpSetting) instance).setVlan(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のvlanの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のvlanの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のvlanの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher vlanM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!vlanM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "vlanの値は無効です。5桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (stpName.equals("mode")) {
                                ((StpSetting) instance).setMode(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のmodeの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のmodeの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("pvst") || slots.get(slotnumber).getValue().equals("pvst+") || slots.get(slotnumber).getValue().equals("rstp") ||slots.get(slotnumber).getValue().equals("rapid-pvst") || slots.get(slotnumber).getValue().equals("mst"))) {
                                        formatErrorStatements.add(instance.getName() + "のmodeの値は無効です。'pvst','pvst+','rstp','rapid-pvst','mstp' のいずれかを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (stpName.equals("macAddress")) {
                                ((StpSetting) instance).setMacAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のmacAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のmacAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher macAddressM = macAddress.matcher(slots.get(slotnumber).getValue());

                                    if (!macAddressM.matches() ) {
                                        formatErrorStatements.add(instance.getName() + "のmacAddressの値は無効です。有効なMACアドレス形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                    }


                } else if (instanceSpecification.getClassifier().getName().equals("Vlan")) {
                    instance = new Vlan();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String vlaName = slots.get(slotnumber).getAttribute();

                            if (vlaName.equals("num")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((Vlan) instance).setNum(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のnumの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnumの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {
                                        formatErrorStatements.add(instance.getName() + "のnumの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher arealdM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!arealdM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のnumの値は無効です。4桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlaName.equals("name")) {
                                ((Vlan) instance).setNamed(slots.get(slotnumber).getValue());
                                if (!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(!slots.get(slotnumber).getValue().isEmpty()) {
                                        if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                            formatErrorStatements.add(instance.getName() + "のnameの値は無効です。入力に全角が含まれています");
                                            errorInstances.add(instance);
                                        }
                                        Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                        if (spaceM.matches()) {
                                            formatErrorStatements.add(instance.getName() + "のnameの値は無効です。 入力に半角スペースが含まれています");
                                            errorInstances.add(instance);

                                        }
                                    }
                                }
                            }

                    }


                } else if (instanceSpecification.getClassifier().getName().equals("CiscoVlanSetting") || (instanceSpecification.getClassifier().getName().equals("VlanSetting"))) { //VlanSetting
                    instance = new VlanSetting();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String vlsName = slots.get(slotnumber).getAttribute();

                            if (vlsName.equals("vlanNum")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((VlanSetting) instance).setVlanNum(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のvlanNumの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のvlanNumの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のvlanNumの値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher vlanNumM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!vlanNumM.matches()) {//半角数値二桁のみ
                                        formatErrorStatements.add(instance.getName() + "のvlanNumの値は無効です。4桁以内の整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("ipAddress")) {
                                ((VlanSetting) instance).setIpAddress(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher ipAddressM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!ipAddressM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("subnetMask")) {
                                ((VlanSetting) instance).setSubnetMask(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    Matcher subnetMaskM = ipAddress.matcher(slots.get(slotnumber).getValue());
                                    if (!subnetMaskM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("accessListNumber")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((VlanSetting) instance).setAccessListNumber(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    } else if (!isInt(slots.get(slotnumber).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumber値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher accessListNumberM = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!accessListNumberM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("accessListName")) {
                                ((VlanSetting) instance).setAccessListName(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListNameの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListNameの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                }
                            }
                            if (vlsName.equals("accessListInOrOut")) {
                                ((VlanSetting) instance).setAccessListInOrOut(slots.get(slotnumber).getValue());
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のaccessListInOrOutの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListInOrOutの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("in") || slots.get(slotnumber).getValue().equals("out"))) {
                                        formatErrorStatements.add(instance.getName() + "のaccessListInOrOutの値は無効です。'in' または 'out' のいずれかを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("inNatInside")) {
                                ((VlanSetting) instance).setInNatInside(slots.get(slotnumber).getValue().equals("true"));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のinNatInsideの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のinNatInsideの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のinNatInsideの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("ipTcpAdjustMss")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((VlanSetting) instance).setIpTcpAdjustMss(number);
                                }
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipTcpAdjustMssの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipTcpAdjustMssの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    } else if (!isInt(slots.get(slotnumber).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のipTcpAdjustMss値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher ipTcpAdjusMss = fourDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!ipTcpAdjusMss.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipTcpAdjustMssの値は無効です。4桁以内の整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("ipVirtualReassembly")) {
                                ((VlanSetting) instance).setIpVirtualReassembly(slots.get(slotnumber).getValue().equals("true"));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のipVirtualReassemblyの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のipVirtualReassemblyの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "ipVirtualReassemblyの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (vlsName.equals("shutdown")) {
                                ((VlanSetting) instance).setShutdown(slots.get(slotnumber).getValue().equals("true"));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のshutdownの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のshutdownの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のshutdownの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);

                                    }
                                }
                            }

                    }

                } else if (instanceSpecification.getClassifier().getName().equals("EthernetType")) {
                    instance = new EthernetType();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String ethName = slots.get(slotnumber).getAttribute();

                            if (ethName.equals("Ethernet")) {
                                ((EthernetType) instance).setEthernet(slots.get(slotnumber).getValue().equals("true"));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のEthernetの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のEthernetの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のEthernetの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (ethName.equals("fastEthernet")) {
                                ((EthernetType) instance).setFastEthernet(slots.get(slotnumber).getValue().equals("true"));
                                if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                    formatErrorStatements.add(instance.getName() + "のfastEthernetの値は無効です。入力に全角が含まれています");
                                    errorInstances.add(instance);
                                }
                                Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のfastEthernetの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "のfastEthernetの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }

                            }
                            if (ethName.equals("gigabitEthernet")) {
                                ((EthernetType) instance).setGigabitEthernet(slots.get(slotnumber).getValue().equals("true"));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のgigabitEthernetの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                if (spaceM.matches()) {
                                    formatErrorStatements.add(instance.getName() + "のgigabitEthernetの値は無効です。 入力に半角スペースが含まれています");
                                    errorInstances.add(instance);

                                }
                                if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                    formatErrorStatements.add(instance.getName() + "のgigabitEthernetの値は無効です。trueまたはfalseを入力してください");
                                    errorInstances.add(instance);
                                } }
                            }
                            if (ethName.equals("10gigabitEthernet")) {
                                ((EthernetType) instance).setTengigabitEthernet(slots.get(slotnumber).getValue().equals("true"));
                                if(!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "の10gigabitEthernetの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "の10gigabitEthernetの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!(slots.get(slotnumber).getValue().equals("true") || slots.get(slotnumber).getValue().equals("false") || slots.get(slotnumber).getValue().isEmpty())) {
                                        formatErrorStatements.add(instance.getName() + "の10gigabitEthernetの値は無効です。trueまたはfalseを入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                    }

                } else if (instanceSpecification.getClassifier().getName().equals("Stack")) {
                    instance = new Stack();
                    instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for (int slotnumber = 0; slotnumber < slots.size(); slotnumber++) {
                        String staName = slots.get(slotnumber).getAttribute();
                        if(zenkakuCheck(slots.get(slotnumber).getValue())){
                            formatErrorStatements.add(instance.getName() + "のの値は無効です。入力に全角が含まれています");
                            errorInstances.add(instance);
                        }
                        Matcher spaceM = space.matcher(String.valueOf(slots.get(slotnumber).getValue()));

                            if (staName.equals("stackMemberNumber")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((Stack) instance).setStackMemberNumber(number);
                                }
                                if (!slots.get(slotnumber).getValue().isEmpty()) {
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のstackMemberNumberの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!isInt(slots.get(slotnumber).getValue())) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のstackMemberNumber値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher stackMemberNumberM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!stackMemberNumberM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "stackMemberNumberの値は無効です。5桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }
                            if (staName.equals("previousStackNumber")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((Stack) instance).setPreviousStackNumber(number);
                                }
                                if (!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のpreviousStackNumberの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のpreviousStackNumberの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!slots.get(slotnumber).getValue().isEmpty()) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のpreviousStackNumber値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }
                                    Matcher previousStackNumberM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!previousStackNumberM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "previousStackNumberの値は無効です。5桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                            if (staName.equals("stackPriority")) {
                                if (isInt(slots.get(slotnumber).getValue())) {
                                    int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                    ((Stack) instance).setStackPriority(number);
                                }
                                if (!slots.get(slotnumber).getValue().isEmpty()) {
                                    if(zenkakuCheck(slots.get(slotnumber).getValue())){
                                        formatErrorStatements.add(instance.getName() + "のstackPriorityの値は無効です。入力に全角が含まれています");
                                        errorInstances.add(instance);
                                    }
                                    if (spaceM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "のstackPriorityの値は無効です。 入力に半角スペースが含まれています");
                                        errorInstances.add(instance);

                                    }
                                    if (!slots.get(slotnumber).getValue().isEmpty()) {//文字列の時
                                        formatErrorStatements.add(instance.getName() + "のstackPriority値は無効です。整数値を入力してください");
                                        errorInstances.add(instance);
                                    }

                                    Matcher stackPriorityM = fiveDigits.matcher(String.valueOf(slots.get(slotnumber).getValue()));
                                    if (!stackPriorityM.matches()) {
                                        formatErrorStatements.add(instance.getName() + "stackPriorityの値は無効です。5桁までの整数を入力してください");
                                        errorInstances.add(instance);
                                    }
                                }
                            }

                    }
                } else {
                }

                try{
                    instance.setElement(model);//astahのインスタンス情報と自インスタンスを関連付ける
                }catch (Exception ex) {

                }


                return instance;
            }
        }
        return null;
    }

    public static boolean isInt(String str) {//int型かどうかチェックするメソッド
        boolean b = true;
        try {
            Integer.parseInt(str);
        } catch (Exception ex) {
            b = false;
        }
        return b;
    }

    public static void changeNodeInformation(IPresentation linkpresentation, ArrayList<ClassElement> instances) {//関連の情報をinstanceの情報に追加するメソッド
        ClassElement instance1 = null;
        ClassElement instance2 = null;
        if (linkpresentation instanceof ILinkPresentation) {
            ILinkPresentation link = (ILinkPresentation) linkpresentation;
            IPresentation target = link.getTargetEnd();
            IPresentation source = link.getSourceEnd();
            IElement targetElement = target.getModel();//先ほど得られた図の情報をモデルの情報に変える
            IElement sourceElement = source.getModel();
            //リンクにつながったインスタンスと対応するインスタンスを自分のinstanceから取り出して情報を追加できるようにする
            if (targetElement instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                com.change_vision.jude.api.inf.model.IInstanceSpecification targetInstance = (com.change_vision.jude.api.inf.model.IInstanceSpecification) targetElement;//インスタンスン情報に変える
                for (ClassElement instance : instances) {
                    if (instance.getElement().equals(targetElement)) {
                        instance1 = instance;
                    }
                }
            }
            if (sourceElement instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                com.change_vision.jude.api.inf.model.IInstanceSpecification sorce_instance = (com.change_vision.jude.api.inf.model.IInstanceSpecification) sourceElement;
                for (ClassElement instance : instances) {
                    if (instance.getElement().equals(sourceElement)) {
                        instance2 = instance;
//                        System.out.println("こka" + instance2);
                    }
                }
            }

            if (instance2 != null) {//応急処置
                if (instance1 != null && instance2 != null) {
                    instance1.setLink(instance2);
                    instance2.setLink(instance1);
                }

                if (instance1 instanceof AccessList) {
                    if (instance2 instanceof Config) {
                        ((AccessList) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setAccessList((AccessList) instance1);
                    } else if (instance2 instanceof AccessList) {
                        ((AccessList) instance1).setAccessList((AccessList) instance2);
                        ((AccessList) instance2).setAccessList((AccessList) instance1);
                    } else {//指定された関連以外の関連を持っていたら
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("AcessListはConfig,AccessList以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof Clients) {
                    if (instance2 instanceof Link) {
                        ((Clients) instance1).setLink((Link) instance2);
                        ((Link) instance2).setLinkableElement((Clients) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof Config) {
                    if (instance2 instanceof Vlan) {
                        ((Config) instance1).setVlan((Vlan) instance2);
                        ((Vlan) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof EthernetSetting) {
                        ((Config) instance1).setEthernetSetting((EthernetSetting) instance2);
                        ((EthernetSetting) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof Hostname) {
                        ((Config) instance1).setHostname((Hostname) instance2);
                        ((Hostname) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof VlanSetting) {
                        ((Config) instance1).setVlanSetting((VlanSetting) instance2);
                        ((VlanSetting) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof OspfSetting) {
                        ((Config) instance1).setOspfSetting((OspfSetting) instance2);
                        ((OspfSetting) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof StpSetting) {
                        ((Config) instance1).setStpSetting((StpSetting) instance2);
                        ((StpSetting) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof AccessList) {
                        ((Config) instance1).setAccessList((AccessList) instance2);
                        ((AccessList) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof IpRoute) {
                        ((Config) instance1).setIpRoute((IpRoute) instance2);
                        ((IpRoute) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof OspfSetting) {
                        ((Config) instance1).setOspfSetting((OspfSetting) instance2);
                        ((OspfSetting) instance2).setConfig((Config) instance1);
                    } else if (instance2 instanceof Stack) {
                        ((Config) instance1).setStack((Stack) instance2);
                        ((Stack) instance2).setConfig((Config) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                instance1.setErrorStatement("ConfigはVlan,EthernetSetting,Hostname,VlanSetting,OspfSetting,StpSetting,AccessList,IpRoute,OspfSetting以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof EthernetSetting) {
                    if (instance2 instanceof Config) {
                        ((EthernetSetting) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setEthernetSetting((EthernetSetting) instance1);
                    } else if (instance2 instanceof Link) {
                        ((EthernetSetting) instance1).setLink((Link) instance2);
                        ((Link) instance2).setLinkableElement((EthernetSetting) instance1);
                    } else if (instance2 instanceof EthernetType) {
                        ((EthernetSetting) instance1).setEthernetType((EthernetType) instance2);
                        ((EthernetType) instance2).setEthernetSetting((EthernetSetting) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("EthernetSettingはConfig,Link以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof Hostname) {

                    if (instance2 instanceof Config) {
                        ((Hostname) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setHostname((Hostname) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("HostnameはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof IpRoute) {
                    if (instance2 instanceof Config) {
                        ((IpRoute) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setIpRoute((IpRoute) instance2);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("HostnameはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof Link) {
                    if (instance2 instanceof Clients) {
                        ((Link) instance1).setLinkableElement((Clients) instance2);
                        ((Clients) instance2).setLink((Link) instance1);
//                        System.out.println("aa");
                    } else if (instance2 instanceof EthernetSetting) {
                        ((Link) instance1).setLinkableElement((EthernetSetting) instance2);
                        ((EthernetSetting) instance2).setLink((Link) instance1);
//                        System.out.println("bb");
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                        System.out.println("cc");
//                        System.out.println("instance1" + instance1.getName());
//                        System.out.println("instance2" + instance2);
//                        System.out.println("asdf");
//                        System.out.println("instance2" + instance2.getName());
//                    instance1.setErrorStatement("LinkはClients,EthernetSetting以外には関連を持ちません");

                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }

                }
                if (instance1 instanceof OspfInterfaceSetting) {
                    if (instance2 instanceof OspfSetting) {
                        ((OspfInterfaceSetting) instance1).setOspfSetting((OspfSetting) instance2);
                        ((OspfSetting) instance2).setOspfInterfaceSettings((OspfInterfaceSetting) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("OspfInterfaceSettingはOspfSetting以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof OspfSetting) {
                    if (instance2 instanceof Config) {
                        ((OspfSetting) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setOspfSetting((OspfSetting) instance1);
                    } else if (instance2 instanceof OspfInterfaceSetting) {
                        ((OspfSetting) instance1).setOspfInterfaceSettings((OspfInterfaceSetting) instance2);
                        ((OspfInterfaceSetting) instance2).setOspfSetting((OspfSetting) instance1);
                    } else if (instance2 instanceof OspfVirtualLink) {
                        ((OspfSetting) instance1).setOspfVirtualLink((OspfVirtualLink) instance2);
                        ((OspfVirtualLink) instance2).setOspfSetting((OspfSetting) instance2);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("OspfSettingはConfig,OspfInterfaceSetting,OspfVirtualLink以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof OspfVirtualLink) {
                    if (instance2 instanceof OspfSetting) {
                        ((OspfVirtualLink) instance1).setOspfSetting((OspfSetting) instance2);
                        ((OspfSetting) instance2).setOspfVirtualLink((OspfVirtualLink) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("OspfVirtualLinkはOspfSetting以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof StpSetting) {
                    if (instance2 instanceof Config) {
                        ((StpSetting) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setStpSetting((StpSetting) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("StpSettingはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof Vlan) {
                    if (instance2 instanceof Config) {
                        ((Vlan) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setVlan((Vlan) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof VlanSetting) {
                    if (instance2 instanceof Config) {
                        ((VlanSetting) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setVlanSetting((VlanSetting) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanSettingはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof EthernetType) {
                    if (instance2 instanceof EthernetSetting) {
                        ((EthernetType) instance1).setEthernetSetting((EthernetSetting) instance2);
                        ((EthernetSetting) instance2).setEthernetType((EthernetType) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanSettingはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
                if (instance1 instanceof Stack) {
                    if (instance2 instanceof Config) {
                        ((Stack) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setStack((Stack) instance1);
                    } else {
                        instance1.setNodeFalseInstances(instance1);
                        instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanSettingはConfig以外には関連を持ちません");
                        instance1.setmultiplicityErrorStatement(instance1.getClassName() + "と" + instance2.getClassName() + "は関連を持ちません");
                    }
                }
            }
        }
    }

    private static boolean zenkakuCheck(String inputs){

        for (char c : inputs.toCharArray()) {
            if (isFullWidth(c)) {
                return true;
            } else {

            }
        }
        return false;
    }

}
