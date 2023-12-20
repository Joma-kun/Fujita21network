package com.example.internal;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.example.classes.*;
import com.example.element.ClassElement;
import com.example.element.LinkElement;


import java.awt.*;
import java.util.*;
import java.util.Stack;

public class Check {


    //新しく図を追加したらうまくいかないことがある
    /*実装したチェック処理
     * 必ず指定された関連先を持ち、多重度のの制限を満たす.何も関連を持ってない物のチェック
     * 必ず関連を持たないといけない物（Linkは二つ関連を持つ）他は要検討
     * ipアドレスの重複　（警告）
     *　Vlanの重複（未実装）
     */

    static String red = "#ff0000";//エラーの色
    static String orangered = "#ff7f50";

    /*
     * 必ず指定された関連先を持ち、多重度を満たす
     * 引数　textarea : 文章を出力するコンポーネント, instances :すべてのインスタンスの配列
     *      presentations ：astahの線の色を変えるために加えた。
     * ChangeClassInformationのchangenodeinformationで指定された関連以外の関連をCheckする
     * 各ClassesのSet<ClassElement>で多重度のチェックを行っている
     * errostatementsに関連に対するエラー文とnodefalseinstanceに「関連でfalseがでたインスタンス」が格納される*/
    public static void nodeCheck(TextArea textarea, ArrayList<ClassElement> instances, ArrayList<LinkElement> links) throws InvalidEditingException {
        for (ClassElement instance : instances) {//すべてのインスタンスについて
            //Linkの関連が二つだけなことをチェックする
            if (instance instanceof Link) {
                if (((Link) instance).getLinkableElement().size() != 2) {
                    changeColor(instance, red);
                    instance.setErrorStatement("Linkは二つの関連を持ちます");
                }
            }
            for (String eroorStatement : instance.getErrorStatement()) {//ノードに関するエラー文をすべて取り出す
                if (eroorStatement != null) {
                    textarea.append(eroorStatement + "\n");
                }
            }
            for (ClassElement falseInstance : instance.getNodeFalseInstances()) {
                try {
                    changeColor(falseInstance, red);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }
            findFalseNode(instance.getNodeFalseInstances(), links);

        }
    }

    public static void notLinkCheck(TextArea textarea, ArrayList<ClassElement> instances, ArrayList<LinkElement> links) throws InvalidEditingException {
        ArrayList<String> notLinkErrorStatements = new ArrayList<>();
        ArrayList<ClassElement> removeinstances = new ArrayList<>(instances);
        for (LinkElement link : links) {
            if (removeinstances.contains(link.getSourceEnd())) {
                removeinstances.remove(link.getSourceEnd());
            }
            if (removeinstances.contains(link.getTargetEnd())) {
                removeinstances.remove(link.getTargetEnd());
            }
        }
        if (removeinstances.size() != 0) {
            for (ClassElement instance : removeinstances) {
                changeColor(instance, red);
                notLinkErrorStatements.add(instance.getName() + "が関連を持っていません");

            }
            for (String notLinkerrorstatement : notLinkErrorStatements) {
                textarea.append(notLinkerrorstatement + "\n");
            }
        }
    }

    /*色を変えるためのメソッド
     * 引数　color :String 例#FFFFFF カラー番号
     * FILL_COLORは要素の背景*/
    public static void changeColor(ClassElement instance, final String color)
            throws InvalidEditingException {
        instance.getPresentation().setProperty(PresentationPropertyConstants.Key.FILL_COLOR,color);
    }




    /*astahでfalseノードを見つけて色を変えるためのメソッド
     * astahの色を変えるため*/
    public static void findFalseNode(ArrayList<ClassElement> nodeFalseInstances, ArrayList<LinkElement> links) {
        for (LinkElement link : links) {
            if (nodeFalseInstances.contains(link.getTargetEnd())) {
                if (nodeFalseInstances.contains(link.getSourceEnd())) {
                    try {
                        link.getLinkPresentation().setProperty(PresentationPropertyConstants.Key.LINE_COLOR, red);
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /*IPアドレスの重複を調べるメソッド（重複してもいい場合が存在するため、警告とする）
     * 引数　textaera:astahの出力のため instances インスタンスすべて*/
//    public static void ipAddressDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances) {
//        ArrayList<String> ipAddressList = new ArrayList<>();//ipAddressのリスト
//        ArrayList<ClassElement> ipAddresslistinstance = new ArrayList<>();//ipAddressノリスとの対応したinstance
//        ArrayList<ClassElement> ipAddressDuplicationinstances = new ArrayList<>();
//        ;//重複したipaddressのインスタンス
//        ArrayList<String> ipWarningStatements = new ArrayList<>();//エラー文
//        for (ClassElement instance : instances) {//すべてのIPアドレスを取得してipAddressListに格納する。他のIpRouteやOspfinterfacesettingは重複して良いため保留
//            if (instance instanceof Clients) {
//                ipAddressList.add(((Clients) instance).getIpAddress());
//            } else if (instance instanceof EthernetSetting) {
//                ipAddressList.add(((EthernetSetting) instance).getIpAddress());
//                ipAddresslistinstance.add(instance);
//            } else if (instance instanceof VlanSetting) {
//                ipAddressList.add(((VlanSetting) instance).getIpAddress());
//                ipAddresslistinstance.add(instance);
//            }//ここまででipAddressをまとめたリストが完成している
//            //重複と重複している箇所とを句呈する
//        }
//
//        for (int i = 0; i < ipAddressList.size(); i++) {
//            String ipAddress = ipAddressList.get(i);
//            for (int j = i + 1; j < ipAddressList.size(); j++) {
//                if (!(ipAddress.equals(""))) {
//                    String ipAddress2 = ipAddressList.get(j);
//                    if (ipAddress.equals(ipAddress2)) {
//                        ipWarningStatements.add(ipAddresslistinstance.get(i).getName() + "と" + ipAddresslistinstance.get(j).getName() + "のipAddressが重複しています");//astahにエラー文を表示するためのメソッド
//                        ipAddressDuplicationinstances.add(ipAddresslistinstance.get(i));//重複しているインスタンスをまとめたリストに加える
//                        ipAddressDuplicationinstances.add(ipAddresslistinstance.get(j));
//                    }
//                }
//            }
//        }//ipaddressduplicationinstanceには重複したインスタンスが入っている
//        //ここからはastahに文章を出力したり、色を変更したりするプログラム
//        for (ClassElement ipaddressduplicationinstance : ipAddressDuplicationinstances) {
//            try {
//                changeColor(ipaddressduplicationinstance, orangered);
//            } catch (InvalidEditingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        for (String iperrorstatement : ipWarningStatements) {
//            textarea.append(iperrorstatement + "\n");
//        }
//    }

    /*IPアドレスの重複を調べるメソッド（重複してもいい場合が存在するため、警告とする）
     * 引数　textaera:astahの出力のため instances インスタンスすべて*/
    public static void ipAddressDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances) {

        ArrayList<String> ipAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<String> clientIpAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<String> ethernetIpAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<String> vlanSettingIpAddressList = new ArrayList<>();//ipAddressのリスト

        ArrayList<ClassElement> ipAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
        ArrayList<Clients> clientIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
        ArrayList<EthernetSetting> ethernetIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
        ArrayList<VlanSetting> vlanSettingIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance


        ArrayList<Config> ipConfiginstance = new ArrayList<>();//同一機器におけるIPアドレス重複チェック用

        ArrayList<String> ipWarningStatements = new ArrayList<>();//警告文
        ArrayList<String> ipErrorStatements = new ArrayList<>(); //エラー文　
        for (ClassElement instance : instances) {//すべてのIPアドレスを取得してipAddressListに格納する。他のIpRouteやOspfinterfacesettingは重複して良いため保留
            if (instance instanceof Clients) {
                clientIpAddressList.add(((Clients) instance).getIpAddress());
                clientIpAddresslistInstance.add((Clients) instance);
            } else if (instance instanceof EthernetSetting) {
                ethernetIpAddressList.add(((EthernetSetting) instance).getIpAddress());
                ethernetIpAddresslistInstance.add((EthernetSetting) instance);
            } else if (instance instanceof VlanSetting) {
                vlanSettingIpAddressList.add(((VlanSetting) instance).getIpAddress());
                vlanSettingIpAddresslistInstance.add((VlanSetting) instance);
            } else if (instance instanceof Config) {
                ipConfiginstance.add((Config) instance);
            }

        }
            ipAddressList.addAll(clientIpAddressList);
            ipAddresslistInstance.addAll(clientIpAddresslistInstance);
            ipAddressList.addAll(ethernetIpAddressList);
            ipAddresslistInstance.addAll(ethernetIpAddresslistInstance);
            ipAddressList.addAll(vlanSettingIpAddressList);
            ipAddresslistInstance.addAll(vlanSettingIpAddresslistInstance);
        //ipAddressListとipAddressListInstanceの要素の順番は対応している。ここは変えない


        //Clientの重複
        //同一セグメント（同一VLAN）かつIPの重複　→　エラー
        //VLAN設定がないかつIPの重複　→　エラー
        //違うVLANかつIPの重複,　→　警告
        //一方のみがVLAN設定してある。
        for (int i = 0; i < clientIpAddressList.size(); i++) {
            String ipAddress = clientIpAddressList.get(i);
            for (int j = i + 1; j < clientIpAddressList.size(); j++) {
                if (!(ipAddress.equals(""))) {
                        String ipAddress2 = clientIpAddressList.get(j);
                        if (ipAddress.equals(ipAddress2)) {//重複したipAddressが見つかったとき
                            //cl1のvlan番号を求める
                            int vlan1 = -1;//vlanが設定されていないとき

                                if (clientIpAddresslistInstance.get(i).getLink().getAnotherLinkableElement(clientIpAddresslistInstance.get(i)) instanceof EthernetSetting) {
                                    EthernetSetting conectedEthernetSetting1 = (EthernetSetting) clientIpAddresslistInstance.get(i).getLink().getAnotherLinkableElement(clientIpAddresslistInstance.get(i));
                                    if (conectedEthernetSetting1.getMode().equals("access")) {
                                        vlan1 = conectedEthernetSetting1.getAccessVlan();
                                    } else if (conectedEthernetSetting1.getMode().equals("trunk")) {
                                        vlan1 = conectedEthernetSetting1.getNativeVlan();
                                    }
                                }


                            //cl2のvlan番号を求める
                            int vlan2 = -2;//vlanが設定されてないとき
                                if (clientIpAddresslistInstance.get(j).getLink().getAnotherLinkableElement(clientIpAddresslistInstance.get(j)) instanceof EthernetSetting) {
                                    EthernetSetting conectedEthernetSetting2 = (EthernetSetting) clientIpAddresslistInstance.get(j).getLink().getAnotherLinkableElement(clientIpAddresslistInstance.get(j));
                                    if (conectedEthernetSetting2.getMode().equals("access")) {
                                        vlan2 = conectedEthernetSetting2.getAccessVlan();
                                    } else if (conectedEthernetSetting2.getMode().equals("trunk")) {
                                        vlan2 = conectedEthernetSetting2.getNativeVlan();
                                    }
                                }



                            if (vlan1 == vlan2) {//IPの重複かつVLANの重複

                                ;//エラー文の追加
                            ipErrorStatements.add("同じVLANに属している" + clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。");
                                try {
                                    changeColor(clientIpAddresslistInstance.get(i), red);//色の切り替え
                                    changeColor(clientIpAddresslistInstance.get(j), red);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }


                            //両方VLAN設定がないかつIPの重複　→　エラー
                            else if((vlan1 == -1) && (vlan2 == -2)){
                                ipErrorStatements.add("同一セグメント内の" + clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。");//エラー文の追加
                                try {
                                    changeColor(clientIpAddresslistInstance.get(i), red);//色の切り替え
                                    changeColor(clientIpAddresslistInstance.get(j), red);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            //片方だけVLAN設定しているかVLANがちがうとき　→　警告
                            if((vlan1 == -1 && vlan2 != -2 ) || (vlan1 != -1 && vlan2 == -2)){//片方だけVLAN設定
                                ipWarningStatements.add(clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。");
                                try {
                                    changeColor(clientIpAddresslistInstance.get(i), orangered);//色の切り替え
                                    changeColor(clientIpAddresslistInstance.get(j), orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }//VLANが違う時
                            if(vlan1 != -1 && vlan2 != -2 && vlan1 != vlan2){
                                ipWarningStatements.add(clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。");
                                try {
                                    changeColor(clientIpAddresslistInstance.get(i), orangered);//色の切り替え
                                    changeColor(clientIpAddresslistInstance.get(j), orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }//ここまで重複したIPアドレスが見つかったとき
                }

            }}
            //ここまでClientのIP重複チェック



            //ここからEthernetSettingクラスのIP重複チェック
            //IPの重複（リングアグリゲーション以外の時はエラー）
            for (int eth1 = 0 ; eth1 < ethernetIpAddressList.size() ; eth1++){
                String ethernetIpAddress1  = ethernetIpAddressList.get(eth1);
                for(int eth2 = eth1+1 ; eth2 < ethernetIpAddressList.size() ; eth2++){
                    String ethernetIpAddress2  = ethernetIpAddressList.get(eth2);
                    if(!ethernetIpAddress2.equals("")&&!ethernetIpAddress1.equals("")) {
                        if(ethernetIpAddress1.equals(ethernetIpAddress2)){
                            ipErrorStatements.add( ethernetIpAddresslistInstance.get(eth1).getName() + "と" + ethernetIpAddresslistInstance.get(eth2).getName() + "のIPアドレスが重複しています。");//エラー文の追加
                            try {
                                changeColor(ethernetIpAddresslistInstance.get(eth1), red);//色の切り替え
                                changeColor(ethernetIpAddresslistInstance.get(eth2), red);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            //ここまでEthernetSettingクラスのIP重複チェック


        //ここからVlanSettingクラスのIP重複チェック
        for (int vl1 = 0 ; vl1 < vlanSettingIpAddressList.size() ; vl1++) {
            String vlanIpAddress1 = vlanSettingIpAddressList.get(vl1);
            for (int vl2 = vl1 + 1; vl2 < vlanSettingIpAddressList.size(); vl2++) {
                String vlanIpAddress2 = vlanSettingIpAddressList.get(vl2);
                if (!vlanIpAddress1.equals("") && !vlanIpAddress2.equals("")) {
                    int vVlan1 = vlanSettingIpAddresslistInstance.get(vl1).getVlanNum();
                    int vVlan2 = vlanSettingIpAddresslistInstance.get(vl2).getVlanNum();
                    if (vlanIpAddress1.equals(vlanIpAddress2)) {
                        //同じVLANIDかつIPの重複→エラー
                        if (vVlan1 == vVlan2) {
                            ipErrorStatements.add("同じVLANに属している" + vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のIPアドレスが重複しています。");//エラー文の追加
                            try {
                                changeColor(vlanSettingIpAddresslistInstance.get(vl1), red);//色の切り替え
                                changeColor(vlanSettingIpAddresslistInstance.get(vl2), red);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }


                        }



                        //違うVLANIDかつIPの重複→警告
                        else {
                            ipWarningStatements.add(vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のIPアドレスが重複しています。");//エラー文の追加
                            try {
                                changeColor(vlanSettingIpAddresslistInstance.get(vl1), orangered);//色の切り替え
                                changeColor(vlanSettingIpAddresslistInstance.get(vl2), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    } else {

                        //違うVLANIDかつサブネットの重複→警告
                        if (vVlan1 != vVlan2 && !vlanSettingIpAddresslistInstance.get(vl1).getSubnetMask().equals("") && !vlanSettingIpAddresslistInstance.get(vl2).getSubnetMask().equals("")) {
                            String ipAddress1 = vlanIpAddress1;
                            String ipAddress2 = vlanIpAddress2;
                            String subnetMask1 = vlanSettingIpAddresslistInstance.get(vl1).getSubnetMask();
                            String subnetMask2 = vlanSettingIpAddresslistInstance.get(vl2).getSubnetMask();

                            // IPアドレスとサブネットマスクをint配列に変換
                            String[] ipParts1 = ipAddress1.split("\\.");
                            String[] ipParts2 = ipAddress2.split("\\.");
                            String[] maskParts1 = subnetMask1.split("\\.");
                            String[] maskParts2 = subnetMask2.split("\\.");


                            int[] ipInt1 = new int[4];
                            int[] ipInt2 = new int[4];
                            int[] maskInt1 = new int[4];
                            int[] maskInt2 = new int[4];

                            for (int i = 0; i < 4; i++) {
                                ipInt1[i] = Integer.parseInt(ipParts1[i]);
                                ipInt2[i] = Integer.parseInt(ipParts2[i]);
                                maskInt1[i] = Integer.parseInt(maskParts1[i]);
                                maskInt2[i] = Integer.parseInt(maskParts2[i]);
                            }
                            // IPアドレスとサブネットマスクのAND演算を行い、結果を比較
                            boolean sameNetwork = true;
                            for (int i = 0; i < 4; i++) {
                                if ((ipInt1[i] & maskInt1[i]) != (ipInt2[i] & maskInt2[i])) {//and演算をした結果が異なったら違う
                                    sameNetwork = false;
                                    break;
                                }
                            }
                            //sameNetworkがfalseの時は違うネットワークに属している。
                            if (sameNetwork) {
                                ipWarningStatements.add("違うVLANに属している" + vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のネットワークアドレスが重複しています");//エラー文の追加
                                try {
                                    changeColor(vlanSettingIpAddresslistInstance.get(vl1), orangered);//色の切り替え
                                    changeColor(vlanSettingIpAddresslistInstance.get(vl2), orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }


                            }
                        }

                    }
                }
            }
        }
        //ここまでVlanSettingのIPの重複とサブネットの重複を調べた。


        //Client同士EthernetSetting同士の場合は除く
        //ここからVlanSettingとEthernetSettingとClientのIPアドレスの重複
        ArrayList<String>  allduplicationWarningStatements = new ArrayList<>();//個々の重複と同じ物が出ているため一旦保留
        for (int ip1 = 0 ; ip1 < ipAddressList.size() ; ip1++){
            String allIpAddress1  = ipAddressList.get(ip1);
            for(int ip2 = ip1+1 ; ip2 < ipAddressList.size() ; ip2++){
                String allIpAddress2  = ipAddressList.get(ip2);
                if(!allIpAddress2.equals("")&&!allIpAddress1.equals("")) {
                    if(allIpAddress1.equals(allIpAddress2)){
                        if(!ipAddresslistInstance.get(ip1).getClassName().equals(ipAddresslistInstance.get(ip2).getClassName()) ) {
                            allduplicationWarningStatements.add(ipAddresslistInstance.get(ip1).getName() + "と" + ipAddresslistInstance.get(ip2).getName() + "のIPアドレスが重複しています");
                            try {
                                changeColor(ipAddresslistInstance.get(ip1), orangered);//色の切り替え
                                changeColor(ipAddresslistInstance.get(ip2), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
        //ここまでVlanSettingとEthernetSettingとClientのIPアドレスの重複

        //ここから同一機器におけるIPアドレスの重複→エラー（機器への入力時にエラーとなる）
        for(Config config :ipConfiginstance){
            ArrayList<String> configIpAddressList = new ArrayList<>();//ipAddressのリスト
            ArrayList<ClassElement> configIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
            for(EthernetSetting ethernetSetting : config.getEthernetSetting()){
                configIpAddressList.add(ethernetSetting.getIpAddress());
                configIpAddresslistInstance.add(ethernetSetting);
            }
            for(VlanSetting vlanSetting:config.getVlanSetting()){
                configIpAddressList.add(vlanSetting.getIpAddress());
                configIpAddresslistInstance.add(vlanSetting);
            }
            for (int sa1 = 0 ; sa1 < configIpAddressList.size() ; sa1++){
                String configIpAddress1  = configIpAddressList.get(sa1);
                for(int sa2 = sa1+1 ; sa2 < configIpAddressList.size() ; sa2++){
                    String configIpAddress2  = configIpAddressList.get(sa2);
                    if(!configIpAddress2.equals("")&&!configIpAddress1.equals("")) {
                        if(configIpAddress1.equals(configIpAddress2)){
                            ipErrorStatements.add("同一機器に設定されている"+ configIpAddresslistInstance.get(sa1).getName()+"と"+configIpAddresslistInstance.get(sa2).getName()+"のIPアドレスが重複しています");
                            try {
                                changeColor(configIpAddresslistInstance.get(sa1), red);//色の切り替え
                                changeColor(configIpAddresslistInstance.get(sa2), red);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }

        //ここまで同一機器におけるIPアドレスの重複

         //エラー文の出力
        for (String allduplicationWarningStatement : allduplicationWarningStatements){
            textarea.append(allduplicationWarningStatement +"\n");
        }
        for (String ipWarningStatement : ipWarningStatements) {
            textarea.append(ipWarningStatement + "\n");
        }
        for(String ipErrorStatement : ipErrorStatements){
            textarea.append(ipErrorStatement + "\n");
        }



    }
    /*vlanの重複をチェックするメソッド
    * 直接リンクがないのに同じVlan番号が振られている
    　異なるサブネットなのにおなじVlan がはいっている
    * 反対側のEthernetSettingのVLAN番号とちがう
    *
    *
*/
    public static void vlanDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances) {//コンフィグが直接つながっていないのに同じVlan番号が振られている。
        ArrayList<EthernetSetting> vlanlists = new ArrayList<>(); //VLANの番号に対応したインスタンス
        ArrayList<Integer> vlanNumbers = new ArrayList<>(); //VLANの番号
        ArrayList<String> vlanWarningStatement = new ArrayList<>();


        for (ClassElement instance : instances) {

           if(instance instanceof EthernetSetting){
               if(((EthernetSetting) instance).getMode().equals("access")) {
                   vlanNumbers.add(((EthernetSetting) instance).getAccessVlan());
                   vlanlists.add((EthernetSetting) instance);
               }
               else if(((EthernetSetting) instance).getMode().equals("trunk")){
                   for(Integer vlan :((EthernetSetting) instance).getAllowedVlans()){
                       vlanNumbers.add(vlan);
                       vlanlists.add((EthernetSetting) instance);
                   }
               }
               //conectedThingsの追加
               ((EthernetSetting) instance).setConectedThing(((EthernetSetting) instance).getLink().getAnotherLinkableElement((LinkableElement) instance));
            }
        }

        ArrayList<EthernetSetting> duplicationVlan = new ArrayList<>();//重複したVLAN番号


        while (vlanNumbers.size() != 0) {

            duplicationVlan.clear();//リセット
            int number = vlanNumbers.get(0);//調べるVLAN番号

            for (int j = 0; j < vlanNumbers.size(); j++) {//numberと一致するＶＬＡＮをぬきだして、調べた物は消す。
                if (number == vlanNumbers.get(j)) {
                    duplicationVlan.add(vlanlists.get(j));
                    vlanNumbers.remove(j);
                    vlanlists.remove(j);
                    j--;
                }
            }
            if (duplicationVlan.size() != 0) {


                //duplicationvlanに重複したvlanが格納されている
                //直接リンクがないのに同じVlan番号が振られている ここから-1
                ArrayList<Config> conectedConfigList = new ArrayList<>(); //ひとまとまりのコンフィグリスト　結線でつながっている
                ArrayList<Config> configList = new ArrayList<>(); //同じＶＬＡＮが割り当てられているコンフィグリスト
                conectedConfigList.add(duplicationVlan.get(0).getConfig());
                boolean check = true;
                int count = 0;//conectedConfigListからどのconfigを選ぶか
                int max = 1;//条件のためのもの
                while(check){
                    Config  config = conectedConfigList.get(count);//つながっているリストに入っているコンフィグ
                    for(EthernetSetting eth : config.getEthernetSetting()) {//コンフィグのEthernetSettingのVLANを探して同じモノガアアルカ
                        ArrayList<Integer> vlann = new ArrayList<>();
                        if (eth.getMode().equals("access")) {
                            vlann.add(eth.getAccessVlan());
                        } else if (eth.getMode().equals("trunk")) {
                            for (Integer vlan : eth.getAllowedVlans()) {
                                vlann.add(vlan);
                            }
                        }
                        if (vlann.contains(number)) {
                            ArrayList<Integer> eth_vlans = new ArrayList<>();//EternetSettingの反対側のVLAN
                            if (eth.getConectedThing() instanceof EthernetSetting) {//EthernetSettingのつながっている方(逆側)のVLAN番号を調べる
                                if (((EthernetSetting) eth.getConectedThing()).getMode().equals("access")) {
                                    eth_vlans.add(((EthernetSetting) eth.getConectedThing()).getAccessVlan());
                                } else if (((EthernetSetting) eth.getConectedThing()).getMode().equals("trunk")) {
                                    for (Integer vlan : ((EthernetSetting) eth.getConectedThing()).getAllowedVlans()) {
                                        eth_vlans.add(vlan);
                                    }
                                }

                                if (eth_vlans.contains(number)) {//同じVLANに所属して、直接リンクがある。
                                    Config co = ((EthernetSetting) eth.getConectedThing()).getConfig(); //coと言う名前はこの三行でしか使わない一時的な名前
                                    if (!conectedConfigList.contains(co)) {//重複している物がないとき
                                        conectedConfigList.add(co);//つながっているリストに追加する
                                        max++;
                                    }

                                } else {//対向のVLANといっちしないとき
//                                    if(!vlanWarningStatement.contains(eth.getName() + "と" + eth.getConectedThing().getName() + "において異なるVLAN同士がつながれています") && !vlanWarningStatement.contains(eth.getConectedThing().getName() + "と" + eth.getName() + "において異なるVLAN同士がつながれています")) {
//                                        vlanWarningStatement.add(eth.getName() + "と" + eth.getConectedThing().getName() + "において異なるVLAN同士がつながれています");
//                                    }
                                }

                            }
                        }
                    }
                        count++;
                        if (max <= count) {
                            check = false;
                        }

                }//ここまで



                //ここから同じVLANを持っているコンフィグをリストにいれる
                for(EthernetSetting vlaneth : duplicationVlan){
                    Config config2 = new Config();
                    config2 = vlaneth.getConfig();
                    if(!configList.contains(config2)){
                        configList.add(config2);
                    }
                }


                //configListとconectedConfigListの比較(重複の確認)　ここから
                for(Config sameConfig : configList){
                    if(!conectedConfigList.contains(sameConfig)){
                        if(!vlanWarningStatement.contains("VLAN"+number+"が重複している可能性があります")){
                            vlanWarningStatement.add("VLAN"+number+"が重複している可能性があります");
                        }
                    }
                }


                //ここまで-1



            }
        }
        //ことなるサブネットなのに同じVLANがふられている。 ここから -2
        ArrayList<VlanSetting> vlanSettings = new ArrayList<>();
        int vlanNumber ;

        for(ClassElement instance  : instances){
            if(instance instanceof VlanSetting){
                vlanSettings.add((VlanSetting) instance);
            }
        }
        ArrayList<VlanSetting> duplicationVlanSettingNumber = new ArrayList<>();
        while (vlanSettings.size() != 0) {
            duplicationVlanSettingNumber.clear();
            int number2 = vlanSettings.get(0).getVlanNum();
            for (int j = 0; j < vlanSettings.size(); j++) {//numberと一致するＶＬＡＮをぬきだして、調べた物は消す。
                if ( number2== vlanSettings.get(j).getVlanNum()) {
                    duplicationVlanSettingNumber.add(vlanSettings.get(j));
                    vlanSettings.remove(j);
                    j--;
                }
            }

            for (VlanSetting vlanSetting1 : duplicationVlanSettingNumber) {
                for (VlanSetting vlanSetting2 : duplicationVlanSettingNumber) {
                    String ipAddress1 = vlanSetting1.getIpAddress();
                    String ipAddress2 = vlanSetting2.getIpAddress();
                    String subnetMask1 = vlanSetting1.getSubnetMask();
                    String subnetMask2 = vlanSetting2.getSubnetMask();
                    if (!ipAddress1.isEmpty() && !ipAddress2.isEmpty() && !subnetMask1.isEmpty() && !subnetMask2.isEmpty()) {

                        // IPアドレスとサブネットマスクをint配列に変換
                        String[] ipParts1 = ipAddress1.split("\\.");
                        String[] ipParts2 = ipAddress2.split("\\.");
                        String[] maskParts1 = subnetMask1.split("\\.");
                        String[] maskParts2 = subnetMask2.split("\\.");


                        int[] ipInt1 = new int[4];
                        int[] ipInt2 = new int[4];
                        int[] maskInt1 = new int[4];
                        int[] maskInt2 = new int[4];

                        for (int i = 0; i < 4; i++) {


                            ipInt1[i] = Integer.parseInt(ipParts1[i]);
                            ipInt2[i] = Integer.parseInt(ipParts2[i]);

                            maskInt1[i] = Integer.parseInt(maskParts1[i]);
                            maskInt2[i] = Integer.parseInt(maskParts2[i]);
                        }

                        // IPアドレスとサブネットマスクのAND演算を行い、結果を比較
                        boolean sameNetwork = true;
                        for (int i = 0; i < 4; i++) {
                            if ((ipInt1[i] & maskInt1[i]) != (ipInt2[i] & maskInt2[i])) {//and演算をした結果が異なったら違う
                                sameNetwork = false;
                                break;
                            }
                        }
                        if (!sameNetwork) {//sameNetworkがfalseの時は違うネットワークに属している。
                            if(!vlanWarningStatement.contains("VLAN" + number2 + "内で異なるネットワークアドレスが割り当てられています。")){
                                vlanWarningStatement.add("VLAN" + number2 + "内で異なるネットワークアドレスが割り当てられています。");
                            }
                        }
                    }
                }
            }
        }
        for(String warningStatemnt : vlanWarningStatement){
            textarea.append(warningStatemnt + "\n");
        }

    }

    //深さ優先探索
    /*dfsを行うための関数 https://qiita.com/drken/items/4a7869c5e304883f539b#3-%E6%B7%B1%E3%81%95%E5%84%AA%E5%85%88%E6%8E%A2%E7%B4%A2-dfs-%E3%81%A8%E5%B9%85%E5%84%AA%E5%85%88%E6%8E%A2%E7%B4%A2-bfs
    * 引数　graph: 隣接リスト表現　configsをchangeメソッドで変換しここに入れる（数字はconfigsのリストの添え字と対応している）
    * 　　　数字が１のときconfigs.get(1)で対応したコンフィグのインスタンス情報を取得する
    * 　　　seen: すでに発見したかどうかを格納するインスタンス　1=true,0= false
    *      int v:DFS探索を始めるための始点
    * */

    public static void dfs(ArrayList<ArrayList<Integer>> graph , ArrayList<Integer> seen, int v){
        seen.set(v,1);
        for(Integer nextV : graph.get(v)) {
            if(seen.get(nextV)==1){
                continue;
            }
            dfs(graph,seen,nextV);
        }
    }

    /*ループ用のDFS（深さ優先探索）　参考：https://drken1215.hatenablog.com/entry/2023/05/20/200517　
     * 引数 : grafh 隣接リスト表現されたリスト＜リスト＞　例graphChange(ArrayList<Config>)
     * seen　すでに見た点（行きがけ順）　finished<>　それ移譲先がない点（帰りがけ順）　hist　ループを復元するためのスタック
     * v 探索開始点　p　探索で戻らないようにするための引数*/
    public static Integer  rupeDfs(ArrayList<ArrayList<Integer>> graph , ArrayList<Integer> seen, ArrayList<Integer> finished, Stack<Integer> hist, int v, int p, ArrayList<Integer> s , ArrayList<Integer> q){
            seen.set(v, 1); //見た点であるためseenの該当の場所を1(true)にする
            hist.push(v); //ループ復元のためにpushする

        if(graph.get(v).size()!=0) {
            for (Integer nextV : graph.get(v)) {//調べている点に隣接する点について調べる
                if (nextV == p) continue;//自分が前に通ってきた点の場合スルー（探索が戻ってしまわないように）

                if (finished.get(nextV) == 1) continue; //すでに探索が終わっている点の場合スルー

                if (seen.get(nextV) == 1 && finished.get(nextV) == 0) {//ループがあると分かった場合,それ以上探索することをやめる
                    s.add(v);
                    q.add(nextV);
                    return nextV;
                }
                int pos = rupeDfs(graph, seen, finished, hist, nextV, v, s, q);//ループを発見した場合ループがある点がposに入り、発見しない場合-1を返す

                if (pos != -1) {//ループがすでに見つかっている場合

                    return pos;//これから先はずっと-1以外がかえる
                }
            }
        }
            hist.pop();
            finished.set(v, 1);
            return -1; //まだループが見つかっていない状態



    }



    /**/
    public static ArrayList<ArrayList<Config>> rupeChecks (ArrayList<ClassElement> instances,TextArea textArea){
        ArrayList<String> rupeStatements = new ArrayList<>();
        int pos = -1;
        ArrayList<Config> configs = new ArrayList<>();//コンフィグインスタンスを格納する（すべて「）
        for(ClassElement instance : instances){
            if(instance instanceof  Config){
                configs.add((Config) instance);
            }
        }
        int count = 0;
        ArrayList<ArrayList<Integer>> graph = graphChange(configs);

        ArrayList<ArrayList<Integer>> graph2 = graphChange(configs);

        ArrayList<ArrayList<Integer>> rupenumbers = new ArrayList<>();
        for (int k = 0;k<configs.size();k++) {
                ArrayList<Integer> s =new ArrayList<>();
                ArrayList<Integer> q=new ArrayList<>();
                ArrayList<Integer> seen = new ArrayList<>(); //1=ture,0=false
                ArrayList<Integer> finished = new ArrayList<>(); //1=ture,0=false
                Stack<Integer> hist = new Stack<>();//リスト復元のためのスタック
                for (int i = 0; i < configs.size(); i++) {//コンフィグの数と同じだけ0を格納する
                    seen.add(0);
                    finished.add(0);
                }
                if(count==0){
                    pos = rupeDfs(graph, seen, finished, hist, k, -1, s, q);//dfsを呼び出
                }else{
                    pos = rupeDfs(graph2, seen, finished, hist, k, -1, s, q);
                }

                ArrayList<Integer> cycle = new ArrayList<>();//サイクルを復元するため、ここにはいっているものがループを作っている
                while (!hist.empty()) {
                    int t = hist.peek();
                    cycle.add(t);
                    hist.pop();
                    if (t == pos) break;//ループ以外の点を抜かすため
                }
                if(cycle.size()!=0){
                Collections.sort(cycle);
                if (rupenumbers.size() == 0) {
                    rupenumbers.add(cycle);
                } else {
                    boolean contain = true;
                    for (ArrayList<Integer> rupe : rupenumbers) {
                        if (rupe.equals(cycle)) {
                            contain = false;
                        }
                    }
                    if (contain) {
                        rupenumbers.add(cycle);
                    }
                }
                if (cycle.size() != 0) {//サイクルが見つかったとき

                    for (int a=0; a<graph2.get(s.get(0)).size();a++){
                        if (graph2.get(s.get(0)).get(a).equals(q.get(0))){
                            graph2.get(s.get(0)).remove(a);
                            break;
                        }
                    }
                    for (int b=0; b<graph2.get(q.get(0)).size();b++){
                        if (graph2.get(q.get(0)).get(b).equals(s.get(0))){
                            graph2.get(q.get(0)).remove(b);
                            break;
                        }
                    }
                    count++;
                    k--;

                } }else {//サイクルが見つからなかったとき
                    count = 0;
                    graph2 = graphChange(configs);
                }
            }
        ArrayList<ArrayList<Config>> rupeconfigs = new ArrayList<>();
        for(ArrayList<Integer> rupes : rupenumbers ) {
            ArrayList<Config> ruco = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            for(Integer number : rupes) {
                ruco.add(configs.get(number));
                names.add(configs.get(number).getName());
            }
            rupeconfigs.add(ruco);
            rupeStatements.add(names+"がループ構成になっています\n");
        }
        for(String statement : rupeStatements){
            textArea.append(statement);
        }
        for(ArrayList<Config> coo : rupeconfigs) {
            for (Config rupes : coo) {
                try {
//                    System.out.println(rupes.getName());
                    changeColor((ClassElement) rupes, orangered);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }
        }



        return rupeconfigs;
    }

    //つながっているEthernetSettingのnativeVlanの確認
    //EthernetSettingのmodeがtrunkの時、NativeVLANが設定されているかを確認する。
    public static void  nativeCheck(ArrayList<ClassElement> instances, TextArea textArea){
        ArrayList<String> nativevlanErrorStatement = new ArrayList<>();
        ArrayList<String> nativevlanWarningStatement = new ArrayList<>();

        for(ClassElement classElement : instances){
            if(classElement instanceof EthernetSetting) {
                EthernetSetting conectedEthernetSetting = null;//リンク先のEthernetSetting
                int myNativeVlan = -1; //classElement側のNativeVLAN
                int conectedNativeVlan = -1; //conectedEthernetSetting側のNativeVLAN
                if (((EthernetSetting) classElement).getMode().equals("trunk")){

                    if (((EthernetSetting) classElement).getNativeVlan() != -1) {
                        myNativeVlan = ((EthernetSetting) classElement).getNativeVlan();
                    }

                if (((EthernetSetting) classElement).getConectedThing() instanceof EthernetSetting) {
                    conectedEthernetSetting = (EthernetSetting) ((EthernetSetting) classElement).getConectedThing();//反対側のEthernetSettingインスタンス
                    if (conectedEthernetSetting.getNativeVlan() != -1) {//nativeVLANが設定されているとき
                        conectedNativeVlan = conectedEthernetSetting.getNativeVlan();
                    }

                    //myNativeVlanとconectedNativeVlanにVLAN番号が格納された状態


                    //両方のmodeにトランクが入力され、NativeVLANが設定されていることを確かめる
                    if (((EthernetSetting) classElement).getNativeVlan() == -1) {//ModeがtrunkなのにNativeVLANが設定されていない
                        if (!nativevlanWarningStatement.contains(classElement.getName() + "のnativeVlanが設定されていません")) {
                            nativevlanWarningStatement.add(classElement.getName() + "のnativeVlanが設定されていません");
                            try {
                                changeColor(classElement,orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    if (conectedEthernetSetting.getMode().equals("trunk") && conectedEthernetSetting.getNativeVlan() == -1) {//ModeがtrunkなのにNativeVLANが設定されていない
                        if (!nativevlanWarningStatement.contains(conectedEthernetSetting.getName() + "のnativeVlanが設定されていません")) {
                            nativevlanWarningStatement.add(conectedEthernetSetting.getName() + "のnativeVlanが設定されていません");
                            try {
                                changeColor(conectedEthernetSetting,orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (conectedEthernetSetting.getMode().equals("access") ) {//ModeがtrunkなのにNativeVLANが設定されていない
                        if (!nativevlanWarningStatement.contains(classElement.getName()+"と"+conectedEthernetSetting.getName() + "のmodeが一致しません")) {
                            nativevlanWarningStatement.add(classElement.getName()+"と"+conectedEthernetSetting.getName() + "のmodeが一致しません");
                            try {
                                changeColor(classElement,orangered);
                                changeColor(conectedEthernetSetting,orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }

                    if (((EthernetSetting) classElement).getMode().equals("trunk") && conectedEthernetSetting.getMode().equals("trunk") && myNativeVlan != conectedNativeVlan) {
                        if (!nativevlanErrorStatement.contains(classElement.getName() + "と" + conectedEthernetSetting.getName() + "のNativeVlanが一致していません") && !nativevlanErrorStatement.contains(conectedEthernetSetting.getName() + "と" + classElement.getName() + "のNativeVlanが一致していません")) {
                            nativevlanErrorStatement.add(classElement.getName() + "と" + conectedEthernetSetting.getName() + "のNativeVlanが一致していません");
                            try {
                                changeColor(classElement,red);
                                changeColor(conectedEthernetSetting,red);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }

                }
            }
            }
        }

        for(String eroorStatemnt : nativevlanErrorStatement){
            textArea.append(eroorStatemnt+"\n");
        }
        for(String warnigStatemnt : nativevlanWarningStatement){
            textArea.append(warnigStatemnt+"\n");
        }

    }


    /*dfsのための処理
    * configsにはConfigクラスのインスタンスがあり，config.getLInkedConfigsの隣接リストを数字で入手している
    * configs = [cf1,cf2,cf3] 隣接リスト(Linkedconfig)　=　[[cf3],[cf1,cf3],[cf2]　
    * 返り値　=　[[2(cf3)],[0(cf1),2],[1(cf2)]]*/

    public static ArrayList<ArrayList<Integer>> graphChange(ArrayList<Config> configs){
        ArrayList<ArrayList<Integer>> graph = new ArrayList<>();
        int number= 0;
        for(Config config : configs){
            ArrayList<Integer> numbers = new ArrayList<>();
            for(Config config1 : config.getLinkedConfigs()){
                for(int i = 0; i<configs.size() ; i++){
                    if(config1.equals(configs.get(i))){
                        numbers.add(i);
                    }
                }
            }
            graph.add(numbers);
            number++;
        }
        return graph;
    }
    /*dfsを実行するメソッド
    * instancesはすべてのインスタンス，vは始める場所*/



    public static void dfsCheck (ArrayList<ClassElement> instances,Config config){
        ArrayList<Config> conf = new ArrayList<>();
        for(ClassElement instance : instances){
            if(instance instanceof  Config){
                conf.add((Config) instance);
            }
        }
        int v=0;
        for(int i=0 ; i<conf.size();i++){
            if(conf.get(i).equals(config)){
               v=i;
            }
        }
        ArrayList<Integer> seen = new ArrayList<>(); //1=ture,0=false
        for (int i=0 ; i<conf.size() ; i++){
            seen.add(0);
        }
        Check.dfs(Check.graphChange(conf),seen,v);

    }

    public static void test(){

    }

/*EhernetSetting
* Modeを設定してVlanも設定する
* Vlanを設定したらModeも設定する*/


}
