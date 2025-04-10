package com.example.internal;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.example.classes.*;
import com.example.element.ClassElement;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class CreateFile {
    public static void Createfile(ArrayList<ClassElement> classElements) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String currentDirectory = System.getProperty("user.dir");
            // プログラムと同じディレクトリに networkmodel.txt を作成
            File file = new File(currentDirectory + "/networkmodel.json");


            // 出力先の確認
            mapper.writeValue(file, classElements);
            System.out.println("JSONファイルに書き込みました: " + file);


        } catch (IOException e) {
            e.printStackTrace();
        }
}
    public static void CreateLinkfile(ArrayList<ClassElement> classElements){
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("1");
        mapper.enable(SerializationFeature.INDENT_OUTPUT);//インデントの設定（整形）
        try {
            String currentDirectory = System.getProperty("user.dir");
            System.out.println("2");
            // プログラムと同じディレクトリに networkmodel.txt を作成
            File file = new File(currentDirectory + "/networkmodelkanren.json");
            System.out.println("3");
            ArrayNode arrayNode = mapper.createArrayNode();

//            EthernetSetting instance = new EthernetSetting();
//            instance.setName("sa");
//            instance.setConfig(new Config());
//            ObjectNode objectNode = mapper.createObjectNode();
//                   objectNode.put("EthernetSetting",instance.getId());
//                   objectNode.put("Config", ((EthernetSetting) instance).getConfig().getId());
////                    objectNode.put("Link", ((EthernetSetting) instance).getLink().getId());
//                   arrayNode.add(objectNode);

            for(ClassElement instance : classElements){
                if (instance instanceof EthernetSetting) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("EthernetSetting",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((EthernetSetting) instance).getConfig() != null){
                        links.add(mapper.createObjectNode().put("Config", ((EthernetSetting) instance).getConfig().getId()));

                    }
                    if(((EthernetSetting) instance).getLink() !=null){
                        links.add(mapper.createObjectNode().put("Link", ((EthernetSetting) instance).getLink().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                                   }
                if (instance instanceof VlanSetting) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("VlanSetting",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((VlanSetting) instance).getConfig() != null){
                        links.add(mapper.createObjectNode().put("Config", ((VlanSetting) instance).getConfig().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof AccessList) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("AccessList",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if((( AccessList) instance).getConfig() != null){
                        links.add(mapper.createObjectNode().put("Config", (( AccessList) instance).getConfig().getId()));
                    }
                    for(AccessList ac :((AccessList) instance).getAccessList()){
                        links.add(mapper.createObjectNode().put("AccessList", ac.getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof Clients) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("Client",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((Clients) instance).getLink() !=null){
                        links.add(mapper.createObjectNode().put("Link", ((Clients) instance).getLink().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof Config) {
                }
                if (instance instanceof Hostname) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("Hostname",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((Hostname) instance).getConfig() !=null){
                        links.add(mapper.createObjectNode().put("Config", ((Hostname) instance).getConfig().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof Link) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("Link",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    for(LinkableElement li : ((Link) instance).getLinkableElement()){
                        links.add(mapper.createObjectNode().put("LinkableElement", li.getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);

                }
                if (instance instanceof IpRoute) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("IpRoute",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((IpRoute) instance).getConfig() !=null){
                        links.add(mapper.createObjectNode().put("Config", ((IpRoute) instance).getConfig().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof OspfInterfaceSetting) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("OspfInterfaceSetting",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((OspfInterfaceSetting) instance).getOspfSetting() !=null){
                        links.add(mapper.createObjectNode().put("OspfSetting", ((OspfInterfaceSetting) instance).getOspfSetting().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof OspfSetting) {
                }
                if (instance instanceof OspfVirtualLink) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("OspfVirtualLink",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((OspfVirtualLink) instance).getOspfSetting() !=null){
                        links.add(mapper.createObjectNode().put("OspfSetting", ((OspfVirtualLink) instance).getOspfSetting().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof Vlan) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("Vlan",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((Vlan) instance).getConfig() !=null){
                        links.add(mapper.createObjectNode().put("Config", ((Vlan) instance).getConfig().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof StpSetting) {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("StpSetting",instance.getId());
                    ArrayNode links = mapper.createArrayNode();
                    if(((StpSetting) instance).getConfig() !=null){
                        links.add(mapper.createObjectNode().put("Config", ((StpSetting) instance).getConfig().getId()));
                    }
                    objectNode.set("kanren",links);
                    arrayNode.add(objectNode);
                }
                if (instance instanceof Stack) {
                }
            }
            System.out.println("4");
            // 出力先の確認
            if(arrayNode==null){
                System.out.println("Error: fe is null.");
            }else{
                System.out.println("Error:dnull.");
            }
            System.out.println("deaou");
            if (file != null) {
                mapper.writeValue(file, arrayNode);
            } else {
                System.out.println("Error: file is null.");
            }
            System.out.println("JSON関連ファイルに書き込みました: " + file);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void CreateCheckFile(ArrayList<ErrorInfo> errorInfos){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // JSONを整形する

//         errorInfos.add(new ErrorInfo("a",true , "ew",null));
        try {
            String currentDirectory = System.getProperty("user.dir");
            System.out.println("2");
            // プログラムと同じディレクトリに networkmodel.txt を作成
            File file = new File(currentDirectory + "/check.json");
            // JSONファイルに保存
            objectMapper.writeValue(file, errorInfos);


            System.out.println("JSONファイル 'errors.json' を作成しました。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
