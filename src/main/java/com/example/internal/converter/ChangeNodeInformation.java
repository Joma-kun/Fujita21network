package com.example.internal.converter;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.example.classes.*;
import com.example.element.ClassElement;

import java.util.ArrayList;

//関連の情報をinstanceの情報に追加するクラス
//クラス間の情報をいれる例ConfigとEthernetSettingがつながっているとか
public class ChangeNodeInformation {
    public static void changeNodeInformation(IPresentation linkpresentation, ArrayList<ClassElement> instances) {
        ClassElement instance1 = null;
        ClassElement instance2 = null;
        if (linkpresentation instanceof ILinkPresentation) {
            ILinkPresentation link = (ILinkPresentation) linkpresentation;
            IPresentation target = link.getTargetEnd();
            IPresentation source = link.getSourceEnd();
            IElement targetElement = target.getModel();
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
                    }
                }
                if (instance1 instanceof Clients) {
                    if (instance2 instanceof Link) {
                        ((Clients) instance1).setLink((Link) instance2);
                        ((Link) instance2).setLinkableElement((Clients) instance1);
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
                    }
                }
                if (instance1 instanceof Hostname) {

                    if (instance2 instanceof Config) {
                        ((Hostname) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setHostname((Hostname) instance1);
                    }
                }
                if (instance1 instanceof IpRoute) {
                    if (instance2 instanceof Config) {
                        ((IpRoute) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setIpRoute((IpRoute) instance2);
                    }
                }
                if (instance1 instanceof Link) {
                    if (instance2 instanceof Clients) {
                        ((Link) instance1).setLinkableElement((Clients) instance2);
                        ((Clients) instance2).setLink((Link) instance1);
//
                    } else if (instance2 instanceof EthernetSetting) {
                        ((Link) instance1).setLinkableElement((EthernetSetting) instance2);
                        ((EthernetSetting) instance2).setLink((Link) instance1);
//
                    }

                }
                if (instance1 instanceof OspfInterfaceSetting) {
                    if (instance2 instanceof OspfSetting) {
                        ((OspfInterfaceSetting) instance1).setOspfSetting((OspfSetting) instance2);
                        ((OspfSetting) instance2).setOspfInterfaceSettings((OspfInterfaceSetting) instance1);
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
                    }
                }
                if (instance1 instanceof OspfVirtualLink) {
                    if (instance2 instanceof OspfSetting) {
                        ((OspfVirtualLink) instance1).setOspfSetting((OspfSetting) instance2);
                        ((OspfSetting) instance2).setOspfVirtualLink((OspfVirtualLink) instance1);
                    }
                }
                if (instance1 instanceof StpSetting) {
                    if (instance2 instanceof Config) {
                        ((StpSetting) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setStpSetting((StpSetting) instance1);
                    }
                }
                if (instance1 instanceof Vlan) {
                    if (instance2 instanceof Config) {
                        ((Vlan) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setVlan((Vlan) instance1);
                    }
                }
                if (instance1 instanceof VlanSetting) {
                    if (instance2 instanceof Config) {
                        ((VlanSetting) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setVlanSetting((VlanSetting) instance1);
                    }
                }
                if (instance1 instanceof EthernetType) {
                    if (instance2 instanceof EthernetSetting) {
                        ((EthernetType) instance1).setEthernetSetting((EthernetSetting) instance2);
                        ((EthernetSetting) instance2).setEthernetType((EthernetType) instance1);
                    }
                }
                if (instance1 instanceof Stack) {
                    if (instance2 instanceof Config) {
                        ((Stack) instance1).setConfig((Config) instance2);
                        ((Config) instance2).setStack((Stack) instance1);
                    }
                }
            }
        }
    }
}
