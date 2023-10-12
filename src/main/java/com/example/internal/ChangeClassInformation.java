package com.example.internal;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.example.classes.*;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
import com.example.element.Slots;

import java.util.ArrayList;

public class ChangeClassInformation {


    public static  ArrayList<ClassElement> changeAllElement (ArrayList<IPresentation> presentations){
        ArrayList<ClassElement> instances = new ArrayList<>();//生成したインスタンスのためのリスト
        for( IPresentation presentation : presentations){//astahの図の情報
            ClassElement instance = changeInstanceInfomation(presentation);
            if (instance != null){
                instances.add(instance);//自分たちのinstance仕様に変換しリストに追加する
            }
        }//instancesに情報を変換して入れていある状態（関連の情報は入っていない）
        for (IPresentation presentation :presentations){
            changeNodeInformation(presentation,instances);//関連の情報をインスタンスの情報に追加する
        }
        setLinkConfig(instances);
        return  instances;
    }

    //configのリンクでつながれたConfigをConfigクラスのlinkedConfig属性に格納する処理。隣接リスト表現
    public  static  void  setLinkConfig(ArrayList<ClassElement> instances){
        for(ClassElement instance : instances){
            if(instance instanceof Config){
                ArrayList<EthernetSetting> ethernetSettings = ((Config) instance).getEthernetSetting();
                for(EthernetSetting ethernetSetting : ethernetSettings){
                    Link link = ethernetSetting.getLink();
                    EthernetSetting ethernetSettingTarget = null;
                    for(LinkableElement linked : link.getLinkableElement()){
                        if(linked != ethernetSetting){
                            if(linked instanceof EthernetSetting){
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
    public static ArrayList<LinkElement> changeLinkInformation(ArrayList<IPresentation> presentations, ArrayList<ClassElement> instances){
        ArrayList<LinkElement> links = new ArrayList<>();
        for(IPresentation presentation : presentations){
            if(presentation instanceof ILinkPresentation){
                LinkElement link = new LinkElement();
                link.setLinkPresentation((ILinkPresentation) presentation);
                for(ClassElement instance : instances){
                    if(instance.getPresentation()==((ILinkPresentation) presentation).getSourceEnd()){
                        link.setSourceEnd(instance);
                    }else if(instance.getPresentation()==((ILinkPresentation) presentation).getTargetEnd()){
                        link.setTargetEnd(instance);
                    }
                }
                links.add(link);
            }
        }
        return links;
    }


    public static ClassElement changeInstanceInfomation(IPresentation nodepresentation) {//自分たちのinstance情報に変換するためのメソッド
        if (nodepresentation instanceof INodePresentation){//インスタンスの名前、スロットの処理
            IElement model = nodepresentation.getModel();
            if(model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification){
                com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;

                ClassElement instance = null;

                ISlot[] slot = instanceSpecification.getAllSlots();//astahのインスタンスの属性値を取得してslotとする
                ArrayList<Slots> slots = new ArrayList<>();//自プロジェクトのslotsを用意する
                for(ISlot s :slot) {
                    Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
                    slots.add(sl);//astah属性の値などを自プロジェクトのslotsに格納する
                }


                //インスタンスの名前によって識別し、自分のインスタンス情報に変換する
                if(instanceSpecification.getClassifier().getName().equals("AccessList")){
                    instance = new AccessList();//自分たちのインスタンスのアクセスリストを作る
                    instance.setClassName(instanceSpecification.getClassifier().getName());//クラスの名前（AccessListを登録する）
                    instance.setName(instanceSpecification.getName());//インスタンスの名前を登録する(例：Acc_1など ）
                    instance.setSlots(slots);//instance のスロットに格納する
                    if(isInt(slots.get(0).getValue())){//int型に変換してAccesslistの属性に入れる処理
                        int number = Integer.parseInt(slots.get(0).getValue());
                        ((AccessList) instance).setAccessListNumber(number);
                    }
                    ((AccessList) instance).setPermitOrDeny(slots.get(1).getValue());
                    ((AccessList) instance).setAccessListInfo(slots.get(2).getValue());


                } else if (instanceSpecification.getClassifier().getName().equals("Client")){
                    instance = new Clients();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    ((Clients) instance).setNames(slots.get(0).getValue());
                    ((Clients) instance).setIpAddress(slots.get(1).getValue());
                    ((Clients) instance).setSubnetMask(slots.get(1).getValue());
                    ((Clients) instance).setDefaultGateway(slots.get(1).getValue());


                }else if (instanceSpecification.getClassifier().getName().equals("Config")){

                    instance = new Config();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    instance.setName(instanceSpecification.getName());
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    ((Config) instance).setDeviceModel(slots.get(0).getValue());
                }else if (instanceSpecification.getClassifier().getName().equals("EthernetSetting")){
                    instance = new EthernetSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    if(isInt(slots.get(0).getValue())){
                        int number = Integer.parseInt(slots.get(0).getValue().trim());
                        ((EthernetSetting) instance).setSlot(number);
                    }
                    if(isInt(slots.get(1).getValue())){
                        int number = Integer.parseInt(slots.get(1).getValue().trim());
                        ((EthernetSetting) instance).setPort(number);
                    }
                    ((EthernetSetting) instance).setIpAddress(slots.get(2).getValue());
                    ((EthernetSetting) instance).setSubnetMask(slots.get(3).getValue());
                    if(isInt(slots.get(4).getValue())){
                        int number = Integer.parseInt(slots.get(4).getValue().trim());
                        ((EthernetSetting) instance).setAccessVlan(number);
                    }
                    if(isInt(slots.get(5).getValue())){
                        int number = Integer.parseInt(slots.get(5).getValue().trim());
                        ((EthernetSetting) instance).setNativeVlan(number);
                    }
                    ((EthernetSetting) instance).setMode(slots.get(6).getValue());
                    if(isInt(slots.get(7).getValue())){
                        int number = Integer.parseInt(slots.get(7).getValue().trim());
                        ((EthernetSetting) instance).setAccessListNumber(number);
                    }
                    ((EthernetSetting) instance).setAccessListName(slots.get(8).getValue());
                    ((EthernetSetting) instance).setAccessListInOrOut(slots.get(9).getValue());
                    ((EthernetSetting) instance).setSpeed(slots.get(10).getValue());
                    ((EthernetSetting) instance).setDuplex(slots.get(11).getValue());
                    ((EthernetSetting) instance).setDuplex(slots.get(11).getValue());
                    ((EthernetSetting) instance).setIpVirtualReassembly(slots.get(12).getValue().equals("true"));
                    ((EthernetSetting) instance).setIpAccessGroup(slots.get(12).getValue());
                    ((EthernetSetting) instance).setSwitchportTrunkEncapsulation(slots.get(13).getValue().equals("true"));
                    ((EthernetSetting) instance).setShutdown(slots.get(14).getValue().equals("true"));

                }else if (instanceSpecification.getClassifier().getName().equals("Hostname")){
                    instance = new Hostname();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    ((Hostname) instance).setHostName(slots.get(0).getValue());
                }else if (instanceSpecification.getClassifier().getName().equals("IpRoute")){
                    instance = new IpRoute();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    ((IpRoute) instance).setNetwork(slots.get(0).getValue());
                    ((IpRoute) instance).setAddressPrefix(slots.get(1).getValue());
                    ((IpRoute) instance).setIpAddress(slots.get(2).getValue());


                }else if (instanceSpecification.getClassifier().getName().equals("Link")){
                    instance = new Link();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    ((Link) instance).setDescription(slots.get(0).getValue());


                }else if (instanceSpecification.getClassifier().getName().equals("LinkableElement")){
                    instance = new LinkableElement();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);

                }else if (instanceSpecification.getClassifier().getName().equals("OspfInterfaceSetting")){
                    instance = new OspfInterfaceSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);

                    ((OspfInterfaceSetting) instance).setIpAddress(slots.get(0).getValue());
                    ((OspfInterfaceSetting) instance).setWildcardMask(slots.get(1).getValue());
                    if(isInt(slots.get(2).getValue())){
                        int number = Integer.parseInt(slots.get(2).getValue().trim());
                        ((OspfInterfaceSetting) instance).setAreaId(number);
                    }


                }else if (instanceSpecification.getClassifier().getName().equals("OspfSetting")){
                    instance = new OspfSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);

                    if(isInt(slots.get(0).getValue())){
                        int number = Integer.parseInt(slots.get(0).getValue().trim());
                        ((OspfSetting) instance).setProcessId(number);
                    }
                    ((OspfSetting) instance).setRouterId(slots.get(1).getValue());



                }else if (instanceSpecification.getClassifier().getName().equals("OspfVirtualLink")){
                    instance = new OspfVirtualLink();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    if(isInt(slots.get(0).getValue())){
                        int number = Integer.parseInt(slots.get(0).getValue().trim());
                        ((OspfVirtualLink) instance).setAreaId(number);
                    }
                    ((OspfVirtualLink) instance).setRouterId(slots.get(1).getValue());

                }else if (instanceSpecification.getClassifier().getName().equals("StpSetting")){
                    instance = new StpSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    if(isInt(slots.get(0).getValue())){
                        int number = Integer.parseInt(slots.get(0).getValue().trim());
                        ((StpSetting) instance).setBridgePriority(number);
                    }
                    if(isInt(slots.get(1).getValue())){
                        int number = Integer.parseInt(slots.get(1).getValue().trim());
                        ((StpSetting) instance).setVlan(number);
                    }
                    if(isInt(slots.get(2).getValue())){
                        int number = Integer.parseInt(slots.get(2).getValue().trim());
                        ((StpSetting) instance).setMode(number);
                    }
                    if(isInt(slots.get(3).getValue())){
                        int number = Integer.parseInt(slots.get(3).getValue().trim());
                        ((StpSetting) instance).setMacAddress(number);
                    }


                }else if (instanceSpecification.getClassifier().getName().equals("Vlan")){
                    instance = new Vlan();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    if(isInt(slots.get(0).getValue())){
                        int number = Integer.parseInt(slots.get(0).getValue().trim());
                        ((Vlan) instance).setNum(number);
                    }
                    ((Vlan) instance).setNamed(slots.get(1).getValue());



                }else if (instanceSpecification.getClassifier().getName().equals("VlanSetting")) {
                    instance = new VlanSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    if(isInt(slots.get(0).getValue())){
                        int number = Integer.parseInt(slots.get(0).getValue().trim());
                        ((VlanSetting) instance).setVlanNum(number);
                    }
                    ((VlanSetting) instance).setIpAddress(slots.get(1).getValue());
                    ((VlanSetting) instance).setSubnetMask(slots.get(2).getValue());
                    if(isInt(slots.get(3).getValue())){
                        int number = Integer.parseInt(slots.get(3).getValue().trim());
                        ((VlanSetting) instance).setAccessListNumber(number);
                    }
                    ((VlanSetting) instance).setAccessListName(slots.get(4).getValue());
                    ((VlanSetting) instance).setAccessListInOrOut(slots.get(5).getValue());
                    ((VlanSetting) instance).setInNatInside(slots.get(6).getValue().equals("true"));

                    if(isInt(slots.get(7).getValue())){
                        int number = Integer.parseInt(slots.get(7).getValue().trim());
                        ((VlanSetting) instance).setIpTcpAdjustMss(number);
                    }
                    ((VlanSetting) instance).setIpVirtualReassembly(slots.get(8).getValue().equals("true"));
                    ((VlanSetting) instance).setIpAccessGroup(slots.get(9).getValue());
                    ((VlanSetting) instance).setShutdown(slots.get(10).getValue().equals("true"));

                }else {}
                instance.setPresentation(nodepresentation);//astahのインスタンス情報と自インスタンスを関連付ける
                instance.setElement(model);//astahのインスタンス情報と自インスタンスを関連付ける
                return instance;
            }
        }
        return null;
    }

    public static boolean isInt(String str){//int型かどうかチェックするメソッド
        boolean b = true;
        try{
            Integer.parseInt(str);
        }catch(Exception ex){
            b = false;
        }
        return b;
    }
    public static void changeNodeInformation(IPresentation linkpresentation, ArrayList<ClassElement> instances){//関連の情報をinstanceの情報に追加するメソッド
        ClassElement instance1 = null;
        ClassElement instance2 = null;
        if(linkpresentation instanceof ILinkPresentation){
            ILinkPresentation link = (ILinkPresentation) linkpresentation;
            IPresentation target = link.getTargetEnd();
            IPresentation source = link.getSourceEnd();
            IElement targetElement =target.getModel();//先ほど得られた図の情報をモデルの情報に変える
            IElement sourceElement =source.getModel();
            //リンクにつながったインスタンスと対応するインスタンスを自分のinstanceから取り出して情報を追加できるようにする
            if(targetElement instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                com.change_vision.jude.api.inf.model.IInstanceSpecification targetInstance = (com.change_vision.jude.api.inf.model.IInstanceSpecification) targetElement;//インスタンスン情報に変える
                for(ClassElement instance :instances){
                    if(instance.getElement().equals(targetElement)){
                        instance1 =instance;
                    }
                }
            }
            if(sourceElement instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                com.change_vision.jude.api.inf.model.IInstanceSpecification sorce_instance = (com.change_vision.jude.api.inf.model.IInstanceSpecification) sourceElement;
                for(ClassElement instance :instances){
                    if(instance.getElement().equals(sourceElement)){
                        instance2 = instance;
                    }
                }
            }

            if(instance1!=null && instance2 != null){
                instance1.setLink(instance2);
                instance2.setLink(instance1);
            }

            if(instance1 instanceof AccessList){
                if(instance2 instanceof Config){
                    ((AccessList) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setAccessList((AccessList) instance1);
                }
                else if(instance2 instanceof  AccessList){
                    ((AccessList) instance1).setAccessList((AccessList) instance2);
                    ((AccessList) instance2).setAccessList((AccessList) instance1);
                }
                else{//指定された関連以外の関連を持っていたら
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("AcessListはConfig,AccessList以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof Clients){
                if(instance2 instanceof Link){
                    ((Clients) instance1).setLink((Link) instance2);
                    ((Link) instance2).setLinkableElement((Clients) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof Config){
                if(instance2 instanceof Vlan){
                    ((Config) instance1).setVlan((Vlan) instance2);
                    ((Vlan) instance2).setConfig((Config) instance1);
                }
                else if(instance2 instanceof EthernetSetting){
                    ((Config) instance1).setEthernetSetting((EthernetSetting) instance2);
                    ((EthernetSetting) instance2).setConfig((Config) instance1);
                }
                else if(instance2 instanceof Hostname){
                    ((Config) instance1).setHostname((Hostname) instance2);
                    ((Hostname) instance2).setConfig((Config) instance1);
                }
                else if (instance2 instanceof VlanSetting){
                    ((Config) instance1).setVlanSetting((VlanSetting) instance2);
                    ((VlanSetting) instance2).setConfig((Config) instance1);
                }
                else if (instance2 instanceof OspfSetting) {
                    ((Config) instance1).setOspfSetting((OspfSetting) instance2);
                    ((OspfSetting) instance2).setConfig((Config) instance1);
                }
                else if (instance2 instanceof StpSetting){
                    ((Config) instance1).setStpSetting((StpSetting) instance2);
                    ((StpSetting) instance2).setConfig((Config) instance1);
                }
                else if (instance2 instanceof AccessList){
                    ((Config) instance1).setAccessList((AccessList) instance2);
                    ((AccessList) instance2).setConfig((Config) instance1);
                }
                else if (instance2 instanceof IpRoute){
                    ((Config) instance1).setIpRoute((IpRoute) instance2);
                    ((IpRoute) instance2).setConfig((Config) instance1);
                }
                else if (instance2 instanceof OspfSetting) {
                    ((Config) instance1).setOspfSetting((OspfSetting) instance2);
                    ((OspfSetting) instance2).setConfig((Config) instance1);
                }else{
                instance1.setNodeFalseInstances(instance1);
                instance1.setNodeFalseInstances(instance2);
//                instance1.setErrorStatement("ConfigはVlan,EthernetSetting,Hostname,VlanSetting,OspfSetting,StpSetting,AccessList,IpRoute,OspfSetting以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
            }}
            if (instance1 instanceof EthernetSetting){
                if(instance2 instanceof Config){
                    ((EthernetSetting) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setEthernetSetting((EthernetSetting) instance1);
                }
                else if(instance2 instanceof Link){
                    ((EthernetSetting) instance1).setLink((Link) instance2);
                    ((Link) instance2).setLinkableElement((EthernetSetting) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("EthernetSettingはConfig,Link以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof Hostname){

                if(instance2 instanceof Config){
                    ((Hostname) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setHostname((Hostname) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("HostnameはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof IpRoute){
                if(instance2 instanceof Config){
                    ((IpRoute) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setIpRoute((IpRoute) instance2);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("HostnameはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof Link){
                if(instance2 instanceof Clients){
                    ((Link) instance1).setLinkableElement((Clients) instance2);
                    ((Clients) instance2).setLink((Link) instance1);
                }
                if(instance2 instanceof EthernetSetting){
                    ((Link) instance1).setLinkableElement((EthernetSetting) instance2);
                    ((EthernetSetting) instance2).setLink((Link) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("LinkはClients,EthernetSetting以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }

            }
            if(instance1 instanceof OspfInterfaceSetting){
                if(instance2 instanceof OspfSetting){
                    ((OspfInterfaceSetting) instance1).setOspfSetting((OspfSetting) instance2);
                    ((OspfSetting) instance2).setOspfInterfaceSettings((OspfInterfaceSetting) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("OspfInterfaceSettingはOspfSetting以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof OspfSetting){
                if(instance2 instanceof Config){
                    ((OspfSetting) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setOspfSetting((OspfSetting) instance1);
                }
                else if(instance2 instanceof OspfInterfaceSetting){
                    ((OspfSetting) instance1).setOspfInterfaceSettings((OspfInterfaceSetting) instance2);
                    ((OspfInterfaceSetting) instance2).setOspfSetting((OspfSetting) instance1);
                }
                else if(instance2 instanceof OspfVirtualLink){
                    ((OspfSetting) instance1).setOspfVirtualLink((OspfVirtualLink) instance2);
                    ((OspfVirtualLink) instance2).setOspfSetting((OspfSetting) instance2);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("OspfSettingはConfig,OspfInterfaceSetting,OspfVirtualLink以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof OspfVirtualLink){
                if(instance2 instanceof OspfSetting){
                    ((OspfVirtualLink) instance1).setOspfSetting((OspfSetting) instance2);
                    ((OspfSetting) instance2).setOspfVirtualLink((OspfVirtualLink) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("OspfVirtualLinkはOspfSetting以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof StpSetting){
                if(instance2 instanceof Config){
                    ((StpSetting) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setStpSetting((StpSetting) instance1);
                }else {
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("StpSettingはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof Vlan){
                if(instance2 instanceof Config){
                    ((Vlan) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setVlan((Vlan) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof VlanSetting){
                if(instance2 instanceof Config){
                    ((VlanSetting) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setVlanSetting((VlanSetting) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanSettingはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
        }
    }
}
