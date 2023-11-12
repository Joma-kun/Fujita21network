package com.example.internal;

import com.example.classes.*;
import com.example.classes.Stack;
import com.example.element.ClassElement;


import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeIntegrityChecker {
    /*実装している物
     * 属性の正規表現、型（構文エラー）チェック
     * VLANとMODEの関係（抜け漏れ）チェック
     *
     *
     * */






    /*フォーマットチェックの手順
     *astahから値を取り出す(全てString型)→それぞれのクラス、属性に変換する(この時点で型が違う物例えばint型やboolean型などを検査する[changeclassinformationクラスで行う])
     * →AttributeCheckerクラスで正規表現や文字列一致のチェックを行う
     *alllowdVlanのみEthernetSettingクラスでやる
     */


    /*チェックしている項目
     * EthernetSettingクラス
     * modeとaccessVlan,nativeVlanの関係（EthernetSettingChecK）*/
    ArrayList<String> errorStatements;
    ArrayList<String> formatErrorStatements;
    ArrayList<ClassElement> instances;
    TextArea textArea;

    //正規表現
    static Pattern twoDigits = Pattern.compile("^([0-9]{1,2})$");//二桁の数字
    static Pattern fourDigits = Pattern.compile("^([0-9]{1,4})$");//四桁の数字
    static Pattern fiveDigits = Pattern.compile("^([0-9]{1,5})$");//5桁の数字
    static Pattern character = Pattern.compile("\\S+");//任意の文字　名前など
    static Pattern listNumber = Pattern.compile("\\[(\\d+(,\\d+)*)?\\]");//数字のリスト　[12,14]
    static Pattern ipAddress = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");//IPアドレス

    static Pattern linkpattern = Pattern.compile("^\\S+ \\S+ and \\S+ \\S+");
    public AttributeIntegrityChecker(ArrayList<ClassElement> instances, TextArea textArea) {
        this.instances = instances;
        this.textArea = textArea;
        errorStatements = new ArrayList<>();
        formatErrorStatements = new ArrayList<>();

    }

    public void AllAttributeIntegrityCheck() {
        for (ClassElement instance : instances) {
            if (instance instanceof EthernetSetting) {
                ethernetSettingCheck((EthernetSetting) instance, errorStatements, formatErrorStatements);
            }
            if (instance instanceof VlanSetting) {
                vlanSettingCheck((VlanSetting) instance, errorStatements, formatErrorStatements);
            }
            if (instance instanceof AccessList) {
                accessListCheck((AccessList) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof Clients){
                clientCheck((Clients) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof Config) {
                configCheck((Config) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof Hostname){
                hostNameCheck((Hostname) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof Link){
                linkCheck((Link) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof IpRoute){
                ipRouteCheck((IpRoute) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof OspfInterfaceSetting){
                ospfInterfaceSettingCheck((OspfInterfaceSetting) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof OspfSetting){
                ospfSettingCheck((OspfSetting) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof OspfVirtualLink){
                ospfvirtualLinkCheck((OspfVirtualLink) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof Vlan){
                vlanheck((Vlan) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof StpSetting){
                stpSettingCheck((StpSetting) instance, errorStatements, formatErrorStatements);
            }
            if(instance instanceof Stack){
                stackCheck((Stack) instance, errorStatements, formatErrorStatements);
            }
        }
        for (String formatErrorstatement : formatErrorStatements) {
            textArea.append(formatErrorstatement + "\n");
        }

        for (String errorstatement : errorStatements) {
            textArea.append(errorstatement + "\n");
        }
    }

    /*EthernetSettingクラスのチェック*/
    public static void ethernetSettingCheck(EthernetSetting ethernetSetting, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        /*フォーマットのチェック*/

        if (ethernetSetting.getStack() != -1) {//int値は初期値-1にしてある。入力がなかったとき用
            Matcher stackM = twoDigits.matcher(String.valueOf(ethernetSetting.getStack()));
            if (!stackM.matches()) {//半角数値二桁のみ
                formatErrorStatements.add(ethernetSetting.getName() + "のstackの値は無効です。1桁または2桁の整数を入力してください");
            }
        }
        if (ethernetSetting.getSlot() != -1) {
            Matcher slotM = twoDigits.matcher(String.valueOf(ethernetSetting.getSlot()));
            if (!slotM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のslotの値は無効です。1桁または2桁の整数を入力してください");
            }
        }
        if (ethernetSetting.getPort() != -1) {
            Matcher portM = twoDigits.matcher(String.valueOf(ethernetSetting.getPort()));
            if (!portM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のportの値は無効です。1桁または2桁の整数を入力してください");
            }
        }
        if (!ethernetSetting.getIpAddress().isEmpty()) {
            Matcher ipAddressM = ipAddress.matcher(ethernetSetting.getIpAddress());
            if (!ipAddressM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のipAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!ethernetSetting.getSubnetMask().isEmpty()) {
            Matcher subnetMaskM = ipAddress.matcher(ethernetSetting.getSubnetMask());
            if (!subnetMaskM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
            }
        }
        if (ethernetSetting.getAccessVlan() != -1) {
            Matcher accessVlanM = fourDigits.matcher(String.valueOf(ethernetSetting.getAccessVlan()));
            if (!accessVlanM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessVlanの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (ethernetSetting.getNativeVlan() != -1) {
            Matcher nativeVlanM = fourDigits.matcher(String.valueOf(ethernetSetting.getNativeVlan()));
            if (!nativeVlanM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のnativeVlanの値は無効です。4桁以内の整数値を入力してください");
            }
        }

        if (!ethernetSetting.getMode().isEmpty()) {
            if (!(ethernetSetting.getMode().equals("access") || ethernetSetting.getMode().equals("trunk"))) {
                formatErrorStatements.add(ethernetSetting.getName() + "のmodeの値は無効です。'access' または 'trunk' のいずれかを入力してください");
            }
        }
        if (ethernetSetting.getAccessListNumber() != -1) {
            Matcher accessListNumberM = fourDigits.matcher(String.valueOf(ethernetSetting.getAccessListNumber()));
            if (!accessListNumberM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (!ethernetSetting.getAccessListName().isEmpty()) {
            Matcher accessListNameM = character.matcher(ethernetSetting.getAccessListName());
            if (!accessListNameM.matches()) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessListNameの値は無効です。正しい形式で入力してください");
            }
        }
        if (!ethernetSetting.getAccessListInOrOut().isEmpty()) {
            if (!(ethernetSetting.getAccessListInOrOut().equals("in") || ethernetSetting.getAccessListInOrOut().equals("out"))) {
                formatErrorStatements.add(ethernetSetting.getName() + "のaccessListInOrOutの値は無効です。'in' または 'out' のいずれかを入力してください");
            }
        }
        if (!ethernetSetting.getSpeed().isEmpty()) {
            if (!(ethernetSetting.getSpeed().equals("auto") || ethernetSetting.getSpeed().equals("10") || ethernetSetting.getSpeed().equals("100") || ethernetSetting.getSpeed().equals("1000"))) {
                formatErrorStatements.add(ethernetSetting.getName() + "のspeedの値は無効です。'auto','10','100','1000'のいずれかを入力してください");
            }
        }
        if (!ethernetSetting.getDuplex().isEmpty()) {
            if (!(ethernetSetting.getDuplex().equals("auto") || ethernetSetting.getDuplex().equals("full") || ethernetSetting.getDuplex().equals("half"))) {
                formatErrorStatements.add(ethernetSetting.getName() + "のduplexの値は無効です。'full' または 'half' のいずれかを入力してください");
            }
        }
        //「true」「false」はChangeClassInformationでチェックし，エラー文をClassElementのbooleanErrorStatementにいれてそれをここで取り出す
        if (ethernetSetting.getAttributeErrorStatement().size() != 0) {
            formatErrorStatements.addAll(ethernetSetting.getAttributeErrorStatement());
        }

        //ModeとVlanの関係
        //vlanに値を入れなかったら値は0になる。
        //Mode記入無しだが、トランクかアクセスに記入がある
        if ((ethernetSetting.getMode().isEmpty()) && ((ethernetSetting.getAccessVlan() != -1) || (ethernetSetting.getNativeVlan() != -1))) {
            errorStatements.add(ethernetSetting.getName() + "のModeが空白です");
        }
        //Mode記入があるがnativeVLANに記入がない
        else if ((ethernetSetting.getMode().equals("trunk")) && ethernetSetting.getNativeVlan() == -1) {
            errorStatements.add(ethernetSetting.getName() + "のnativeVlanが空白です");
        }
        //Mode記入があるがアクセスに記入がない
        else if ((ethernetSetting.getMode().equals("access")) && ethernetSetting.getAccessVlan() == -1) {
            errorStatements.add(ethernetSetting.getName() + "のaccessVlanが空白です");
        }


    }

    public static void vlanSettingCheck(VlanSetting vlanSetting, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
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
            if (!(vlanSetting.getAccessListInOrOut() == "in" || vlanSetting.getAccessListInOrOut() == "out")) {
                formatErrorStatements.add(vlanSetting.getName() + "のaccessListInOrOutの値は無効です。'in' または 'out' のいずれかを入力してください");
            }
        }
        //「true」「false」はChangeClassInformationでチェックし，エラー文をClassElementのbooleanErrorStatementにいれてそれをここで取り出す
        if (vlanSetting.getAttributeErrorStatement().size() != 0) {
            formatErrorStatements.addAll(vlanSetting.getAttributeErrorStatement());
        }

    }

    public static void accessListCheck(AccessList accessList, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {

        if (accessList.getAccessListNumber() != -1) {
            Matcher accessListNumberM = fourDigits.matcher(String.valueOf(accessList.getAccessListNumber()));
            if (!accessListNumberM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のaccessListNumberの値は無効です。4桁以内の整数値を入力してください");
            }
        }
        if (!accessList.getPermitOrDeny().isEmpty()) {
            if (!(accessList.getPermitOrDeny() == "permit" || accessList.getPermitOrDeny() == "deny")) {
                formatErrorStatements.add(accessList.getName() + "のpermitOrDenyの値は無効です。'permit' または 'deny' のいずれかを入力してください");
            }
        }
        if (!accessList.getProtocol().isEmpty()) {
            Matcher protocolM = character.matcher(String.valueOf(accessList.getProtocol()));
            if (!protocolM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のprotcolの値は無効です。正しい形式で入力してください");
            }
        }
        if (!accessList.getSorceIpAddress().isEmpty()) {
            Matcher sourseipAddressM = ipAddress.matcher(accessList.getSorceIpAddress());
            if (!sourseipAddressM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のsourceIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!accessList.getSourceWildcardMask().isEmpty()) {
            Matcher sourceWildcardMaskM = ipAddress.matcher(accessList.getSourceWildcardMask());
            if (!sourceWildcardMaskM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のsourceWildcardMaskの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }

        if (!accessList.getDestIpAddress().isEmpty()) {
            Matcher destIpAddressM = ipAddress.matcher(accessList.getDestIpAddress());
            if (!destIpAddressM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のdestIpAddressの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!accessList.getDestWildcardMask().isEmpty()) {
            Matcher destWildcardMaskM = ipAddress.matcher(accessList.getDestWildcardMask());
            if (!destWildcardMaskM.matches()) {
                formatErrorStatements.add(accessList.getName() + "のdestWildcardMaskの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
//sorceportとdenyportは選択肢が多すぎるため保留

    }

    public static void clientCheck(Clients clients, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
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
        if (!clients.getSubnetMask().isEmpty()) {
            Matcher subnetMaskM = ipAddress.matcher(clients.getSubnetMask());
            if (!subnetMaskM.matches()) {
                formatErrorStatements.add(clients.getName() + "のsubnetMaskの値は無効です。有効なサブネットマスク形式で入力してください");
            }
        }
        if (!clients.getSubnetMask().isEmpty()) {
            Matcher defaultGateWayM = ipAddress.matcher(clients.getSubnetMask());
            if (!defaultGateWayM.matches()) {
                formatErrorStatements.add(clients.getName() + "のdefaultGateWayの値は無効です。有効なデフォルトゲートウェイ形式で入力してください");
            }
        }
    }

    public static void configCheck(Config config, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (!config.getDeviceModel().isEmpty()) {
            Matcher devicemodelM = character.matcher(String.valueOf(config.getDeviceModel()));
            if (!devicemodelM.matches()) {
                formatErrorStatements.add(config.getName() + "のdevicemodelの値は無効です。正しい形式で入力してください");
            }
        }
    }

    public static void hostNameCheck(Hostname hostname, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (!hostname.getHostName().isEmpty()) {
            Matcher nameM = character.matcher(String.valueOf(hostname.getHostName()));
            if (!nameM.matches()) {
                formatErrorStatements.add(hostname.getName() + "のnameの値は無効です。正しい形式で入力してください");
            }
        }
    }

    //Linkは自由に記述できてしまうため保留
    public static void linkCheck(Link link, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (!link.getDescription().isEmpty()) {
            Matcher descriptionM = linkpattern.matcher(String.valueOf(link.getDescription()));
            if (!descriptionM.matches()) {
                formatErrorStatements.add(link.getName() + "のdescriptionの値は無効です。正しい形式で入力してください");
            }
        }
    }

    public static void ipRouteCheck(IpRoute ipRoute, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (!ipRoute.getNetwork().isEmpty()) {
            Matcher networkM = ipAddress.matcher(ipRoute.getNetwork());
            if (!networkM.matches()) {
                formatErrorStatements.add(ipRoute.getName() + "のnetworkの値は無効です。有効なIPアドレス形式で入力してください");
            }
        }
        if (!ipRoute.getAddressPrefix().isEmpty()) {
            Matcher addressPrefixM = ipAddress.matcher(ipRoute.getAddressPrefix());
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
    public static void ospfInterfaceSettingCheck(OspfInterfaceSetting ospfInterfaceSetting, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
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
    }
    public static void ospfSettingCheck(OspfSetting ospfSetting, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
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
    public static void ospfvirtualLinkCheck(OspfVirtualLink ospfVirtualLink, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
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

    public static void vlanheck(Vlan vlan, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (vlan.getNum() != -1) {
            Matcher arealdM = fourDigits.matcher(String.valueOf(vlan.getNum()));
            if (!arealdM.matches()) {
                formatErrorStatements.add(vlan.getName() + "のnumの値は無効です。4桁までの整数を入力してください");
            }
        }
        if (!vlan.getNamd().isEmpty()) {
            Matcher nameM = character.matcher(vlan.getNamd());
            if (!nameM.matches()) {
                formatErrorStatements.add(vlan.getName() + "のnameの値は無効です。正しい形式で入力してください");
            }
        }

    }
    public static void stpSettingCheck(StpSetting stpSetting, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (stpSetting.getBridgePriority() != -1) {
            Matcher bredgePriorityM = fiveDigits.matcher(String.valueOf(stpSetting.getBridgePriority()));
            if (!bredgePriorityM.matches()) {
                formatErrorStatements.add(stpSetting.getName() +"bredgePriorityの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (stpSetting.getVlan() != -1) {
            Matcher vlanM = fourDigits.matcher(String.valueOf(stpSetting.getVlan()));
            if (!vlanM.matches()) {
                formatErrorStatements.add(stpSetting.getName() +"vlanの値は無効です。5桁までの整数を入力してください");
            }
        }
        if (!stpSetting.getMode().isEmpty()) {
            if (!(stpSetting.getMode()== "pvst" || stpSetting.getMode() == "pvst+"|| stpSetting.getMode() == "rstp"|| stpSetting.getMode() == "mstp")) {
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

    public static void stackCheck(Stack stack, ArrayList<String> errorStatements, ArrayList<String> formatErrorStatements) {
        if (stack.getStackMemberNumber() != -1) {
            Matcher stackMemberNumberM = fiveDigits.matcher(String.valueOf(stack.getStackMemberNumber()));
            if (!stackMemberNumberM.matches()) {
                formatErrorStatements.add(stack.getName() +"stackMemberNumberの値は無効です。5桁までの整数を入力してください");
            }
        }if (stack.getPreviousStackNumber() != -1) {
            Matcher previousStackNumberM = fiveDigits.matcher(String.valueOf(stack.getPreviousStackNumber()));
            if (!previousStackNumberM.matches()) {
                formatErrorStatements.add(stack.getName() +"previousStackNumberの値は無効です。5桁までの整数を入力してください");
            }
        }if (stack.getStackPriority() != -1) {
            Matcher stackPriorityM = fiveDigits.matcher(String.valueOf(stack.getStackPriority()));
            if (!stackPriorityM.matches()) {
                formatErrorStatements.add(stack.getName() +"stackPriorityの値は無効です。5桁までの整数を入力してください");
            }
        }
    }




    }
