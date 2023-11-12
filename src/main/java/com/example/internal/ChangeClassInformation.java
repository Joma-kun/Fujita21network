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
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String accName = slots.get(slotnumber).getAttribute();
                        if(accName.equals("accessListNumber")){
                            if(isInt(slots.get(slotnumber).getValue())){//int型に変換してAccesslistの属性に入れる処理
                                int number = Integer.parseInt(slots.get(slotnumber).getValue());
                                ((AccessList) instance).setAccessListNumber(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のaccessListNumberの値は無効です。整数値を入力してください");
                            }
                        }
                        if(accName.equals("permitOrDeny")){
                            ((AccessList) instance).setPermitOrDeny(slots.get(slotnumber).getValue());
                        }
                        if(accName.equals("protocol")){
                            ((AccessList) instance).setProtocol(slots.get(slotnumber).getValue());
                        }
                        if(accName.equals("sourceIpAddress")){
                            ((AccessList) instance).setSorceIpAddress(slots.get(slotnumber).getValue());
                        }
                        if(accName.equals("sourceWildcardMask")){
                            ((AccessList) instance).setSourceWildcardMask(slots.get(slotnumber).getValue());

                        }
                        if(accName.equals("sourcePort")){
                            ((AccessList) instance).setSourcePort(slots.get(slotnumber).getValue());
                        }
                        if(accName.equals("destIpAddress")){
                            ((AccessList) instance).setDestIpAddress(slots.get(slotnumber).getValue());
                        }
                        if(accName.equals("destWildcardMask")){
                            ((AccessList) instance).setDestWildcardMask(slots.get(slotnumber).getValue());
                        }
                        if(accName.equals("destPort")){
                            ((AccessList) instance).setDestPort(slots.get(slotnumber).getValue());
                        }
                    }





                } else if (instanceSpecification.getClassifier().getName().equals("Client")){
                    instance = new Clients();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String cliName = slots.get(slotnumber).getAttribute();
                        if(cliName.equals("name")){
                            ((Clients) instance).setNames(slots.get(slotnumber).getValue());
                        }
                        if(cliName.equals("ipAddress")){
                            ((Clients) instance).setIpAddress(slots.get(slotnumber).getValue());
                        }
                        if(cliName.equals("subnetMask")){
                            ((Clients) instance).setSubnetMask(slots.get(slotnumber).getValue());
                        }
                        if(cliName.equals("defaultGataway")){
                            ((Clients) instance).setDefaultGateway(slots.get(slotnumber).getValue());
                        }
                    }

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



                    for(int e = 0 ; e<slots.size() ; e++) {
                        String ethName = slots.get(e).getAttribute();
                        if(ethName.equals("slot")){
                            if (isInt(slots.get(e).getValue())) {
                                int number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setSlot(number);
                            }else if(!slots.get(e).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のslotの値は無効です。整数値を入力してください");
                            }
                        }

                        if(ethName.equals("port")) {
                            if (isInt(slots.get(e).getValue())) {
                                int number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setPort(number);
                            }else if(!slots.get(e).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のportの値は無効です。正整数値を入力してください");
                            }
                        }
                        if(ethName.equals("ipAddress")) {
                            ((EthernetSetting) instance).setIpAddress(slots.get(e).getValue());
                        }
                        if(ethName.equals("subnetMask")) {
                            ((EthernetSetting) instance).setSubnetMask(slots.get(e).getValue());
                        }
                        if(ethName.equals("accessVlan")) {
                            if (isInt(slots.get(e).getValue())) {
                                int number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setAccessVlan(number);
                            }else if(!slots.get(e).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のaccessVlankの値は無効です。整数値を入力してください");
                            }
                        }
                        if(ethName.equals("nativeVlan")){
                            if (isInt(slots.get(e).getValue())) {
                                int number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setNativeVlan(number);
                            }else if(!slots.get(e).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のnativeVlanの値は無効です。整数値を入力してください");
                            }
                        }
                        if(ethName.equals("mode")) {
                            ((EthernetSetting) instance).setMode(slots.get(e).getValue());
                        }
                        if(ethName.equals("accessListNumber")) {
                            if (isInt(slots.get(e).getValue())) {
                                int number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setAccessListNumber(number);
                            }else if(!slots.get(e).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のaccessListNumberの値は無効です。整数値を入力してください");
                            }
                        }
                        if(ethName.equals("accessListName")){
                            ((EthernetSetting) instance).setAccessListName(slots.get(e).getValue());
                        }
                        if(ethName.equals("accessListInOrOut")){
                            ((EthernetSetting) instance).setAccessListInOrOut(slots.get(e).getValue());

                        }
                        if(ethName.equals("speed")){
                            ((EthernetSetting) instance).setSpeed(slots.get(e).getValue());

                        }
                        if(ethName.equals("duplex")){
                            ((EthernetSetting) instance).setDuplex(slots.get(e).getValue());

                        }
                        if(ethName.equals("ipVirtualReassembly")){
                            ((EthernetSetting) instance).setIpVirtualReassembly(slots.get(e).getValue().equals("true"));
                            if(!(slots.get(e).getValue().equals("true") || slots.get(e).getValue().equals("false")|| slots.get(e).getValue().isEmpty() )){
                                instance.setAttributeErrorStatement(instance.getName()+"のipVirtualReassemblyの値は無効です。trueまたはfalseを入力してください");
                            }
                        }

                        if(ethName.equals("switchportTrunkEncapsulation")){
                            ((EthernetSetting) instance).setSwitchportTrunkEncapsulation(slots.get(e).getValue().equals("true"));
                            if(!(slots.get(e).getValue().equals("true") || slots.get(e).getValue().equals("false")|| slots.get(e).getValue().isEmpty() )){
                                instance.setAttributeErrorStatement(instance.getName()+"のswitchportTrunkEncapsulationの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                        if(ethName.equals("shutdown")){
                            ((EthernetSetting) instance).setShutdown(slots.get(e).getValue().equals("true"));
                            if(!(slots.get(e).getValue().equals("true") || slots.get(e).getValue().equals("false") || slots.get(e).getValue().isEmpty())){
                                instance.setAttributeErrorStatement(instance.getName()+"のshutdownの値は無効です。trueまたはfalseを入力してください");
                            }

                        }
                        if(ethName.equals("allowedVlan")){
                            ((EthernetSetting) instance).setAllowedVlan(slots.get(e).getValue());
                        }
                        if (ethName.equals("stack")){
                            if (isInt(slots.get(e).getValue())) {
                                int number = Integer.parseInt(slots.get(e).getValue().trim());
                                ((EthernetSetting) instance).setStack(number);
                            }else if(!slots.get(e).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のstackの値は無効です。整数値を入力してください");
                            }
                        }
                        }

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
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String iprName = slots.get(slotnumber).getAttribute();
                        if(iprName.equals("network")){
                            ((IpRoute) instance).setNetwork(slots.get(slotnumber).getValue());
                        }
                        if(iprName.equals("addressPrefix")){
                            ((IpRoute) instance).setAddressPrefix(slots.get(slotnumber).getValue());

                        }
                        if(iprName.equals("nextHopAddress")){
                            ((IpRoute) instance).setNetHopAddress(slots.get(slotnumber).getValue());

                        }
                    }



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
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String ospName = slots.get(slotnumber).getAttribute();
                        if(ospName.equals("ipAddress")){
                            ((OspfInterfaceSetting) instance).setIpAddress(slots.get(slotnumber).getValue());
                        }
                        if(ospName.equals("wildcardMask")){
                            ((OspfInterfaceSetting) instance).setWildcardMask(slots.get(slotnumber).getValue());

                        }
                        if(ospName.equals("areaId")){
                            if (isInt(slots.get(slotnumber).getValue())) {
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((OspfInterfaceSetting) instance).setAreaId(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のareaIdの値は無効です。整数値を入力してください");
                            }
                        }
                    }




                }else if (instanceSpecification.getClassifier().getName().equals("OspfSetting")){
                    instance = new OspfSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String ospName = slots.get(slotnumber).getAttribute();
                        if(ospName.equals("processId")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((OspfSetting) instance).setProcessId(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のprocessIdの値は無効です。整数値を入力してください");
                            }
                        }
                        if(ospName.equals("routerId")) {
                            ((OspfSetting) instance).setRouterId(slots.get(slotnumber).getValue());

                        }
                    }



                }else if (instanceSpecification.getClassifier().getName().equals("OspfVirtualLink")){
                    instance = new OspfVirtualLink();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String ospName = slots.get(slotnumber).getAttribute();
                        if(ospName.equals("areaId")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((OspfVirtualLink) instance).setAreaId(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のareaIdの値は無効です。整数値を入力してください");
                            }
                        }
                        if(ospName.equals("routerId")){
                            ((OspfVirtualLink) instance).setRouterId(slots.get(slotnumber).getValue());
                        }
                    }



                }else if (instanceSpecification.getClassifier().getName().equals("StpSetting")){
                    instance = new StpSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String stpName = slots.get(slotnumber).getAttribute();
                        if(stpName.equals("bridgePriority")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((StpSetting) instance).setBridgePriority(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のbridgePriorityの値は無効です。整数値を入力してください");
                            }
                        }
                        if(stpName.equals("vlan")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((StpSetting) instance).setVlan(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のvlanの値は無効です。整数値を入力してください");
                            }
                        }
                        if(stpName.equals("mode")){
                            ((StpSetting) instance).setMode(slots.get(slotnumber).getValue());

                        }
                        if(stpName.equals("macAddress")){
                            ((StpSetting) instance).setMacAddress(slots.get(slotnumber).getValue());
                        }
                    }





                }else if (instanceSpecification.getClassifier().getName().equals("Vlan")){
                    instance = new Vlan();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String vlaName = slots.get(slotnumber).getAttribute();
                        if(vlaName.equals("num")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((Vlan) instance).setNum(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){
                                instance.setAttributeErrorStatement(instance.getName()+"のnumの値は無効です。整数値を入力してください");
                            }
                        }
                        if(vlaName.equals("name")){
                            ((Vlan) instance).setNamed(slots.get(slotnumber).getValue());
                        }
                    }





                }else if (instanceSpecification.getClassifier().getName().equals("VlanSetting")) {
                    instance = new VlanSetting();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String vlsName = slots.get(slotnumber).getAttribute();
                        if(vlsName.equals("vlanNum")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((VlanSetting) instance).setVlanNum(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のvlanNumの値は無効です。整数値を入力してください");
                            }
                        }
                        if(vlsName.equals("ipAddress")){
                            ((VlanSetting) instance).setIpAddress(slots.get(slotnumber).getValue());
                        }
                        if(vlsName.equals("subnetMask")){
                            ((VlanSetting) instance).setSubnetMask(slots.get(slotnumber).getValue());
                        }
                        if(vlsName.equals("accessListNumber")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((VlanSetting) instance).setAccessListNumber(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のaccessListNumber値は無効です。整数値を入力してください");
                            }
                        }
                        if(vlsName.equals("accessListName")){
                            ((VlanSetting) instance).setAccessListName(slots.get(slotnumber).getValue());
                        }
                        if(vlsName.equals("accessListInOrOut")){
                            ((VlanSetting) instance).setAccessListInOrOut(slots.get(slotnumber).getValue());
                        }
                        if(vlsName.equals("inNatInside")){
                            ((VlanSetting) instance).setInNatInside(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"のinNatInsideの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                        if(vlsName.equals("ipTcpAdjustMss")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((VlanSetting) instance).setIpTcpAdjustMss(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のipTcpAdjustMss値は無効です。整数値を入力してください");
                            }
                        }
                        if(vlsName.equals("ipVirtualReassembly")){
                            ((VlanSetting) instance).setIpVirtualReassembly(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"ipVirtualReassemblyの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                        if(vlsName.equals("shutdown")){
                            ((VlanSetting) instance).setShutdown(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"のshutdownの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                    }

                }else if(instanceSpecification.getClassifier().getName().equals("EthernetType")){
                    instance = new EthernetType();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String ethName = slots.get(slotnumber).getAttribute();
                        if(ethName.equals("Ethernet")){
                            ((EthernetType) instance).setEthernet(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"のEthernetの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                        if(ethName.equals("fastEthernet")){
                            ((EthernetType) instance).setFastEthernet(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"のfastEthernetの値は無効です。trueまたはfalseを入力してください");
                            }

                        }
                        if(ethName.equals("gigabitEthernet")){
                            ((EthernetType) instance).setGigabitEthernet(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"のgigabitEthernetの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                        if(ethName.equals("10gigabitEthernet")){
                            ((EthernetType) instance).setTengigabitEthernet(slots.get(slotnumber).getValue().equals("true"));
                            if(!(slots.get(slotnumber).getValue()=="true" || slots.get(slotnumber).getValue()=="true" )){
                                instance.setAttributeErrorStatement(instance.getName()+"の10gigabitEthernetの値は無効です。trueまたはfalseを入力してください");
                            }
                        }
                    }

                }else if(instanceSpecification.getClassifier().getName().equals("Stack")){
                    instance = new Stack();
                    instance.setClassName(instanceSpecification.getClassifier().getName());
                    instance.setName(instanceSpecification.getName());
                    instance.setSlots(slots);
                    for(int slotnumber = 0 ; slotnumber<slots.size() ; slotnumber++) {
                        String staName = slots.get(slotnumber).getAttribute();
                        if(staName.equals("stackMemberNumber")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((Stack) instance).setStackMemberNumber(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のstackMemberNumber値は無効です。整数値を入力してください");
                            }
                        }
                        if(staName.equals("previousStackNumber")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((Stack) instance).setPreviousStackNumber(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のpreviousStackNumber値は無効です。整数値を入力してください");
                            }
                        }
                        if(staName.equals("stackPriority")){
                            if(isInt(slots.get(slotnumber).getValue())){
                                int number = Integer.parseInt(slots.get(slotnumber).getValue().trim());
                                ((Stack) instance).setStackPriority(number);
                            }else if(!slots.get(slotnumber).getValue().isEmpty()){//文字列の時
                                instance.setAttributeErrorStatement(instance.getName()+"のstackPriority値は無効です。整数値を入力してください");
                            }
                        }
                    }
                }

                else {}
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
                }else if(instance2 instanceof Stack){
                    ((Config) instance1).setStack((Stack) instance2);
                    ((Stack) instance2).setConfig((Config) instance1);
                }
                else{
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
                }
                else if(instance2 instanceof EthernetType){
                    ((EthernetSetting) instance1).setEthernetType((EthernetType) instance2);
                    ((EthernetType) instance2).setEthernetSetting((EthernetSetting) instance1);
                }
                    else{
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
                else if(instance2 instanceof EthernetSetting){
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
            if(instance1 instanceof EthernetType){
                if(instance2 instanceof EthernetSetting){
                    ((EthernetType) instance1).setEthernetSetting((EthernetSetting) instance2);
                    ((EthernetSetting) instance2).setEthernetType((EthernetType) instance1);
                }else{
                    instance1.setNodeFalseInstances(instance1);
                    instance1.setNodeFalseInstances(instance2);
//                    instance1.setErrorStatement("VlanSettingはConfig以外には関連を持ちません");
                    instance1.setErrorStatement(instance1.getClassName()+"と"+instance2.getClassName()+"は関連を持ちません");
                }
            }
            if(instance1 instanceof Stack){
                if(instance2 instanceof Config){
                    ((Stack) instance1).setConfig((Config) instance2);
                    ((Config) instance2).setStack((Stack) instance1);
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
