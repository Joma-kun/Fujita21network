package com.example.internal.converter;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.example.classes.Config;
import com.example.classes.EthernetSetting;
import com.example.classes.Link;
import com.example.classes.LinkableElement;
import com.example.element.ClassElement;
import com.example.element.LinkElement;

import java.io.File;
import java.util.ArrayList;

//変換処理に使うメソッドを格納しておくクラス
public class SetOthersInformation {
    //EthernetSettingのEthernetSettingを格納する処
    public static void setConectedConfigs(ArrayList<ClassElement> instances) {
        //対向のEthernetSetting(Client)をわかりやすくするため　EthernetSetting(自分)-Link-EthernetSetting(対向)
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



    public static void setallowedVlans(ArrayList<ClassElement> instances) {//allowedVlanを設定するメソッド
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


    //idをセットするメソッド
    public static void setId(ArrayList<ClassElement> instances, ProjectAccessor projectAccessor) {
        try {
            String projectFile = new File(projectAccessor.getProjectPath()).getName();

            if (projectFile == null) {
                throw new IllegalStateException("プロジェクトファイルのパスが取得できません。");
            }

            IDiagram diagram = projectAccessor.getViewManager().getDiagramViewManager().getCurrentDiagram();
            if (diagram == null) {
                throw new IllegalStateException("現在開いている図が取得できません。");
            }

            String diagramName = diagram.getName();
            INamedElement owner = (INamedElement) diagram.getOwner();
            String packageName = owner.getName();
            for (ClassElement classElement : instances) {
                String id = String.join("/",
                        projectFile,
                        packageName,
                        diagramName,
                        classElement.getClassName(),
                        classElement.getName()
                );
                classElement.setId(id);
            }

        } catch (ProjectNotFoundException | InvalidUsingException e) {
            throw new RuntimeException("IDの設定中に例外が発生しました", e);
        }
    }

    //
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

}
