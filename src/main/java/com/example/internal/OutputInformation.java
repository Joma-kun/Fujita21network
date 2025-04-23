package com.example.internal;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import com.example.classes.*;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
import com.example.internal.converter.ChangeClassInformation;
import com.example.internal.converter.SetOthersInformation;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OutputInformation {
    JTabbedPane tabbedPane = new JTabbedPane();
    /*出力できる情報
    * show vlan brief <コンフィグ名>　各コンフィグのVLAN情報
    * show vlan all                すべてのコンフィグのVLAN情報
    * show vlan <VlanID>    　　　　　指定したVLANIDに属するコンフィグとポートを出力する　色も*/

//    show ospf neighbor <コンフィグ名＞　SVIを用いて設定したときのみ対応(accessのみ)　インターフェースにIPアドレスを設定する場合は出力方法を工夫する必要あり

    public OutputInformation(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public void createLabelPaneJoho(String input, ArrayList<IPresentation> beforeInstance, ArrayList<String> beforeInstanceColor) throws RuntimeException {//Panelの中身
        boolean createTab = false; //Tabを作成するときのみ
        JPanel tab = new JPanel(new BorderLayout());//追加するタブを表すパネル
        TextArea textarea = new TextArea();//パネルの文章を入力する部分
        textarea.setText("<実行結果>\n");
        ProjectAccessor projectAccessor = null;
        String name = null;//タブに表示する名前

        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            projectAccessor = api.getProjectAccessor();
            ITransactionManager transactionManager = projectAccessor.getTransactionManager();
            IModel iCurrentProject = projectAccessor.getProject();//Imodelは最初のパッケージを表す
            ArrayList<IPresentation> presentations = new ArrayList<>();//表示されている図や関連の線についての情報を格納するリスト
            IDiagram[] diagrams = iCurrentProject.getDiagrams();//図を得る// stpmodel,tobemode

            for (IDiagram i : diagrams) {//クラス図の数だけ繰り返す（インスタンスの数ではない）
                presentations.addAll(Arrays.asList(i.getPresentations()));
            }//取得した情報のIPresentation(関連船やグラフの情報）を得る．

            //自分たちのinstance情報に変えるための処理
            ArrayList<ClassElement> instances = null;
            ArrayList<String> formatErrorStatements = new ArrayList<>();
            ArrayList<ClassElement> errorInstances = new ArrayList<>();
            try {
                instances = ChangeClassInformation.changeAllElement(presentations,projectAccessor);
            } catch (InvalidEditingException e) {
                throw new RuntimeException(e);
            }
            //linkの情報を変換する
            ArrayList<LinkElement> links = SetOthersInformation.changeLinkInformation(presentations, instances);

            try {//モデル編集のためのトランザクション処理
                transactionManager.beginTransaction();//トランザクションの開始
                /*インスタンスのカラーを初期の状態に戻すための処理
                beforeInstance:runを押した時の一個前のインスタンス
                beforeInstanceColor:beforeInstanceのカラー情報
                presentations:runを押した時点でのインスタンス
                runを押したときのインスタンスとそのカラーをリストに格納して、最初にそのリストを元にbeforeinstanceと同じ
                instanceの色を元に戻し、その状態を新しくbeforeInstanceとbeforeInstanceColorに格納して次につなげる。
                 */
                if (beforeInstance.size() != 0) {//一回目は実行しない
                    for (IPresentation presentation : presentations) {//現在のpresentationを取得したもの
                        for (int bf = 0; bf < beforeInstance.size(); bf++) {//前のインスタンスと同じところの色を戻すためのループ
                            if (presentation == beforeInstance.get(bf)) {//前のインスタンスが存在するとき
                                presentation.setProperty(PresentationPropertyConstants.Key.FILL_COLOR, beforeInstanceColor.get(bf));//前の色（初期の色）を適用させる
                            }
                        }
                    }
                }
                //最後
                beforeInstance.clear();//新しくInstanceを登録するために一旦すべて削除する。
                for (IPresentation presentation : presentations) {//インスタンスのみを取り出すためのループ。カラーを設定する処理がインスタンスと線で違うため

                    if (presentation instanceof INodePresentation) {//インスタンスの名前、スロットの処理
                        IElement model = presentation.getModel();
                        if (model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                            com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;
                            beforeInstance.add(presentation);//インスタンスの時のみリストに追加する
                        }
                    }
                    if (presentation instanceof ILinkPresentation) {//線の時
                        presentation.setProperty(PresentationPropertyConstants.Key.LINE_COLOR, "#000000");
                    }
                }
                beforeInstanceColor = recordBeforeInstanceColar(presentations);
                //ここまで（astahの色戻し処理）
                //ここまで（astahの色戻し処理）
                ArrayList<Config> configs = new ArrayList<>();
                for (ClassElement instance : instances) {
                    if (instance instanceof Config) {
                        configs.add((Config) instance);
                    }
                }

                //ここから違うクラスの処理に書き換えておく
                if (input.matches("^show vlan brief .*")) {//入力[show vlan brief <コンフィグ名>(例：Cf5)]
                    name = input.substring(16);
                    Config conf = new Config();//入力したコンフィグ
                    for (Config con : configs) {
                        if (con.getName().equals(name)) {
                            createTab = true;
                            conf = con;
                            break;
                        }
                    }//入力したコンフィグ名と名前が一致するコンフィグインスタンスを見つけて格納する


                    ArrayList<Integer> usePort = new ArrayList<>();//VLANが設定されているポートをリストに格納する
                    //VLANが設定されているポートを見つける処理　ここから
                    for (ClassElement cs : instances) {
                        if (cs instanceof EthernetSetting) {
                            if (((EthernetSetting) cs).getConfig() != null) {
                                if (((EthernetSetting) cs).getConfig().equals(conf)) {
                                    if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                        usePort.add(((EthernetSetting) cs).getPort());
                                    }
                                }
                            }
                        }
                    }
                    //ここまで

                    //出力の形VLAN1の表示まで
                    textarea.append("VLAN Name          Status     Ports                  \n");
                    textarea.append("---- ------------- ---------- --------------------------\n");
                    textarea.append(String.format("%-4s %-13s", "1", "default"));
                    textarea.append(String.format("%-13s", " active"));
                    int firstcount = 0;
                    //使用されていないポートをVLAN1に表示する。
                    for (int portNumber = 2; portNumber < 10; portNumber++) {//ポートの最大数は12と仮定する
                        if (!usePort.contains(portNumber)) {
                            if (firstcount != 0) {
                                textarea.append(",");
                            }
                            textarea.append("Po" + portNumber);
                            firstcount++;
                        }
                    }
                    textarea.append("\n");

                    //ここからVLAN1以外の情報出力
                    for (ClassElement ins : instances) {
                        if (ins instanceof Vlan) {//VLANクラスから情報を抜き出す//VLANクラス（番号）の数だけ繰り返す
                            if (((Vlan) ins).getConfig().equals(conf)) {//指定したConfigクラスとつながっているVLANクラス
                                ArrayList<EthernetSetting> ethList = new ArrayList<>(); //EthernetSettingクラスをまとめたリスト(指定したコンフィグクラスとつながっている)
                                ArrayList<Integer> ports = new ArrayList<>();//VLANが設定されているポートをまとめた物
                                ArrayList<String> portnames = new ArrayList<>();
                                int vlanNum = ((Vlan) ins).getNum();//VLAN番号の抽出
                                String vlanName = ((Vlan) ins).getVlanName();//VLANーNAME抽出
                                for (ClassElement ethins : instances) {
                                    if (ethins instanceof EthernetSetting) {
                                        if (((EthernetSetting) ethins).getConfig() != null) {
                                            if (((EthernetSetting) ethins).getConfig().equals(conf)) {//指定してコンフィグクラスとつながるEthernetSetttingクラスならば
                                                ethList.add((EthernetSetting) ethins);//指定したコンフィグクラスとつながる物をまとめたリストに入れる
                                                if (((EthernetSetting) ethins).getAccessVlan() == vlanNum) {//vlanが一致するとき
                                                    Integer port = ((EthernetSetting) ethins).getPort();//Port番号を取得する
                                                    ports.add(port);//そのVLANが設定されたポートをまとめるリストに追加する
                                                }
                                            }
                                        }
                                    }
                                }//portsの追加終わり
                                Collections.sort(ports);//ポート番号の並び替え

                                for (Integer por : ports) {//EthernetTypeを見つけるための処理
                                    for (EthernetSetting eth : ethList) {
                                        if (eth.getPort() == por.intValue()) {

                                            if (eth.getEthernetType() != null) {
                                                if (eth.getEthernetType().getType().equals("Ethernet")) {
                                                    portnames.add("Et" + por);
                                                } else if (eth.getEthernetType().getType().equals("fastEthernet")) {
                                                    portnames.add("Fa" + por);
                                                } else if (eth.getEthernetType().getType().equals("gigabitEthernet")) {
                                                    portnames.add("Gi" + por);
                                                } else if (eth.getEthernetType().getType().equals("10gigabitEthernet")) {
                                                    portnames.add("10Gi" + por);
                                                }
                                            } else {
                                                ;
                                                portnames.add("Po" + por);//EthernetTypeが設定されていないとき
                                            }
                                        }
                                    }
                                }
                                //情報の出力
                                if (vlanNum != -1) {
                                    if(vlanNum!=1){
                                    textarea.append(String.format("%-4s %-13s", vlanNum, vlanName));
                                    textarea.append(String.format("%-13s", " active"));
                                    int count = 0;//カンマの調整のため
                                    int size = portnames.size();
                                    for (String n : portnames) {
                                        textarea.append(n);
                                        count++;
                                        if (count < size) {
                                            textarea.append(",");
                                        }
                                    }
                                    textarea.append("\n");
                                }
                                }
                            }
                        }
                    }
                }
                //ここまで　show vlan brief

                //ここから　show vlan all　すべてのvlan情報
                if (input.matches("^show vlan all")) {
                    createTab = true; //tabの作成
                    name = "vlan-all"; // tabの名前
                    ArrayList<Integer> vlanNumbers = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Vlan> vlanInstance = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Config> vlan1Configs = new ArrayList<>();//すべてのVLAN番号

                    for (ClassElement classElement : instances) {//図にあるすべてのVLAN番号の把握
                        if (classElement instanceof Vlan) {
                            if (!vlanNumbers.contains(((Vlan) classElement).getNum())) {
                                vlanNumbers.add(((Vlan) classElement).getNum());
                            }
                            vlanInstance.add((Vlan) classElement);
                        }
                        if (classElement instanceof Config) {
                            vlan1Configs.add((Config) classElement);
                        }
                    }
                    Collections.sort(vlanNumbers);//ポート番号の並び替え
                    //vlanNumberにネットワークに存在するVLAN全てがある状態
                    textarea.append("VLAN Name          Config             Status     Ports                  \n");
                    textarea.append("---- ------------- ----------------   --------   --------------------------\n");

                    //vlna1のコンフィグ出力　ここから
                    for (Config config : vlan1Configs) {//すべてConfigについてvlan1はかく
                        textarea.append(String.format("%-4s %-14s", "1", "default"));
                        textarea.append(String.format("%-18s", config.getName()));
                        textarea.append(String.format("%-12s", " active"));

                        ArrayList<Integer> usePort = new ArrayList<>();//VLANが設定されているポートをリストに格納する
                        //VLANが設定されているポートを見つける処理　ここから
                        for (ClassElement cs : instances) {
                            if (cs instanceof EthernetSetting) {
                                if (((EthernetSetting) cs).getConfig() != null) {
                                    if (((EthernetSetting) cs).getConfig().equals(config)) {
                                        if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                            usePort.add(((EthernetSetting) cs).getPort());
                                        }
                                    }
                                }
                            }
                        }
                        int firstcount = 0;
                        //使用されていないポートをVLAN1に表示する。
                        for (int portNumber = 1; portNumber < 13; portNumber++) {//ポートの最大数は12と仮定する
                            if (!usePort.contains(portNumber)) {
                                if (firstcount != 0) {
                                    textarea.append(",");
                                }
                                textarea.append("Po" + portNumber);
                                firstcount++;
                            }
                        }
                        textarea.append("\n");


                    }

                    textarea.append("\n");
                    for (Integer vlanN : vlanNumbers) {//vlanNが60のときVLAN60に属するコンフィグやポートを出力する
                        if (vlanN != 1) {
                            //出力しなければいけない情報→name config status port
                            //VLANインスタンスを元にコンフィグを探す
                            for (Vlan vlanI : vlanInstance) {
                                if (vlanI.getNum() == vlanN) {
                                    Config conf = new Config();
                                    conf = vlanI.getConfig();
                                    ArrayList<EthernetSetting> ethernetSettings = new ArrayList<>();
                                    ethernetSettings.addAll(conf.getEthernetSetting());
                                    ArrayList<Integer> ports = new ArrayList<>();//VLANが設定されているポートをまとめた物
                                    ArrayList<String> portnames = new ArrayList<>();
                                    for (EthernetSetting eth : ethernetSettings) {
                                        if (eth.getAccessVlan() == vlanN) {//現在調べているVLANと同じ時
                                            ports.add(eth.getPort());
                                        }
                                    }
                                    Collections.sort(ports);//ポート番号の並び替え
                                    for (Integer por : ports) {//EthernetTypeを見つけるための処理
                                        for (EthernetSetting eth : ethernetSettings) {
                                            if (eth.getPort() == por.intValue()) {
                                                if (eth.getEthernetType() != null) {
                                                    if (eth.getEthernetType().getType().equals("Ethernet")) {
                                                        portnames.add("Et" + por);
                                                    } else if (eth.getEthernetType().getType().equals("fastEthernet")) {
                                                        portnames.add("Fa" + por);
                                                    } else if (eth.getEthernetType().getType().equals("gigabitEthernet")) {
                                                        portnames.add("Gi" + por);
                                                    } else if (eth.getEthernetType().getType().equals("10gigabitEthernet")) {
                                                        portnames.add("10Gi" + por);
                                                    }
                                                } else {
                                                    ;
                                                    portnames.add("Po" + por);//EthernetTypeが設定されていないとき
                                                }
                                            }
                                        }
                                    }
                                    //情報の出力　 vlan1以外
                                    textarea.append(String.format("%-4s %-14s", vlanN, vlanI.getVlanName()));
                                    textarea.append(String.format("%-18s", conf.getName()));
                                    textarea.append(String.format("%-12s", " active"));
                                    int count = 0;//カンマの調整のため
                                    int size = portnames.size();
                                    for (String n : portnames) {
                                        textarea.append(n);
                                        count++;
                                        if (count < size) {
                                            textarea.append(",");
                                        }
                                    }
                                    textarea.append("\n");

                                }


                            }
                            textarea.append("\n");
                        }
                    }
                }



                if (input.matches("^show vlan .*")) {//入力[show vlan <vlanID>(10)] (show vlan allをコピーしているところがあるため，余分な文があるかも）
                    createTab = true;
                    String numbers = input.substring(10);//vlanの番号
                    name = "vlan " + numbers;//tabの名前
                    String rightblue = "#87cefa";
                    ArrayList<Integer> vlanNumbers = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Vlan> vlanInstance = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Config> vlan1Configs = new ArrayList<>();//すべてのVLAN番号
                    vlanNumbers.add(1);
                    for (ClassElement classElement : instances) {//図にあるすべてのVLAN番号の把握
                        if (classElement instanceof Vlan) {
                            if (!vlanNumbers.contains(((Vlan) classElement).getNum())) {
                                vlanNumbers.add(((Vlan) classElement).getNum());
                            }
                            vlanInstance.add((Vlan) classElement);
                        }
                        if (classElement instanceof Config) {
                            vlan1Configs.add((Config) classElement);
                        }
                    }
                    try {//intに型変換要
                        int vlanN = Integer.parseInt(numbers);
                        if (vlanNumbers.contains(vlanN)) {//指定したvlanがモデルに存在しているとき
                            createTab = true;
                        }
                        textarea.append(("Config        Ports    \n"));
                        textarea.append(("------------- -------------------------\n"));
                        if (vlanN == 1) {
                            for (Config config : vlan1Configs) {//すべてConfigについてvlan1はかく
                                textarea.append(String.format("%-14s", config.getName()));
                                Check.changeColor(config, rightblue);
                                ArrayList<Integer> usePort = new ArrayList<>();//VLANが設定されているポートをリストに格納する
                                //VLANが設定されているポートを見つける処理　ここから
                                for (ClassElement cs : instances) {
                                    if (cs instanceof EthernetSetting) {
                                        if (((EthernetSetting) cs).getConfig() != null) {
                                            if (((EthernetSetting) cs).getConfig().equals(config)) {
                                                if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                                    usePort.add(((EthernetSetting) cs).getPort());
                                                }
                                            }
                                        }
                                    }
                                }
                                int firstcount = 0;
                                //使用されていないポートをVLAN1に表示する。
                                for (int portNumber = 1; portNumber < 13; portNumber++) {//ポートの最大数は12と仮定する
                                    if (!usePort.contains(portNumber)) {
                                        if (firstcount != 0) {
                                            textarea.append(",");
                                        }
                                        textarea.append("Po" + portNumber);
                                        firstcount++;
                                    }
                                }
                                textarea.append("\n");


                            }
                        } else {
                            for (Vlan vlanI : vlanInstance) {
                                if (vlanI.getNum() == vlanN) {
                                    Config conf = new Config();
                                    conf = vlanI.getConfig();
                                    ArrayList<EthernetSetting> ethernetSettings = new ArrayList<>();
                                    ethernetSettings.addAll(conf.getEthernetSetting());
                                    ArrayList<Integer> ports = new ArrayList<>();//VLANが設定されているポートをまとめた物
                                    ArrayList<String> portnames = new ArrayList<>();
                                    for (EthernetSetting eth : ethernetSettings) {
                                        if (eth.getAccessVlan() == vlanN) {//現在調べているVLANと同じ時
                                            ports.add(eth.getPort());
                                            Check.changeColor(eth, rightblue);
                                        }
                                    }
                                    Collections.sort(ports);//ポート番号の並び替え
                                    for (Integer por : ports) {//EthernetTypeを見つけるための処理
                                        for (EthernetSetting eth : ethernetSettings) {
                                            if (eth.getPort() == por.intValue()) {
                                                if (eth.getEthernetType() != null) {
                                                    if (eth.getEthernetType().getType().equals("Ethernet")) {
                                                        portnames.add("Et" + por);
                                                    } else if (eth.getEthernetType().getType().equals("fastEthernet")) {
                                                        portnames.add("Fa" + por);
                                                    } else if (eth.getEthernetType().getType().equals("gigabitEthernet")) {
                                                        portnames.add("Gi" + por);
                                                    } else if (eth.getEthernetType().getType().equals("10gigabitEthernet")) {
                                                        portnames.add("10Gi" + por);
                                                    }
                                                } else {
                                                    ;
                                                    portnames.add("Po" + por);//EthernetTypeが設定されていないとき
                                                }
                                            }
                                        }
                                    }
                                    //情報の出力　 vlan1以外
                                    textarea.append(String.format("%-14s", conf.getName()));
                                    Check.changeColor(conf, rightblue);
                                    int count = 0;//カンマの調整のため
                                    int size = portnames.size();
                                    for (String n : portnames) {
                                        textarea.append(n);
                                        count++;
                                        if (count < size) {
                                            textarea.append(",");
                                        }
                                    }
                                    textarea.append("\n");

                                }


                            }

                        }
                    } catch (NumberFormatException e) {
                        // パースできない場合の処理
                    }


                }
                if(input.matches("^show running-config .*")){
                    createTab = true;
                    String configName = input.substring(20);
                    name = "run" + configName;
                    Config config = new Config();
                    for(ClassElement instance : instances){
                        if(instance instanceof Config){
                            if(instance.getName().equals(configName)){
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    if(config == null){
                        textarea.append("正しくConfigインスタンスの名前を入力してください");
                    }else{
                        textarea.append(config.getHostname().getHostName()+"\n");
                        textarea.append("!\n");
                        textarea.append("hostname " + config.getHostname().getHostName() + "\n");


                        for(StpSetting stpSetting : config.getStpSetting()){
                            if(stpSetting.getBridgePriority()!=32768){
                                textarea.append("spanning-tree vlan " + stpSetting.getVlan() + "priority" + stpSetting.getBridgePriority() + "\n");
                            }

                        }
                        textarea.append("!\n");
                        String  ethrenettype = null;
                        ArrayList<Integer> nativeVlans = new ArrayList<>();
                        for(EthernetSetting ethernetSetting : config.getEthernetSetting()){
                            if(ethernetSetting.getEthernetType() != null){
                                ethrenettype = ethernetSetting.getEthernetType().getType();

                            }
                            if(ethernetSetting.getNativeVlan() != -1){
                                if(!nativeVlans.contains(ethernetSetting.getNativeVlan())){
                                    nativeVlans.add(ethernetSetting.getNativeVlan());
                                }
                            }
                        }
                        int ports = 9;
                        for(int portN = 0; portN < ports+1 ;portN++){
                            EthernetSetting ethernetSetting = null;
                            for(EthernetSetting ethernetSetting1 : config.getEthernetSetting()) {
                                if (ethernetSetting1.getPort() == portN) {
                                    ethernetSetting = ethernetSetting1;
                                    break;
                                }
                            }
                            textarea.append("!\n");
                            if(ethrenettype!=null){
                                textarea.append("interface " + ethrenettype + portN + "\n");
                            }
                            else {
                                textarea.append("interface " + portN + "\n");

                            }

                            if(portN == 0 ||portN == 1){
                                if(ethernetSetting != null) {
                                    if (ethernetSetting.getIpAddress().isEmpty()) {
                                        textarea.append(" no ip address\n");
                                    } else {
                                        textarea.append(" ipaddress " + ethernetSetting.getIpAddress() + " " + ethernetSetting.getSubnetMask() + "\n");
                                    }
                                    if (ethernetSetting.getShutdown()) {
                                        textarea.append(" shutdown\n");
                                    } else {
                                        textarea.append(" no shutdown\n");
                                    }
                                    textarea.append(" duplex " + ethernetSetting.getDuplex() + "\n");
                                    textarea.append(" speed " + ethernetSetting.getSpeed() + "\n");

                                    textarea.append("!\n");
                                }else{
                                    textarea.append(" no ip address\n");
                                    textarea.append(" shutdown\n");
//                                    textarea.append(" duplex auto");
//                                    textarea.append(" speed auto\n");
                                }
                            }else {
                                if(ethernetSetting != null) {


                                    if (ethernetSetting.getMode().equals("trunk")) {
                                        if (!ethernetSetting.getAllowdVlanString().isEmpty()) {
                                            textarea.append(" switchport trunk allowed vlan " + ethernetSetting.getAllowdVlanString() + "\n");
                                        }
                                        textarea.append(" switchport mode trunk\n");
                                    } else if (ethernetSetting.getMode().equals("access")) {
                                        if (ethernetSetting.getAccessVlan() != -1) {
                                            textarea.append(" switchport access vlan " + ethernetSetting.getAccessVlan() + "\n");
                                        }
                                    }
//                                    if(!ethernetSetting.getSpeed().isEmpty() || ethernetSetting.getSpeed().equals("auto")){
//                                        textarea.append(" spped "+ethernetSetting.getSpeed() + "\n");
//                                    }
                                }
                            }


                        }
                        //ここまでインターフェース
                        //ここからVLAN
                        ArrayList<Integer> allvlan = new ArrayList<>();
                        for(Vlan vlan : config.getVlan()){
                            allvlan.add(vlan.getNum());
                        }
                        for(Integer integer : allvlan){
                            VlanSetting vlanSetting =null;
                            for(VlanSetting vlanSetting1 : config.getVlanSetting()) {
                                if(vlanSetting1.getVlanNum()==integer){
                                    vlanSetting = vlanSetting1;
                                }
                            }
                                    if(nativeVlans.contains(integer)){
                                        textarea.append("!\n");
                                        textarea.append("interface Vlan"+ integer + "\n");
                                        if(vlanSetting!=null){
                                            if(vlanSetting.getIpAddress().isEmpty()){
                                                textarea.append("no ip address\n");
                                            }else{
                                                textarea.append("ip address "+ vlanSetting.getIpAddress()+" "+vlanSetting.getSubnetMask()+"\n");
                                            }
                                        }else{
                                            textarea.append("no ip address\n");
                                        }
                                    }else{
                                        if(vlanSetting!=null){
                                        if(!vlanSetting.getIpAddress().isEmpty()){
                                            textarea.append("!\n");
                                            textarea.append("interface Vlan" + integer+"\n");
                                            textarea.append("ip address" + vlanSetting.getIpAddress()+" "+vlanSetting.getSubnetMask()+"\n");
                                        }}
                                    }
                        }

                       //ここからOSPF
                        if(config.getOspfSetting()!=null){
                            textarea.append("router ospf "+config.getOspfSetting().getProcessId()+"\n");
                            textarea.append(" router-id "+config.getOspfSetting().getRouterId()+"\n");
                            if(config.getOspfSetting().getOspfVirtualLinks().size()!=0){
                                for(OspfVirtualLink ov : config.getOspfSetting().getOspfVirtualLinks()){
                                    textarea.append(" area "+ ov.getAreaId()+" virtual-link "+ov.getRouterId()+"\n");
                                }
                            }
                            if(config.getOspfSetting().getOspfInterfaceSettings().size()!=0){
                                for(OspfInterfaceSetting oi : config.getOspfSetting().getOspfInterfaceSettings()){
                                    textarea.append(" network "+ oi.getIpAddress()+" "+oi.getWildcardMask()+" area "+oi.getAreaId()+"\n");
                                }
                            }
                        }



                    }
                    textarea.append("\n");
                    textarea.append("end\n");
                }
                if (input.matches("^show spanning-tree .*")){
                    createTab = true;
                    String configName = input.substring(19);//vlanの番号
                    name = "stp " + configName;//tabの名前
                    Config config = new Config();//情報を出力するコンフィグ
                    for(ClassElement instance : instances){
                        if(instance instanceof Config){
                            if(instance.getName().equals(configName)){
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    if(config == null){
                        textarea.append("正しくConfigインスタンスの名前を入力してください");
                    }else{
                    ArrayList<ArrayList<Config>> stprupes = new ArrayList<>() ;//ループ
                    ArrayList<String> warningStatement = new ArrayList<>();//使わない
                    ArrayList<ArrayList<Config>> rupeConfigs=Check.rupeChecks(instances,textarea,warningStatement);
                    for(ArrayList<Config> configs1 : rupeConfigs){//入力したコンフィグに関するループを抜き出す．
                        if(configs1.contains(config)){
                            stprupes.add(configs1);
                        }
                    }
                    for(ArrayList<Config> stprupe : stprupes) {//複数のループにかかわる場合
                        if(stprupes.size() > 1){//二つ以上の時だけループの詳細を書く
                            for(Config config2 : stprupe){
                                textarea.append(config2.getName() + ",");
                            }
                            textarea.append("\n");
                        }


                        ArrayList<Integer> vlanNumbers = new ArrayList<>();

                        for (StpSetting stpSetting : config.getStpSetting()) {
                            vlanNumbers.add(stpSetting.getVlan());
                        }
                        Collections.sort(vlanNumbers);
                        textarea.append(configName + "#show spanning-tree\n");
                        for (Integer vlanNum : vlanNumbers) {//Vlanごとに計算する．

                            //ルートブリッジの選定
                            ArrayList<StpSetting> stpSettings = new ArrayList<>();//vlanのstpSettingのリスト
                            ArrayList<Integer> bridgePriority = new ArrayList<>();
                            for (Config rupe : stprupe) {
                                for (StpSetting s : rupe.getStpSetting()) {
                                    if (s.getVlan() == vlanNum) {
                                        stpSettings.add(s);
                                        bridgePriority.add(s.getBridgePriority());
                                    }
                                }
                            }
                            int minbridgePriority = Collections.min(bridgePriority);
                            ArrayList<StpSetting> stpSettingsruto = new ArrayList<>();//ルートブリッジが最小のもの
                            Config rutobridge = new Config();
                            for (int i = 0; i < stpSettings.size(); i++) {
                                if (bridgePriority.get(i) == minbridgePriority) {
                                    stpSettingsruto.add(stpSettings.get(i));
                                }
                            }
                            ArrayList<String> macaddresses = new ArrayList<>();

                            if (stpSettingsruto.size() != 1) {//bridgePriorityでルートブリッジが決まらないとき
                                for (StpSetting stpSetting : stpSettingsruto) {
                                    macaddresses.add(stpSetting.getMacAddress());
                                }
                                Collections.sort(macaddresses);
                                String minMacAddress = macaddresses.get(0);
                                for (StpSetting stpSetting : stpSettings) {
                                    if (stpSetting.getMacAddress().equals(minMacAddress)) {
                                        rutobridge = stpSetting.getConfig();
                                    }
                                }

                            } else {//bridgePriorityでルートブリッジが決まったとき
                                for (StpSetting stpSetting : stpSettings) {
                                    if (stpSetting.getBridgePriority() == minbridgePriority) {
                                        rutobridge = stpSetting.getConfig();
                                    }
                                }
                            }
                            //この時点でルートブリッジが決定

                            ArrayList<EthernetSetting> rutoports = new ArrayList<>();

                            ArrayList<EthernetSetting> portEthernetSetting = new ArrayList<>();//パスコストのEthernetSetting
                            ArrayList<Integer> rutopasscost = new ArrayList<>();//パスコスト
                            //10→100 100→19 1000→4
                            for (Config conf : stprupe) {
                                if (!conf.equals(rutobridge)) {
                                    for (EthernetSetting ethernetSetting : conf.getEthernetSetting()) {
                                        int passcost = 0;
                                        if (ethernetSetting.getConectedThing() instanceof EthernetSetting) {
                                            if (stprupe.contains(((EthernetSetting) ethernetSetting.getConectedThing()).getConfig())) {
                                                passcost = passcost + keisanPasCost(ethernetSetting.getSpeed());
                                                EthernetSetting eth0 = ethernetSetting;
//                                                while (!((EthernetSetting) eth.getConectedThing()).getConfig().equals(rutobridge)){
                                                for (int ji = 0; ji < stprupe.size(); ji++) {
                                                    EthernetSetting eth = (EthernetSetting) eth0.getConectedThing();
                                                    Config onotherConfig = null;
                                                    if (eth0.getConectedThing() instanceof EthernetSetting) {
                                                        onotherConfig = ((EthernetSetting) eth0.getConectedThing()).getConfig();
                                                        for (EthernetSetting eth2 : onotherConfig.getEthernetSetting()) {
                                                            if (!eth2.equals(eth)) {
                                                                if (eth2.getConectedThing() instanceof EthernetSetting) {
//                                                                    if (stprupes.contains(((EthernetSetting) eth2.getConectedThing()).getConfig())) {

                                                                    if (stprupe.contains(((EthernetSetting) eth2.getConectedThing()).getConfig())) {
                                                                        if (!eth2.getConfig().equals(rutobridge)) {
                                                                            passcost = passcost + keisanPasCost(eth2.getSpeed());
                                                                        }

                                                                        eth = eth2;

                                                                        break;
                                                                    }

//                                                                    }
                                                                }
                                                            } else {
                                                            }
                                                        }
                                                        if (eth.getConectedThing() instanceof EthernetSetting) {
                                                            if (((EthernetSetting) eth.getConectedThing()).getConfig().equals(rutobridge)) {
                                                                break;
                                                            }
                                                        }

                                                    }

//                                                }
                                                }

                                                portEthernetSetting.add(ethernetSetting);
                                                rutopasscost.add(passcost);
                                            }
                                        }

                                    }
                                }
                            }
                            for (EthernetSetting eth : rutobridge.getEthernetSetting()) {
                                if (eth.getConectedThing() instanceof EthernetSetting) {
                                    if (stprupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())) {
                                        portEthernetSetting.add(eth);
                                        rutopasscost.add(0);
                                    }
                                }
                            }
                            //passcostの計算終わり

                            for (Config config1 : stprupe) {
                                if(!config1.equals(rutobridge)) {
                                    ArrayList<EthernetSetting> ethernetSettings = new ArrayList<>();
                                    int passcost1 = 0;
                                    int passcost2 = 0;
                                    for(EthernetSetting ethernetSetting : config1.getEthernetSetting()){
                                        if(ethernetSetting.getConectedThing() instanceof EthernetSetting){
                                            if(stprupe.contains(((EthernetSetting) ethernetSetting.getConectedThing()).getConfig())){
                                             ethernetSettings.add(ethernetSetting);
                                            }
                                        }
                                    }
                                    for(int i =0; i < portEthernetSetting.size();i ++){
                                        if(portEthernetSetting.get(i).equals(ethernetSettings.get(0))){
                                            passcost1 = rutopasscost.get(i);
                                        }else if(portEthernetSetting.get(i).equals(ethernetSettings.get(1)))
                                            {
                                                passcost2 = rutopasscost.get(i);
                                            }
                                        }
                                    if (passcost1==passcost2){
                                        int bridge1 =0;
                                        int bridge2 =0;
                                        //送信元ブリッジID　で比較　まだ書いてはいない

                                    } else if (passcost1>passcost2) {
                                        if(!rutoports.contains(ethernetSettings.get(1))){
                                            rutoports.add(ethernetSettings.get(1));
                                        }
                                    }else{
                                        if(!rutoports.contains(ethernetSettings.get(0))){
                                            rutoports.add(ethernetSettings.get(0));
                                        }
                                    }

//                                    ArrayList<EthernetSetting> ethernetSetting1 = new ArrayList<>();
//                                    ArrayList<Integer> ethernetSetting2 = new ArrayList<>();
//                                    for (EthernetSetting eth3 : config1.getEthernetSetting()) {
//                                        if (eth3.getConectedThing() instanceof EthernetSetting) {
//                                            if (stprupe.contains(((EthernetSetting) eth3.getConectedThing()).getConfig())) {
//                                                ethernetSetting1.add((EthernetSetting) eth3.getConectedThing());
//                                                int count = 0;
//                                                for (int i = 0; i < portEthernetSetting.size(); i++) {
//                                                    if (portEthernetSetting.get(i).equals(eth3)) {
//                                                        count = i;
//                                                        break;
//                                                    }
//                                                }
//                                                ethernetSetting2.add(rutopasscost.get(count));
//                                            }
//                                        }
//                                    }
//                                    if (ethernetSetting2.get(0) > ethernetSetting2.get(1)) {
//                                        if (!rutoports.contains(ethernetSetting1.get(1))) {
//                                            rutoports.add(ethernetSetting1.get(1));
//                                        }
//                                    } else {
//                                        if (!rutoports.contains(ethernetSetting1.get(0))) {
//                                            rutoports.add(ethernetSetting1.get(0));
//                                        }
//                                    }
                                }
                            }
                            //ルートポートの決定
                            //指定ポートの選出
                            ArrayList<EthernetSetting> siteiports = new ArrayList<>();
                            for (Config rupeconfig : stprupe) {
                                for (EthernetSetting eth : rupeconfig.getEthernetSetting()) {
                                    if (eth.getConectedThing() instanceof EthernetSetting) {
                                        boolean rutocheck = true;
                                        if (stprupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())) {
                                            for(EthernetSetting ethernetSetting :rutobridge.getEthernetSetting()){
                                                if(ethernetSetting.equals(eth)){
                                                    siteiports.add(eth);

                                                    rutocheck = false;
                                                }else if(ethernetSetting.equals(eth.getConectedThing())){
                                                    siteiports.add((EthernetSetting) eth.getConectedThing());
                                                    rutocheck = false;
                                                }
                                            }
                                    if(rutocheck){
                                    int port1 = 0;
                                    String mac1 = null;
                                    int bridge1 = 0;
                                    EthernetSetting e1 = new EthernetSetting();
                                    int port2 = 0;
                                    EthernetSetting e2 = new EthernetSetting();
                                    String mac2 = null;
                                    int bridge2 = 0;
                                    if (eth.getConectedThing() instanceof EthernetSetting) {
                                        if (stprupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())) {
                                            EthernetSetting eth2 = (EthernetSetting) eth.getConectedThing();
                                            if(rutoports.contains(eth)){
                                                e1 = eth;
                                            }else{
                                                for(EthernetSetting ethernetSetting : rutoports){
                                                    if(eth.getConfig().equals(ethernetSetting.getConfig())){
                                                        e1 = ethernetSetting;
                                                        break;
                                                    }
                                                }
                                            }
                                            if(rutoports.contains(eth2)){
                                                e2 = eth2;
                                            }else{
                                                for(EthernetSetting ethernetSetting : rutoports){
                                                    if(eth2.getConfig().equals(ethernetSetting.getConfig())){
                                                        e2 = ethernetSetting;
                                                    }
                                                }
                                            }
                                            for (int count = 0; count < portEthernetSetting.size(); count++) {
                                                if (e1.equals(portEthernetSetting.get(count))) {
                                                    port1 = rutopasscost.get(count);

                                                    for (StpSetting stpSetting : eth.getConfig().getStpSetting()) {
                                                        if (stpSetting.getVlan() == vlanNum) {
                                                            mac1 = stpSetting.getMacAddress();
                                                            bridge1 = stpSetting.getBridgePriority();
                                                        }
                                                    }
                                                }
                                                if (e2.equals(portEthernetSetting.get(count))) {
                                                    port2 = rutopasscost.get(count);

                                                    for (StpSetting stpSetting : eth2.getConfig().getStpSetting()) {
                                                        if (stpSetting.getVlan() == vlanNum) {
                                                            mac2 = stpSetting.getMacAddress();
                                                            bridge2 = stpSetting.getBridgePriority();
                                                        }
                                                    }
                                                }
                                            }

                                            if (port1 == port2) {
                                                if (bridge1 == bridge2) {
                                                    if (mac1.compareTo(mac2) < 0) {
                                                        siteiports.add(eth);
                                                    } else {
                                                        siteiports.add(eth2);

                                                    }
                                                } else if (bridge1 > bridge2) {

                                                    siteiports.add(eth2);
                                                } else {

                                                    siteiports.add(eth);
                                                }
                                            } else if (port1 > port2) {

                                                siteiports.add(eth2);
                                            } else {

                                                siteiports.add(eth);
                                            }
                                        }}}
                                    }
                                }}
                            }
                            //指定ポート終わり
                            EthernetSetting hisiteiports = new EthernetSetting();
                            for (Config rupeconfig : stprupe) {
                                for (EthernetSetting eth : rupeconfig.getEthernetSetting()) {
                                    if (eth.getConectedThing() instanceof EthernetSetting) {
                                        if (stprupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())) {
                                            if (!siteiports.contains(eth) && !rutoports.contains(eth)) {
                                                hisiteiports = eth;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            //非指定ポート終わり

                            //VLANごとの情報の出力
                            textarea.append("\n");
                            textarea.append("VLAN" + vlanNum + "\n");
                            String mode = null;
                            int priority = 0;
                            int rutopriority = 0;
                            String macAddress = null;
                            String rutoMacAddress = null;
                            int myCost = 0;
                            for (StpSetting stpSetting : config.getStpSetting()) {
                                if (stpSetting.getVlan() == vlanNum) {
                                    if(stpSetting.getMode().equals("pvst") || stpSetting.getMode().equals("pvst")){
                                        mode = "ieee";
                                    }else if (stpSetting.getMode().equals("rstp")){
                                        mode = "rstp";
                                    }else{
                                        mode = stpSetting.getMode();
                                    }

                                    priority = stpSetting.getBridgePriority() ;
                                    macAddress = stpSetting.getMacAddress();
                                }
                            }
                            for (StpSetting stpSetting : rutobridge.getStpSetting()) {
                                if (stpSetting.getVlan() == vlanNum) {
                                    rutoMacAddress = stpSetting.getMacAddress();
                                    rutopriority = stpSetting.getBridgePriority();
                                }
                            }

                            ArrayList<Integer> pass = new ArrayList<>();
                            for(EthernetSetting ethernetSetting : config.getEthernetSetting()){
                                if(ethernetSetting.getConectedThing() instanceof  EthernetSetting){
                                    if(stprupe.contains(((EthernetSetting) ethernetSetting.getConectedThing()).getConfig())){
                                        for(int i = 0 ;i < portEthernetSetting.size() ;i++){
                                            if(portEthernetSetting.get(i).equals(ethernetSetting)){
                                                pass.add(rutopasscost.get(i));
                                            }
                                        }
                                    }
                                }
                            }
                            if(pass.get(0) < pass.get(1)){
                                myCost = pass.get(0);
                            }else{
                                myCost = pass.get(1);
                            }
                            textarea.append("  Spanning tree enabled protocol " + mode + "\n");
                            textarea.append(String.format("%-2s %-9s %-8s  %-1s %-1s", "", "Root ID ", "Priority", rutopriority, "\n"));
                            textarea.append(String.format("%-12s %-9s %-1s %-1s", "", "Address", rutoMacAddress, "\n"));
                            if (config.equals(rutobridge)) {
                                textarea.append(String.format("%-12s %-8s %-1s ", "", "This bridge is the root", "\n"));
                            } else{
                                textarea.append(String.format("%-13s %-10s %-1s %-1s", "", "Cost",myCost , "\n"));
                            }
                            textarea.append("\n");
                            textarea.append(String.format("%-2s %-7s %-9s %-1s %-1s", "", "Bridge ID ", "Priority", priority, "\n"));
                            textarea.append(String.format("%-13s %-9s %-1s %-1s", "", "Address", macAddress, "\n"));
                            textarea.append("\n");

                            textarea.append("interfaceName        Port ID Prio Cost  Sts Cost  Bridge ID            Port ID\n");
                            textarea.append("-------------------- ------- ---- ----- --- ----- -------------------- -------\n");


                            for(int j=0 ; j<portEthernetSetting.size() ; j++){

                            }
                            for(EthernetSetting si : rutoports){
                            }
                            for(EthernetSetting si : siteiports){
                            }

                            ArrayList<EthernetSetting> ethernetSettings = new ArrayList<>();
                            ArrayList<EthernetSetting> ethernetSettings2 = new ArrayList<>();
                            for(EthernetSetting et : config.getEthernetSetting()){
                                if(et.getConectedThing() instanceof  EthernetSetting){
                                    if(stprupe.contains(((EthernetSetting) et.getConectedThing()).getConfig())){
                                        ethernetSettings.add(et);
                                    }
                                }
                            }
                            if(ethernetSettings.get(0).getPort() > ethernetSettings.get(1).getPort()){
                                ethernetSettings2.add(ethernetSettings.get(1));
                                ethernetSettings2.add(ethernetSettings.get(0));
                            }else{
                                ethernetSettings2.add(ethernetSettings.get(0));
                                ethernetSettings2.add(ethernetSettings.get(1));
                            }
                            //並び替えの方法は後で変える．ここからはfastEthernet5のための処理，　アクセスポート
                            for(EthernetSetting ethernetSettin : config.getEthernetSetting()){
                                if(!ethernetSettings2.contains(ethernetSettin)){
                                    if(ethernetSettin.getAllowedVlans()!=null){
                                    if(ethernetSettin.getAccessVlan()==vlanNum ){
                                        ethernetSettings2.add(ethernetSettin);
                                    }}
                                }
                            }

                            for (EthernetSetting ethernetSetting : ethernetSettings2) {
                                if (ethernetSettings.contains(ethernetSetting)) {
                                    String interfacename = null;
                                    String role = null;
                                    String sts = null;
                                    String cost = null;
                                    String prio = null;
                                    String type = null;
                                    int disignatedCost = 0;
                                    String disignatedBridgeID = null;
                                    if (ethernetSetting.getEthernetType() != null) {
                                        interfacename = ethernetSetting.getEthernetType().getType() + "" + ethernetSetting.getPort();
                                    } else {
                                        interfacename = "ethernet" + ethernetSetting.getPort();
                                    }
                                    if (rutoports.contains(ethernetSetting)) {
                                        role = "Root";
                                    } else if (siteiports.contains(ethernetSetting)) {
                                        role = "Desg";
                                    } else {
                                        role = "Altn";
                                    }
                                    if (role.equals("Altn")) {
                                        sts = "BLK";
                                    } else {
                                        sts = "FWD";
                                    }
                                    if (ethernetSetting.getSpeed().equals("10")) {
                                        cost = "100";
                                    }
                                    if (ethernetSetting.getSpeed().equals("100")) {
                                        cost = "19";
                                    }
                                    if (ethernetSetting.getSpeed().equals("1000")) {
                                        cost = "4";
                                    }
                                    if (ethernetSetting.getSpeed().equals("10000")) {
                                        cost = "2";
                                    }
                                    prio = "128";
                                    type = "P2p";
                                    EthernetSetting designatedport = null;
                                    if (siteiports.contains(ethernetSetting)) {//指定ポートが自分自身の時
                                        designatedport = ethernetSetting;


                                    } else if (siteiports.contains(ethernetSetting.getConectedThing())) {
                                        if (ethernetSetting.getConectedThing() instanceof EthernetSetting) {
                                            designatedport = (EthernetSetting) ethernetSetting.getConectedThing();
                                        }

                                    }
                                    EthernetSetting ruto = null;
                                    for (EthernetSetting ethernetSetting1 : designatedport.getConfig().getEthernetSetting()) {
                                        if (rutoports.contains(ethernetSetting1)) {
                                            ruto = ethernetSetting1;
                                            break;
                                        }
                                    }
                                    for (int i = 0; i < portEthernetSetting.size(); i++) {
                                        if (portEthernetSetting.get(i).equals(ruto)) {
                                            disignatedCost = rutopasscost.get(i);
                                        }
                                    }
                                    if (ruto == null) {
                                        disignatedCost = 0;
                                    }

                                    StpSetting designatedStpSetting = new StpSetting();

                                    for (StpSetting stpSetting : designatedport.getConfig().getStpSetting()) {
                                        if (stpSetting.getVlan() == vlanNum) {
                                            designatedStpSetting = stpSetting;
                                        }
                                    }

                                    disignatedBridgeID = designatedStpSetting.getBridgePriority() + " " + designatedStpSetting.getMacAddress();
                                    textarea.append(String.format("%-20s %-7s %-4s %-5s %-3s %-5s %-20s %-7s %-1s", interfacename, prio, "128", cost, sts, disignatedCost, disignatedBridgeID, "128", "\n"));


                                } else {

                                    String interfacename = null;
                                    String role = null;
                                    String sts = null;
                                    String cost = null;
                                    String prio = null;
                                    String type = null;
                                    int disignatedCost = 0;
                                    String disignatedBridgeID = null;
                                    if (ethernetSetting.getEthernetType() != null) {
                                        interfacename = ethernetSetting.getEthernetType().getType() + "" + ethernetSetting.getPort();
                                    } else {
                                        interfacename = "ethernet" + ethernetSetting.getPort();
                                    }

                                        sts = "FWD";

                                    if (ethernetSetting.getSpeed().equals("10")) {
                                        cost = "100";
                                    }
                                    if (ethernetSetting.getSpeed().equals("100")) {
                                        cost = "19";
                                    }
                                    if (ethernetSetting.getSpeed().equals("1000")) {
                                        cost = "4";
                                    }
                                    if (ethernetSetting.getSpeed().equals("10000")) {
                                        cost = "2";
                                    }
                                    prio = "128";
                                    type = "P2p";
                                    EthernetSetting designatedport = null;
                                    if (siteiports.contains(ethernetSetting)) {//指定ポートが自分自身の時
                                        designatedport = ethernetSetting;


                                    } else  {

                                            designatedport = ethernetSetting
                                        ;

                                    }
                                    for(StpSetting stpSetting : ethernetSetting.getConfig().getStpSetting()){
                                        if(stpSetting.getVlan() == vlanNum){
                                            disignatedBridgeID = stpSetting.getBridgePriority() + " " + stpSetting.getMacAddress();

                                        }
                                    }
                                    if(ethernetSetting.getConfig().equals(rutobridge)){
                                        disignatedCost = 0;
                                    }else{
                                        for(EthernetSetting ethernetSetting1 : config.getEthernetSetting()){
                                            EthernetSetting eth = ethernetSetting1;
                                            int count = 0;
                                            int costs = 0;
                                            boolean ok = true;
                                            if(eth.getConectedThing()==null) {
                                             break;
                                            }else if(!(eth.getConectedThing() instanceof EthernetSetting)){
                                                break;
                                            }else if(!stprupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())){
                                                break;
                                            }
                                            while(true){
                                                count+=1;
                                                if(eth.getConectedThing()!=null){
                                                    if(eth.getConectedThing() instanceof EthernetSetting){
                                                        if(stprupe.contains(((EthernetSetting) eth.getConectedThing()).getConfig())){
                                                            if(eth.equals(hisiteiports) || eth.getConectedThing().equals(hisiteiports)){
                                                                ok = false;
                                                                break;
                                                            }else if(((EthernetSetting) eth.getConectedThing()).getConfig().equals(rutobridge)) {
                                                                if (eth.getSpeed().equals("10")) {
                                                                    costs+=  100;
                                                                }
                                                                if (eth.getSpeed().equals("100")) {
                                                                    costs+=  19;
                                                                }
                                                                if (eth.getSpeed().equals("1000")) {
                                                                    costs+=  4;
                                                                }
                                                                if (eth.getSpeed().equals("10000")) {
                                                                    costs+=  2;
                                                                }
                                                                break;
                                                            }else{
                                                                if (eth.getSpeed().equals("10")) {
                                                                    costs+=  100;
                                                                }
                                                                if (eth.getSpeed().equals("100")) {
                                                                    costs+=  19;
                                                                }
                                                                if (eth.getSpeed().equals("1000")) {
                                                                    costs+=  4;
                                                                }
                                                                if (eth.getSpeed().equals("10000")) {
                                                                    costs+=  2;
                                                                }
                                                                for(EthernetSetting ethernetSetting2 : ((EthernetSetting) eth.getConectedThing()).getConfig().getEthernetSetting()) {
                                                                    if (!ethernetSetting2.equals(eth)) {
                                                                        if (ethernetSetting2.getConectedThing() != null) {
                                                                            if (ethernetSetting2.getConectedThing() instanceof EthernetSetting) {
                                                                                if (stprupe.contains(((EthernetSetting) ethernetSetting2.getConectedThing()).getConfig())) {
                                                                                    eth=ethernetSetting2;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if(count >6){
                                                    break;
                                                }
                                            }
                                            if(ok){
                                                disignatedCost = costs;
                                                break;
                                            }
                                        }

                                    }



                                    textarea.append(String.format("%-20s %-7s %-4s %-6s %-3s %-6s %-20s %-7s %-1s", interfacename, prio, "128", cost, sts, disignatedCost, disignatedBridgeID, "128", "\n"));

                                }
                            }
                        }
                        }

                        }
                }
                if (input.matches("^show ip protocols .*")) {//show ip protocols R1
                    createTab = true;
                    String configName = input.substring(18);//vlanの番号
                    name = "show ip protocols "+ configName ;//tabの名前

                    Config config = new Config();//情報を出力するコンフィグ
                    for(ClassElement instance : instances){
                        if(instance instanceof Config){
                            if(instance.getName().equals(configName)){
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    if(config == null){
                        textarea.append("正しくConfigインスタンスの名前を入力してください");
                    }else {
                        OspfSetting ospfSetting = config.getOspfSetting();
                        if(ospfSetting==null){
                            textarea.append((config.getName()+"のOspfSettingインスタンスがありません"));
                        }
                        Integer processID = null;//プロセスID
                        processID = ospfSetting.getProcessId();
                        String RouterID = null;//ルーターID
                        RouterID = ospfSetting.getRouterId();
                        ArrayList<OspfSetting> ospfSettings = new ArrayList<>();//図に存在するOspfSettingインスタンスの全て
                        for(ClassElement classElement : instances){//area0Checkのためだけのもの
                            if(classElement instanceof  OspfSetting){
                                ospfSettings.add((OspfSetting) classElement);
                            }
                        }


                        ospfNeighbor(config);
                        //出力
                        textarea.append("Routing Protocol is \"ospf " + processID + "\""+"\n");
                        textarea.append("  Outgoing update filter list for all interfaces is not set\n");//いらないかも知れないから保留
                        textarea.append("  Incoming update filter list for all interfaces is not set\n");//いらないかも知れないから保留
                        textarea.append("  Router ID "+RouterID+"\n");
                        HashSet<Integer> abrHantei = new HashSet<>();
                        Integer normal = 0;
                        Integer stub = 0;
                        Integer nssa = 0;
                        boolean area0kadouka = false;//エリア0につながっているかのチェック
                        int area = 0;
                        for(OspfInterfaceSetting areas : config.getOspfSetting().getOspfInterfaceSettings()){
                            abrHantei.add(areas.getAreaId());
                            if(areas.getStub().equals("stub")){
                                stub+=1;
                            }else if (areas.getStub().equals("nssa")){
                                nssa +=1;
                            }else{
                                normal +=1;
                            }
                        }
                        for(OspfInterfaceSetting areas : config.getOspfSetting().getOspfInterfaceSettings()){
                            if(areas.getAreaId()==0){
                                area0kadouka=true;
                                area = areas.getAreaId();
                                break;
                            }
                        }
                        ArrayList<String> w = new ArrayList<>();
                        if(area0kadouka) {
                            area0kadouka = Check.area0Check(area, ospfSettings,w);
                        }
//                        if(area0kadouka){//エリア０代わり当てられていないが、エリア０につながっている場合
//                            normal+=1;
//                        }
                        if(!abrHantei.contains(0)){
                            normal+=1;

                        }
                        if(abrHantei.size()!=1){
                            textarea.append("  It is an area border router"+"\n");//ABRかどうかによって変わる
                        }

                        int count=abrHantei.size()+1;
                        textarea.append("  Number of areas in this router is "+count+". "+normal+" normal "+stub+" stub "+nssa+" nssa\n");//ルータが属しているエリアの数　「通常のエリア」、「stubエリア」、「nssa」
                        int maximumPath = 4;//デフォルト
                        textarea.append("  Maximum path: "+maximumPath+"\n");//同じコスト値のパス（経路）を保持できる最大数を表示-機器依存？基本はデフォルトは4
                        textarea.append("  Routing for Networks:\n");//定型文「ルーターがどのネットワークについてOSPFルーティングを行っているかを示す。」ospfコンフィグレーションで定義したnetworkコマンドの情報を表示
                        for(OspfInterfaceSetting areas : config.getOspfSetting().getOspfInterfaceSettings()){
                            textarea.append("    "+areas.getIpAddress()+" "+areas.getWildcardMask()+" area "+areas.getAreaId()+"\n");
                        }
                        textarea.append(" Reference bandwidth unit is 100 mbps\n");//ospf残すと計算に使用する基準帯域幅 デフォルトは100Mbps
                        textarea.append("  Routing Information Sources:\n");//ルーティング情報を受信したネイバーを表示　ABRの場合エリア内でLSAを交換しているルータの情報を表示　ABRの場合は全てのルーター
                        textarea.append("    Gateway         Distance      Last Update\n");
                        ArrayList<Config> configs1 = new ArrayList<>();
                        if(abrHantei.size()!=1) {//ABRの場合

                            for(OspfSetting os : ospfSettings){
                                if(!os.getRouterId().equals(ospfSetting.getRouterId())){
                                    if(os.getOspfInterfaceSettings().size()>1){
                                        textarea.append("    " + os.getRouterId() + "         110      \n");//distanceはデフォルト　時間は表示無し
                                    }                                }
                            }
                        }else {//ABR以外の場合

                            HashMap<Integer, Set<Config>> lsdb = LSDBList(config);
                            for (Map.Entry<Integer, Set<Config>> entry : lsdb.entrySet()) {
                                for (Config co : entry.getValue()) {
                                    if(!configs1.contains(co)){
                                        textarea.append("    " + co.getOspfSetting().getRouterId() + "         110      \n");//distanceはデフォルト　時間は表示無し
                                    }
                                    configs1.add(co);
                                }
                            }
                        }
                        textarea.append("  Distance: (default is 110)\n");//OSPFルーティングの管理距離


                    }
                }
                if (input.matches("^show ip ospf neighbor .*")) {//show ip ospf neighbor R1 //SVIで設定している場合
                    createTab = true;
                    String configName = input.substring(22);//vlanの番号
                    name = "show ip ospf neighbor " + configName;//tabの名前
                    Config config = new Config();//情報を出力するコンフィグ
                    for (ClassElement instance : instances) {
                        if (instance instanceof Config) {
                            if (instance.getName().equals(configName)) {
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    if (config == null) {
                        textarea.append("正しくConfigインスタンスの名前を入力してください");
                    } else {
                        Map<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> neighbor = ospfNeighbor(config);
//                        System.out.println("EthernetSetting終了");
                        textarea.append(String.format("%-15s %-5s %-16s %-12s %-18s %s\n", "NeighborID", "Pri", "State", "Dead Time", "Address", "Interface"));
                        //OSPF_VirtualLinkの出力
                        for (Map.Entry<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> outerEntry : neighbor.entrySet()) {
//                            System.out.println("EthernetSetting終了-1");
                            Integer arId = outerEntry.getKey();
                            HashMap<EthernetSetting, OspfInterfaceSetting> innerMap = outerEntry.getValue();
                            int vlNumber = 0;
                            for (Map.Entry<EthernetSetting, OspfInterfaceSetting> entry : innerMap.entrySet()) {
                                EthernetSetting ethernetSetting = entry.getKey();  // キー (EthernetSetting)
                                OspfInterfaceSetting ospfSetting = entry.getValue();  // 値 (OspfInterfaceSetting)
                                VlanSetting vs = new VlanSetting();
                                for(VlanSetting vss : ospfSetting.getOspfSetting().getConfig().getVlanSetting()){
                                    if(ethernetSetting.getConectedThing() instanceof EthernetSetting){
                                        EthernetSetting s = (EthernetSetting) ethernetSetting.getConectedThing();
                                        if(vss.getVlanNum()==s.getAccessVlan()){
                                            vs = vss;
                                        }
                                    }

                                }
//                                    String ip = entry.getValue().getIpAddress();
                                String ip = vs.getIpAddress();
                                String id = entry.getValue().getOspfSetting().getRouterId();
//                                String deadtime = "00:00:" + entry.getValue().getDeadInterval();
                                String deadtime = "-";
                                Integer pri = entry.getValue().getPriority();
                                if (pri == -1) pri = 1;//設定されていないときはデフォルト
                                String Interface = "ethernet" + String.valueOf(entry.getKey().getPort());
                                if (entry.getKey().getEthernetType() != null) {
                                    Interface = entry.getKey().getEthernetType().getType() + String.valueOf(entry.getKey().getPort());
                                }

                                String interFace = "OSPF_VL" + vlNumber;//accessVlan以外の時は表示の仕方を変える必要あり

                                String DRDBR = null;

//                                System.out.println("------------------------");
//                                System.out.println(config.getName());
//                                System.out.println(ethernetSetting.getName());
                                textarea.append(String.format("%-15s %-5s %-16s %-12s %-18s %s\n", id, "0", "FULL/ - ", deadtime, ip, interFace));
                                vlNumber += 1;
//                                textarea.append(String.format("%-15s %-5s %-16s %-12s %-18s %s\n", id, pri, "FULL/", deadtime, ip, Interface));
                            }
                        }

                        //普通の出力 show ip ospf neighbor Cf3
                            for (Map.Entry<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> outerEntry : neighbor.entrySet()) {
//                            System.out.println("EthernetSetting終了-1");
                                Integer arId = outerEntry.getKey();

                                HashMap<EthernetSetting, OspfInterfaceSetting> innerMap = outerEntry.getValue();
                                for (Map.Entry<EthernetSetting, OspfInterfaceSetting> entry : innerMap.entrySet()) {
                                    EthernetSetting ethernetSetting = entry.getKey();  // キー (EthernetSetting)
                                    OspfInterfaceSetting ospfSetting = entry.getValue();  // 値 (OspfInterfaceSetting)
                                    VlanSetting vs = new VlanSetting();

                                    for(VlanSetting vss : ospfSetting.getOspfSetting().getConfig().getVlanSetting()){
                                        if(ethernetSetting.getConectedThing() instanceof EthernetSetting){
                                            EthernetSetting s = (EthernetSetting) ethernetSetting.getConectedThing();
                                            if(vss.getVlanNum()==s.getAccessVlan()){
                                                vs = vss;
                                            }
                                        }

                                    }
//                                    String ip = entry.getValue().getIpAddress();
                                    String ip = vs.getIpAddress();
                                    String id = entry.getValue().getOspfSetting().getRouterId();
//                                String deadtime = "00:00:" + entry.getValue().getDeadInterval();
                                    String deadtime = "-";
                                    Integer pri = entry.getValue().getPriority();
                                    if (pri == -1) pri = 1;//設定されていないときはデフォルト
                                    String Interface = "ethernet" + String.valueOf(entry.getKey().getPort());
                                    if (entry.getKey().getEthernetType() != null) {
                                        Interface = entry.getKey().getEthernetType().getType() + String.valueOf(entry.getKey().getPort());
                                    }
                                    String interFace = "Vlan" + ethernetSetting.getAccessVlan();//accessVlan以外の時は表示の仕方を変える必要あり
                                    HashMap<String, HashMap<Config, Config>> drbdr = getDrBdr(ospfSetting.getOspfSetting().getConfig());
                                    String DRDBR = null;
                                    for (Map.Entry<String, HashMap<Config, Config>> out : drbdr.entrySet()) {
//                                        for (Map.Entry<Config, Config> ds: out.getValue().entrySet()) {
//                                            System.out.println(out.getKey() + "のDRDBRは" +ds.getKey().getName()+ds.getValue().getName());
//                                        }
                                        if (calculateNetworkAddress(ospfSetting.getIpAddress(), ospfSetting.getWildcardMask()).equals(out.getKey())) {
                                            HashMap<Config, Config> drbdrs = out.getValue();
                                            for (Map.Entry<Config, Config> d : drbdrs.entrySet()) {
//                                                textarea.append(d.getKey().getName()+d.getValue().getName());
                                                if (d.getKey() == ospfSetting.getOspfSetting().getConfig()) {
                                                    DRDBR = "DR";
                                                }
                                                if (d.getValue() == ospfSetting.getOspfSetting().getConfig()) {
                                                    DRDBR = "BDR";
                                                }
                                            }
                                        }
                                    }
//                                System.out.println("------------------------");
//                                System.out.println(config.getName());
//                                System.out.println(ethernetSetting.getName());
                                    textarea.append(String.format("%-15s %-5s %-16s %-12s %-18s %s\n", id, pri, "FULL/" + DRDBR, deadtime, ip, interFace));
//                                textarea.append(String.format("%-15s %-5s %-16s %-12s %-18s %s\n", id, pri, "FULL/", deadtime, ip, Interface));
                                }
                            }
//                        Result  result = spfTreeCost(config);
//                        System.out.println("Distances:");
//                        textarea.append("ここから\n");
//                        result.distances.forEach((router, distance) -> textarea.append(router.getName() + ": " + distance + " units"));


                        }
                    }
                if (input.matches("^show ip ospf interface .*")) {//show ip ospf interface Cf3 vlan 30
                    createTab = true;
                    Pattern pattern = Pattern.compile("^show ip ospf interface (\\S+) (\\S+)$");
                    Matcher matcher = pattern.matcher(input);
                    String routerName = null;
                    String interfaceName =null;
                    System.out.println("show ip ospf interface vlan a");
                    if (matcher.matches()) {
                        // キャプチャされたルーター名とインターフェース名を取得
                        routerName = matcher.group(1);
                        interfaceName = matcher.group(2);

                        System.out.println("match");
                    }
//                    System.out.println("koko");
                    System.out.println(routerName);
                    System.out.println(interfaceName);
                    name = "ospf interface" +routerName+" "+interfaceName;//tabの名前
                    int interfaceNumber = 0;
                    Config config = new Config();//情報を出力するコンフィグ
                    for (ClassElement instance : instances) {
                        if (instance instanceof Config) {
                            if (instance.getName().equals(routerName)) {
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    System.out.println("show ip ospf interface vlan q");
                    System.out.println("show ip ospf interface vlanw");
//                    if (interfaceName.matches("\\d+")) {//インターフェース指定の時
                    if(false){//一時的な処理
                        System.out.println("show ip ospf interface vlan e");
                        // 数字の場合、int型に変換
                        interfaceNumber = Integer.parseInt(interfaceName);

//                    System.out.println("qqq");
                        if (config == null) {
                            textarea.append("正しくConfigインスタンスの名前を入力してください");
                        } else {
//                        System.out.println("qqq");
                            EthernetSetting ethernetSetting = new EthernetSetting();
                            for(EthernetSetting eth : config.getEthernetSetting()){
                                if(eth.getPort()==interfaceNumber){
                                    ethernetSetting =eth;
                                }
                            }
//                        System.out.println("111");
                            HashMap<Integer , OspfInterfaceSetting>  ospfInterfaceSettingArrrayList = new HashMap<>();
//                        System.out.println("111");
                            ospfInterfaceSettingArrrayList = Check.getOspfSettingInformation(ethernetSetting);
//                        System.out.println("111");
                            for(EthernetSetting eth : config.getEthernetSetting()){
                                if(interfaceNumber==eth.getPort()){
                                    ethernetSetting = eth;
                                    break;
                                }
                            }
//                        System.out.println("qqqiii");
//                        System.out.println(ospfInterfaceSettingArrrayList.size());
                            for(Map.Entry<Integer, OspfInterfaceSetting> entry : ospfInterfaceSettingArrrayList.entrySet()) {
                                OspfInterfaceSetting ospfInterfaceSetting = entry.getValue();
//                            System.out.println("ttt");
                                textarea.append("Interface: Port" + interfaceNumber + "\n");
                                String[] parts = ospfInterfaceSetting.getWildcardMask().split("\\.");
//                            System.out.println("ttt");
                                int subnetMaskBits = 0;

                                for (String part : parts) {
                                    int octet = Integer.parseInt(part);
                                    subnetMaskBits += (255 - octet); // 255から引いた値を加算
                                }

//                            System.out.println("qqq");
                                String sub = "/"+(32 - subnetMaskBits);
                                HashMap<String,HashMap<Config,Config>> drbdr = new HashMap<>();
                                drbdr = getDrBdr(config);
                                for(Map.Entry<String,HashMap<Config,Config>> ent : drbdr.entrySet()){
                                    textarea.append(ent.getKey()+"-");
                                    for(Map.Entry<Config,Config> en : ent.getValue().entrySet()){
                                        textarea.append("DR:"+en.getKey().getName()+" BDR:"+en.getValue().getName() +"\n");
                                    }
                                }
//                            System.out.println("qqq");
//                            textarea.append("  Internet address is "+ospfInterfaceSetting.getIpAddress()+sub+"\n");
//                            textarea.append("  OSPF area "+ospfInterfaceSetting.getAreaId()+"\n");
//                            textarea.append("  Process ID "+ospfInterfaceSetting.getOspfSetting().getProcessId()+"\n");
//                            textarea.append("  State is DR\n");
//                            textarea.append("  Cost: 10\n");
//                            textarea.append("  Transmit Delay: 5 sec\n");
//                            textarea.append("  Last State Change: 00:00:05\n");
//                            textarea.append("  Input Bandwidth: 1000000 Kbit\n");
//                            textarea.append("  Output Bandwidth: 1000000 Kbit\n");
//                            textarea.append("  Neighbor Count: 2\n");
//                            textarea.append("\n"); // 空行を追加
//                            textarea.append("Interface: FastEthernet0/1\n");
//                            textarea.append("  Internet address is 192.168.2.1/24\n");
//                            textarea.append("  OSPF area 0.0.0.1\n");
//                            textarea.append("  Process ID 1\n");
//                            textarea.append("  State is BDR\n");
//                            textarea.append("  Cost: 20\n");
//                            textarea.append("  Transmit Delay: 5 sec\n");
//                            textarea.append("  Last State Change: 00:00:10\n");
//                            textarea.append("  Input Bandwidth: 1000000 Kbit\n");
//                            textarea.append("  Output Bandwidth: 1000000 Kbit\n");
//                            textarea.append("  Neighbor Count: 1\n");
                            }
                        }
                    }else{//VLAN指定の時
                        System.out.println("show ip ospf interface vlan -1");
                        String numberStr = interfaceName.replaceAll("\\D+", ""); // 数字以外の文字を削除
                        // 抽出した文字列をint型に変換
                        int vlanNumber = Integer.parseInt(numberStr);
                        VlanSetting vlanSetting = new VlanSetting();
                        for(VlanSetting vlanSettings : config.getVlanSetting()){
                            if(vlanSettings.getVlanNum()==vlanNumber){
                                vlanSetting = vlanSettings;
                            }
                        }
                        System.out.println("show ip ospf interface vlan -2");
                        OspfInterfaceSetting ospfInterfaceSetting = Check.getSameNetworkOspfInterfaceSetting(vlanSetting.getIpAddress(),config.getOspfSetting());
                        System.out.println("show ip ospf interface vlan -3");
                        OspfSetting ospfSetting = ospfInterfaceSetting.getOspfSetting();
                        System.out.println("show ip ospf interface vlan -4");
                        String[] parts = ospfInterfaceSetting.getWildcardMask().split("\\.");
//                            System.out.println("ttt");
                        int subnetMaskBits = 0;

                        for (String part : parts) {
                            int octet = Integer.parseInt(part);
//                            int invertedOctet = 255 - octet; // 反転処理
                            subnetMaskBits += Integer.bitCount(octet); // 反転したオクテットのビット数を加算
                        }
                        System.out.println("show ip ospf interface vlan -5");
                        String sub = "/"+(32 - subnetMaskBits);
                        textarea.append("Vlan"+vlanNumber+"\n");
                        textarea.append("  Internet Address"+ vlanSetting.getIpAddress()+sub+" Area"+ospfInterfaceSetting.getAreaId()+"\n");
                        textarea.append("  Process ID "+ospfSetting.getProcessId()+", Router ID"+ ospfSetting.getRouterId()+" Network Type BROADCAST, Cost: 1\n");
//                        textarea.append("  Enabled by interface config, including secondary ip addresses\n");
//                        textarea.append("  Transmit Delay is 1 sec, State DR, Priority 1\n");
                        HashMap<String, HashMap<Config, Config>> drbdr = getDrBdr(config);
                        String DRDBR = null;
                        Config drConfig = new Config();
                        Config bdrConfig = new Config();
                        for (Map.Entry<String, HashMap<Config, Config>> out : drbdr.entrySet()) {
//                                        for (Map.Entry<Config, Config> ds: out.getValue().entrySet()) {
//                                            System.out.println(out.getKey() + "のDRDBRは" +ds.getKey().getName()+ds.getValue().getName());
//                                        }
                            if (calculateNetworkAddress(ospfInterfaceSetting.getIpAddress(), ospfInterfaceSetting.getWildcardMask()).equals(out.getKey())) {
                                HashMap<Config, Config> drbdrs = out.getValue();
                                for (Map.Entry<Config, Config> d : drbdrs.entrySet()) {
//                                                textarea.append(d.getKey().getName()+d.getValue().getName());
                                    if (d.getKey() == config) {
                                        DRDBR = "DR";
                                    }
                                    if (d.getValue() == config) {
                                        DRDBR = "BDR";
                                    }
                                    drConfig = d.getKey();
                                    bdrConfig = d.getValue();
                                }
                            }
                        }
                        VlanSetting drvlanSetting = new VlanSetting();
                        for(VlanSetting vlanSettings : drConfig.getVlanSetting()){
                            if(vlanSettings.getVlanNum()==vlanNumber){
                                drvlanSetting = vlanSettings;
                            }
                        }
                        VlanSetting bdrvlanSetting = new VlanSetting();
                        for(VlanSetting vlanSettings : bdrConfig.getVlanSetting()){
                            if(vlanSettings.getVlanNum()==vlanNumber){
                                bdrvlanSetting = vlanSettings;
                            }
                        }
                        OspfInterfaceSetting drospf = Check.getSameNetworkOspfInterfaceSetting(vlanSetting.getIpAddress(),drConfig.getOspfSetting());
                        OspfInterfaceSetting bdrospf = Check.getSameNetworkOspfInterfaceSetting(vlanSetting.getIpAddress(),bdrConfig.getOspfSetting());
                        textarea.append("  State "+DRDBR+" Priority"+ ospfInterfaceSetting.getPriority()+"\n");
                        textarea.append("  Designated Router (ID) "+drConfig.getOspfSetting().getRouterId()+", Interface address"+drvlanSetting.getIpAddress()+"\n");
                        textarea.append("  Backup Designated router (ID) "+bdrConfig.getOspfSetting().getRouterId()+", Interface address"+bdrvlanSetting.getIpAddress()+"\n");
                        textarea.append("  Timer intervals configured, Hello "+ospfInterfaceSetting.getHelloInterval()+", Dead "+ospfInterfaceSetting.getDeadInterval()+"\n");
                        int neighborCount = 0;
                        int neighborCoun2 = 0;
                        ArrayList<Config > configs1 = new ArrayList<>();
                        Map<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> a =ospfNeighbor(config);
                        HashMap<EthernetSetting, OspfInterfaceSetting> e = a.get(ospfInterfaceSetting.getAreaId());
                        for(EthernetSetting ethernetSetting : config.getEthernetSetting()){
                            if(Check.getEhternetSettingVlans(ethernetSetting).contains(vlanNumber)){
                                neighborCount+=1;
                                if(ethernetSetting.getConectedThing() instanceof EthernetSetting){
                                    EthernetSetting es = (EthernetSetting) ethernetSetting.getConectedThing();
                                    configs1.add(es.getConfig());
                                }

                                if(e!=null ){
                                    for(Map.Entry<EthernetSetting,OspfInterfaceSetting> entry : e.entrySet()){
                                        if(entry.getKey().equals(ethernetSetting)){
                                            neighborCoun2+=1;
                                        }
                                    }
                                }
                            }
                        }

                        textarea.append("  Neighbor Count is "+neighborCount+", Adjacent neighbor count is"+neighborCoun2+"\n");
                        for(Config config1 : configs1){
                            if(drConfig.equals(config1)){
                                textarea.append("    Adjacent with neighbor "+config1.getOspfSetting().getRouterId()+"  (Designated Router)\n");
                            } else if (bdrConfig.equals(config1)) {
                                textarea.append("    Adjacent with neighbor "+config1.getOspfSetting().getRouterId()+"  (Backup Designated Router)\n");

                            }else{
                                textarea.append("    Adjacent with neighbor "+config1.getOspfSetting().getRouterId()+" \n");

                            }
                        }
                    }

//

                }
                //show ip ospf R1
                if (input.matches("^show ip ospf .*") && !input.matches(("^show ip ospf neighbor .*"))  && !input.matches(("^show ip ospf neighbor .*")) &&!input.matches(("^show ip ospf interface .*") )){
                    createTab = true;
                    name = "ospf " ;//tabの名前
                    String configName = input.substring(13);
                    Config config = new Config();//情報を出力するコンフィグ
                    for (ClassElement instance : instances) {
                        if (instance instanceof Config) {
                            if (instance.getName().equals(configName)) {
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    if (config == null) {
                        textarea.append("正しくConfigインスタンスの名前を入力してください");
                    } else {
                        OspfSetting ospfSetting = config.getOspfSetting();
                        if(ospfSetting==null){
                            textarea.append((config.getName()+"のOspfSettingインスタンスがありません"));
                        }
                        Integer processID = null;//プロセスID
                        processID = ospfSetting.getProcessId();
                        String routerID = null;//ルーターID
                        routerID = ospfSetting.getRouterId();
                        ospfNeighbor(config);
                        HashSet<Integer> abrHantei = new HashSet<>();
                        Integer normal = 0;
                        Integer stub = 0;
                        Integer nssa = 0;
                        boolean area0kadouka = false;//エリア0につながっているかのチェック
                        ArrayList<OspfSetting> ospfSettings = new ArrayList<>();//図に存在するOspfSettingインスタンスの全て
                        for(ClassElement classElement : instances){//area0Checkのためだけのもの
                            if(classElement instanceof  OspfSetting){
                                ospfSettings.add((OspfSetting) classElement);
                            }
                        }
                        for(OspfInterfaceSetting areas : config.getOspfSetting().getOspfInterfaceSettings()) {
                            abrHantei.add(areas.getAreaId());
                            if(areas.getStub().equals("stub")){
                                stub+=1;
                            }else if (areas.getStub().equals("nssa")){
                                nssa +=1;
                            }else{
                                normal +=1;
                            }
                        }
                        int area = 0;

                        for(OspfInterfaceSetting areas : config.getOspfSetting().getOspfInterfaceSettings()){
                            if(areas.getAreaId()==0){
                                area0kadouka=true;
                                area = areas.getAreaId();
                                break;
                            }
                        }
                        ArrayList<String> w = new ArrayList<>();
                        if(area0kadouka) {
                            area0kadouka = Check.area0Check(area, ospfSettings,w);
                        }
                        if(!abrHantei.contains(0)){//エリア０代わり当てられていないが、エリア０につながっている場合
                            normal+=1;
                        }

                            spfTreeCost(config,textarea);
                        textarea.append("Routing Process \"ospf "+processID+"\" with ID "+routerID+"\n");
//                        textarea.append("Start time: 00:54:30.048, Time elapsed: 04:15:53.160\n");
//                        textarea.append("Supports only single TOS(TOS0) routes\n");
//                        textarea.append("Supports opaque LSA\n");
//                        textarea.append("Supports Link-local Signaling (LLS)\n");
//                        textarea.append("Supports area transit capability\n");
                        if(abrHantei.size()!=1){
                            textarea.append("  It is an area border router"+"\n");//ABRかどうかによって変わる
                        }
//                        textarea.append("Router is not originating router-LSAs with maximum metric\n");
//                        textarea.append("Initial SPF schedule delay 5000 msecs\n");
//                        textarea.append("Minimum hold time between two consecutive SPFs 10000 msecs\n");
//                        textarea.append("Maximum wait time between two consecutive SPFs 10000 msecs\n");
//                        textarea.append("Incremental-SPF disabled\n");
//                        textarea.append("Minimum LSA interval 5 secs\n");
//                        textarea.append("Minimum LSA arrival 1000 msecs\n");
//                        textarea.append("LSA group pacing timer 240 secs\n");
//                        textarea.append("Interface flood pacing timer 33 msecs\n");
//                        textarea.append("Retransmission pacing timer 66 msecs\n");
//                        textarea.append("Number of external LSA 0. Checksum Sum 0x000000\n");
//                        textarea.append("Number of opaque AS LSA 0. Checksum Sum 0x000000\n");
//                        textarea.append("Number of DCbitless external and opaque AS LSA 0\n");
//                        textarea.append("Number of DoNotAge external and opaque AS LSA 0\n");
                        int count = abrHantei.size()+1;
                        textarea.append("  Number of areas in this router is "+count+". "+normal+" normal "+stub+" stub "+nssa+" nssa\n");//ルータが属しているエリアの数　「通常のエリア」、「stubエリア」、「nssa」
//                        textarea.append("Number of areas transit capable is 2\n");
//                        textarea.append("External flood list length 0\n");
//                        textarea.append("IETF NSF helper support enabled\n");
//                        textarea.append("Cisco NSF helper support enabled\n");

//                        if(area0kadouka){//エリア０に接続されているとき
                        if(true){
                            textarea.append("Area BACKBONE(0)\n");
                            int areaCount = 0;
                            areaCount = config.getOspfSetting().getOspfInterfaceSettings().size();
                            textarea.append("Number of interfaces in this area is "+areaCount+"\n");
//                            textarea.append("Area has no authentication\n");
//                            textarea.append("SPF algorithm last executed 02:12:20.364 ago\n");
//                            textarea.append("SPF algorithm executed 9 times\n");
//                            textarea.append("Area ranges are\n");
//                            textarea.append("Number of LSA 26. Checksum Sum 0x0E2A25\n");
//                            textarea.append("Number of opaque link LSA 0. Checksum Sum 0x000000\n");
//                            textarea.append("Number of DCbitless LSA 0\n");
//                            textarea.append("Number of indication LSA 0\n");
//                            textarea.append("Number of DoNotAge LSA 23\n");
//                            textarea.append("Flood list length 0\n");
                        }
                        System.out.println("abrHantei"+abrHantei);
                        for(Integer areaN : abrHantei){

                            OspfInterfaceSetting ospfInterfaceSetting = new OspfInterfaceSetting();
                            if(areaN==0) break;
                            for(OspfInterfaceSetting os : config.getOspfSetting().getOspfInterfaceSettings()){
                                if(os.getAreaId()==areaN){
                                    ospfInterfaceSetting=os;
                                    break;
                                }
                            }

                            textarea.append("Area "+ospfInterfaceSetting.getAreaId()+"\n");
                            int areaCount = 0;
                            for(OspfInterfaceSetting ospfInterfaceSetting1 : config.getOspfSetting().getOspfInterfaceSettings()){
                                if(ospfInterfaceSetting1.getAreaId()==areaN){
                                    areaCount+=1;
                                }
                            }
                            textarea.append("    Number of interfaces in this area is "+areaCount+"\n");
                            if(config.getOspfSetting().getOspfVirtualLinks()!=null){
                                textarea.append("    This area has transit capability: Virtual Link Endpoint\n");
                            }
//
//                            textarea.append("    Area has no authentication\n");
//                            textarea.append("    SPF algorithm last executed 03:16:45.256 ago\n");
//                            textarea.append("    SPF algorithm executed 5 times\n");
//                            textarea.append("    Area ranges are\n");
//                            textarea.append("    Number of LSA 12. Checksum Sum 0x05BF2A\n");
//                            textarea.append("    Number of opaque link LSA 0. Checksum Sum 0x000000\n");
//                            textarea.append("    Number of DCbitless LSA 0\n");
//                            textarea.append("    Number of indication LSA 0\n");
//                            textarea.append("    Number of DoNotAge LSA 0\n");
//                            textarea.append("    Flood list length 0\n");
                        }

                    }

                }

                if (input.matches("^show access-lists .*")) {//入力[show access-lists Cf1}  シーケンス番号のソートとhostの扱いのみ未定
                    createTab = true;
                    String configName = input.substring(18);//vlanの番号
                    name = "access-lists"+configName;//tabの名前
                    Config config = null;
                    for(ClassElement instance : instances){
                        if(instance instanceof Config){
                            if(instance.getName().equals(configName)){
                                config = (Config) instance;
                                break;
                            }
                        }
                    }
                    if(config == null){
                        textarea.append("正しくConfigインスタンスの名前を入力してください");
                    }else {
                        //順番　標準番号→標準名前→拡張番号→拡張名前
                        ArrayList<AccessList> standardNumberList = new ArrayList<>();//標準番号
                        ArrayList<AccessList> standardNameList = new ArrayList<>();//標準名前
                        ArrayList<AccessList> extendNumberList = new ArrayList<>();//拡張番号
                        ArrayList<AccessList> extendNameList = new ArrayList<>();//拡張名前
                        //AccessListインスタンスを上のリストに当てはめていく
                        for (ClassElement instance : instances){
                            if(instance instanceof AccessList){
                                AccessList acc = (AccessList) instance;
                                //シーケンス番号の確認
                                if(acc.getSequenceNumber()==-1){
                                    textarea.append(acc.getName()+"のsequenceNumberを入力してください");
                                }

                                //拡張番号の時
                                if(acc.getAccessListNumber()!=-1 && !acc.getPermitOrDeny().equals("") && !acc.getSorceIpAddress().equals("") && !acc.getProtocol().equals("") && !acc.getDestIpAddress().equals("")){
                                    extendNumberList.add(acc);
                                }
                                //標準番号の時
                                else if(acc.getAccessListNumber()!=-1 && !acc.getPermitOrDeny().equals("") && !acc.getSorceIpAddress().equals("")){
                                    standardNumberList.add(acc);
                                }
                                //拡張名前の時
                                else if(!acc.getAccessListName().equals("") && !acc.getPermitOrDeny().equals("") && !acc.getSorceIpAddress().equals("") && !acc.getProtocol().equals("") && !acc.getDestIpAddress().equals("")){
                                    extendNameList.add(acc);
                                }
                                //標準名前の時
                                else if(!acc.getAccessListName().equals("") && !acc.getPermitOrDeny().equals("") && !acc.getSorceIpAddress().equals("")){
                                    standardNameList.add(acc);
                                }else{
                                    textarea.append(acc.getName()+"の属性が正しく設定されていない可能性があります\n");
                                }
                            }
                        }
                        //ソート
                        Collections.sort(standardNumberList, Comparator.comparingInt(a -> a.getAccessListNumber()));
//                        Standard IP access list 1
//                        10 permit 192.168.0.0, wildcard bits 0.0.0.255
//                        20 permit 192.122.0.0, wildcard bits 0.0.0.255
//                        Standard IP access list 2
//                        10 deny   192.168.0.1
//                        Standard IP access list 40
//                        10 deny   192.166.0.0, wildcard bits 0.0.0.255
//                        Standard IP access list daw
//                        10 permit 192.144.0.0, wildcard bits 0.0.0.255
//                        Standard IP access list hos
//                        10 permit 192.167.0.0, wildcard bits 0.0.0.255
//                        Extended IP access list 100
//                        10 permit ip host 192.168.1.1 host 10.1.1.1
//                        Extended IP access list TELNET
//                        10 deny tcp host 192.168.0.1 host 10.1.1.1 eq telnet
//                        Extended IP access list hos2

                        //番号と名前でグループ化
                        Map<Integer, List<AccessList>> groupstandardNumberList = standardNumberList.stream()
                                .collect(Collectors.groupingBy(a -> a.getAccessListNumber()));
                        Map<String, List<AccessList>> groupstandardNameList = standardNameList.stream()
                                .collect(Collectors.groupingBy(a -> a.getAccessListName()));
                        Map<Integer, List<AccessList>> groupextendNumberList = extendNumberList.stream()
                                .collect(Collectors.groupingBy(a -> a.getAccessListNumber()));
                        Map<String, List<AccessList>> groupextendNameList = extendNameList.stream()
                                .collect(Collectors.groupingBy(a -> a.getAccessListName()));

                        //AccessList情報出力 シーケンス番号は入力必須
                        if(standardNumberList.size()!=0){//標準番号の時
                            for (Map.Entry<Integer, List<AccessList>> entry : groupstandardNumberList.entrySet()) {
                                int num = entry.getKey();
                                int seqNum =10;
                                List<AccessList> accessListsinfo = entry.getValue();
                                textarea.append("Standard IP access list "+num+"\n");
                                Collections.sort(accessListsinfo, Comparator.comparingInt(a -> a.getSequenceNumber()));
                                for(AccessList acc : accessListsinfo){
                                    textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" "+acc.getSorceIpAddress()+", wildcard bits "+acc.getSourceWildcardMask()+"\n");
                                    seqNum+=10;
                                }
                            }


                        }
                        if(standardNameList.size()!=0){//標準名前の時
                            for (Map.Entry<String, List<AccessList>> entry : groupstandardNameList.entrySet()) {
                                String names = entry.getKey();
                                int seqNum =10;
                                List<AccessList> accessListsinfo = entry.getValue();
                                Collections.sort(accessListsinfo, Comparator.comparingInt(a -> a.getSequenceNumber()));
                                textarea.append("Standard IP access list "+names+"\n");
                                Collections.sort(accessListsinfo, Comparator.comparingInt(a -> a.getSequenceNumber()));
                                for(AccessList acc : accessListsinfo){
                                    textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" "+acc.getSorceIpAddress()+", wildcard bits "+acc.getSourceWildcardMask()+"\n");
                                    seqNum+=10;
                                }
                            }
                        }
//
                        if(extendNumberList.size()!=0){//拡張番号の時
                            for (Map.Entry<Integer, List<AccessList>> entry : groupextendNumberList.entrySet()) {
                                int num = entry.getKey();
                                int seqNum =10;
                                List<AccessList> accessListsinfo = entry.getValue();
                                textarea.append("Extend IP access list "+num+"\n");
                                Collections.sort(accessListsinfo, Comparator.comparingInt(a -> a.getSequenceNumber()));

                                for(AccessList acc : accessListsinfo){

                                    if(acc.getDestOperator()==null){
                                        if(acc.getSourceIsHost()==false  && acc.getDestIsHost()==false ){//host表記対応用
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" "+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+"\n");
                                        }else if(acc.getSourceIsHost()==true&& acc.getDestIsHost()==false){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+"\n");
                                        }else if(acc.getDestIsHost()==false&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+"\n");
                                        }else if(acc.getDestIsHost()==true&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+"\n");
                                        }
                                    }else{
                                        if(acc.getSourceIsHost()==false  && acc.getDestIsHost()==false){//host表記対応用
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" "+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }else if(acc.getSourceIsHost()==true&& acc.getDestIsHost()==false){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }else if(acc.getDestIsHost()==false&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }else if(acc.getDestIsHost()==true&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }
                                    }
//                                    seqNum+=10;
                                }
                            }
                        }
                        if(extendNameList.size()!=0){//拡張名前の時
                            for (Map.Entry<String, List<AccessList>> entry : groupextendNameList.entrySet()) {
                                String names = entry.getKey();
                                int seqNum =10;
                                List<AccessList> accessListsinfo = entry.getValue();
                                Collections.sort(accessListsinfo, Comparator.comparingInt(a -> a.getSequenceNumber()));
                                textarea.append("Extend IP access list "+names+"\n");
                                for(AccessList acc : accessListsinfo){
                                    if(acc.getDestOperator()==null){
                                        if(acc.getSourceIsHost()==false  && acc.getDestIsHost()==false){//host表記対応用
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" "+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+"\n");
                                        }else if(acc.getSourceIsHost()==true&& acc.getDestIsHost()==false){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+"\n");
                                        }else if(acc.getDestIsHost()==false&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+"\n");
                                        }else if(acc.getDestIsHost()==true&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+"\n");
                                        }
                                    }else{
                                        if(acc.getSourceIsHost()==false  && acc.getDestIsHost()==false){//host表記対応用
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" "+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }else if(acc.getSourceIsHost()==true&& acc.getDestIsHost()==false){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+acc.getDestIpAddress()+" "+acc.getDestWildcardMask()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }else if(acc.getDestIsHost()==false&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }else if(acc.getDestIsHost()==true&& acc.getDestIsHost()==true){
                                            textarea.append("   "+seqNum+" "+acc.getPermitOrDeny()+" ip host "+acc.getSorceIpAddress()+" "+acc.getSourceWildcardMask()+" host "+acc.getDestIpAddress()+" "+acc.getDestOperator()+" "+acc.getDestPort()+"\n");
                                        }                                    }
//                                    seqNum+=10;
                                }
                            }
                        }
                    }
                }

                if(input.matches("help")){
                    createTab = true;
                    name = "command " ;//tabの名前
                    textarea.append("show vlan brief <Configインスタンス名>\n");
                    textarea.append("   ConfigのVLAN情報が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show vlan all\n");
                    textarea.append("   ネットワーク全体のVLAN情報が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show vlan　<vlanID>\n");
                    textarea.append("   <vlanID>が割り当てられてるConfigとポートのIDが出力されます\n");
                    textarea.append("\n");
                    textarea.append("show running-config <Configインスタンス名>\n");
                    textarea.append("   Configの設定情報が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show spanning-tree　<Configインスタンス名>\n");
                    textarea.append("   STPの情報が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show ip protocols　<Configインスタンス名>\n");
                    textarea.append("   ルーティングプロトコルのステータスが出力されます\n");
                    textarea.append("\n");
                    textarea.append("show ip ospf neighbor　<Configインスタンス名>\n");
                    textarea.append("   隣接関係が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show ip ospf interface <Configインスタンス名>　vlan<vlanID>   例：show ip ospf interface Cf3 vlan30\n");
                    textarea.append("   インターフェイスごとのOSPFの情報が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show ip ospf <Configインスタンス名>\n");
                    textarea.append("   OSPFプロセスの全般的な情報が出力されます\n");
                    textarea.append("\n");
                    textarea.append("show access-lists <Configインスタンス名>\n");
                    textarea.append("   コンフィグに設定したアクセスリストの情報が出力されます\n");
                }

                textarea.append("終了");
                transactionManager.endTransaction();//トランザクションの終了
            } catch (Exception e) {

                transactionManager.abortTransaction();//トランザクションの例外処理

            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ProjectNotFoundException e) {
            String message = "Please open a project";

        } catch (InvalidUsingException e) {
            throw new RuntimeException(e);
        }

        textarea.setEditable(false);//テキストエリアを編集不可能にする
        JScrollPane pane = new JScrollPane(textarea);//スクロールできるようにして型変換も行う
        tab.add(pane, BorderLayout.CENTER);//テキストエリアをtabのパネルに追加する

        //ここからタブの削除ボタン追加の処理
        JPanel p1 = new JPanel(new GridLayout(1, 1, 5, 5));
        p1.setOpaque(false);
        p1.add(new JLabel(name), BorderLayout.WEST);
        JButton b1 = new JButton("X");
        String finalName = name;
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int tabIndex = tabbedPane.indexOfTab(finalName);
                if (tabIndex != -1) {
                    tabbedPane.removeTabAt(tabIndex);
                }
            }
        });
        b1.setMargin(new Insets(0, 5, 0, 5));
        p1.add(b1, BorderLayout.EAST);

        //ここまで

        if (createTab) {//createTabがtureのとき(入力が適切なとき)
            tabbedPane.addTab(name, tab);//astahにタブを追加する
            tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, p1);//タブに削除ボタンを追加する
        }

    }

    public static ArrayList<String> recordBeforeInstanceColar(ArrayList<IPresentation> presentations) {//最初のインスタンスの色を記憶しておくための物
        ArrayList<String> firstPresenttationColor = new ArrayList<>();
        for (IPresentation presentation : presentations) {
            if (presentation instanceof INodePresentation) {//インスタンスの名前、スロットの処理
                IElement model = presentation.getModel();
                if (model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
                    com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;
                    firstPresenttationColor.add(presentation.getProperty(PresentationPropertyConstants.Key.FILL_COLOR));
                }
            }
        }
        return firstPresenttationColor;
    }

    public static Integer keisanPasCost(String string){
        if(string.equals("10")){
            return 100;
        }if(string.equals("100")){
            return 19;
        }if(string.equals("1000")){
            return 4 ;
        }if(string.equals("auto")){
            return 4;
        }
        return 0;
    }
//    OSPFのコストを計算するメソッド
//    各線（EhternetTyep）と両側のルータのスピード設定の最小値を採用
    public static Integer keisanOspfCost(EthernetType ethernetType1,EthernetType ethernetType2,String speed1,String speed2){
        String ethernet = "";
//        System.out.println("keisandekiai-1");
        if(ethernetType1!=null){
            ethernet = ethernetType1.getType();
        } else if (ethernetType2 != null) {
            ethernet = ethernetType2.getType();
        }
        if(ethernet.equals("") && speed1==null && speed2==null){
            return null;
        }
//        System.out.println("keisandekiai-3");
//        System.out.println("keisandekiai-4");
        Integer maxSpeed = 0;
        if(ethernet.equals("Ethernet")){
            maxSpeed = 10;
        }if(ethernet.equals("fastEthernet")){
            maxSpeed = 100;
        }if(ethernet.equals("10gigabitEthernet")){
            maxSpeed = 1000;
        }if(ethernet.equals("gigabitEthernet")){
            maxSpeed = 10000;
        }if(ethernet.equals("")){//EthernetTypeが設定されていない場合
            maxSpeed = 10000;
        }
//        System.out.println("keisandekiai-5");
        if(speed1.equals("")){
            speed1 = "auto";
        }
        if(speed2.equals("")){
            speed2 = "auto";
        }
//        System.out.println("keisandekiai-6");
        Integer speed1_int = 0;
        Integer speed2_int = 0;
        if(speed1.equals("auto")) speed1_int = maxSpeed;
        if(speed1.equals("10")) speed1_int = 10;
        if(speed1.equals("100")) speed1_int = 100;
        if(speed1.equals("1000")) speed1_int = 1000;
        if(speed2.equals("auto")) speed2_int = maxSpeed;
        if(speed2.equals("10")) speed2_int = 10;
        if(speed2.equals("100")) speed2_int = 100;
        if(speed2.equals("1000")) speed2_int = 1000;
        Integer taiikihaba =  Math.min(maxSpeed, Math.min(speed1_int, speed2_int));
//        System.out.println("keisandekiai-1");
        if(taiikihaba==10) return 10;
        if(taiikihaba==100) return 1;
        else return 1;


    }



    //引数で指定したコンフィグのネイバーテーブルを返す
    //辺数　 エリア毎のネイバーテーブルのイーサネットとOSPFの設定。
    //イーサネットは調べているコンフィグの対面の物を出力
    // R1-Eht-Eth2-R2 引数:R1  変数：eth
    public  static Map<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>>  ospfNeighbor(Config config) {//show ip protocols R1
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
                    for (Map.Entry<Integer, OspfInterfaceSetting> entry : os.entrySet()) {//VLAN毎にみる
                        if(entry.getValue().getAreaId()!=ar) break;
                        OspfInterfaceSetting ospfSetting = entry.getValue();
                        if (anoteros.get(entry.getKey()) == null) break;
                        OspfInterfaceSetting anoterOspfSetting = anoteros.get(entry.getKey());
                        //ここで対向機器のOspfSettingかつ同一VLANで取り出せたのでこの後、隣接条件の比較
                        //（エリアID、出力インターフェースのサブネット、Helloインターバル、Stub/NSSAのフラグの一致、（認証情報が同じ））

                        if (ospfSetting.getAreaId() != anoterOspfSetting.getAreaId()) break;//エリアIDの一致
                        if (!checkSameNetworkW(ospfSetting.getIpAddress(), anoterOspfSetting.getIpAddress(), ospfSetting.getWildcardMask(), anoterOspfSetting.getWildcardMask()))
                            break;//出力インターフェースのサブネット
                        if (ospfSetting.getHelloInterval() != anoterOspfSetting.getHelloInterval()) break;//Helloインターバル
                        if (ospfSetting.getDeadInterval() != anoterOspfSetting.getDeadInterval()) break;//deadインターバル
                        if (!ospfSetting.getStub().equals(anoterOspfSetting.getStub())) break;

                        //条件を満たした場合
                        neighbor.put(eth, anoterOspfSetting);
//                        System.out.println("anotherEth" + anotherEth.getName());
//                        System.out.println("anotherOspfSetting" + anoterOspfSetting.getName());
//                        System.out.println("ospfneightor実行－5");
                    }
                }
            }
//            System.out.println("ospfneightor実行－6");
//            System.out.println("ar" + ar);
            areaNeighbors.put(ar,neighbor);
//            System.out.println("ospfneightor実行－7");

        }
        return areaNeighbors;
    }

    public static void dfsOspf(Config config) {
        if (config == null) {
            return;
        }

        for (Config anothercf : config.getLinkedConfigs()) {
            if (anothercf.getOspfSetting() == null) {//Ospf関連の設定がなかったとき
                break;
            }


            for (Config conf : config.getLinkedConfigs()) {
                dfsOspf(conf);
            }
        }
    }
//    SPFツリーを作成するメソッド　ダイクストラ法を利用 VLANは考慮していない　同じエリア  Textareaは消しても大丈夫
    public static ArrayList<SpfTreeResult> spfTreeCost(Config config,TextArea textArea){//show ip ospf R1
//        textArea.append("実行一回目\n");
        ArrayList<SpfTreeResult> results = new ArrayList<>();
                //configのネイバーテーブルを取得
        Map<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> configNeighbor = ospfNeighbor(config);
        //configのLSDBを取得
        HashMap<Integer,Set<Config>> lsdb = LSDBList(config);


        //エリア毎にコストの計算を行う。
        for (Map.Entry<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> outerEntry : configNeighbor.entrySet()) {
            Map<String, List<Config>> nextHopMap = new HashMap<>();// ネットワークアドレスとそのネクストホップルーターを保持するマップ
            HashMap<String,Integer> costs = new HashMap<>();
            HashMap<EthernetSetting,OspfInterfaceSetting> neighbor = outerEntry.getValue();
//            for(Map.Entry<EthernetSetting, OspfInterfaceSetting> entry : neighbor.entrySet()) {
//                EthernetSetting ethernetSetting = entry.getKey();
//                textArea.append(ethernetSetting.getConfig().getName());
//            }
            Integer area = outerEntry.getKey(); //エリア
//            textArea.append("area "+area+" \n");
            Set<Config> lsdbTable = new LinkedHashSet<>();

            for(Map.Entry<Integer,Set<Config>> etn : lsdb.entrySet()){
                if(area==etn.getKey()){
                    lsdbTable = etn.getValue();
                    break;
                }
            }


            // 全ネットワークアドレスの最小コストを保持するマップ
            HashMap<String, Integer> networkCostMap = new HashMap<>();
            // 優先度付きキュー（最小コストから処理する）
            PriorityQueue<OspfNetworkCost> queue = new PriorityQueue<>(Comparator.comparingInt(nc -> nc.cost));

            //ルーターの接続をキューに追加
            for(EthernetSetting connection : config.getEthernetSetting()){
//                HashMap<EthernetSetting,OspfInterfaceSetting> nei = ospfNeighbor()
                //ネイバーテーブル(LSDB)に登録されているコンフィグ（Ethernet）のとき　確立条件よりサブネットの一致　エリアの一致がされている。

                if(connection.getConectedThing() instanceof EthernetSetting) {
                    if (lsdbTable.contains(((EthernetSetting) connection.getConectedThing()).getConfig())) {
                        for (String net : Check.calculateNetworkAddress(connection)) {
                            if (connection.getConectedThing() instanceof EthernetSetting) {
                                Integer cost = keisanOspfCost(connection.getEthernetType(), ((EthernetSetting) connection.getConectedThing()).getEthernetType(), connection.getSpeed(), ((EthernetSetting) connection.getConectedThing()).getSpeed());
                                queue.add(new OspfNetworkCost(net, cost, ((EthernetSetting) connection.getConectedThing()).getConfig(),config));
                                networkCostMap.put(net, cost);
                                nextHopMap.computeIfAbsent(net,k -> new ArrayList<>()).add(((EthernetSetting) connection.getConectedThing()).getConfig());
                            }
                        }

                    }
                }



            }

            //ダイクストラ法のメインループ
            while(!queue.isEmpty()){
                OspfNetworkCost current = queue.poll();
                Config currentRouter = current.connectedRouter;
//                textArea.append(currentRouter.getName());
                int currentCost = current.cost;

                //現在のルーターの接続先を全て処理
                for(EthernetSetting connection : current.connectedRouter.getEthernetSetting()){
                    for(String networkIpaddress : Check.calculateNetworkAddress(connection)){
                        Integer cost = keisanOspfCost(connection.getEthernetType(), ((EthernetSetting) connection.getConectedThing()).getEthernetType(),connection.getSpeed(),((EthernetSetting) connection.getConectedThing()).getSpeed());
                        int newCost = currentCost + cost;

                        //より小さいコストでネットワークに到達できる場合のみ更新
                        if (!networkCostMap.containsKey(networkIpaddress) || newCost < networkCostMap.get(networkIpaddress)) {
                            networkCostMap.put(networkIpaddress, newCost);
                            nextHopMap.put(networkIpaddress, nextHopMap.get(current.networkAddress));
                            if(connection.getConectedThing() instanceof EthernetSetting){
                                queue.add(new OspfNetworkCost(networkIpaddress, newCost, ((EthernetSetting) connection.getConectedThing()).getConfig(),currentRouter));
                            }
                        }
                        else if (newCost == networkCostMap.get(networkIpaddress)) {
                            nextHopMap.get(networkIpaddress).add(nextHopMap.get(current.networkAddress).get(0));
                        }
                    }
                }


            }


//            for (Map.Entry<EthernetSetting, OspfInterfaceSetting> innerEntry : innerMapValue.entrySet()) {
//
//            }
//            for (Map.Entry<String, Integer> entry : networkCostMap.entrySet()) {
//                textArea.append(config.getName()+"から " + entry.getKey() + " までの最小コスト: " + entry.getValue()+"\n");
//            }
//            for (Map.Entry<String, List<Config>> entry : nextHopMap.entrySet()) {
//                textArea.append(entry.getKey()+"までのネクストホップ" + entry.getValue().get(0).getName()+"\n");
//            }
            results.add(new SpfTreeResult(networkCostMap,nextHopMap,area));
        }

        return results;
    }
    public static String calculateNetworkAddress(String ipAddress, String wildcardmask) {
        // IPアドレスとサブネットマスクをドット区切りで分割して配列に変換
        String[] ipOctets = ipAddress.split("\\.");
        String[] maskOctets = wildcardmask.split("\\.");

        // ネットワークアドレスを保存する配列
        int[] networkAddress = new int[4];

        // 各オクテットごとにAND演算を行いネットワークアドレスを計算
        for (int i = 0; i < 4; i++) {
            int wildcard = Integer.parseInt(maskOctets[i]);
            int subnetMask = 255 - wildcard;  // ワイルドカードマスクを反転してサブネットマスクを取得
            networkAddress[i] = Integer.parseInt(ipOctets[i]) & subnetMask;
        }

        // 計算されたネットワークアドレスをドット区切りの文字列に変換して返す
        return String.format("%d.%d.%d.%d", networkAddress[0], networkAddress[1], networkAddress[2], networkAddress[3]);
    }


    public static class SpfTreeResult {
        HashMap<String,Integer> areaCostMap = new HashMap<>();
        Map<String, List<Config>> nextHopMap = new HashMap<>();
        Integer areaId ;


        public HashMap<String, Integer> getAreaCostMap() {
            return areaCostMap;
        }

        public Map<String, List<Config>> getNextHopMap() {
            return nextHopMap;
        }

        public Integer getAreaId() {
            return areaId;
        }

        public SpfTreeResult(HashMap<String, Integer> areaCostMap, Map<String, List<Config>> nextHopMap, Integer areaId) {
            this.areaCostMap = areaCostMap;
            this.nextHopMap = nextHopMap;
            this.areaId = areaId;
        }
    }

//    public static Result spfTreeCost2(Config config){
//
//
//    }

    //OSPFのトポロジテーブルを求めるためのクラス
    //area,LSDBのコンフィグ　が返り値
    public static HashMap<Integer,Set<Config>> LSDBList(Config config){//show ip ospf R1
        HashMap<Integer,Set<Config>> lsdbList = new HashMap<>();
        Map<Integer,HashMap<EthernetSetting,OspfInterfaceSetting>> configNeighbor = ospfNeighbor(config);
        for (Map.Entry<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> outerEntry : configNeighbor.entrySet()) {
            HashMap<EthernetSetting, OspfInterfaceSetting> neighbor = outerEntry.getValue();
            Integer area = outerEntry.getKey(); //エリア
//            System.out.println("area=" + area);
            ArrayList<Config> visitedConfigs = new ArrayList<>();
            visitedConfigs.add(config);
            // BFSで使用するキューと訪問フラグ
            Queue<Config> queue = new LinkedList<>();
            Set<Config> visited = new HashSet<>();

            // 開始ルーターをキューに追加
            queue.add(config);
            visited.add(config);

            // キューが空になるまでBFSを実行
            while (!queue.isEmpty()) {
                Config currentRouter = queue.poll();
//                System.out.println("while config"+ currentRouter.getName());

                Map<Integer,HashMap<EthernetSetting,OspfInterfaceSetting>> cf =ospfNeighbor(currentRouter);
                for (Map.Entry<Integer, HashMap<EthernetSetting, OspfInterfaceSetting>> outerEntry2 : cf.entrySet()) {
                    int areaID = outerEntry2.getKey();
//                    System.out.println("areaID"+ areaID);
                    if(area==areaID) {
//                        System.out.println("area = area ID");
                        // 現在のルーターIDとその隣接ノードを出力
                        for (Map.Entry<EthernetSetting, OspfInterfaceSetting> entry : outerEntry2.getValue().entrySet()) {
                            EthernetSetting ethernet = entry.getKey();
//                            System.out.println("ethernet " + ethernet.getName());
                            if(ethernet.getConectedThing() instanceof EthernetSetting) {
                                if (((EthernetSetting) ethernet.getConectedThing()).getConfig() instanceof Config){
                                    Config anotherConfig = (Config) ((EthernetSetting) ethernet.getConectedThing()).getConfig();
//                                System.out.println("anotherConfig" + anotherConfig.getName());
                                OspfInterfaceSetting ospfInterface = entry.getValue();
                                // 隣接するルーターのIDを取得し、未訪問であればキューに追
                                if (!visited.contains(anotherConfig)) {
                                    queue.add(anotherConfig);
                                    visited.add(anotherConfig);
                                }
                            }
                            }
                        }
                    }


                }
            }
            lsdbList.put(area,visited);
//            System.out.println("実行");
            for(Config cf : visited){
//                System.out.println(cf.getName());
            }
        }
        return lsdbList;
    }

    //DRとBDRを選出するためのメソッド
    public static HashMap<String, HashMap<Config,Config>> getDrBdr(Config config){
        HashMap<String, HashMap<Config,Config>> drBdr = new HashMap<>();

        for(OspfInterfaceSetting ospfInterfaceSetting : config.getOspfSetting().getOspfInterfaceSettings()){
            HashMap<Config,Config> drbdrs = new HashMap<>();
            String ipAddress = ospfInterfaceSetting.getIpAddress();
            String subnetmask = ospfInterfaceSetting.getWildcardMask();

            HashMap<Integer,Set<Config>> lsdbs = LSDBList(config);
            Set<Config> lsdb =lsdbs.get(ospfInterfaceSetting.getAreaId());
            List<OspfInterfaceSetting> drbdrList = new ArrayList<>();

            for(Config conf : lsdb){
                for(OspfInterfaceSetting osp : conf.getOspfSetting().getOspfInterfaceSettings()){
                    if(ospfInterfaceSetting.getAreaId()==osp.getAreaId() && checkSameNetwork(ipAddress,osp.getIpAddress(),subnetmask,osp.getWildcardMask())){
                        drbdrList.add(osp);
                    }
                }
            }
            drbdrList.sort((r1, r2) -> {
                if (r2.getPriority() != r1.getPriority()) {
                    return Integer.compare(r2.getPriority(), r1.getPriority()); // 高い優先度が優先
                } else {
                    return r2.getOspfSetting().getRouterId().compareTo(r1.getOspfSetting().getRouterId()); // IDが大きいほうが優先
                }
            });

//            System.out.println(calculateNetworkAddress(ipAddress,subnetmask));
//            for(OspfInterfaceSetting config1 : drbdrList){
//                System.out.println(config1.getIpAddress());
//                System.out.println(config1.getPriority());
//                System.out.println(config1.getOspfSetting().getRouterId());
//
//            }

            Config dr = drbdrList.get(0).getOspfSetting().getConfig(); //DRは最も優先度が高いルーター
            Config bdr = drbdrList.get(1).getOspfSetting().getConfig();//BDRは次に優先度が高いルーター
            drbdrs.put(dr,bdr);
            drBdr.put(calculateNetworkAddress(ipAddress,subnetmask),drbdrs);
        }
        return drBdr;
    }
    //OSPFのコストを求めるためのクラス
    //ネットワーク毎の属するルーターやネットワーク間のEhternetSettingインスタンスを格納している。

    public static class OspfNetworkCost{
        String networkAddress;
        int cost;
        Config connectedRouter;

        Config nextHopRouter;
        public OspfNetworkCost(String networkAddress, int cost, Config connectedRouter,Config nextHopRouter) {
            this.networkAddress = networkAddress;
            this.cost = cost;
            this.connectedRouter = connectedRouter;
            this.nextHopRouter = nextHopRouter;
        }
//        String network ;//設定されているネットワークアドレス
//        ArrayList<EthernetSetting> ethernetSettings = new ArrayList<>();//ネットワークアドレスの端のEthernetSetting
//        ArrayList<Config> configs = new ArrayList<>();//設定されているネットワークアドレスに属するルーター
//        public String getNetwork() {
//            return network;
//        }
//
//        public void setNetwork(String network) {
//            this.network = network;
//        }
//
//        public void setEthernetSettings(ArrayList<EthernetSetting> ethernetSettings) {
//            this.ethernetSettings = ethernetSettings;
//        }
//
//        public ArrayList<Config> getConfigs() {
//            return configs;
//        }
//
//        public void setConfigs(ArrayList<Config> configs) {
//            this.configs = configs;
//        }
//        public ArrayList<EthernetSetting> getEthernetSettings() {
//            return ethernetSettings;
//        }
//
//        public void setEthernetSettings(EthernetSetting ethernetSetting) {
//            this.ethernetSettings.add(ethernetSetting);
//        }
    }
    //ospf用のメソッド
//    ネットワークアドレス毎にルーターをあつめてかえす
//    public static ArrayList<OspfNetworkCost> ospfPerNetwork(Config config) {
//        Config firstConfig = config;
//
//    }
//
//    public static void dfs(Config config, List<Config> visitedConfig , String networkAddress, String subnetMask) {
//        visitedConfig.add(config);
//
//        for(EthernetSetting conectedEthernetSetting : config.getEthernetSetting()){// ルーターに接続されているすべてのイーサネットを探索
//            for(Integer num : Check.getEhternetSettingVlans(conectedEthernetSetting)) {
//                if (checkSameNetwork(conectedEthernetSetting.getIpAddress(), networkAddress, conectedEthernetSetting.getSubnetMask(), subnetMask)) {// 同じネットワークアドレスのイーサネットのみを探索
//                    if (conectedEthernetSetting.getConectedThing() instanceof Config) {
//                        Config anotherconfig = (Config) conectedEthernetSetting.getConectedThing();
//                        if (!visitedConfig.contains(anotherconfig)) {
//                            dfs(anotherconfig, visitedConfig, networkAddress, subnetMask);
//                        }
//                    }
//                }
//            }
//
//        }
//        // ルーターに接続されているすべてのイーサネットを探索
//        for (Ethernet connectedEthernet : router.connectedEthernets) {
//            // 同じネットワークアドレスのイーサネットのみを探索
//            if (connectedEthernet.networkAddress.equals(networkAddress)) {
//                // そのイーサネットに接続されている他のルーターを探索
//                for (Router connectedRouter : connectedEthernet.connectedRouters) {
//                    if (!visitedRouters.contains(connectedRouter)) {
//                        dfs(connectedRouter, visitedRouters, networkAddress);
//                    }
//                }
//            }
//        }
//        //        if (visitedConfig.contains(config)){//すでに訪問したルーターはスキップ
////            return;
////        }
////
////        visitedConfig.add(config);//ルーターを訪問済みとしてリストに追加
////
////        for(EthernetSetting conectedEthernt : config.getEthernetSetting()){
////            if(conectedEthernt.equals(ethernetSetting)){
////                if(!(visitedConfig.contains(conectedEthernt.getConectedThing())) && conectedEthernt.getConectedThing() instanceof Config){
////                    dfs((Config) conectedEthernt.getConectedThing(),visitedConfig,ethernetSetting);
////                }
////            }
////        }



//    }

    //引数に指定したイーサネットのネットワークアドレスを返す
//    public static HashMap<String,String> getnetworkaddress(EthernetSetting ethernetSetting){
//        for(Integer vlans : get)
//    }

    //ネットワークアドレス（サブネット）が同値かどうかを判断するメソッド trueが同値
    //ワイルドカードマスクをつかったバージョン
    public static boolean checkSameNetworkW(String ipAddress1, String ipAddress2, String wildcardmask1, String wildcardmask2    ){
        // IPアドレスとサブネットマスクをint配列に変換
        String[] ipParts1 = ipAddress1.split("\\.");
        String[] ipParts2 = ipAddress2.split("\\.");
        String[] maskParts1 = wildcardmask1.split("\\.");
        String[] maskParts2 = wildcardmask2.split("\\.");


        int[] ipInt1 = new int[4];
        int[] ipInt2 = new int[4];
        int[] maskInt1 = new int[4];
        int[] maskInt2 = new int[4];

        for (int i = 0; i < 4; i++) {


            ipInt1[i] = Integer.parseInt(ipParts1[i]);
            ipInt2[i] = Integer.parseInt(ipParts2[i]);

            maskInt1[i] = ~Integer.parseInt(maskParts1[i]) & 0xFF;  // 0xFF で符号を調整
            maskInt2[i] = ~Integer.parseInt(maskParts2[i]) & 0xFF;;
        }

        // IPアドレスとサブネットマスクのAND演算を行い、結果を比較
        boolean sameNetwork = true;
        for (int i = 0; i < 4; i++) {
            if ((ipInt1[i] & maskInt1[i]) != (ipInt2[i] & maskInt2[i])) {//and演算をした結果が異なったら違う
                sameNetwork = false;
                break;
            }
        }

        return sameNetwork;
    }
    public static boolean checkSameNetwork(String ipAddress1, String ipAddress2, String subnetMask1, String subnetmask2    ){
        // IPアドレスとサブネットマスクをint配列に変換
        String[] ipParts1 = ipAddress1.split("\\.");
        String[] ipParts2 = ipAddress2.split("\\.");
        String[] maskParts1 = subnetMask1.split("\\.");
        String[] maskParts2 = subnetmask2.split("\\.");


        int[] ipInt1 = new int[4];
        int[] ipInt2 = new int[4];
        int[] maskInt1 = new int[4];
        int[] maskInt2 = new int[4];

        for (int i = 0; i < 4; i++) {


            ipInt1[i] = Integer.parseInt(ipParts1[i]);
            ipInt2[i] = Integer.parseInt(ipParts2[i]);


        }

        // IPアドレスとサブネットマスクのAND演算を行い、結果を比較
        boolean sameNetwork = true;
        for (int i = 0; i < 4; i++) {
            if ((ipInt1[i] & maskInt1[i]) != (ipInt2[i] & maskInt2[i])) {//and演算をした結果が異なったら違う
                sameNetwork = false;
                break;
            }
        }

        return sameNetwork;
    }
    public static boolean checkSameNetwork2(String ipAddress1, String ipAddress2, String wildcardMask, String subnetmask2    ){
        // IPアドレスとサブネットマスクをint配列に変換
        int[] targetIpParts = new int[4];
        int[] baseIpParts = new int[4];
        int[] wildcardMaskParts = new int[4];

        String[] targetParts = ipAddress2.split("\\.");
        String[] baseParts = ipAddress1.split("\\.");
        String[] wildcardParts = wildcardMask.split("\\.");

        for (int i = 0; i < 4; i++) {
            targetIpParts[i] = Integer.parseInt(targetParts[i]);
            baseIpParts[i] = Integer.parseInt(baseParts[i]);
            wildcardMaskParts[i] = Integer.parseInt(wildcardParts[i]);
        }

        // ワイルドカードマスクを使用して、範囲を決定
        for (int i = 0; i < 4; i++) {
            int baseMasked = baseIpParts[i] & ~wildcardMaskParts[i]; // ベースIPからワイルドカードを引く
            int targetMasked = targetIpParts[i] & ~wildcardMaskParts[i]; // ターゲットIPからワイルドカードを引く

            if (baseMasked != targetMasked) {
                return false; // IPアドレスが範囲外の場合
            }
        }

        return true; // IPアドレスが範囲内の場合
//        String[] ipParts1 = ipAddress1.split("\\.");
//        String[] ipParts2 = ipAddress2.split("\\.");
//        String[] maskParts1 = wildcardMask.split("\\.");
//
//
//
//        int[] ipInt1 = new int[4];
//        int[] ipInt2 = new int[4];
//        int[] maskInt1 = new int[4];
//        int[] maskInt2 = new int[4];
//
//        for (int i = 0; i < 4; i++) {
//
//
//            ipInt1[i] = Integer.parseInt(ipParts1[i]);
//            ipInt2[i] = Integer.parseInt(ipParts2[i]);
//
//            maskInt1[i] = ~Integer.parseInt(maskParts1[i]) & 0xFF;
//
//        }
//
//        // IPアドレスとサブネットマスクのAND演算を行い、結果を比較
//        boolean sameNetwork = true;
//        for (int i = 0; i < 4; i++) {
//            if ((ipInt1[i] & maskInt1[i]) != (ipInt2[i] & maskInt2[i])) {//and演算をした結果が異なったら違う
//                sameNetwork = false;
//                break;
//            }
//        }
//
//        return sameNetwork;
    }

}
