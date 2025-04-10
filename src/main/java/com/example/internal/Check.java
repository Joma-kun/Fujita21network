package com.example.internal;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.example.classes.*;
import com.example.data.OspfData;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
import com.fasterxml.jackson.databind.JsonNode;


import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ConnectIOException;
import java.util.*;
import java.util.List;
import java.util.Stack;
import java.io.IOException;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Check {
    protected static ArrayList<ErrorInfo> errorInfos = new ArrayList<>();
    public Check(ArrayList<ErrorInfo> errorInfos) {//JSON出力のためのコンストラクタ
        this.errorInfos=errorInfos;
    }
//    //                        1023行目当たりの    ここに色を変える処理を追加する　をやる

    //要編集と書いてあるところをする



    /*実装するしたもの　メソッド名と内容
    * nodeCheck 関連の多重度チェック
    * notLink 関連がないもののチェック
    * changeColor 色を変えるためのメソッド
    * astahCheck astahの必須項目のチェック　EthernetSettingにLinkとConcigがあるべきなど　ー今は使っていない
    * findFalseNode 関連線の色を変えるためのメソッド
    * ipAddressDuplicationCheck IPアドレスの重複を調べるためのメソッド
    *   Clientの重複：同一セグメント（同一VLAN）かつIPの重複　→　エラー,VLAN設定がないかつIPの重複　→　エラー,違うVLANかつIPの重複,　→　警告
    *   EthernetSettingクラスのIP重複チェック,//IPの重複（リングアグリゲーション以外の時はエラー）
    *   VlanSettingクラスのIP重複チェック,同じVLANIDかつIPの重複→エラー,違うVLANIDかつIPの重複→警告
    *                                 違うVLANIDかつサブネットの重複→警告
    *
    *clientVlanCheck ClietntのipAddressがEthernetSettingにせっていされているVLANのネットワークアドレスに一致しているか
    *
    *   VlanSettingとEthernetSettingとClientのIPアドレスの重複
    *   同一機器におけるIPアドレスの重複→エラー（機器への入力時にエラーとなる）
    * oposingVlanCheck　VlanSetting内の属性の関係や対向のVlanSettingとの関係
    *   Mode記入無しだが、トランクかアクセスに記入がある，Mode記入があるがnativeVLANに記入がない,Mode記入があるがアクセスに記入がない
    *   allowedVlanが一致しない：allowedVlan内vlaiIDでVlanインスタンスが存在するものを抜き出し，一致しないものがあれば警告，allowedVlan内のvlanIDでVlanインスタンスがないものがあれば警告
    * 　accessVlanが一致しない　(対向するポートと)
    * vlanDuplicationCheck Vlanの重複をチェックするメソッド
    *   直接リンクがないのに同じVlan番号が振られている，異なるサブネットなのにおなじVlan がはいっている
    * nativeVlanCheck ネイティブVLANについて調査するメソッド
    *   両方のmodeにトランクが入力されるとき、NativeVLANが設定されていることを確かめる，両方のmodeが異なるか，ネイティブVLANの不一致の検証
    * oneNodeCheck
    * EthernetSettingのportの欠如
    * VlanSettingのvlanNumの欠如
    *
    * nodeKetujo check
    * 設定値の欠如などを扱う
    *EthernetSetttingノード　portの欠如　shutdownがfalseかどうか
    *Vlan　num の欠如
    *VlanSetting vlanNumの欠如
    *未使用インターフェースの有効化
    *
    * 実装しないといけないもの
    * 同じスイッチの異なるVLANに同じIPアドレスを割り当てることはできない→エラー
    *
    *
    * 実装しないといけないもの２
    * EthernetSetttingのIPaddressが設定されたときにサブネットマスクが設定されているか
    * エリアIDの不一致\\
    *
警告&インターフェース間のサブネットの不一致

    *     * */


    //新しく図を追加したらうまくいかないことがある
    /*実装したチェック処理
     * 必ず指定された関連先を持ち、多重度のの制限を満たす.何も関連を持ってない物のチェック
     * 必ず関連を持たないといけない物（Linkは二つ関連を持つ）他は要検討
     * ipアドレスの重複　（警告）
     *　Vlanの重複（未実装）
     */

    static String red = "#ff0000";//エラーの色
    static String orangered = "#ff7f50";//警告の色

    /*
     * 必ず指定された関連先を持ち、多重度を満たすことを検証するメソッド（Linkの関連数も）
     * 引数　 instances :astahの図ないの全てのclassElement(インスタンス), links:astah内の関連線が格納されている配列,errorStatement:エラー文を格納する配列
     * ・Linkの関連が二つのみであることを検証する
     * 　Linkクラスのインスタンスに関連付けられているインスタンス数が二個以外の時にエラー
     * ・必ず指定された関連先を持ち、多重度を満たすことを検証する
     * 　ChangeclassInformationで指定された関連かを検証、型変換のSetメソッド内で多重度の検証（各各Classesのsetメソッド内で記述）
     *   上記の検証結果のエラー文はmultiplictyErrorStatementに格納、エラーのインスタンス情報はnodeFalseInstancesに格納
     * 　このメソッド内でその配列からエラー文やインスタンスを取り出し検証する。
     */
    public static void nodeCheck( ArrayList<ClassElement> instances, ArrayList<LinkElement> links,ArrayList<String> errorStatement) throws InvalidEditingException {
        for (ClassElement instance : instances) {//すべてのインスタンスについて
            //Linkの関連が二つだけなことをチェックする
            if (instance instanceof Link) {//Linkクラスのインスタンスの時
                if (((Link) instance).getLinkableElement().size() != 2) {
                    changeColor(instance, red);
                    String message = instance.getName()+"：リンクは二つの関連を持ちます";
                    errorInfos.add(new ErrorInfo(message, true, "関連チェック",mapOf(instance.getId(), null) ));//JSONを作るための処理
                    errorStatement.add(message);

                }
            }
            //必ず指定された関連先を持ち、多重度を満たすことを検証する
            for (String statement : instance.getmultiplictyErrorStatement()) {//ノードに関するエラー文(関連先、多重度に関する)をすべて取り出す
                if (statement != null) {
                    errorStatement.add(statement);//エラー文に追加する
                }
            }
            //エラーに関する色変換の処理
            for (ClassElement falseInstance : instance.getNodeFalseInstances()) {//関連線、多重度に関するエラーを含むインスタンスを取り出す
                try {
                    changeColor(falseInstance, red);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    public static void notLinkCheck(TextArea textarea, ArrayList<ClassElement> instances, ArrayList<LinkElement> links,ArrayList<String> notLinkErrorStatements) throws InvalidEditingException {
//        ArrayList<String> notLinkErrorStatements = new ArrayList<>();
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
                String message = instance.getName() + "が関連を持っていません";
                errorInfos.add(new ErrorInfo(message, true, "関連無し",mapOf(instance.getId(), null) ));//JSONを作るための処理
                notLinkErrorStatements.add(message);

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
        instance.getPresentation().setProperty(PresentationPropertyConstants.Key.FILL_COLOR, color);
    }


    /*astahでfalseノードを見つけて色を変えるためのメソッド
     * astahの色を変えるため*/
//    public static void findFalseNode(ArrayList<ClassElement> nodeFalseInstances, ArrayList<LinkElement> links) {
//        for (LinkElement link : links) {
//            if (nodeFalseInstances.contains(link.getTargetEnd())) {
//                if (nodeFalseInstances.contains(link.getSourceEnd())) {
//                    try {
//                        link.getLinkPresentation().setProperty(PresentationPropertyConstants.Key.LINE_COLOR, red);
//                    } catch (InvalidEditingException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }
//    }



    /*IPアドレスの重複を調べるメソッド（重複してもいい場合が存在するため、警告とする）
     * 引数　textaera:astahの出力のため instances インスタンスすべて
     *エラー：５
     * 警告　：６
     * */
    public static void ipAddressDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances,ArrayList<String> ipErrorStatements,ArrayList<String> ipWarningStatements) {

//        ArrayList<String> vlanWarningStatement = new ArrayList<>();
        for (ClassElement instance : instances) {
            if (instance instanceof EthernetSetting) {
                if (instance.getlink() != null) {

                    if (((EthernetSetting) instance).getLink() != null) {

                        ((EthernetSetting) instance).setConectedThing(((EthernetSetting) instance).getLink().getAnotherLinkableElement((LinkableElement) instance));
                    }
                }
            }
        }



        ArrayList<String> ipAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<String> clientIpAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<String> ethernetIpAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<String> vlanSettingIpAddressList = new ArrayList<>();//ipAddressのリスト

        ArrayList<ClassElement> ipAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
        ArrayList<Clients> clientIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
        ArrayList<EthernetSetting> ethernetIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
        ArrayList<VlanSetting> vlanSettingIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance


        ArrayList<Config> ipConfiginstance = new ArrayList<>();//同一機器におけるIPアドレス重複チェック用


//        ArrayList<String> ipWarningStatements = new ArrayList<>();//警告文
//        ArrayList<String> ipErrorStatements = new ArrayList<>(); //エラー文　
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
                                for(Vlan vlan : conectedEthernetSetting1.getConfig().getVlan()){
                                    if(vlan.getNum() == conectedEthernetSetting1.getAccessVlan()) {
                                        vlan1 = conectedEthernetSetting1.getAccessVlan();
                                    }}
                            } else if (conectedEthernetSetting1.getMode().equals("trunk")) {
                                vlan1 = conectedEthernetSetting1.getNativeVlan();
                            }
                        }


                        //cl2のvlan番号を求める
                        int vlan2 = -2;//vlanが設定されてないとき
                        if (clientIpAddresslistInstance.get(j).getLink().getAnotherLinkableElement(clientIpAddresslistInstance.get(j)) instanceof EthernetSetting) {
                            EthernetSetting conectedEthernetSetting2 = (EthernetSetting) clientIpAddresslistInstance.get(j).getLink().getAnotherLinkableElement(clientIpAddresslistInstance.get(j));
                            if (conectedEthernetSetting2.getMode().equals("access")) {
                                for(Vlan vlan : conectedEthernetSetting2.getConfig().getVlan()){
                                    if(vlan.getNum() == conectedEthernetSetting2.getAccessVlan()) {
                                        vlan2 = conectedEthernetSetting2.getAccessVlan();
                                    }}
                            } else if (conectedEthernetSetting2.getMode().equals("trunk")) {
                                vlan2 = conectedEthernetSetting2.getNativeVlan();
                            }
                        }


                        if (vlan1 == vlan2) {//IPの重複かつVLANの重複

                            ;//エラー文の追加
                            String message = "同じVLANに属している" + clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。";
                            Map<String,String> ins = new HashMap<>();
                            ins.put(clientIpAddresslistInstance.get(i).getId(),"ipAddress");
                            ins.put(clientIpAddresslistInstance.get(i).getId(),"ipAddress");
                            errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（Client間,同一VLAN内）",ins));//JSONを作るための処理
                            ipErrorStatements.add("同じVLANに属している" + clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。");
                            try {
                                changeColor(clientIpAddresslistInstance.get(i), orangered);//色の切り替え
                                changeColor(clientIpAddresslistInstance.get(j), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }


                        //両方VLAN設定がないかつIPの重複　→　エラー
                        else if ((vlan1 == -1) && (vlan2 == -2)) {

                            Map<String,String> ins = new HashMap<>();
                            ins.put(clientIpAddresslistInstance.get(i).getId(),"ipAddress");
                            ins.put(clientIpAddresslistInstance.get(j).getId(),"ipAddress");
                            String message = "同一セグメント内の" + clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。";
                            errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（Client間、同一セグメント内（VLAN設定無し））",ins));//JSONを作るための処理

                            ipErrorStatements.add(message);//エラー文の追加
                            try {
                                changeColor(clientIpAddresslistInstance.get(i), orangered);//色の切り替え
                                changeColor(clientIpAddresslistInstance.get(j), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        //片方だけVLAN設定しているかVLANがちがうとき　→　警告
                        if ((vlan1 == -1 && vlan2 != -2) || (vlan1 != -1 && vlan2 == -2)) {//片方だけVLAN設定


                            Map<String,String> ins = new HashMap<>();
                            ins.put(clientIpAddresslistInstance.get(i).getId(),"ipAddress");
                            ins.put(clientIpAddresslistInstance.get(j).getId(),"ipAddress");

                            String message = clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。";
                            errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（Client間、異なるセグメント間,片方だけVLAN設定）",ins));//JSONを作るための処理


                            ipWarningStatements.add(message);
                            try {
                                changeColor(clientIpAddresslistInstance.get(i), orangered);//色の切り替え
                                changeColor(clientIpAddresslistInstance.get(j), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }//VLANが違う時
                        if (vlan1 != -1 && vlan2 != -2 && vlan1 != vlan2) {
                            Map<String,String> ins = new HashMap<>();
                            ins.put(clientIpAddresslistInstance.get(i).getId(),"ipAddress");
                            ins.put(clientIpAddresslistInstance.get(j).getId(),"ipAddress");
                            String message = clientIpAddresslistInstance.get(i).getName() + "と" + clientIpAddresslistInstance.get(j).getName() + "のIPアドレスが重複しています。";
                            errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（Client間、異なるセグメント間,異なるVLAN）",ins));//JSONを作るための処理

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

            }
        }
        //ここまでClientのIP重複チェック


        //ここからEthernetSettingクラスのIP重複チェック
        //IPの重複（リングアグリゲーション以外の時はエラー）
        for (int eth1 = 0; eth1 < ethernetIpAddressList.size(); eth1++) {
            String ethernetIpAddress1 = ethernetIpAddressList.get(eth1);

            for (int eth2 = eth1 + 1; eth2 < ethernetIpAddressList.size(); eth2++) {
                String ethernetIpAddress2 = ethernetIpAddressList.get(eth2);

                if (!ethernetIpAddress2.equals("") && !ethernetIpAddress1.equals("")) {
                    if (ethernetIpAddress1.equals(ethernetIpAddress2)) {

                        Map<String,String> ins = new HashMap<>();
                        ins.put(ethernetIpAddresslistInstance.get(eth1).getId(),"ipAddress");
                        ins.put(ethernetIpAddresslistInstance.get(eth2).getId(),"ipAddress");
                        String message = ethernetIpAddresslistInstance.get(eth1).getName() + "と" + ethernetIpAddresslistInstance.get(eth2).getName() + "のIPアドレスが重複しています。";
                        errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（EthernetSetting間）",ins));//JSONを作るための処理


                        ipErrorStatements.add(message);//エラー文の追加
                        try {

                            changeColor(ethernetIpAddresslistInstance.get(eth1), orangered);//色の切り替え
                            changeColor(ethernetIpAddresslistInstance.get(eth2), orangered);

                        } catch (InvalidEditingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        //ここまでEthernetSettingクラスのIP重複チェック


        //ここからVlanSettingクラスのIP重複チェック
        for (int vl1 = 0; vl1 < vlanSettingIpAddressList.size(); vl1++) {
            String vlanIpAddress1 = vlanSettingIpAddressList.get(vl1);
            for (int vl2 = vl1 + 1; vl2 < vlanSettingIpAddressList.size(); vl2++) {
                String vlanIpAddress2 = vlanSettingIpAddressList.get(vl2);
                if (!vlanIpAddress1.equals("") && !vlanIpAddress2.equals("")) {
                    int vVlan1 = vlanSettingIpAddresslistInstance.get(vl1).getVlanNum();
                    int vVlan2 = vlanSettingIpAddresslistInstance.get(vl2).getVlanNum();
                    if (vlanIpAddress1.equals(vlanIpAddress2)) {

                        //同じVLANIDかつIPの重複→エラー
                        if (vVlan1 == vVlan2) {
                            ArrayList<Config> conectedConfig1 = new ArrayList<>();
                            ArrayList<Config> conectedConfig2 = new ArrayList<>();
                             for(int i= 0; i< 2 ;i++) {
                                ArrayList<EthernetSetting> vlanlists = new ArrayList<>(); //VLANの番号に対応したインスタンス
                                ArrayList<Integer> vlanNumbers = new ArrayList<>(); //VLANの番号
                                for (ClassElement instance : instances) {

                                    if (instance instanceof EthernetSetting) {

                                        if (((EthernetSetting) instance).getMode().equals("access")) {
                                            for (Vlan vlan : ((EthernetSetting) instance).getConfig().getVlan()) {
                                                if (vlan.getNum() == ((EthernetSetting) instance).getAccessVlan()) {
                                                    vlanNumbers.add(((EthernetSetting) instance).getAccessVlan());
                                                    vlanlists.add((EthernetSetting) instance);
                                                    break;
                                                }
                                            }


                                        } else if (((EthernetSetting) instance).getMode().equals("trunk")) {

                                            for (Integer vlan : ((EthernetSetting) instance).getAllowedVlans()) {
                                                for (Vlan vlan1 : ((EthernetSetting) instance).getConfig().getVlan()) {
                                                    if (vlan1.getNum() == vlan) {
                                                        vlanNumbers.add(vlan);
                                                        vlanlists.add((EthernetSetting) instance);
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        //conectedThingsの追加


                                    }
                                }
                                ArrayList<EthernetSetting> duplicationVlan = new ArrayList<>();//重複したVLAN番号



                                    duplicationVlan.clear();//リセット
                                    int number = vVlan1;//調べるVLAN番号

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
                                        if(i == 0){
                                            conectedConfigList.add(vlanSettingIpAddresslistInstance.get(vl1).getConfig());
                                        }else{
                                            conectedConfigList.add(vlanSettingIpAddresslistInstance.get(vl2).getConfig());
                                        }

                                        boolean check = true;
                                        int count = 0;//conectedConfigListからどのconfigを選ぶか
                                        int max = 1;//条件のためのもの
                                        while (check) {


                                            Config config = conectedConfigList.get(count);//つながっているリストに入っているコンフィグ
                                            for (EthernetSetting eth : config.getEthernetSetting()) {//コンフィグのEthernetSettingのVLANを探して同じモノガアアルカ

                                                ArrayList<Integer> vlann = new ArrayList<>();
                                                if (eth.getMode().equals("access")) {
                                                    for (Vlan vlan : eth.getConfig().getVlan()) {
                                                        if (vlan.getNum() == eth.getAccessVlan()) {
                                                            vlann.add(eth.getAccessVlan());
                                                        }
                                                    }

                                                } else if (eth.getMode().equals("trunk")) {
                                                    for (Integer vlan : eth.getAllowedVlans()) {
                                                        for (Vlan vlan1 : eth.getConfig().getVlan()) {
                                                            if (vlan1.getNum() == vlan) {
                                                                vlann.add(vlan);
                                                            }
                                                        }
                                                    }
                                                }

                                                if (vlann.contains(number)) {
                                                    ArrayList<Integer> eth_vlans = new ArrayList<>();//EternetSettingの反対側のVLAN
                                                    if (eth.getConectedThing() instanceof EthernetSetting) {//EthernetSettingのつながっている方(逆側)のVLAN番号を調べる
                                                        if (((EthernetSetting) eth.getConectedThing()).getMode().equals("access")) {
                                                            for (Vlan vlan : ((EthernetSetting) eth.getConectedThing()).getConfig().getVlan()) {
                                                                if (vlan.getNum() == ((EthernetSetting) eth.getConectedThing()).getAccessVlan()) {
                                                                    eth_vlans.add(((EthernetSetting) eth.getConectedThing()).getAccessVlan());
                                                                }
                                                            }
                                                        } else if (((EthernetSetting) eth.getConectedThing()).getMode().equals("trunk")) {
                                                            ;
                                                            for (Integer vlan : ((EthernetSetting) eth.getConectedThing()).getAllowedVlans()) {
                                                                for (Vlan vlan1 : ((EthernetSetting) eth.getConectedThing()).getConfig().getVlan()) {
                                                                    if (vlan1.getNum() == vlan) {
                                                                        eth_vlans.add(vlan);
                                                                    }
                                                                }
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
                                        for (EthernetSetting vlaneth : duplicationVlan) {
                                            Config config2 = new Config();
                                            config2 = vlaneth.getConfig();
                                            if (!configList.contains(config2)) {
                                                configList.add(config2);
                                            }
                                        }
                                        if(i == 0){
                                            conectedConfig1 = conectedConfigList;
                                        }else{
                                            conectedConfig2 = conectedConfigList;
                                        }

                                    }

                            }
                             boolean ok = true;
                            for (Config sameConfig : conectedConfig1) {
                                if (!conectedConfig2.contains(sameConfig)) {
                                        ok = false;
                                }
                            }
                            if(ok){
                                Map<String,String> ins = new HashMap<>();
                                ins.put(vlanSettingIpAddresslistInstance.get(vl1).getId(),"ipAddress");
                                ins.put(vlanSettingIpAddresslistInstance.get(vl2).getId(),"ipAddress");

                                String message = "同じセグメントのVLANに属している" + vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のIPアドレスが重複しています。";
                                errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（VlanSetting、同一VLAN）",ins));//JSONを作るための処理


                                ipErrorStatements.add(message);//エラー文の追加
                            }else{
                                Map<String,String> ins = new HashMap<>();
                                ins.put(vlanSettingIpAddresslistInstance.get(vl1).getId(),"ipAddress");
                                ins.put(vlanSettingIpAddresslistInstance.get(vl2).getId(),"ipAddress");

                                String message = "異なるセグメントのVLANに属している" + vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のIPアドレスが重複しています。";
                                errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（VlanSetting、異なるVLAN）",ins));//JSONを作るための処理

                                ipWarningStatements.add(message);//エラー文の追加

                            }
//                            System.out.println("configlist1");
//                            System.out.println("size" );
//
//
//                            for (Config config : conectedConfig1) {
//                                System.out.println(config.getName());
//                            }
//                            System.out.println("conectedConfiglist" );
//                            System.out.println("size");

                            try {
                                changeColor(vlanSettingIpAddresslistInstance.get(vl1), orangered);//色の切り替え
                                changeColor(vlanSettingIpAddresslistInstance.get(vl2), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }


                        }


                        //違うVLANIDかつIPの重複→警告
                        else {

                            Map<String,String> ins = new HashMap<>();
                            ins.put(vlanSettingIpAddresslistInstance.get(vl1).getId(),"ipAddress");
                            ins.put(vlanSettingIpAddresslistInstance.get(vl2).getId(),"ipAddress");

                            String message = vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のIPアドレスが重複しています。";
                            errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（VlanSetting、異なるVLAN）",ins));//JSONを作るための処理

                            ipWarningStatements.add(message);//エラー文の追加
                            try {
                                changeColor(vlanSettingIpAddresslistInstance.get(vl1), orangered);//色の切り替え
                                changeColor(vlanSettingIpAddresslistInstance.get(vl2), orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }
//                    else {

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

                                Map<String,String> ins = new HashMap<>();
                                ins.put(vlanSettingIpAddresslistInstance.get(vl1).getId(),"ipAddress");
                                ins.put(vlanSettingIpAddresslistInstance.get(vl2).getId(),"ipAddress");
                                ins.put(vlanSettingIpAddresslistInstance.get(vl1).getId(),"subNetMask");
                                ins.put(vlanSettingIpAddresslistInstance.get(vl2).getId(),"subNetMask");

                                String message = "違うVLANに属している" + vlanSettingIpAddresslistInstance.get(vl1).getName() + "と" + vlanSettingIpAddresslistInstance.get(vl2).getName() + "のネットワークアドレスが重複しています";
                                errorInfos.add(new ErrorInfo(message, false, "異なるVLANかつサブネットの重複",ins));//JSONを作るための処理

                                ipWarningStatements.add(message);//エラー文の追加
                                try {
                                    changeColor(vlanSettingIpAddresslistInstance.get(vl1), orangered);//色の切り替え
                                    changeColor(vlanSettingIpAddresslistInstance.get(vl2), orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }


                            }
                        }

//                    }
                }
            }
        }
        //ここまでVlanSettingのIPの重複とサブネットの重複を調べた。


        //Client同士EthernetSetting同士の場合は除く
        //ここからVlanSettingとEthernetSettingとClientのIPアドレスの重複
        ArrayList<String> allduplicationWarningStatements = new ArrayList<>();//個々の重複と同じ物が出ているため一旦保留
        for (int ip1 = 0; ip1 < ipAddressList.size(); ip1++) {
            String allIpAddress1 = ipAddressList.get(ip1);
            for (int ip2 = ip1 + 1; ip2 < ipAddressList.size(); ip2++) {
                String allIpAddress2 = ipAddressList.get(ip2);
                if (!allIpAddress2.equals("") && !allIpAddress1.equals("")) {
                    if (allIpAddress1.equals(allIpAddress2)) {
                        if (!ipAddresslistInstance.get(ip1).getClassName().equals(ipAddresslistInstance.get(ip2).getClassName())) {
                            Map<String,String> ins = new HashMap<>();
                            ins.put(ipAddresslistInstance.get(ip1).getId(),"ipAddress");
                            ins.put(ipAddresslistInstance.get(ip1).getId(),"ipAddress");

                            String message = ipAddresslistInstance.get(ip1).getName() + "と" + ipAddresslistInstance.get(ip2).getName() + "のIPアドレスが重複しています";
                            errorInfos.add(new ErrorInfo(message, false, "IPアドレスの重複（異なるクラス）",ins));//JSONを作るための処理

                            allduplicationWarningStatements.add(message);
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
        for (Config config : ipConfiginstance) {
            ArrayList<String> configIpAddressList = new ArrayList<>();//ipAddressのリスト
            ArrayList<ClassElement> configIpAddresslistInstance = new ArrayList<>();//ipAddressnのリストとの対応したinstance
            for (EthernetSetting ethernetSetting : config.getEthernetSetting()) {
                configIpAddressList.add(ethernetSetting.getIpAddress());
                configIpAddresslistInstance.add(ethernetSetting);
            }
            for (VlanSetting vlanSetting : config.getVlanSetting()) {
                configIpAddressList.add(vlanSetting.getIpAddress());
                configIpAddresslistInstance.add(vlanSetting);
            }
            for (int sa1 = 0; sa1 < configIpAddressList.size(); sa1++) {
                String configIpAddress1 = configIpAddressList.get(sa1);
                for (int sa2 = sa1 + 1; sa2 < configIpAddressList.size(); sa2++) {
                    String configIpAddress2 = configIpAddressList.get(sa2);
                    if (!configIpAddress2.equals("") && !configIpAddress1.equals("")) {
                        if (configIpAddress1.equals(configIpAddress2)) {
                            Map<String,String> ins = new HashMap<>();
                            ins.put(configIpAddresslistInstance.get(sa1).getId(),"ipAddress");
                            ins.put(configIpAddresslistInstance.get(sa2).getId(),"ipAddress");

                            String message = "同一機器に設定されている" + configIpAddresslistInstance.get(sa1).getName() + "と" + configIpAddresslistInstance.get(sa2).getName() + "のIPアドレスが重複しています";
                            errorInfos.add(new ErrorInfo(message, true, "同一機器内でのIPアドレスの重複",ins));//JSONを作るための処理
                            ipErrorStatements.add(message);
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
        for (String allduplicationWarningStatement : allduplicationWarningStatements) {
//            textarea.append(allduplicationWarningStatement + "\n");
            ipWarningStatements.add(allduplicationWarningStatement);
        }
//        for (String ipWarningStatement : ipWarningStatements) {
//            textarea.append(ipWarningStatement + "\n");
//        }
//        for (String ipErrorStatement : ipErrorStatements) {
//            textarea.append(ipErrorStatement + "\n");
//        }

    }
//    //astahだからこそのチェック 今は使っていない
//    //・EthernetSettingにはLinkとConfigがつくべきである
//    public static void astahCheck(ArrayList<ClassElement> instances,TextArea textArea,ArrayList<String> errorStatements,ArrayList<String> warningStatements) throws InvalidEditingException {
//        for (ClassElement classElement : instances) {
//
//            if (classElement instanceof EthernetSetting) {
////                if (((EthernetSetting) classElement).getConfig() == null) {
////                    errorStatements.add(classElement.getName() + "がコンフィグと関連付けられていません");
////                    changeColor(classElement,red);
////
////                }
////                if (((EthernetSetting) classElement).getLink() == null) {
////                    errorStatements.add(classElement.getName() + "がLinkと関連付けられていません");
////                    changeColor(classElement,red);
////                }
//            }
//        }
//    }
    /*単一ノード
    エラー３
    複数ノード
    エラー：　１
    * 警告：　２*/
    public static void oposingVlancheck(ArrayList<ClassElement> instances,TextArea textArea,ArrayList<String> errorStatements,ArrayList<String> warningStatements) throws InvalidEditingException {


        for (ClassElement classElement : instances) {

            if (classElement instanceof EthernetSetting) {

                EthernetSetting ethernetSeting = (EthernetSetting) classElement;
                //ModeとVlanの関係
                //vlanに値を入れなかったら値は0になる。
                //Mode記入無しだが、トランクかアクセスに記入がある
                if ((ethernetSeting.getMode().isEmpty()) && ((ethernetSeting.getAccessVlan() != -1) || (ethernetSeting.getNativeVlan() != -1))) {
                    String message =ethernetSeting.getName() + "のModeが空白です" ;
                    errorInfos.add(new ErrorInfo(message, true, "Mode記入無しかつトランクかアクセスに値あり",mapOf(ethernetSeting.getId(), "mode") ));//JSONを作るための処理
                    errorStatements.add(message);
                    changeColor(ethernetSeting,red);
                }
                //Mode記入があるがnativeVLANに記入がない
                else if ((ethernetSeting.getMode().equals("trunk")) && ethernetSeting.getNativeVlan() == -1) {
                    String message = ethernetSeting.getName() + "のnativeVlanが空白です";
                    errorInfos.add(new ErrorInfo(message, true, "Mode記入があるがnativeVLANに記入がない",mapOf(ethernetSeting.getId(), "nativeVlan") ));//JSONを作るための処理
                    errorStatements.add(message);
                    changeColor(ethernetSeting,red);
                }
                //Mode記入があるがアクセスに記入がない
                else if ((ethernetSeting.getMode().equals("access")) && ethernetSeting.getAccessVlan() == -1) {
                    String message = ethernetSeting.getName() + "のaccessVlanが空白です";
                    errorInfos.add(new ErrorInfo(message, true, "Mode記入があるがアクセスに記入がな",mapOf(ethernetSeting.getId(), "accessVlan") ));//JSONを作るための処理
                    errorStatements.add(message);
                    changeColor(ethernetSeting,red);
                }
                //allowedVlanが一致しない
                //allowedVlan内のvlanIDでVlanインスタンスがないものがあれば警告
                if(ethernetSeting.getMode().equals("trunk")) {
                    boolean allowedVlanfuItti = false;
                    ArrayList<Vlan> vlanInstance = ethernetSeting.getConfig().getVlan();
                    ArrayList<Integer> vlanInstanceNumber = new ArrayList<>();//コンフィグにつながっているVlanインスタンスID
                    for (Vlan vlanid : vlanInstance) {
                        vlanInstanceNumber.add(vlanid.getNum());
                    }

                        for (Integer vlanid : ethernetSeting.getAllowedVlans()) {
                            if (vlanid != 1) {
                                if(vlanid !=1002 &&vlanid !=1003 &&vlanid !=1004 &&vlanid !=1005 &&vlanid !=2)
                                {
                                    if (!vlanInstanceNumber.contains(vlanid)) {
                                        allowedVlanfuItti = true;
                                    }
                                }
                            }
                        }
                        if (allowedVlanfuItti) {
                            if(!warningStatements.contains(ethernetSeting.getName() + "で作成されていないvlanが許可されています．")) {
                                String message = ethernetSeting.getName() + "で作成されていないvlanが許可されています．";
                                errorInfos.add(new ErrorInfo(message, false, "作成されていないVLANの許可",mapOf(ethernetSeting.getId(), "allowedVlan") ));//JSONを作るための処理
                                warningStatements.add(message);
                            }
                            changeColor(ethernetSeting,orangered);
                        }

                    // allowedVlan内vlaiIDでVlanインスタンスが存在するもの(acitveなallowedVlan)を抜き出し，一致しないものがあれば警告
                    ArrayList<Integer> acitiveAllowedVlan = new ArrayList<>(); //activeVlanのリスト
                    ArrayList<Integer> oppsingAcitiveAllowedVlan = new ArrayList<>(); //対向のactiveVlanのリスト

                    for (int number1 : ethernetSeting.getAllowedVlans()) {
                        if (vlanInstanceNumber.contains(number1) && !acitiveAllowedVlan.contains(number1)) {
                            acitiveAllowedVlan.add(number1);
                        }
                    }
                    if (ethernetSeting.getConectedThing() instanceof EthernetSetting) {
                        ArrayList<Vlan> opposingVlanInstance = ((EthernetSetting) ethernetSeting.getConectedThing()).getConfig().getVlan();
                        ArrayList<Integer> opposingVlanInstanceNumber = new ArrayList<>();//コンフィグにつながっているVlanインスタンスID
                        for (Vlan vlanid : opposingVlanInstance) {
                            opposingVlanInstanceNumber.add(vlanid.getNum());
                        }

                        for (int number2 : ((EthernetSetting) ethernetSeting.getConectedThing()).getAllowedVlans()) {
                            if (opposingVlanInstanceNumber.contains(number2) && !oppsingAcitiveAllowedVlan.contains(number2)) {
                                oppsingAcitiveAllowedVlan.add(number2);
                            }
                        }
                    }

                    Collections.sort(acitiveAllowedVlan);
                    Collections.sort(oppsingAcitiveAllowedVlan);
                    if (!acitiveAllowedVlan.equals(oppsingAcitiveAllowedVlan) && ethernetSeting.getConectedThing()!=null) {
                        if (!warningStatements.contains(ethernetSeting.getName() + "と" + ((ethernetSeting).getConectedThing()).getName() + "の転送許可VLANが一致しません．VLANインスタンスやallowedVlanの値が誤っている可能性があります．") && !warningStatements.contains(((EthernetSetting)ethernetSeting).getConectedThing().getName() + "と" + ethernetSeting.getName() + "の転送許可VLANが一致しません．VLANインスタンスやallowedVlanの値が誤っている可能性があります．")) {
                            String message = ethernetSeting.getName() + "と" + ((ethernetSeting).getConectedThing()).getName() + "の転送許可VLANが一致しません．VLANインスタンスやallowedVlanの値が誤っている可能性があります．";
                            Map<String,String> ins = new HashMap<>();
                            ins.put(ethernetSeting.getId(),"allowedVlan");
                            ins.put(((EthernetSetting) ethernetSeting).getConectedThing().getId(),"allowedVlan");
                            errorInfos.add(new ErrorInfo(message, false, "転送許可VLANの不一致",ins));//JSONを作るための処理
                            warningStatements.add(message);
                        }
                         Check.changeColor(ethernetSeting, orangered);
                        Check.changeColor(((EthernetSetting) ethernetSeting).getConectedThing(), orangered);
                    }
//

                }

//                ArrayList<Integer> myallowedVlan = new ArrayList<>();
//                myallowedVlan = ((EthernetSetting) ethernetSeting).getAllowedVlans();
//                ArrayList<Integer> anotherallowedVlan = new ArrayList<>();
//
//
//                if (((EthernetSetting) ethernetSeting).getConectedThing() instanceof EthernetSetting) {
//                    anotherallowedVlan = ((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getAllowedVlans();
//                    Collections.sort(myallowedVlan);
//                    Collections.sort(anotherallowedVlan);
//
//                    if (!myallowedVlan.equals(anotherallowedVlan)) {
//                        if (!warningStatements.contains(ethernetSeting.getName() + "と" + ((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getName() + "のallowedVlanが一致しません") && !warningStatements.contains(((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getName() + "と" + ethernetSeting.getName() + "のallowedVlanが一致しません")) {
//                            warningStatements.add(ethernetSeting.getName() + "と" + ((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getName() + "のallowedVlanが一致しません");
//                            System.out.println(ethernetSeting.getName() +myallowedVlan + ethernetSeting.getConectedThing().getName() + anotherallowedVlan);
//                        }
//                         Check.changeColor(ethernetSeting, orangered);
//                        Check.changeColor(((EthernetSetting) ethernetSeting).getConectedThing(), orangered);
//                    }
//                }
//                System.out.println("my" + myallowedVlan + "another" + anotherallowedVlan);


                //accessVlanが一致しない
                int myaccessvlan = ((EthernetSetting) ethernetSeting).getAccessVlan();
                int anotheraccessVlan;
                if(ethernetSeting.getMode().equals("access") ){
                    if (((EthernetSetting) ethernetSeting).getConectedThing() instanceof EthernetSetting) {
                        anotheraccessVlan = ((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getAccessVlan();
                        if (myaccessvlan != 0 && anotheraccessVlan != 0) {
                            if (myaccessvlan != anotheraccessVlan) {
                                if (!warningStatements.contains(ethernetSeting.getName() + "と" + ((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getName() + "のaccessVlanが一致しません") && !warningStatements.contains(((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getName() + "と" + ethernetSeting.getName() + "のaccessVlanが一致しません")) {

                                    Map<String,String> ins = new HashMap<>();
                                    ins.put(ethernetSeting.getId(),"accessVlan");
                                    ins.put(((EthernetSetting) ethernetSeting).getConectedThing().getId(),"accessVlan");

                                    String message = ethernetSeting.getName() + "と" + ((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()).getName() + "のaccessVlanが一致しません";
                                    errorInfos.add(new ErrorInfo(message, false, "accessVlanの不一致",ins));//JSONを作るための処理
                                    warningStatements.add(message);
                                    changeColor(ethernetSeting,orangered);
                                    changeColor(((EthernetSetting) ((EthernetSetting) ethernetSeting).getConectedThing()),orangered);
                                }
                            }
                        }
                    }
                }

                //mode

            }
        }
//        for (String warningStatement : warningStatements) {
//            textArea.append(warningStatement + "\n");
//        }
//        for (String errorStatement : errorStatements) {
//            textArea.append(errorStatement + "\n");
//        }
    }



    /*vlanの重複をチェックするメソッド
    * 直接リンクがないのに同じVlan番号が振られている
    　異なるサブネットなのにおなじVlan がはいっている
    *　警告２
*/
    public static void vlanDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances,ArrayList<String> vlanWarningStatement) {//コンフィグが直接つながっていないのに同じVlan番号が振られている。
        ArrayList<EthernetSetting> vlanlists = new ArrayList<>(); //VLANの番号に対応したインスタンス
        ArrayList<Integer> vlanNumbers = new ArrayList<>(); //VLANの番号
//        ArrayList<String> vlanWarningStatement = new ArrayList<>();



        for (ClassElement instance : instances) {
            if (instance instanceof EthernetSetting) {
                if (((EthernetSetting) instance).getMode().equals("access")) {

                    for(Vlan vlan : ((EthernetSetting) instance).getConfig().getVlan()){
                        if(vlan.getNum() == ((EthernetSetting) instance).getAccessVlan()){
                            vlanNumbers.add(((EthernetSetting) instance).getAccessVlan());
                            vlanlists.add((EthernetSetting) instance);
                            break;
                        }
                    }



                } else if (((EthernetSetting) instance).getMode().equals("trunk")) {for (Integer vlan : ((EthernetSetting) instance).getAllowedVlans()) {
                        for(Vlan vlan1 : ((EthernetSetting) instance).getConfig().getVlan()){
                            if(vlan1.getNum() == vlan) {
                                vlanNumbers.add(vlan);

                                vlanlists.add((EthernetSetting) instance);
                                break;
                            }}
                    }
                }

                //conectedThingsの追加
                if(instance.getlink() != null){

                    if(((EthernetSetting) instance).getLink() != null){

                        ((EthernetSetting) instance).setConectedThing(((EthernetSetting) instance).getLink().getAnotherLinkableElement((LinkableElement) instance));
                    }
                }

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
                while (check) {


                    Config config = conectedConfigList.get(count);//つながっているリストに入っているコンフィグ
                    for (EthernetSetting eth : config.getEthernetSetting()) {//コンフィグのEthernetSettingのVLANを探して同じモノガアアルカ

                        ArrayList<Integer> vlann = new ArrayList<>();
                        if (eth.getMode().equals("access")) {
                            for(Vlan vlan : eth.getConfig().getVlan()){
                                if(vlan.getNum() == eth.getAccessVlan()){
                                    vlann.add(eth.getAccessVlan());
                                }
                            }

                        } else if (eth.getMode().equals("trunk")) {
                            for (Integer vlan : eth.getAllowedVlans()) {
                                for(Vlan vlan1 : eth.getConfig().getVlan()){
                                    if(vlan1.getNum() == vlan) {
                                        vlann.add(vlan);
                                    }}
                            }
                        }

                        if (vlann.contains(number)) {
                            ArrayList<Integer> eth_vlans = new ArrayList<>();//EternetSettingの反対側のVLAN
                            if (eth.getConectedThing() instanceof EthernetSetting) {//EthernetSettingのつながっている方(逆側)のVLAN番号を調べる
                                if (((EthernetSetting) eth.getConectedThing()).getMode().equals("access")) {
                                    for(Vlan vlan : ((EthernetSetting) eth.getConectedThing()).getConfig().getVlan()){
                                        if(vlan.getNum() == ((EthernetSetting) eth.getConectedThing()).getAccessVlan()) {
                                            eth_vlans.add(((EthernetSetting) eth.getConectedThing()).getAccessVlan());
                                        }}
                                } else if (((EthernetSetting) eth.getConectedThing()).getMode().equals("trunk")) {
                                    ;
                                    for (Integer vlan : ((EthernetSetting) eth.getConectedThing()).getAllowedVlans()) {
                                        for(Vlan vlan1 : ((EthernetSetting) eth.getConectedThing()).getConfig().getVlan()){
                                            if(vlan1.getNum() == vlan) {
                                                eth_vlans.add(vlan);
                                            }}
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
                for (EthernetSetting vlaneth : duplicationVlan) {
                    Config config2 = new Config();
                    config2 = vlaneth.getConfig();
                    if (!configList.contains(config2)) {
                        configList.add(config2);
                    }
                }



                //configListとconectedConfigListの比較(重複の確認)　ここから

                for (Config sameConfig : configList) {
//                    System.out.println("configList"+sameConfig.getName());
                    if (!conectedConfigList.contains(sameConfig)) {
                        if (!vlanWarningStatement.contains("VLAN" + number + "が重複してます")) {
                            String message = "VLAN" + number + "が重複してます";
                            errorInfos.add(new ErrorInfo(message, false, "VLANの重複", null));//JSONを作るための処理
                            vlanWarningStatement.add(message);
//                            ここに色を変える処理を追加する 要編集
                        }
                    }
                }
                for (Config sa : conectedConfigList) {
//                    System.out.println("conectedConfigList" +
//                            ""+sa.getName());

                }

                //ここまで-1


            }
        }
        //ことなるサブネットなのに同じVLANがふられている。 ここから -2


        ArrayList<VlanSetting> vlanSettings = new ArrayList<>();
        int vlanNumber;

        for (ClassElement instance : instances) {
            if (instance instanceof VlanSetting) {
                vlanSettings.add((VlanSetting) instance);
            }
        }
        ArrayList<VlanSetting> duplicationVlanSettingNumber = new ArrayList<>();
        while (vlanSettings.size() != 0) {
            duplicationVlanSettingNumber.clear();
            int number2 = vlanSettings.get(0).getVlanNum();
            for (int j = 0; j < vlanSettings.size(); j++) {//numberと一致するＶＬＡＮをぬきだして、調べた物は消す。
                if (number2 == vlanSettings.get(j).getVlanNum()) {
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
                            if (!vlanWarningStatement.contains("VLAN" + number2 + "内で異なるネットワークアドレスが割り当てられています。")) {
                                String message = "VLAN" + number2 + "内で異なるネットワークアドレスが割り当てられています。";
                                vlanWarningStatement.add(message);
                                Map<String,String> ins = new HashMap<>();
                                for (VlanSetting vlanSetting3 : duplicationVlanSettingNumber) {
                                    try {
                                        Check.changeColor(vlanSetting3, orangered);
                                        ins.put(vlanSetting3.getId(),"ipAddress");
                                        ins.put(vlanSetting3.getId(),"subNetMask");
                                    } catch (InvalidEditingException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                errorInfos.add(new ErrorInfo(message, false, "同一VLAN内でことなるネットワークの割り当て",ins));//JSONを作るための処理
                            }
                        }
                    }
                }
            }
        }


    }


    //設定値の欠如などを扱う
    //EthernetSetttingノード　portの欠如　shutdownがfalseかどうか
    //Vlan　num の欠如
    //VlanSetting vlanNumの欠如
    //未使用インターフェースの有効化
    public static void nodeKetujoCheck(ArrayList<ClassElement> instances,TextArea textArea,ArrayList<String> errorStatements,ArrayList<String> warningStatements){
        for(ClassElement instance : instances){
            if(instance instanceof  EthernetSetting){
                if(((EthernetSetting) instance).getPort()==-1){
                    String message = instance.getName() + "のportを設定して下さい";
                    errorInfos.add(new ErrorInfo(message, true, "portの未設定",mapOf(instance.getId(), "port") ));//JSONを作るための処理
                    errorStatements.add(message);//エラー文の追加
                    try {

                        changeColor(instance, red);//色の切り替え

                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
//                System.out.println(instance.getName()+":" + ((EthernetSetting) instance).getShutdown()
//                );
                if(((EthernetSetting) instance).getShutdown() && ((EthernetSetting) instance).getLink()!=null){
                    String message = instance.getName()+"がshutdownされています";
                    errorInfos.add(new ErrorInfo(message, false, "",mapOf(instance.getId(), "shutdown") ));//JSONを作るための処理
                    warningStatements.add(message);

                    try {

                        changeColor(instance, orangered);//色の切り替え

                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }

                //未使用インターフェースの有効化
                if(((EthernetSetting) instance).getLink()==null && !((EthernetSetting) instance).getShutdown()){
                    String message = instance.getName()+"が使用されていないポートが有効化されています";
                    errorInfos.add(new ErrorInfo(message, false, "使用されていないポートの有効化",mapOf(instance.getId(), "shutdown") ));//JSONを作るための処理
                    warningStatements.add(message);

                    try {
                        changeColor(instance, orangered);//色の切り替え
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

            if(instance instanceof Vlan){
                if(((Vlan) instance).getNum()==-1){
                    String message = instance.getName() + "のnumを設定して下さい";
                    errorInfos.add(new ErrorInfo(message, false, "VLANのnumの未設定",mapOf(instance.getId(), "num") ));//JSONを作るための処理
                    errorStatements.add(message);//エラー文の追加
                    try {

                        changeColor(instance, red);//色の切り替え

                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if(instance instanceof VlanSetting){
                if(((VlanSetting) instance).getVlanNum()==-1){
                    String message = instance.getName() + "のvlanNumを設定して下さい";
                    errorInfos.add(new ErrorInfo(message, false, "VlanSettingのVlannumの未設定",mapOf(instance.getId(), "vlanNum") ));//JSONを作るための処理
                    errorStatements.add(message);//エラー文の追加
                    try {

                        changeColor(instance, red);//色の切り替え

                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }

    }
    //対向機器とのチェック　speed duplex
    public static void conectedConfigCheck(ArrayList<ClassElement> instances,TextArea textArea,ArrayList<String> errorStatements,ArrayList<String> warningStatements){
        for(ClassElement classElement : instances){
            if(classElement instanceof EthernetSetting){
                EthernetSetting ethernetSetting = (EthernetSetting) classElement;
                if(ethernetSetting.getConectedThing() instanceof  EthernetSetting){
                    //speed
                    if(!ethernetSetting.getSpeed().equals(((EthernetSetting) ethernetSetting.getConectedThing()).getSpeed())){
                        if (!warningStatements.contains(ethernetSetting.getName() + "と" + ((ethernetSetting).getConectedThing()).getName() + "のspeedが一致しません") && !warningStatements.contains(((EthernetSetting)ethernetSetting).getConectedThing().getName() + "と" + ethernetSetting.getName() + "のspeedが一致しません")) {
                            Map<String,String> ins = new HashMap<>();
                            ins.put(ethernetSetting.getId(),"speed");
                            ins.put(((ethernetSetting).getConectedThing()).getId(),"speed");

                            String message = ethernetSetting.getName() + "と" + ((ethernetSetting).getConectedThing()).getName() + "のspeedが一致しません．";
                            errorInfos.add(new ErrorInfo(message, false, "",ins));//JSONを作るための処理

                            warningStatements.add(message);
                        }
                        try {
                            Check.changeColor(ethernetSetting, orangered);
                            Check.changeColor(((EthernetSetting) ethernetSetting).getConectedThing(), orangered);
                        } catch (InvalidEditingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //duplex
                    if(!ethernetSetting.getDuplex().equals(((EthernetSetting) ethernetSetting.getConectedThing()).getDuplex())){
                        if (!warningStatements.contains(ethernetSetting.getName() + "と" + ((ethernetSetting).getConectedThing()).getName() + "のduplexが一致しません") && !warningStatements.contains(((EthernetSetting)ethernetSetting).getConectedThing().getName() + "と" + ethernetSetting.getName() + "のduplexが一致しません")) {
                            Map<String,String> ins = new HashMap<>();
                            ins.put(ethernetSetting.getId(),"duplex");
                            ins.put(((ethernetSetting).getConectedThing()).getId(),"duplex");

                            String message = ethernetSetting.getName() + "と" + ((ethernetSetting).getConectedThing()).getName() + "のduplexが一致しません";
                            errorInfos.add(new ErrorInfo(message, false, "duplexの不一致",ins));//JSONを作るための処理
                            warningStatements.add(message);
                        }
                        try {
                            Check.changeColor(ethernetSetting, orangered);
                            Check.changeColor(((EthernetSetting) ethernetSetting).getConectedThing(), orangered);
                        } catch (InvalidEditingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
    //深さ優先探索
    /*dfsを行うための関数 https://qiita.com/drken/items/4a7869c5e304883f539b#3-%E6%B7%B1%E3%81%95%E5%84%AA%E5%85%88%E6%8E%A2%E7%B4%A2-dfs-%E3%81%A8%E5%B9%85%E5%84%AA%E5%85%88%E6%8E%A2%E7%B4%A2-bfs
     * 引数　graph: 隣接リスト表現　configsをchangeメソッドで変換しここに入れる（数字はconfigsのリストの添え字と対応している）
     * 　　　数字が１のときconfigs.get(1)で対応したコンフィグのインスタンス情報を取得する
     * 　　　seen: すでに発見したかどうかを格納するインスタンス　1=true,0= false
     *      int v:DFS探索を始めるための始点
     * */

    public static void dfs(ArrayList<ArrayList<Integer>> graph, ArrayList<Integer> seen, int v) {
        seen.set(v, 1);
        for (Integer nextV : graph.get(v)) {
            if (seen.get(nextV) == 1) {
                continue;
            }
            dfs(graph, seen, nextV);
        }
    }

    /*ループ用のDFS（深さ優先探索）　参考：https://drken1215.hatenablog.com/entry/2023/05/20/200517* 引数 : grafh 隣接リスト表現されたリスト＜リスト＞　例graphChange(ArrayList<Config>)
     * seen　すでに見た点（行きがけ順）　finished<>　それ移譲先がない点（帰りがけ順）　hist　ループを復元するためのスタック
     * v 探索開始点　p　探索で戻らないようにするための引数*/
    public static Integer rupeDfs(ArrayList<ArrayList<Integer>> graph, ArrayList<Integer> seen, ArrayList<Integer> finished, Stack<Integer> hist, int v, int p, ArrayList<Integer> s, ArrayList<Integer> q) {
        seen.set(v, 1); //見た点であるためseenの該当の場所を1(true)にする
        hist.push(v); //ループ復元のためにpushする

        if (graph.get(v).size() != 0) {
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


    /*警告　１
    * エラー　１*/
    public static ArrayList<ArrayList<Config>> rupeChecks(ArrayList<ClassElement> instances, TextArea textArea,ArrayList<String> warningStatement) {
        ArrayList<String> rupeStatements = new ArrayList<>();
        int pos = -1;
        ArrayList<Config> configs = new ArrayList<>();//コンフィグインスタンスを格納する（すべて「）
        for (ClassElement instance : instances) {
            if (instance instanceof Config) {
                configs.add((Config) instance);
            }
        }
        int count = 0;
        ArrayList<ArrayList<Integer>> graph = graphChange(configs);

        ArrayList<ArrayList<Integer>> graph2 = graphChange(configs);

        ArrayList<ArrayList<Integer>> rupenumbers = new ArrayList<>();
        for (int k = 0; k < configs.size(); k++) {
            ArrayList<Integer> s = new ArrayList<>();
            ArrayList<Integer> q = new ArrayList<>();
            ArrayList<Integer> seen = new ArrayList<>(); //1=ture,0=false
            ArrayList<Integer> finished = new ArrayList<>(); //1=ture,0=false
            Stack<Integer> hist = new Stack<>();//リスト復元のためのスタック
            for (int i = 0; i < configs.size(); i++) {//コンフィグの数と同じだけ0を格納する
                seen.add(0);
                finished.add(0);
            }
            if (count == 0) {
                pos = rupeDfs(graph, seen, finished, hist, k, -1, s, q);//dfsを呼び出
            } else {
                pos = rupeDfs(graph2, seen, finished, hist, k, -1, s, q);
            }

            ArrayList<Integer> cycle = new ArrayList<>();//サイクルを復元するため、ここにはいっているものがループを作っている
            while (!hist.empty()) {
                int t = hist.peek();
                cycle.add(t);
                hist.pop();
                if (t == pos) break;//ループ以外の点を抜かすため
            }
            if (cycle.size() != 0) {
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

                    for (int a = 0; a < graph2.get(s.get(0)).size(); a++) {
                        if (graph2.get(s.get(0)).get(a).equals(q.get(0))) {
                            graph2.get(s.get(0)).remove(a);
                            break;
                        }
                    }
                    for (int b = 0; b < graph2.get(q.get(0)).size(); b++) {
                        if (graph2.get(q.get(0)).get(b).equals(s.get(0))) {
                            graph2.get(q.get(0)).remove(b);
                            break;
                        }
                    }
                    count++;
                    k--;

                }
            } else {//サイクルが見つからなかったとき
                count = 0;
                graph2 = graphChange(configs);
            }
        }
        ArrayList<ArrayList<Config>> rupeconfigs = new ArrayList<>();
        for (ArrayList<Integer> rupes : rupenumbers) {
            ArrayList<Config> ruco = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            for (Integer number : rupes) {
                ruco.add(configs.get(number));
                names.add(configs.get(number).getName());
            }
            rupeconfigs.add(ruco);
            rupeStatements.add(names + "がループ構成になっています\n");
        }
        for (String statement : rupeStatements) {
//            textArea.append(statement);
            warningStatement.add(statement);
        }
        Map<String,String> ins = new HashMap<>();
        for (ArrayList<Config> coo : rupeconfigs) {
            for (Config rupes : coo) {
                try {
                    changeColor((ClassElement) rupes, orangered);
                    ins.put(rupes.getId(),null);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String message = "がループ構成になっています";
        errorInfos.add(new ErrorInfo(message, false, "ループの検出",ins));//JSONを作るための処理


        return rupeconfigs;
    }
//    単体ノードでのチェック
    public static void oneNodeCheck(ArrayList<ClassElement> instances , ArrayList<String> errorStatements){
        for(ClassElement instance : instances){
            if(instance instanceof EthernetSetting){
                if(((EthernetSetting) instance).getPort()==-1){
                    String message = instance.getName() + "のportの値が設定されていません";
                    errorInfos.add(new ErrorInfo(message, true, "portの未設定",mapOf(instance.getId(), "port") ));//JSONを作るための処理
                    errorStatements.add(message);
                    try {
                        changeColor(instance, red);
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if(instance instanceof VlanSetting){
                if(((VlanSetting) instance).getVlanNum() == -1){
                    String message = instance.getName() + "のvlanNumの値が設定されていません";
                    errorInfos.add(new ErrorInfo(message, true, "vlanNumの未設定",mapOf(instance.getId(), "vlanNum") ));//JSONを作るための処理
                    errorStatements.add(message);

                    try {
                        changeColor(instance, red);
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if(instance instanceof Vlan){
                if(((Vlan) instance).getNum()==-1){
                    String message = instance.getName() + "のnumの値が設定されていません";
                    errorInfos.add(new ErrorInfo(message, true, "numの未設定",mapOf(instance.getId(), "num") ));//JSONを作るための処理
                    errorStatements.add(message);

                    try {
                        changeColor(instance, red);
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }







    //つながっているEthernetSettingのnativeVlanの確認
    //EthernetSettingのmodeがtrunkの時、NativeVLANが設定されているかを確認する。
//    警告　３
    public static void nativeVlanCheck(ArrayList<ClassElement> instances, TextArea textArea,ArrayList<String> nativevlanErrorStatement,ArrayList<String> nativevlanWarningStatement) {
//        ArrayList<String> nativevlanErrorStatement = new ArrayList<>();
//        ArrayList<String> nativevlanWarningStatement = new ArrayList<>();

        for (ClassElement classElement : instances) {
            if (classElement instanceof EthernetSetting) {
                EthernetSetting conectedEthernetSetting = null;//リンク先のEthernetSetting
                int myNativeVlan = -1; //classElement側のNativeVLAN
                int conectedNativeVlan = -1; //conectedEthernetSetting側のNativeVLAN
                if (((EthernetSetting) classElement).getMode().equals("trunk")) {

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
                                String message = classElement.getName() + "のnativeVlanが設定されていません";
                                errorInfos.add(new ErrorInfo(message, false, "nativeVlanの未設定",mapOf(classElement.getId(), "nativeVlan") ));//JSONを作るための処理
                                nativevlanWarningStatement.add(message);
                                try {
                                    changeColor(classElement, orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        if (conectedEthernetSetting.getMode().equals("trunk") && conectedEthernetSetting.getNativeVlan() == -1) {//ModeがtrunkなのにNativeVLANが設定されていない
                            if (!nativevlanWarningStatement.contains(conectedEthernetSetting.getName() + "のnativeVlanが設定されていません")) {
                                String message = conectedEthernetSetting.getName() + "のnativeVlanが設定されていません";
                                errorInfos.add(new ErrorInfo(message, false, "nativeVlanの未設定",mapOf(classElement.getId(), "nativeVlan") ));//JSONを作るための処理
                                nativevlanWarningStatement.add(message);
                                try {
                                    changeColor(conectedEthernetSetting, orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        if (conectedEthernetSetting.getMode().equals("access")) {//Modeがtrunkなのに対向のMODEが違う
                            if (!nativevlanWarningStatement.contains(classElement.getName() + "と" + conectedEthernetSetting.getName() + "のmodeが一致しません")) {
                                Map<String,String> ins = new HashMap<>();
                                ins.put(classElement.getId(),"mdoe");
                                ins.put(conectedEthernetSetting.getId(),"mode");

                                String message = classElement.getName() + "と" + conectedEthernetSetting.getName() + "のmodeが一致しません";
                                errorInfos.add(new ErrorInfo(message, false, "",ins));//JSONを作るための処理
                                nativevlanWarningStatement.add(message);
                                try {
                                    changeColor(classElement, orangered);
                                    changeColor(conectedEthernetSetting, orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        }

                        if (((EthernetSetting) classElement).getMode().equals("trunk") && conectedEthernetSetting.getMode().equals("trunk") && myNativeVlan != conectedNativeVlan) {
                            if (!nativevlanErrorStatement.contains(classElement.getName() + "と" + conectedEthernetSetting.getName() + "のNativeVlanが一致していません") && !nativevlanErrorStatement.contains(conectedEthernetSetting.getName() + "と" + classElement.getName() + "のNativeVlanが一致していません")) {

                                Map<String,String> ins = new HashMap<>();
                                ins.put(classElement.getId(),"nativeVlan");
                                ins.put(conectedEthernetSetting.getId(),"nativeVlan");

                                String message = classElement.getName() + "と" + conectedEthernetSetting.getName() + "のNativeVlanが一致していません";
                                errorInfos.add(new ErrorInfo(message, false, "",ins));//JSONを作るための処理
                                nativevlanErrorStatement.add(message);
                                try {
                                    changeColor(classElement,   orangered);
                                    changeColor(conectedEthernetSetting, orangered);
                                } catch (InvalidEditingException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }

                    }
                }
            }
        }

//        for (String eroorStatemnt : nativevlanErrorStatement) {
//            textArea.append(eroorStatemnt + "\n");
//        }
//        for (String warnigStatemnt : nativevlanWarningStatement) {
//            textArea.append(warnigStatemnt + "\n");
//        }

    }


    /*dfsのための処理
     * configsにはConfigクラスのインスタンスがあり，config.getLInkedConfigsの隣接リストを数字で入手している
     * configs = [cf1,cf2,cf3] 隣接リスト(Linkedconfig)　=　[[cf3],[cf1,cf3],[cf2]* 返り値　=　[[2(cf3)],[0(cf1),2],[1(cf2)]]*/

    public static ArrayList<ArrayList<Integer>> graphChange(ArrayList<Config> configs) {
        ArrayList<ArrayList<Integer>> graph = new ArrayList<>();
        int number = 0;
        for (Config config : configs) {
            ArrayList<Integer> numbers = new ArrayList<>();
            for (Config config1 : config.getLinkedConfigs()) {
                for (int i = 0; i < configs.size(); i++) {
                    if (config1.equals(configs.get(i))) {
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


    public static void dfsCheck(ArrayList<ClassElement> instances, Config config) {
        ArrayList<Config> conf = new ArrayList<>();
        for (ClassElement instance : instances) {
            if (instance instanceof Config) {
                conf.add((Config) instance);
            }
        }
        int v = 0;
        for (int i = 0; i < conf.size(); i++) {
            if (conf.get(i).equals(config)) {
                v = i;
            }
        }
        ArrayList<Integer> seen = new ArrayList<>(); //1=ture,0=false
        for (int i = 0; i < conf.size(); i++) {
            seen.add(0);
        }
        Check.dfs(Check.graphChange(conf), seen, v);

    }
//stpについてはすべて確認する必要がある //要編集　表に追加
    /*警告　５
    * エラー　５*/
public static void stpCheck(ArrayList<ClassElement> instances,ArrayList<ArrayList<Config>> rupeConfigs, TextArea textArea,ArrayList<String> stpErrorStatement,ArrayList<String> stpWarningStatement){
//    ArrayList<String> stpErrorStatement = new ArrayList<>();
//    ArrayList<String> stpWarningStatement = new ArrayList<>();
//    System.out.println("stp実行");
    for(ArrayList<Config> currentRupe : rupeConfigs){//調べているループ
//        System.out.println("stp実行ーループ" +
//                "");
        for(Config c : currentRupe){
//            System.out.println(c.getName());
        }
        //１つのコンフィグにstpSettingインスタンスがあった場合に他のコンフィグでstpSettingインスタンスが存在するかを検証 いらない？
//        boolean stp = false; //stpの設定があるかどうか
//        for(Config config : currentRupe){
//            if(config.getStpSetting().size() > 1){
//                stp = true;
//            }
//        }
//        if(stp){
//            for(Config conf : currentRupe){
//                if(conf.getStpSetting().size() == 0){
//                    if(!stpWarningStatement.contains(conf.getName() + "にstpの設定がありません．")){
//                        stpWarningStatement.add(conf.getName() + "にstpの設定がありません．");
//                    }
//                }
//            }
//        }
        ArrayList<String> name = new ArrayList<>();
        for(Config c : currentRupe){
            name.add(c.getName());
        }
        //すべてのモードが同じかどうか 機器に設定できるモードは一つののみ
        boolean first = true; //for文の一回目か
        String mode = null; //モードの格納
        boolean sameMode = true; //モードが同じかどうか
        for(Config conf : currentRupe){
            for(StpSetting stpSetting : conf.getStpSetting()){
                if(first){
                    mode = stpSetting.getMode();
                    first = false;
                }else {
                    if(!stpSetting.getMode().equals(mode)){

                        stpWarningStatement.add(name +"に設定されているstpのmodeが統一されていません");
                        sameMode = false;
                        break;
                    }
                }
            }
        }

        //ループを形成しているEthernetSettingインスタンスを見つける．
        ArrayList<EthernetSetting> ethernetsettings = new ArrayList<>();//ループを形成しているEthernetSettingインスタンス
        for(Config config :currentRupe){
            for(EthernetSetting eth :config.getEthernetSetting()){
                if(eth.getConectedThing() instanceof EthernetSetting){
                    if(currentRupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())){
                        ethernetsettings.add(eth);
                    }
                }
            }
        }
//        System.out.println("1");
//        System.out.println();
        //設定すべき値がすべて設定されているか　情報が正しく出力されない可能性があるためエラー
        for(Config config :currentRupe) {
//           System.out.println(config.getStpSetting());
            for (StpSetting stpS : config.getStpSetting()) {
//                System.out.println(stpS.getMacAddress());
                if (stpS.getBridgePriority() == -1) {
                    stpErrorStatement.add(stpS.getName() + "のbridgePriorityが設定されていません");
                }
                if (stpS.getVlan() == -1) {
                    stpErrorStatement.add(stpS.getName() + "のvlanが設定されていません");
                }
//                System.out.println("qqq");
                if (stpS.getMacAddress() == null || stpS.getMacAddress().isEmpty()) {
                    stpWarningStatement.add(stpS.getName() + "のmacaddressが設定されていません");
                }
//                System.out.println("www");
            }
        }

        for(EthernetSetting ethernetSetting : ethernetsettings){
            if(ethernetSetting.getSpeed().equals("")){
                stpWarningStatement.add(ethernetSetting.getName() + "のspeedが設定されていません");
            }
        }
//        System.out.println("2");
        ArrayList<ArrayList<Integer>> allvlans = new ArrayList<>();
        
        //StpSettingで指定したvlanがEthernetSettingで指定されていない．+ 指定したvlanのインスタンスがない
        for(Config config : currentRupe){
//            System.out.println("11");
//            System.out.println(config.getStpSetting());
            for(StpSetting stpS : config.getStpSetting()){
//                System.out.println("12");
                int stpVlan = stpS.getVlan();
                EthernetSetting checkEthernetSetting = new EthernetSetting();//configにつながれたループを構成しているethernetSetting
                for(EthernetSetting eth :ethernetsettings){

                    if(config.getEthernetSetting().contains(eth)){
                        checkEthernetSetting = eth;
//                        System.out.println("13");




                //EtherneSettingのvlanを割り出す．
                ArrayList<Integer> vlans =  new ArrayList<>();
                if(checkEthernetSetting.getMode().equals("access")){

                    for(Vlan vlan : checkEthernetSetting.getConfig().getVlan()){

                        if(vlan.getNum() == checkEthernetSetting.getAccessVlan()) {

                            vlans.add(checkEthernetSetting.getAccessVlan());


                        }}
                }else if(checkEthernetSetting.getMode().equals("trunk")){

                    for (Integer vlan : checkEthernetSetting.getAllowedVlans()) {

                        for(Vlan vlan1 : checkEthernetSetting.getConfig().getVlan()){

                            if(vlan1.getNum() == vlan) {
                                vlans.add(vlan);
                            }}
                    }

                }

//                        System.out.println("3" + vlans);
                allvlans.add(vlans);
                ArrayList<Integer> cvlans = new ArrayList<>();//stpを設定すべきvlan
                if(!vlans.contains(stpVlan)) {
                    stpWarningStatement.add(stpS.getName() + "で指定したvlanIDが" + checkEthernetSetting.getName() + "に設定されていません");
                }else{
                    if(!cvlans.contains(stpVlan)){
                        cvlans.add(stpVlan);
                    }
                }

                //指定したvlanのインスタンスがない
                boolean vlanCheck = false ;
                for(Vlan vlaninstance : config.getVlan()){
                    if(stpVlan == vlaninstance.getNum()){
                        vlanCheck = true;
                    }
                }

                if(!vlanCheck){
                    if(!stpErrorStatement.contains(stpS.getName()+"で指定したvlanIDが"+config.getName()+"で作成されていません")){
                        stpErrorStatement.add(stpS.getName()+"で指定したvlanIDが"+config.getName()+"で作成されていません");
                    }
                }
                    }
                }

            }
        }

//        System.out.println("4");
        //ひとつでもstpsettingでvlanが指定されていたらループ全体で指定されている必要がある．(EhternetSetting内で設定されているのも条件)
        for(Config config : currentRupe){
            //同じvlanの設定がある
            ArrayList<Integer> dvlans = new ArrayList<>();//stpの設定があるvlan
            for(StpSetting stpSetting : config.getStpSetting()){
                if(dvlans.contains(stpSetting.getVlan())){
                    stpErrorStatement.add(config.getName() + "で，vlan"+stpSetting.getVlan()+"のstpSettingインスタンスが複数あります．");
                }else{
                    dvlans.add(stpSetting.getVlan());

                }
            }//同じvlanのstpの設定が複数ない状態
            //ひとつでもstpsettingでvlanが指定されていたらループ全体で指定されている必要がある．(EhternetSetting内で設定されているのも条件)
            for(StpSetting stpSetting : config.getStpSetting()){
                int vlanSt = stpSetting.getVlan();
                boolean check2 = false;
                String configName = "";
                for(Config config2 : currentRupe){
                    for(StpSetting stpSetting2 : config2.getStpSetting()){
                        if(stpSetting2.getVlan() == vlanSt){
                            check2=true;
                            configName=config2.getName();
                        }
                    }
                }
                if(!check2){
                    if(!stpErrorStatement.contains(configName + "で" + "vlan"+vlanSt +"のstpの設定を行ってください")){
                        stpErrorStatement.add(configName + "で" + "vlan"+vlanSt +"のstpの設定を行ってください");
                    }

                }
            }



        }
//        System.out.println("5");
        //allvlansにそれぞれのethernetSettingの通信に使われているvlanがリストで入っている．
        ArrayList<Integer> nowvlans = new ArrayList<>();

//        System.out.println("6");
//        System.out.println(nowvlans);
//        System.out.println(allvlans);
        nowvlans.addAll(allvlans.get(0));
//        System.out.println("-1");
        for(ArrayList<Integer> a : allvlans){
//            System.out.println("6-2");
            for(Integer b : a) {
//                System.out.println("6-3");
                if(!nowvlans.contains(b)){
//                    System.out.println("6-4");
                    nowvlans.remove(b);

                }
            }
        }
//        System.out.println("8");
        //nowvlansにはループにあるvlanがすべて入っている．
        //ループのstp設定の中に何も対策されていないＶＬＡＮＩＤがないかどうか
        for(Integer v : nowvlans) {
            boolean check = false;
            for (Config confi : currentRupe) {
                for (StpSetting stps1 : confi.getStpSetting()) {
                    if (stps1.getVlan() == v) {
                        check = true;
                    }
                }
            }
            if (!check) {//falseの時は設定がないとき
                stpErrorStatement.add(name + "vlan" + v + "のstpの設定がありません");
            }
//            System.out.println("7");



        }























    }
}

//public static void clientVlanCheck(ArrayList<ClassElement> instances, TextArea textArea,ArrayList<String> nativevlanErrorStatement,ArrayList<String> nativevlanWarningStatement){
//    for(ClassElement instance : instances){
//        if(instance instanceof Clients){
//
//        }
//    }
//}


    //ospfの確認メソッド

    //mtuのミスマッチ
    //ospfnetworktypeのミスマッチ
    //helloまたはdeadタイマーのインターバルのミスマッチ
    //重複するルータIDを検出して出力
    //stubのミスマッチ
    //エリア0の未設定

    //OSPFInterfaceSettingで指定したネットワークアドレスが存在しない場合
    //片方のみOspfInterfaceSettingが設定されている場合警告文を出す。
    //OspfinterfaceSettingにあるipAddressの重複も追加する（必ず）記載2025年3月1日

    //表の追加　ここから
    public static void osfpCheck(ArrayList<ClassElement> instances,TextArea textArea,ArrayList<String> ospfErrorStatement,ArrayList<String> ospfWarningStatement) {
        System.out.println("ospfCheck開始");
        ArrayList<EthernetSetting> ospfEthernetSetting = new ArrayList<>();//図にあるOSPFに関するEhternetSettingインスタンスを全て格納していく
        ArrayList<OspfSetting> ospfSettings = new ArrayList<>();//図に存在するOspfSettingインスタンスの全て
//        System.out.println("ospfcheck実行");
        for (ClassElement classElement : instances) {
            if (classElement instanceof EthernetSetting) {
                if (((EthernetSetting) classElement).getConfig().getOspfSetting() != null) {
                    ospfEthernetSetting.add((EthernetSetting) classElement);
                }
            }
            if (classElement instanceof OspfSetting) {
                ospfSettings.add((OspfSetting) classElement);
            }
            if (classElement instanceof Config) {
                virtualLinkketujo((Config) classElement, ospfWarningStatement, instances);
            }
        }
        if (ospfSettings.size() != 0) {


//        System.out.println("ospfcheck実行-1");
        //ここから　　対向のルータートの整合性確認 *2

        for (EthernetSetting ethernetSetting : ospfEthernetSetting) {
            if (ethernetSetting.getConectedThing() instanceof EthernetSetting) {
                EthernetSetting otherethernetSetting = (EthernetSetting) ethernetSetting.getConectedThing();//対向のethernetSettingインスタンス

                HashMap<Integer, OspfInterfaceSetting> ospfInterfaceSettingInstance = getOspfSettingInformation(ethernetSetting);
                HashMap<Integer, OspfInterfaceSetting> otherOspfInterfaceSettingInstance = getOspfSettingInformation(otherethernetSetting);


                for (HashMap.Entry<Integer, OspfInterfaceSetting> ospfInterfaceSettingEntry : ospfInterfaceSettingInstance.entrySet()) {//vlan毎に回す
                    //片方のみOspfInterfaceSettingが設定されている場合警告文を出す。

                    if (otherOspfInterfaceSettingInstance.containsKey(ospfInterfaceSettingEntry.getKey())) {
                        //片方のみnullのとき ここから　*1
                        if ((otherOspfInterfaceSettingInstance.get(ospfInterfaceSettingEntry.getKey()) == null && ospfInterfaceSettingEntry.getValue() != null)) {
                            if (!ospfWarningStatement.contains(otherethernetSetting.getConfig().getName() + "でvlan" + ospfInterfaceSettingEntry.getKey() + "に対応するOspfInterfaceSettingが設定されていません")) {
                                String message = otherethernetSetting.getConfig().getName() + "でvlan" + ospfInterfaceSettingEntry.getKey() + "に対応するOspfInterfaceSettingが設定されていません";
                                errorInfos.add(new ErrorInfo(message, false, "指定したVLANに対応するOspfInterfaceSettingの不足",mapOf(otherethernetSetting.getId(), null) ));//JSONを作るための処理
                                ospfWarningStatement.add(message);
                            }
                            try {
                                changeColor(otherethernetSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        } else if ((otherOspfInterfaceSettingInstance.get(ospfInterfaceSettingEntry.getKey()) != null && ospfInterfaceSettingEntry.getValue() == null)) {
                            if (!ospfWarningStatement.contains(ethernetSetting.getConfig().getName() + "でvlan" + ospfInterfaceSettingEntry.getKey() + "に対応するOspfInterfaceSettingが設定されていません")) {
                                String message = ethernetSetting.getConfig().getName() + "でvlan" + ospfInterfaceSettingEntry.getKey() + "に対応するOspfInterfaceSettingが設定されていません";
                                errorInfos.add(new ErrorInfo(message, false, "指定したVLANに対応するOspfInterfaceSettingの不足",mapOf(ethernetSetting.getId(), null) ));//JSONを作るための処理
                                ospfWarningStatement.add(ethernetSetting.getConfig().getName() + "でvlan" + ospfInterfaceSettingEntry.getKey() + "に対応するOspfInterfaceSettingが設定されていません");
                            }
                            try {
                                changeColor(ethernetSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        }
                        //ここまで *1
                        //両方nullの時
                        if (otherOspfInterfaceSettingInstance.get(ospfInterfaceSettingEntry.getKey()) != null && ospfInterfaceSettingEntry.getValue() == null) {
                            break;
                        }
//                        System.out.println("ospfcheck実行-2");

                        //対向のEhternetSettingインスタンスのOspfinterfaceSettingの比較
                        OspfInterfaceSetting ospfInterfaceSetting = ospfInterfaceSettingEntry.getValue();
                        OspfInterfaceSetting otherOspfInterfaceSetting = otherOspfInterfaceSettingInstance.get(ospfInterfaceSettingEntry.getKey());//対向のEhternetSettingインスタンスのVLANに対応するOspfInterfaceSetting
                        //helloまたはdeadタイマーのインターバルのミスマッチ
                        if (ospfInterfaceSetting.getHelloInterval() != otherOspfInterfaceSetting.getHelloInterval()) {
//                            System.out.println(ospfInterfaceSetting.getHelloInterval());
//                            System.out.println(otherOspfInterfaceSetting.getHelloInterval());
                            if (!ospfWarningStatement.contains(ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のhelloIntervalが一致していません") && !ospfWarningStatement.contains(otherOspfInterfaceSetting.getName() + "と" + ospfInterfaceSetting.getName() + "のhelloIntervalが一致していません")) {
                                Map<String,String> ins = new HashMap<>();
                                ins.put(ospfInterfaceSetting.getId(),"helloInterval");
                                ins.put(otherOspfInterfaceSetting.getId(),"helloInterval");

                                String message = ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のhelloIntervalが一致していません";
                                errorInfos.add(new ErrorInfo(message, false, "helloIntervalの不一致",ins));//JSONを作るための処理
                                ospfWarningStatement.add(message);
                            }
                            try {
                                changeColor(ospfInterfaceSetting, orangered);
                                changeColor(otherOspfInterfaceSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (ospfInterfaceSetting.getDeadInterval() != otherOspfInterfaceSetting.getDeadInterval()) {
//                            System.out.println(ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName());
//                            System.out.println(otherOspfInterfaceSetting.getName()+ "と" + ospfInterfaceSetting.getName() +"のdeadintervalが一致していません");
                            if ((!ospfWarningStatement.contains(ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のdeadintervalが一致していません")) && (!ospfWarningStatement.contains(otherOspfInterfaceSetting.getName() + "と" + ospfInterfaceSetting.getName() + "のdeadintervalが一致していません"))) {
                                Map<String,String> ins = new HashMap<>();
                                ins.put(ospfInterfaceSetting.getId(),"");
                                ins.put(otherOspfInterfaceSetting.getId(),"");

                                String message = ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のdeadIntervalが一致していません";
                                errorInfos.add(new ErrorInfo(message, false, "deadIntervalの不一致",ins));//JSONを作るための処理

                                ospfWarningStatement.add(ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のdeadIntervalが一致していません");
                            }
                            try {
                                changeColor(ospfInterfaceSetting, orangered);
                                changeColor(otherOspfInterfaceSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        //ospfnetworktypeのミスマッチ
                        if (!ospfInterfaceSetting.getOspfNetworkMode().equals(otherOspfInterfaceSetting.getOspfNetworkMode())) {
                            if (!ospfWarningStatement.contains(ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のospfNetworkModeが一致していません") && !ospfWarningStatement.contains(otherOspfInterfaceSetting.getName() + "と" + ospfInterfaceSetting.getName() + "のospfNetworkModeが一致していません")) {
                                Map<String,String> ins = new HashMap<>();
                                ins.put(ospfInterfaceSetting.getId(),"spfNetworkMode");
                                ins.put(otherOspfInterfaceSetting.getId(),"spfNetworkMode");

                                String message = ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のspfNetworkModeが一致していません";
                                errorInfos.add(new ErrorInfo(message, false, "",ins));//JSONを作るための処理
                                ospfWarningStatement.add(message);
                            }
                            try {
                                changeColor(ospfInterfaceSetting, orangered);
                                changeColor(otherOspfInterfaceSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }
//                        System.out.println("ospfcheck実行-3");

                        //stubのミスマッチ
                        if (!ospfInterfaceSetting.getStub().equals(otherOspfInterfaceSetting.getStub())) {
                            if (!ospfWarningStatement.contains(ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のstubが一致していません") && !ospfWarningStatement.contains(otherOspfInterfaceSetting.getName() + "と" + ospfInterfaceSetting.getName() + "のstubが一致していません")) {

                                Map<String,String> ins = new HashMap<>();
                                ins.put(ospfInterfaceSetting.getId(),"stub");
                                ins.put(otherOspfInterfaceSetting.getId(),"stub");

                                String message = ospfInterfaceSetting.getName() + "と" + otherOspfInterfaceSetting.getName() + "のstubが一致していません";
                                errorInfos.add(new ErrorInfo(message, false, "",ins));//JSONを作るための処理

                                ospfWarningStatement.add(message);
                            }
                            try {
                                changeColor(ospfInterfaceSetting, orangered);
                                changeColor(otherOspfInterfaceSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        //mtuのミスマッチ
                        if (ethernetSetting.getMtu() != otherethernetSetting.getMtu()) {
                            if (!ospfWarningStatement.contains(ethernetSetting.getName() + "と" + otherethernetSetting.getName() + "のmtuが一致していません") && !ospfWarningStatement.contains(otherethernetSetting.getName() + "と" + ethernetSetting.getName() + "のmtuが一致していません")) {
                                ospfWarningStatement.add(ethernetSetting.getName() + "と" + otherethernetSetting.getName() + "のmtuが一致していません");
                            }
                            try {
                                changeColor(ethernetSetting, orangered);
                                changeColor(otherethernetSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                        }


//                        if(ethernetSetting.getName().equals("eth3")){
//                            textArea.append(ethernetSetting.getName());
//                            textArea.append(ospfInterfaceSetting.getName());
//                            textArea.append(otherOspfInterfaceSetting.getName());
//                        }
                    }
                }


            }
        }//ここまで *2
        //重複するルータIDの検出
        HashMap<String, List<OspfSetting>> routerIds = new HashMap<>();
        for (OspfSetting ospfSetting : ospfSettings) {//Mapを構築
            routerIds.computeIfAbsent(ospfSetting.getRouterId(), k -> new ArrayList<>()).add(ospfSetting);
        }
//        System.out.println("ospfcheck実行-4");

        //重複するルータIDを検出して出力
        for (Map.Entry<String, List<OspfSetting>> entry : routerIds.entrySet()) {
            if (entry.getValue().size() > 1) {
//                System.out.println("ospfcheck実行-4--1");
                if (!ospfWarningStatement.contains(entry.getValue().get(0).getName() + "と" + entry.getValue().get(1).getName() + "のルータIDが重複しています") && !ospfWarningStatement.contains(entry.getValue().get(1) + "と" + entry.getValue().get(0) + "のルータIDが重複しています")) {
                    ospfWarningStatement.add(entry.getValue().get(0).getName() + "と" + entry.getValue().get(1).getName() + "のルータIDが重複しています");
//                    System.out.println(entry.getValue().get(0).getName() + "と" + entry.getValue().get(1).getName() + "のルータIDが重複しています");
                }
                try {
                    changeColor(entry.getValue().get(0), orangered);
                    changeColor(entry.getValue().get(1), orangered);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
//        System.out.println("ospfcheck実行-4--2");

        //OsofInterfaceSettingでしていしたネットワークアドレスの欠如
        for (OspfSetting os : ospfSettings) {
            for (OspfInterfaceSetting oss : os.getOspfInterfaceSettings()) {
//                System.out.println(oss.getName());
//                System.out.println("ospfche/ck実行-4--3-1");
                boolean check = false;
                for (EthernetSetting es : os.getConfig().getEthernetSetting()) {
                    HashMap<String, String> a = getEthernetSettingAddress(es);
//                    System.out.println("ospfcheck実行-4--3-2");
                    for (Map.Entry<String, String> entry : a.entrySet()) {
//                        System.out.println("ospfcheck実行-4--3-3-212");
//                        System.out.println(oss.getIpAddress()+"-"+entry.getKey()+"-"+oss.getWildcardMask()+"-"+entry.getValue());
//                        System.out.println(OutputInformation.checkSameNetwork2(oss.getIpAddress(),entry.getKey(),oss.getWildcardMask(),entry.getValue()));
                        if (check == false && !oss.getIpAddress().equals("") && !entry.getKey().equals("") && !oss.getWildcardMask().equals("") && !entry.getValue().equals("") && OutputInformation.checkSameNetwork2(oss.getIpAddress(), entry.getKey(), oss.getWildcardMask(), entry.getValue())) {
//                            System.out.println("ospfcheck実行-4--3-3");
                            check = true;
                            check = true;
                        }
//                        System.out.println("ospfcheck実行-4--3-3-555");
                    }
                }

//                System.out.println("ospfcheck実行-4--1");
                if (check) {//大丈夫な場合

                } else {
                    if (!ospfWarningStatement.contains(oss.getName() + "で指定したネットワークアドレスが設定されていません")) {
//                        System.out.println("ospfcheck実行-4--3-4");
                        ospfWarningStatement.add(oss.getName() + "で指定したネットワークアドレスが設定されていません");
                    }
                    try {
                        changeColor(oss, orangered);
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

//        System.out.println("ospfcheck実行-5");
        //エリアがエリア０につながっているかどうかの確認
        HashSet<Integer> areas = new HashSet<>();
        for (OspfSetting os : ospfSettings) {//エリアの集約
            for (OspfInterfaceSetting oss : os.getOspfInterfaceSettings()) {
                areas.add(oss.getAreaId());
            }
        }
//        System.out.println("ospfcheck実行-5-1");
        //エリア毎に検証
        for (Integer area : areas) {
            boolean area0C = false;//エリア0につながっているかの検証
//            if(area == 0) break;//エリア0はやらなくていい
//
//            //直接つながっている場合
//            Config startConfig = new Config();
//            ArrayList<Config> configs = new ArrayList<>();
//            for(OspfSetting os : ospfSettings){
//                for(OspfInterfaceSetting oss : os.getOspfInterfaceSettings()){
//                    if(oss.getAreaId()==area) {
//                        startConfig = os.getConfig();
//                        configs.add(os.getConfig());
//                    }
//                }
//            }
//            //startConfigにareaのコンフィグが張っている状態。
//            ArrayList<Config> visitedConfig = new ArrayList<>();
//            if(area0Checkdfs(startConfig,visitedConfig,area)){
//                break;
//            }
//
//            //直接つながってない場合、VirtualLinkをたどってたどり着けるかどうか
//            for(Config ccf : configs){//configsはarea代わり当てられた物全部
//                if(ccf.getOspfSetting().getOspfInterfaceSettings().size()>1 && ccf.getOspfSetting().getOspfVirtualLink().size()!=0){//ABRの場合かつVirtualLinkがある
//                    virtualarea0Checkdfs(ccf,visitedConfig,area,ospfSettings);
//                }
//            }
//                        System.out.println("ospfcheck実行-5---");
//            System.out.println(area+"実行");
            area0C = area0Check(area, ospfSettings, ospfWarningStatement);
//            System.out.println("ospfcheck実行-5-6");
            if (!area0C) {
                if (!ospfWarningStatement.contains("エリア" + area + "の機器がエリア0と接続されていません.")) {
                    ospfWarningStatement.add("エリア" + area + "の機器がエリア0と接続されていません.");
                }
            }
        }
//        System.out.println("ospfcheck実行-5-7");

        boolean erea0check = false;
        for (OspfSetting os : ospfSettings) {
            oposingCheck(os.getConfig(), ospfErrorStatement, ospfWarningStatement);
            //エリア0の未設定
            for (OspfInterfaceSetting oi : os.getOspfInterfaceSettings()) {
                if (oi.getAreaId() == 0) {
                    erea0check = true;
                    break;
                }
            }
        }

        if (!erea0check) {//area0が未設定の時
            ospfWarningStatement.add("エリア0が設定されていません");
        }


//        System.out.println("ospfcheck実行-5-7");
//        System.out.println("ospfCheck終了");
    }

    }

    //AccessListのチェック
    /*・不要なアクセスリスト文のチェック
      （・インターフェースに対してin,out一つずつかどうかのチェック）
    * ・*/
    public static void AccessListCheck(){
        
    }


    public  static Map<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>>  oposingCheck(Config config,ArrayList<String> ospfErrorStatement,ArrayList<String> ospfWarningStatement) {//show ip protocols R1
        Map<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> areaNeighbors = new HashMap<>();//エリア毎のネイバーテーブル
        Set<Integer> areas = new HashSet<>();
        for(OspfInterfaceSetting os : config.getOspfSetting().getOspfInterfaceSettings()){
            areas.add(os.getAreaId());
        }
//        System.out.println("ospfneightor実行－２");
        for(Integer ar : areas) {
            HashMap<EthernetSetting, OspfInterfaceSetting> neighbor = new HashMap<>();
//            System.out.println("ospfneightor実行－3");
            for (EthernetSetting eth : config.getEthernetSetting()) {//インターフェース毎に確かめる
                if (eth.getConectedThing() instanceof EthernetSetting) {
                    Config anotherConfig = ((EthernetSetting) eth.getConectedThing()).getConfig();//対抗機器
                    EthernetSetting anotherEth = (EthernetSetting) eth.getConectedThing();//対抗機器のインターフェース
                    if (config.getOspfSetting() == null) ;
                    if (anotherConfig.getOspfSetting() == null) ;
                    //                if (config.getOspfSetting() == null || anotherConfig.getOspfSetting() == null) break;//OSPFの設定が片方でもないとき
                    HashMap<Integer, OspfInterfaceSetting> os = Check.getOspfSettingInformation(eth);//インターフェースに対応するOSPF（VLANごと）
                    HashMap<Integer, OspfInterfaceSetting> anoteros = Check.getOspfSettingInformation(anotherEth);
//                    System.out.println("ospfneightor実行－4");
//                    System.out.println(os);
                    for (Map.Entry<Integer, OspfInterfaceSetting> entry : os.entrySet()) {//VLAN毎にみる
                        if(entry.getValue()==null) break;
                        if(entry.getValue().getAreaId()!=ar) break;
                        OspfInterfaceSetting ospfSetting = entry.getValue();
//                        System.out.println("ospfneightor実行－4-1");
                        if (anoteros.get(entry.getKey()) == null) break;
//                        System.out.println("ospfneightor実行－4-2");
                        OspfInterfaceSetting anoterOspfSetting = anoteros.get(entry.getKey());
                        //ここで対向機器のOspfSettingかつ同一VLANで取り出せたのでこの後、隣接条件の比較
                        //（エリアID、出力インターフェースのサブネット、Helloインターバル、Stub/NSSAのフラグの一致、（認証情報が同じ））
//                        System.out.println("ospfneightor実行－4-3");
                        if (ospfSetting.getAreaId() != anoterOspfSetting.getAreaId()){
                            if(!ospfWarningStatement.contains(ospfSetting.getName()+"と"+anoterOspfSetting.getName()+"のエリアIDが一致していません")  &&!ospfWarningStatement.contains(anoterOspfSetting.getName()+"と"+ospfSetting.getName()+"のエリアIDが一致していません")) {
                                ospfWarningStatement.add(ospfSetting.getName()+"と"+anoterOspfSetting.getName()+"のエリアIDが一致していません");
                            }
                            try {
                                changeColor(ospfSetting, orangered);
                                changeColor(anoterOspfSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                            break;//エリアIDの一致

                        }
                        if (!OutputInformation.checkSameNetworkW(ospfSetting.getIpAddress(), anoterOspfSetting.getIpAddress(), ospfSetting.getWildcardMask(), anoterOspfSetting.getWildcardMask())){
                            if(!ospfWarningStatement.contains(ospfSetting.getName()+"と"+anoterOspfSetting.getName()+"のネットワークアドレスが一致していません")  &&!ospfWarningStatement.contains(anoterOspfSetting.getName()+"と"+ospfSetting.getName()+"のネットワークアドレスが一致していません")) {
                                ospfWarningStatement.add(ospfSetting.getName()+"と"+anoterOspfSetting.getName()+"のネットワークアドレスが一致していません");
                            }
                            try {
                                changeColor(ospfSetting, orangered);
                                changeColor(anoterOspfSetting, orangered);
                            } catch (InvalidEditingException e) {
                                throw new RuntimeException(e);
                            }
                            break;//出力インターフェースのサブネット
                        }
//                        System.out.println("ospfneightor実行－4-6");
                        if (ospfSetting.getHelloInterval() != anoterOspfSetting.getHelloInterval()) {

                            break;//Helloインターバル


                        }
                        if (ospfSetting.getDeadInterval() != anoterOspfSetting.getDeadInterval()) break;//deadインターバル
                        if (!ospfSetting.getStub().equals(anoterOspfSetting.getStub())) break;

                        //条件を満たした場合
                        neighbor.put(eth, anoterOspfSetting);
                    }
                }
            }
            areaNeighbors.put(ar,neighbor);
        }
        return areaNeighbors;
    }
    public  static boolean area0Check(int area, ArrayList<OspfSetting> ospfSettings , ArrayList<String> warning){
        if(area == 0) return true;//エリア0はやらなくていい
//        System.out.println("1");
//        System.out.println("area"+area);
        //直接つながっている場合
        Config startConfig = new Config();
        ArrayList<Config> configs = new ArrayList<>();
        for(OspfSetting os : ospfSettings){
            for(OspfInterfaceSetting oss : os.getOspfInterfaceSettings()){
                if(oss.getAreaId()==area) {
                    startConfig = os.getConfig();
                    configs.add(os.getConfig());
                }
            }
        }
//        System.out.println("2");
        //startConfigにareaのコンフィグが張っている状態。
        ArrayList<Config> visitedConfig = new ArrayList<>();
        if(area0Checkdfs(startConfig,visitedConfig,area)){
//            System.out.println("2-1");
            return true;
        }

//        System.out.println("3");
        //直接つながってない場合、VirtualLinkをたどってたどり着けるかどうか
        for(Config ccf : configs){//configsはarea代わり当てられた物全部
//            System.out.println("3-1");
//            System.out.println(ccf.getName());
//            System.out.println(ccf.getOspfSetting().getOspfInterfaceSettings());
//            System.out.println(ccf.getOspfSetting().getOspfVirtualLinks());

//            System.out.println(ccf.getName());
            if(ccf.getOspfSetting().getOspfInterfaceSettings().size()>1 && ccf.getOspfSetting().getOspfVirtualLinks().size()!=0){//ABRの場合かつVirtualLinkがある
//                System.out.println("3-2");
                ArrayList<Config> visitdConfig = new ArrayList<>();
//                System.out.println("3-2");
                if(virtualarea0Checkdfs(ccf,visitdConfig,area,ospfSettings,warning)){
                    return true;
                }

//                System.out.println("3-4");
            }
        }
//        System.out.println("4");
        return false;

    }

    //VirtualLinkの不足を検出するメソッド 対向する機器との不一致
    public static void virtualLinkketujo(Config config,ArrayList<String> warningStatement,ArrayList<ClassElement> instances){
        if(config.getOspfSetting().getOspfVirtualLinks()!=null){
            for(OspfVirtualLink ov:config.getOspfSetting().getOspfVirtualLinks()){
                int areaId=ov.getAreaId();
                String routerId=ov.getRouterId();
                Config conf = null;
                boolean check = false;
                OspfVirtualLink ovl = new OspfVirtualLink();
                boolean ospfIntCheck = true;//該当するareaIDを持つOspfInterfaceSettingが無いとき
                //対向の物を調べる
                for(Config config1: config.getLinkedConfigs()){
//                    System.out.println(config.getName()+":"+config1.getName()+";"+ov.getName());
//                    System.out.println("1");
//                    System.out.println(config1.getOspfSetting().getName());
//                    System.out.println("2");
//                    System.out.println(config1.getOspfSetting().getRouterId());
                    if(config1.getOspfSetting().getRouterId().equals(routerId)){
                        //対校する物があったとき
                        conf=config1;
//                        System.out.println("実行1");
                        ospfIntCheck = false;//該当するareaIDを持つOspfInterfaceSettingが無いとき
                        for (OspfInterfaceSetting oii:config1.getOspfSetting().getOspfInterfaceSettings()){
                            if(oii.getAreaId()==areaId){
//                                System.out.println("実行2");
                                ospfIntCheck = true;
                                for(OspfVirtualLink ov1: config1.getOspfSetting().getOspfVirtualLinks()){
                                    if(ov1.getAreaId()==areaId && ov1.getRouterId().equals(config.getOspfSetting().getRouterId())) {
//                                        System.out.println("実3");
                                        check = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if(!ospfIntCheck){
                            warningStatement.add(conf.getName()+"において"+ov.getName()+"のVirturalLinkで指定しているareaIdを持つOspfInterfaceSettingが設定されていません");
                            try {
                                changeColor(conf.getOspfSetting(), orangered);
                                changeColor(ov, orangered);

                            } catch (InvalidEditingException e) {

                                throw new RuntimeException(e);

                            }

                        }
                    }
//                    System.out.println("if文外");
                }

//                System.out.println("for文外" );

                if((!check ) && (ospfIntCheck)){//対向するものがなかったとき
//                    System.out.println("forノアと");
//                    System.out.println(conf);
//                    System.out.println(Objects.isNull(conf));
                    if(!Objects.isNull(conf)){
                        warningStatement.add(conf.getName()+"においてVirtualLinkが不足している可能性があります");

                    }
//                    System.out.println("ここ");
                    if(!Objects.isNull(conf)) {
                        try {
//                            System.out.println("ここ?2");


                            changeColor(conf, orangered);

//                            System.out.println("ここ3");
                        } catch (InvalidEditingException e) {
//                            System.out.println("ここ4");
                            throw new RuntimeException(e);

                        }
                    }
                }


                //OspfVirtualLinkのRouterIdをもつコンフィグが存在しないとき
                boolean ok = false;
                for(ClassElement classElement : instances) {
                    if(classElement instanceof OspfSetting){
                        if(((OspfSetting) classElement).getRouterId().equals(routerId)){
                            ok=true;
                            break;
                        }
                    }
                }
                if(!ok){
                    warningStatement.add(ov.getName()+"に設定されているRouterIDが存在しません");
                    try {
                        changeColor(ov, orangered);
                    } catch (InvalidEditingException e) {
                        throw new RuntimeException(e);
                    }
                }

            }





        }



    }


    public static boolean area0Checkdfs(Config config ,ArrayList<Config> visited,int area) {
        if (visited.contains(config)) return false;

//        System.out.println(config.getName());
        visited.add(config);

        boolean chs = false;
        for(OspfInterfaceSetting s :config.getOspfSetting().getOspfInterfaceSettings()){
            if(s.getAreaId()==area){
                chs=true;
                break;
            }
        }
        if(!chs){
            return false;
        }
        for(OspfInterfaceSetting os : config.getOspfSetting().getOspfInterfaceSettings()){
            if(os.getAreaId() == 0){
                return true;
            }
        }
        boolean c = false;
        for(OspfInterfaceSetting os : config.getOspfSetting().getOspfInterfaceSettings()){

            if(os.getAreaId() == area){
                c=true;
            }
        }
        if(!c) return false;
        for(Config cf : config.getLinkedConfigs()){
            if(area0Checkdfs(cf,visited,area)){
                return true;
            }
        }

        return false;
    }

    public static boolean virtualarea0Checkdfs(Config config ,ArrayList<Config> visited,int area,ArrayList<OspfSetting> os,ArrayList<String> warning) {
        if (visited.contains(config)) return false;

        visited.add(config);


//        System.out.println("-"+config.getName());
        for(OspfInterfaceSetting os1 : config.getOspfSetting().getOspfInterfaceSettings()){
            if(os1.getAreaId() == 0){
                return true;
            }
        }
        for(OspfSetting oss : os){
            boolean ch = false;
            if(config.getOspfSetting().getOspfVirtualLinks()!=null) {
                for (OspfVirtualLink ov : config.getOspfSetting().getOspfVirtualLinks()) {
                    if (oss.getRouterId().equals(ov.getRouterId())) {
                        if(oss.getConfig()!=config) {
                            ch=true;
                            if (virtualarea0Checkdfs(oss.getConfig(), visited, area, os,warning)) {
                                return true;
                            }
                        }

//                        else {
//                            if (!warning.contains(oss.getConfig() + "のVirtualLinkが足りない可能性があります")){
//                                warning.add(oss.getConfig().getName() + "のVirtualLinkが足りない可能性があります");
//                        }

                        }
                    }
                }
//            if(!ch) {
//                if (!warning.contains(oss.getConfig() + "のVirtualLinkが足りない可能性があります")) {
//                    warning.add(oss.getConfig().getName() + "のVirtualLinkが足りない可能性があります");
//                }
//            }
        }


        return false;
    }

    //設計者の意図と矛盾がないかの確認メソッド
    //該当するエリアが存在するかどうか
    //エリアに属するルーターおよびインターフェイスは一致するか
    public  static void ospfIntentionCheck(ArrayList<ClassElement> instances, OspfData ospfjson, TextArea textArea, ArrayList<String> ospfErrorStatement, ArrayList<String> ospfWarningStatement){



    //        for (OspfData.OspfArea area : ospfjson.getOspfAreas()) {
//            System.out.println("Area ID: " + area.getAreaId());
//            for (OspfData.Device device : area.getDevices()) {
//                System.out.println("  Device Hostname: " + device.getHostname());
//                for (OspfData.Interface iface : device.getInterfaces()) {
//                    System.out.println("    Interface Name: " + iface.getInterfaceName());
//                }
//            }
//        }
    }
    //引数に指定したインターフェイスに対応するOspfInterfaceSettingクラスを返す。
    //返り値はHashMapになっており、＜VLAN番号、OspfInterfaceSetting>となりVLANが設定されていない場合は、VLAN番号は0とする
    public static HashMap<Integer , OspfInterfaceSetting> getOspfSettingInformation(EthernetSetting ethernetSetting) {
//        System.out.println("getOSpfSettinginformation-1");
        HashMap<Integer,OspfInterfaceSetting> returnOspfInterfaceSettings = new HashMap<>();
//        System.out.println("getOSpfSettinginformation-2");
        if (!(ethernetSetting.getConfig().getOspfSetting() == null)) {//OspfSettingが存在するとき
//            System.out.println("getOSpfSettinginformation-4");
            if (!ethernetSetting.getIpAddress().isEmpty()) {//インターフェースにIPアドレスが存在するとき
                String ipaddress = ethernetSetting.getIpAddress();
//                System.out.println("getOSpfSettinginformation-212");
                returnOspfInterfaceSettings.put(0,getSameNetworkOspfInterfaceSetting(ipaddress,ethernetSetting.getConfig().getOspfSetting()));
//                System.out.println("getOSpfSettinginformation-3212");
            }
//            System.out.println("getOSpfSettinginformation-3");
            ArrayList<Integer> ethVlans = getEhternetSettingVlans(ethernetSetting);
//            System.out.println("getOSpfSettinginformation-4");
            for(Integer vlanNumber : ethVlans){
                for(VlanSetting vlanSetting:ethernetSetting.getConfig().getVlanSetting()){
                    if(vlanSetting.getVlanNum() == vlanNumber) {
                        String ipAddress = vlanSetting.getIpAddress();
                        returnOspfInterfaceSettings.put(vlanNumber, getSameNetworkOspfInterfaceSetting(ipAddress, ethernetSetting.getConfig().getOspfSetting()));

                    }
                }
            }

//            System.out.println("getOSpfSettinginformation-5");

        }else{}
//            System.out.println("else");
//        System.out.println("getOSpfSettinginformation-6");
            return returnOspfInterfaceSettings;
    }

    //インターフェースに設定されているVLANを出力するためのメソッド
    //引数に指定したEhternetSettingインスタンスに設定されているVLANを返す。
    public static ArrayList<Integer> getEhternetSettingVlans(EthernetSetting eth) {
        ArrayList<Integer> eth_vlans = new ArrayList<>();
        if (eth.getMode().equals("access")) {
            eth_vlans.add(eth.getAccessVlan());
        } else if (eth.getMode().equals("trunk")){
            eth_vlans.addAll(eth.getAllowedVlans());
        }
        return eth_vlans;
    }
    //インターフェースに設定されているIPアドレスとサブネットマスクを出力するメソッド
    public static HashMap<String,String> getEthernetSettingAddress(EthernetSetting eth){
        ArrayList<Integer> vlans = getEhternetSettingVlans(eth);

        HashMap<String,String> ethernetSettingAddress = new HashMap<>();
        if(vlans.size()==0){

            ethernetSettingAddress.put(eth.getIpAddress(),eth.getSubnetMask());
        }
        for(Integer vlan : vlans){


                Config config = eth.getConfig();
                for(VlanSetting vs : config.getVlanSetting()){
                    if(vs.getVlanNum()==vlan){
                        ethernetSettingAddress.put(vs.getIpAddress(),vs.getSubnetMask());
                    }
                }

        }


        return ethernetSettingAddress;
    }
    //インターフェースに設定されているネットワークアドレスを出力するメソッド
    public static ArrayList<String> calculateNetworkAddress(EthernetSetting eth){
        HashMap<String,String> ethernetSettingAddress = getEthernetSettingAddress(eth);
        ArrayList<String> subnetMasks = new ArrayList<>();
        for(Map.Entry<String,String> entry : ethernetSettingAddress.entrySet()){
            String ipAddress = entry.getKey();
            String subnetMask = entry.getValue();

            // IPアドレスとサブネットマスクをバイト配列に変換
            byte[] ipBytes = new byte[0];
            try {
                ipBytes = InetAddress.getByName(ipAddress).getAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            byte[] maskBytes = new byte[0];
            try {
                maskBytes = InetAddress.getByName(subnetMask).getAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            byte[] networkBytes = new byte[ipBytes.length];

            // IPアドレスとサブネットマスクのAND演算を実行
            for (int i = 0; i < ipBytes.length; i++) {
                networkBytes[i] = (byte) (ipBytes[i] & maskBytes[i]);
            }

            // バイト配列をネットワークアドレスの文字列形式に変換
            InetAddress networkAddress = null;
            try {
                networkAddress = InetAddress.getByAddress(networkBytes);
                subnetMasks.add(networkAddress.getHostAddress());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        return subnetMasks;
    }
    //ospfのネットワークアドレスが一致しているものを返すメソッド　getOspfSettingInformation で使用される
    //checkIpAddressと一致するネットワークアドレスを持つOspfInterfaceSettingを返す
    //対応する物がない場合はnullがかえる
    public static OspfInterfaceSetting getSameNetworkOspfInterfaceSetting(String checkIpAddress, OspfSetting ospfSetting) {
        OspfInterfaceSetting currentOspfInterfaceSetting = null;
        byte[] currentwildcaldmask = new byte[0];
        for(OspfInterfaceSetting ospfInterfaceSetting : ospfSetting.getOspfInterfaceSettings()) {//ospfSettingに関連付けられているOspfInterfacSetting
            String wildcardmask = ospfInterfaceSetting.getWildcardMask(); //OspfInterfaceSettingのワイルドカートマスク
            String ipAddress = ospfInterfaceSetting.getIpAddress();
            try {
                boolean check = true;//一致しているかどうかの判別変数
                InetAddress networkInetAddress = InetAddress.getByName(ipAddress);
                InetAddress maskInetAddress = InetAddress.getByName(wildcardmask);
                InetAddress testInetAddress = InetAddress.getByName(checkIpAddress);

                byte[] networkBytes = networkInetAddress.getAddress();
                byte[] maskBytes = maskInetAddress.getAddress();
                byte[] testBytes = testInetAddress.getAddress();

                for (int i = 0; i < networkBytes.length; i++) {

                    if ((networkBytes[i] & ~maskBytes[i]) != (testBytes[i] & ~maskBytes[i])) {
                        check = false;
                        break ;//ネットワークアドレスが異なった場合
                    }
                }
                if(check) {
                    if (currentOspfInterfaceSetting == null) {
                        currentOspfInterfaceSetting = ospfInterfaceSetting;
                        currentwildcaldmask = maskBytes;
                    } else {
                        for (int i = 0; i < networkBytes.length; i++) {
                            int maskSegment = maskBytes[i] & 0xFF;
                            int testSegment = currentwildcaldmask[i] & 0xFF;
                            if ((maskSegment != testSegment) && (maskSegment < testSegment)) {
                                currentOspfInterfaceSetting = ospfInterfaceSetting;
                                currentwildcaldmask = maskBytes;
                                break;
                            }else if ((maskSegment != testSegment) && (maskSegment < testSegment)){
                                break;
                            }
                        }


                    }
                }
                // return true;
            }  catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        return currentOspfInterfaceSetting;
            }
///JSON形式のためのメソッド（検証結果を配列に変換する）
    public static Map<String, String> mapOf(String key, String value) {//MAPを一行で作るための処理
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


}
