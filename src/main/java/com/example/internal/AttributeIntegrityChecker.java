package com.example.internal;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.example.classes.*;
import com.example.classes.Stack;
import com.example.element.ClassElement;


import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeIntegrityChecker {//構文エラーをチェックするクラス
     /*実装しているもの
     * 属性の正規表現と型チェック
     * チェックの手順
     * 半角スペース intとbooleanはChangeClassinformation,他はAttributeIntegrityChecker
     * 型(int,boolean)　changeclassinformation
     * 入力すべき文字が決まっているもの changeclassinformation
     * 正規表現 このクラス*/

    /*まだやっていないこと
    * 色の変更
    **/

//    EthernetSettingに属性mtuを追加
//            OspfInterfaceSettingに
//    helloInterval
//            deadInterval
//    ospfNetworkMode
//            stubを追加
//            個々の正規表現は考え直す必要あり


    /*memo
    * エラー文の出力箇所　formatErrorStatement
    * 警告文の出力箇所　warningStatement
    * 属性の初期値　「-1」
    * 「true」「false」「allowedVlan」はChangeClassInformationでチェックし，エラー文をClassElementのbooleanErrorStatementにいれてそれを
    * ここで取り出す．　型変換の際にチェックする必要があるため*/
    static String red = "#ff0000";//エラーの色　赤
    static String orangered = "#ff7f50";//警告の色　オレンジ

    ArrayList<String> formatErrorStatements;//構文・字句解析の検証結果のエラー文のリスト
    ArrayList<ClassElement> instances;//モデルにある全インスタンスを格納するリスト
    TextArea textArea;//出力する場所
    ArrayList<ErrorInfo> errorInfos ;//JSON形式に変換する際にエラー情報を格納するリスト

    //正規表現
    static Pattern twoDigits = Pattern.compile("^([0-9]{1,2})$");//二桁の数字
    static Pattern fourDigits = Pattern.compile("^([0-9]{1,4})$");//四桁の数字
    static Pattern fiveDigits = Pattern.compile("^([0-9]{1,5})$");//5桁の数字
    static Pattern character = Pattern.compile("\\S+");//任意の文字　名前など
    static Pattern space = Pattern.compile(" "); //半角スペース
    static Pattern ipAddress = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");//IPアドレス
    static Pattern linkpattern = Pattern.compile("^\\S+ \\S+ and \\S+ \\S+");//Linkクラスの形式
    static Pattern alloedVlanpattern = Pattern.compile("^\\d+(-\\d+)?(,\\d+(-\\d+)?)*$");
//https://www.cisco.com/c/ja_jp/td/docs/wireless/controller/ewc/17-7/config-guide/ewc_cg_17_7/ipv4_acls.html#
    //を参考に
    public AttributeIntegrityChecker(ArrayList<ClassElement> instances, TextArea textArea,ArrayList<ErrorInfo> errorInfos) {//コンストラクタ
        this.instances = instances;
        this.textArea = textArea;
        formatErrorStatements = new ArrayList<>();
        this.errorInfos = errorInfos;

    }

    public void AllAttributeIntegrityCheck() {//属性の構文チェック　クラスごと
        for (ClassElement instance : instances) {
            if (instance instanceof EthernetSetting) {
                try {
                    ethernetSettingCheck((EthernetSetting) instance, formatErrorStatements,errorInfos);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }
            if (instance instanceof VlanSetting) {
                vlanSettingCheck((VlanSetting) instance,  formatErrorStatements);
            }
            if (instance instanceof AccessList) {
                accessListCheck((AccessList) instance,  formatErrorStatements);
            }
            if (instance instanceof Clients) {
                clientCheck((Clients) instance,  formatErrorStatements);
            }
            if (instance instanceof Config) {
                configCheck((Config) instance,  formatErrorStatements);
            }
            if (instance instanceof Hostname) {
                hostNameCheck((Hostname) instance,  formatErrorStatements);
            }
            if (instance instanceof Link) {
                linkCheck((Link) instance,  formatErrorStatements);
            }
            if (instance instanceof IpRoute) {
                ipRouteCheck((IpRoute) instance,  formatErrorStatements);
            }
            if (instance instanceof OspfInterfaceSetting) {
                ospfInterfaceSettingCheck((OspfInterfaceSetting) instance, formatErrorStatements);
            }
            if (instance instanceof OspfSetting) {
                ospfSettingCheck((OspfSetting) instance,  formatErrorStatements);
            }
            if (instance instanceof OspfVirtualLink) {
                ospfvirtualLinkCheck((OspfVirtualLink) instance,  formatErrorStatements);
            }
            if (instance instanceof Vlan) {
                vlanheck((Vlan) instance,  formatErrorStatements);
            }
            if (instance instanceof StpSetting) {
                stpSettingCheck((StpSetting) instance, formatErrorStatements);
            }
            if (instance instanceof Stack) {
                stackCheck((Stack) instance,  formatErrorStatements);
            }
        }
        for (String formatErrorstatement : formatErrorStatements) {
            textArea.append(formatErrorstatement + "\n");
        }
    }

    private static Map<String, String> mapOf(String key, String value) {//MAPを一行で作るための処理、JSONの出力用
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
    public static void ethernetSettingCheck(EthernetSetting ethernetSetting,ArrayList<String> formatErrorStatements,ArrayList<ErrorInfo> errorInfos) throws InvalidEditingException {

        if (ethernetSetting.getStack() != -1) {//int値は初期値-1にしてある。入力がなかったとき用
            Matcher stackM = twoDigits.matcher(String.valueOf(ethernetSetting.getStack()));
            if (!stackM.matches()) {//半角数値二桁のみ
                String message = ethernetSetting.getName() + "のstackの値は無効です。1桁または2桁の整数を入力してください";
                formatErrorStatements.add(message);//エラー文
                errorInfos.add(new ErrorInfo( true, "構文エラー",mapOf(ethernetSetting.getId(), "stack") ));//JSONを作るための処理
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (ethernetSetting.getSlot() != -1) {
            Matcher slotM = twoDigits.matcher(String.valueOf(ethernetSetting.getSlot()));

            if (!slotM.matches()) {
                System.out.println("koko");
                String message=ethernetSetting.getName() + "のslotの値は無効です。1桁または2桁の整数を入力してください";
                formatErrorStatements.add(message);//エラー文
                errorInfos.add(new ErrorInfo(true, "構文エラー",mapOf(ethernetSetting.getId(), "slot") ));//JSONを作るための処理

                formatErrorStatements.add(ethernetSetting.getName() + "のslotの値は無効です。1桁または2桁の整数を入力してください");

                Check.changeColor(ethernetSetting,red);
            }
        }
        if (ethernetSetting.getPort() != -1) {
            Matcher portM = twoDigits.matcher(String.valueOf(ethernetSetting.getPort()));
            if (!portM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のportの値は無効です。1桁または2桁の整数を入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (!ethernetSetting.getIpAddress().isEmpty()) {
            Matcher ipAddressM = ipAddress.matcher(ethernetSetting.getIpAddress());
            if (!ipAddressM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
                Check.changeColor(ethernetSetting,red);
            }

        }
        if (!ethernetSetting.getSubnetMask().isEmpty()) {
            Matcher subnetMaskM = ipAddress.matcher(ethernetSetting.getSubnetMask());
            if (!subnetMaskM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (ethernetSetting.getAccessVlan() != -1) {
            Matcher accessVlanM = fourDigits.matcher(String.valueOf(ethernetSetting.getAccessVlan()));
            if (!accessVlanM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessVlanの値は無効です。4桁以内の整数値を入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (ethernetSetting.getNativeVlan() != -1) {
            Matcher nativeVlanM = fourDigits.matcher(String.valueOf(ethernetSetting.getNativeVlan()));
            if (!nativeVlanM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のnativeVlanの値は無効です。4桁以内の整数値を入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }

        if (!ethernetSetting.getMode().isEmpty()) {
            if (!(ethernetSetting.getMode().equals("access") || ethernetSetting.getMode().equals("trunk") || ethernetSetting.getMode().isEmpty())) {
                formatErrorStatements.add(ethernetSetting.getName() + "のmodeの値は無効です。'access' または 'trunk' のいずれかを入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (ethernetSetting.getAccessListNumber() != -1) {
            Matcher accessListNumberM = fourDigits.matcher(String.valueOf(ethernetSetting.getAccessListNumber()));
            if (!accessListNumberM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (!ethernetSetting.getAccessListName().isEmpty()) {
            Matcher accessListNameM = character.matcher(ethernetSetting.getAccessListName());
            if (!accessListNameM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessListNameの値は無効です。正しい形式で入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (!ethernetSetting.getAccessListInOrOut().isEmpty()) {
            if (!(ethernetSetting.getAccessListInOrOut().equals("in") || ethernetSetting.getAccessListInOrOut().equals("out") || ethernetSetting.getAccessListInOrOut().isEmpty())) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessListInOrOutの値は無効です。'in' または 'out' のいずれかを入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (!ethernetSetting.getSpeed().isEmpty()) {
            if (!(ethernetSetting.getSpeed().equals("auto") || ethernetSetting.getSpeed().equals("10") || ethernetSetting.getSpeed().equals("100") || ethernetSetting.getSpeed().equals("1000") || ethernetSetting.getSpeed().isEmpty())) {
                formatErrorStatements.add(ethernetSetting.getName() + "のspeedの値は無効です。'auto','10','100','1000'のいずれかを入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (!ethernetSetting.getDuplex().isEmpty()) {
            if (!(ethernetSetting.getDuplex().equals("auto") || ethernetSetting.getDuplex().equals("full") || ethernetSetting.getDuplex().equals("half") || ethernetSetting.getDuplex().isEmpty())) {
                formatErrorStatements.add(ethernetSetting.getName() + "のduplexの値は無効です。'full' または 'half' のいずれかを入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }
        if (ethernetSetting.getMtu() != -1) {
            Matcher accessListNumberM = fourDigits.matcher(String.valueOf(ethernetSetting.getMtu()));
            if (!accessListNumberM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のmtuの値は無効です。4桁以内の整数値を入力してください");
                Check.changeColor(ethernetSetting,red);
            }
        }

        //「true」「false」「allowedVlan」はChangeClassInformationでチェックし，エラー文をClassElementのbooleanErrorStatementにいれてそれをここで取り出す
        if (ethernetSetting.getAttributeErrorStatement().size() != 0) {
            formatErrorStatements.addAll(ethernetSetting.getAttributeErrorStatement());
        }
    }


    public static void vlanSettingCheck(VlanSetting vlanSetting, ArrayList<String> formatErrorStatements) {
        if (vlanSetting.getVlanNum() != -1) {
            Matcher vlanNumM = fourDigits.matcher(String.valueOf(vlanSetting.getVlanNum()));
            if (!vlanNumM.matches()) {//半角数値二桁のみ
                formatErrorStatements.add(vlanSetting.getName() + "のvlanNumの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (vlanSetting.getAccessListNumber() != -1) {
            Matcher accessListNumberM = fourDigits.matcher(String.valueOf(vlanSetting.getAccessListNumber()));
            if (!accessListNumberM.matches()) {
                formatErrorStatements.add(vlanSetting.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (vlanSetting.getIpTcpAdjustMss() != -1) {
            Matcher ipTcpAdjusMss = fourDigits.matcher(String.valueOf(vlanSetting.getIpTcpAdjustMss()));
            if (!ipTcpAdjusMss.matches()) {
                formatErrorStatements.add(vlanSetting.getName() + "のipTcpAdjusMssの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (!vlanSetting.getIpAddress().isEmpty()) {
            Matcher ipAddressM = ipAddress.matcher(vlanSetting.getIpAddress());
            if (!ipAddressM.matches()) {
                formatErrorStatements.add(vlanSetting.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!vlanSetting.getSubnetMask().isEmpty()) {
            Matcher subnetMaskM = ipAddress.matcher(vlanSetting.getSubnetMask());
            if (!subnetMaskM.matches()) {
                formatErrorStatements.add(vlanSetting.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
            }
        }
        if (!vlanSetting.getAccessListName().isEmpty()) {
            Matcher accessListNameM = character.matcher(vlanSetting.getAccessListName());
            if (!accessListNameM.matches()) {
                formatErrorStatements.add(vlanSetting.getName() + "のaccessListNameの値は無効です。正しい形式で入力してください");
            }
        }
        if (!vlanSetting.getAccessListInOrOut().isEmpty()) {
            if (!(vlanSetting.getAccessListInOrOut().equals("in") || vlanSetting.getAccessListInOrOut().equals("out") || vlanSetting.getAccessListInOrOut().isEmpty())) {
                formatErrorStatements.add(vlanSetting.getName() + "のaccessListInOrOutの値は無効です。'in' または 'out' のいずれかを入力してください");
            }
        }
        //「true」「false」はChangeClassInformationでチェックし，エラー文をClassElementのbooleanErrorStatementにいれてそれをここで取り出す
        if (vlanSetting.getAttributeErrorStatement().size() != 0) {
            formatErrorStatements.addAll(vlanSetting.getAttributeErrorStatement());
        }

    }
   //ここを修正する
    public static void accessListCheck(AccessList accessList,  ArrayList<String> formatErrorStatements) {

        if (accessList.getAccessListNumber() != -1) {
            Matcher accessListNumberM = fourDigits.matcher(String.valueOf(accessList.getAccessListNumber()));
            if (!accessListNumberM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (!accessList.getPermitOrDeny().isEmpty()) {
            if (!(accessList.getPermitOrDeny().equals("permit") || accessList.getPermitOrDeny().equals("deny") || accessList.getPermitOrDeny().isEmpty())) {
                formatErrorStatements.add(accessList.getName() + "のpermitOrDenyの値は無効です。'permit' または 'deny' のいずれかを入力してください");
            }
        }
        //ここ
        if (!accessList.getProtocol().isEmpty()) {
            Matcher protocolM = character.matcher(String.valueOf(accessList.getProtocol()));
            if (!protocolM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のprotcolの値は無効です。正しい形式で入力してください");
            }
        }
        //ここ
        if (!accessList.getSorceIpAddress().isEmpty()) {
            Matcher sourseipAddressM = ipAddress.matcher(accessList.getSorceIpAddress());
            if (!sourseipAddressM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のsourceIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        //ここ
        if (!accessList.getSourceWildcardMask().isEmpty()) {
            Matcher sourceWildcardMaskM = ipAddress.matcher(accessList.getSourceWildcardMask());
            if (!sourceWildcardMaskM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のsourceWildcardMaskの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }

        //ここ
        if (!accessList.getDestIpAddress().isEmpty()) {
            Matcher destIpAddressM = ipAddress.matcher(accessList.getDestIpAddress());
            if (!destIpAddressM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のdestIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        //ここ
        if (!accessList.getDestWildcardMask().isEmpty()) {
            Matcher destWildcardMaskM = ipAddress.matcher(accessList.getDestWildcardMask());
            if (!destWildcardMaskM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のdestWildcardMaskの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
//sorceportとdenyport,protocolは選択肢が多すぎるため保留

    }

    public static void clientCheck(Clients clients,  ArrayList<String> formatErrorStatements) {
        if (!clients.getName2().isEmpty()) {
            Matcher nameM = character.matcher(String.valueOf(clients.getName2()));
            if (!nameM.matches()) {
                formatErrorStatements.add(clients.getName() + "のprotcolの値は無効です。正しい形式で入力してください");
            }
        }
        if (!clients.getIpAddress().isEmpty()) {
            Matcher IpAddressM = ipAddress.matcher(clients.getIpAddress());
            if (!IpAddressM.matches()) {
                formatErrorStatements.add(clients.getName() + "のIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
//        if (!clients.getSubnetMask().isEmpty()) {
//            Matcher subnetMaskM = ipAddress.matcher(clients.getSubnetMask());
//            if (!subnetMaskM.matches()) {
//                formatErrorStatements.add(clients.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
//            }
//        }
//        if (!clients.getSubnetMask().isEmpty()) {
//            Matcher defaultGateWayM = ipAddress.matcher(clients.getSubnetMask());
//            if (!defaultGateWayM.matches()) {
//                formatErrorStatements.add(clients.getName() + "のdefaultGateWayの値は無効です。有効なデフォルトゲートウェイ形式で入力してください");
//            }
//        }
    }

    public static void configCheck(Config config, ArrayList<String> formatErrorStatements) {
        if (!config.getDeviceModel().isEmpty()) {
            Matcher devicemodelM = character.matcher(String.valueOf(config.getDeviceModel()));
            if (!devicemodelM.matches()) {
                formatErrorStatements.add(config.getName() + "のdevicemodelの値は無効です。正しい形式で入力してください");
            }
        }
    }

    public static void hostNameCheck(Hostname hostname, ArrayList<String> formatErrorStatements) {
        if (!hostname.getHostName().isEmpty()) {
            Matcher nameM = character.matcher(String.valueOf(hostname.getHostName()));
            if (!nameM.matches()) {
                formatErrorStatements.add(hostname.getName() + "のnameの値は無効です。正しい形式で入力してください");
            }
        }
    }

    //Linkは自由に記述できてしまうため保留
    public static void linkCheck(Link link,  ArrayList<String> formatErrorStatements) {
        if (!link.getDescription().isEmpty()) {
            Matcher descriptionM = linkpattern.matcher(String.valueOf(link.getDescription()));
            if (!descriptionM.matches()) {
//                formatErrorStatements.add(link.getName() + "のdescriptionの値は無効です。正しい形式で入力してください");
            }
        }
    }

    public static void ipRouteCheck(IpRoute ipRoute, ArrayList<String> formatErrorStatements) {
        if (!ipRoute.getIpAddress().isEmpty()) {
            Matcher networkM = ipAddress.matcher(ipRoute.getIpAddress());
            if (!networkM.matches()) {
                formatErrorStatements.add(ipRoute.getName() + "のnetworkの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!ipRoute.getSubnetMask().isEmpty()) {
            Matcher addressPrefixM = ipAddress.matcher(ipRoute.getSubnetMask());
            if (!addressPrefixM.matches()) {
                formatErrorStatements.add(ipRoute.getName() + "のaddressPrefixの値は無効です。有効なサブネットマスク形式で入力してください");
            }
        }
        if (!ipRoute.getNetHopAddress().isEmpty()) {
            Matcher nextHopAddressM = ipAddress.matcher(ipRoute.getNetHopAddress());
            if (!nextHopAddressM.matches()) {
                formatErrorStatements.add(ipRoute.getName() + "のnextHopAddressの値は無効です。有効なゲートウェイアドレス形式で入力してください");
            }
        }
    }

    public static void ospfInterfaceSettingCheck(OspfInterfaceSetting ospfInterfaceSetting, ArrayList<String> formatErrorStatements) {
        if (!ospfInterfaceSetting.getIpAddress().isEmpty()) {
            Matcher ipAddressM = ipAddress.matcher(ospfInterfaceSetting.getIpAddress());
            if (!ipAddressM.matches()) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!ospfInterfaceSetting.getWildcardMask().isEmpty()) {
            Matcher wildcardMaskM = ipAddress.matcher(ospfInterfaceSetting.getWildcardMask());
            if (!wildcardMaskM.matches()) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のwildcardMaskの値は無効です。有効なワイルドカードマスク形式で入力してください");
            }
        }
        if (ospfInterfaceSetting.getAreaId() != -1) {
            Matcher areaIdM = fourDigits.matcher(String.valueOf(ospfInterfaceSetting.getAreaId()));
            if (!areaIdM.matches()) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のareaIdの値は無効です。4桁までの整数を入力してください");
            }
        }
        if (ospfInterfaceSetting.getHelloInterval() != -1) {
            Matcher helloIntervalM = fourDigits.matcher(String.valueOf(ospfInterfaceSetting.getHelloInterval()));
            if (!helloIntervalM.matches()) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のhelloIntervalの値は無効です。4桁までの整数を入力してください");
            }
        }
        if (ospfInterfaceSetting.getDeadInterval() != -1) {
            Matcher areaIdM = fourDigits.matcher(String.valueOf(ospfInterfaceSetting.getDeadInterval()));
            if (!areaIdM.matches()) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のdeadIntervalの値は無効です。4桁までの整数を入力してください");
            }
        }

        if (!ospfInterfaceSetting.getOspfNetworkMode().isEmpty()) {
            if (!(ospfInterfaceSetting.getOspfNetworkMode().equals("broadcast") || ospfInterfaceSetting.getOspfNetworkMode().equals("point-to-point") ||ospfInterfaceSetting.getOspfNetworkMode().equals("non-broadcast") ||ospfInterfaceSetting.getOspfNetworkMode().equals("point-to-multipoint") ||ospfInterfaceSetting.getOspfNetworkMode().equals("point-to-multipoint nonbroadcast") || ospfInterfaceSetting.getOspfNetworkMode().isEmpty())) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のospfNetworkModeの値は無効です。'broadcast','point-to-point','non-broadcast','point-to-multipoint','point-to-multipoint nonbroadcast' のいずれかを入力してください");
            }
        }

        if (!ospfInterfaceSetting.getStub().isEmpty()) {
            if (!(ospfInterfaceSetting.getStub().equals("stub") || ospfInterfaceSetting.getStub().equals("stub no-summary") ||ospfInterfaceSetting.getStub().equals("nssa") ||ospfInterfaceSetting.getStub().equals("nssa no-smmary")  || ospfInterfaceSetting.getStub().equals("normal")  ||ospfInterfaceSetting.getStub().isEmpty())) {
                formatErrorStatements.add(ospfInterfaceSetting.getName() + "のstubの値は無効です。'stub','stub no-summary','nssa','nssa no-smmary'のいずれかを入力してください");

            }
        }

    }

    public static void ospfSettingCheck(OspfSetting ospfSetting,  ArrayList<String> formatErrorStatements) {
        if (ospfSetting.getProcessId() != -1) {
            Matcher processIdM = fiveDigits.matcher(String.valueOf(ospfSetting.getProcessId()));
            if (!processIdM.matches()) {
                formatErrorStatements.add(ospfSetting.getName() + "のprocessIdの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (!ospfSetting.getRouterId().isEmpty()) {
            Matcher routerIdM = ipAddress.matcher(ospfSetting.getRouterId());
            if (!routerIdM.matches()) {
                formatErrorStatements.add(ospfSetting.getName() + "のrouterIdの値は無効です。有効なルーターID形式で入力してください");
            }
        }
    }

    public static void ospfvirtualLinkCheck(OspfVirtualLink ospfVirtualLink,  ArrayList<String> formatErrorStatements) {
        if (ospfVirtualLink.getAreaId() != -1) {
            Matcher arealdM = fourDigits.matcher(String.valueOf(ospfVirtualLink.getAreaId()));
            if (!arealdM.matches()) {
                formatErrorStatements.add(ospfVirtualLink.getName() + "のareaIdの値は無効です。4桁までの整数を入力してください");
            }
        }
        if (!ospfVirtualLink.getRouterId().isEmpty()) {
            Matcher routerIdM = ipAddress.matcher(ospfVirtualLink.getRouterId());
            if (!routerIdM.matches()) {
                formatErrorStatements.add(ospfVirtualLink.getName() + "のrouterIdの値は無効です。有効なルーターID形式で入力してください");
            }
        }
    }

    public static void vlanheck(Vlan vlan,  ArrayList<String> formatErrorStatements) {
        if (vlan.getNum() != -1) {
            Matcher arealdM = fourDigits.matcher(String.valueOf(vlan.getNum()));
            if (!arealdM.matches()) {
                formatErrorStatements.add(vlan.getName() + "のnumの値は無効です。4桁までの整数を入力してください");
            }
        }
        if (!vlan.getVlanName().isEmpty()) {
            Matcher nameM = character.matcher(vlan.getVlanName());
            if (!nameM.matches()) {
                formatErrorStatements.add(vlan.getName() + "のnameの値は無効です。正しい形式で入力してください");
            }
        }

    }

    public static void stpSettingCheck(StpSetting stpSetting,  ArrayList<String> formatErrorStatements) {
        if (stpSetting.getBridgePriority() != -1) {
            Matcher bredgePriorityM = fiveDigits.matcher(String.valueOf(stpSetting.getBridgePriority()));
            if (!bredgePriorityM.matches()) {
                formatErrorStatements.add(stpSetting.getName() + "bredgePriorityの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (stpSetting.getVlan() != -1) {
            Matcher vlanM = fourDigits.matcher(String.valueOf(stpSetting.getVlan()));
            if (!vlanM.matches()) {
                formatErrorStatements.add(stpSetting.getName() + "vlanの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (!stpSetting.getMode().isEmpty()) {
            if (!(stpSetting.getMode().equals("pvst") || stpSetting.getMode().equals("pvst+") || stpSetting.getMode().equals("rstp") || stpSetting.getMode().equals("mstp") || stpSetting.getMode().isEmpty())) {
                formatErrorStatements.add(stpSetting.getName() + "のmodeの値は無効です。'pvst','pvst+','rstp','mstp' のいずれかを入力してください");
            }
        }
        if (!stpSetting.getMacAddress().isEmpty()) {
            Matcher macAddressM = ipAddress.matcher(stpSetting.getMacAddress());
            if (!macAddressM.matches()) {
                formatErrorStatements.add(stpSetting.getName() + "のmacAddressの値は無効です。有効なMACアドレス形式で入力してください");
            }
        }
    }

    public static void stackCheck(Stack stack,  ArrayList<String> formatErrorStatements) {
        if (stack.getStackMemberNumber() != -1) {
            Matcher stackMemberNumberM = fiveDigits.matcher(String.valueOf(stack.getStackMemberNumber()));
            if (!stackMemberNumberM.matches()) {
                formatErrorStatements.add(stack.getName() + "stackMemberNumberの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (stack.getPreviousStackNumber() != -1) {
            Matcher previousStackNumberM = fiveDigits.matcher(String.valueOf(stack.getPreviousStackNumber()));
            if (!previousStackNumberM.matches()) {
                formatErrorStatements.add(stack.getName() + "previousStackNumberの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (stack.getStackPriority() != -1) {
            Matcher stackPriorityM = fiveDigits.matcher(String.valueOf(stack.getStackPriority()));
            if (!stackPriorityM.matches()) {
                formatErrorStatements.add(stack.getName() + "stackPriorityの値は無効です。5桁までの整数を入力してください");
            }
        }
    }


}
