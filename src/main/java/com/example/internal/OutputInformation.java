package com.example.internal;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.example.classes.*;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
import com.example.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
public class OutputInformation {
    JTabbedPane tabbedPane = new JTabbedPane();

    public OutputInformation(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public  void createLabelPaneJoho(String input,ArrayList<IPresentation> beforeInstance, ArrayList<String> beforeInstanceColor) throws RuntimeException {//Panelの中身
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
            ArrayList<ClassElement> instances = ChangeClassInformation.changeAllElement(presentations);
            //linkの情報を変換する
            ArrayList<LinkElement> links = ChangeClassInformation.changeLinkInformation(presentations,instances);

            try {//モデル編集のためのトランザクション処理
                transactionManager.beginTransaction();//トランザクションの開始
                /*インスタンスのカラーを初期の状態に戻すための処理
                beforeInstance:runを押した時の一個前のインスタンス
                beforeInstanceColor:beforeInstanceのカラー情報
                presentations:runを押した時点でのインスタンス
                runを押したときのインスタンスとそのカラーをリストに格納して、最初にそのリストを元にbeforeinstanceと同じ
                instanceの色を元に戻し、その状態を新しくbeforeInstanceとbeforeInstanceColorに格納して次につなげる。
                 */
                if(beforeInstance.size()!=0){//一回目は実行しない
                    for(IPresentation presentation :presentations){//現在のpresentationを取得したもの
                        for(int bf=0;bf<beforeInstance.size();bf++){//前のインスタンスと同じところの色を戻すためのループ
                            if(presentation==beforeInstance.get(bf)){//前のインスタンスが存在するとき
                                presentation.setProperty(PresentationPropertyConstants.Key.FILL_COLOR,beforeInstanceColor.get(bf));//前の色（初期の色）を適用させる
                            }
                        }
                    }
                }
                //最後
                beforeInstance.clear();//新しくInstanceを登録するために一旦すべて削除する。
                for(IPresentation presentation:presentations){//インスタンスのみを取り出すためのループ。カラーを設定する処理がインスタンスと線で違うため

                    if (presentation instanceof INodePresentation){//インスタンスの名前、スロットの処理
                        IElement model = presentation.getModel();
                        if(model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification){
                            com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;
                            beforeInstance.add(presentation);//インスタンスの時のみリストに追加する
                        }
                    }
                    if(presentation instanceof ILinkPresentation){//線の時
                        presentation.setProperty(PresentationPropertyConstants.Key.LINE_COLOR,"#000000");
                    }
                }
                beforeInstanceColor = recordBeforeInstanceColar(presentations);
                //ここまで（astahの色戻し処理）
                //ここまで（astahの色戻し処理）
                ArrayList<Config> configs = new ArrayList<>();
                for(ClassElement instance : instances){
                    if(instance instanceof Config){
                        configs.add((Config) instance);
                    }
                }

                //ここから違うクラスの処理に書き換えておく
                if(input.matches("^show vlan brief .*")){//入力[show vlan brief <コンフィグ名>(例：Cf5)]
                    name = input.substring(16);
                    Config conf = new Config();//入力したコンフィグ
                    for(Config con : configs){
                        if(con.getName().equals(name)){
                            createTab=true;
                            conf = con;
                            break;
                        }
                    }//入力したコンフィグ名と名前が一致するコンフィグインスタンスを見つけて格納する


                    ArrayList<Integer> usePort = new ArrayList<>();//VLANが設定されているポートをリストに格納する
                    //VLANが設定されているポートを見つける処理　ここから
                    for(ClassElement cs : instances){
                        if(cs instanceof EthernetSetting){
                            if(((EthernetSetting) cs).getConfig()!=null){
                                if(((EthernetSetting) cs).getConfig().equals(conf)) {
                                    if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                        usePort.add(((EthernetSetting) cs).getPort());
                                    }
                                }
                            }}
                    }
                    //ここまで

                    //出力の形VLAN1の表示まで
                    textarea.append("VLAN Name          Status     Ports                  \n");
                    textarea.append("---- ------------- ---------- --------------------------\n");
                    textarea.append(String.format("%-4s %-13s", "1","default"));
                    textarea.append(String.format("%-13s", " active"));
                    int firstcount = 0;
                    //使用されていないポートをVLAN1に表示する。
                    for(int portNumber = 1; portNumber<13;portNumber++){//ポートの最大数は12と仮定する
                        if(!usePort.contains(portNumber)){
                            if(firstcount!=0) {
                                textarea.append(",");
                            }
                            textarea.append("Po"+portNumber);
                            firstcount++;
                        }
                    }
                    textarea.append("\n");

                    //ここからVLAN1以外の情報出力
                    for(ClassElement ins : instances){
                        if(ins instanceof Vlan){//VLANクラスから情報を抜き出す//VLANクラス（番号）の数だけ繰り返す
                            if(((Vlan) ins).getConfig().equals(conf)){//指定したConfigクラスとつながっているVLANクラス
                                ArrayList<EthernetSetting> ethList = new ArrayList<>(); //EthernetSettingクラスをまとめたリスト(指定したコンフィグクラスとつながっている)
                                ArrayList<Integer> ports = new ArrayList<>();//VLANが設定されているポートをまとめた物
                                ArrayList<String> portnames = new ArrayList<>();
                                int vlanNum = ((Vlan) ins).getNum();//VLAN番号の抽出
                                String vlanName = ((Vlan) ins).getNamd();//VLANーNAME抽出
                                for(ClassElement ethins : instances){
                                    if(ethins instanceof EthernetSetting) {
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

                                for(Integer por : ports){//EthernetTypeを見つけるための処理
                                    for(EthernetSetting eth : ethList){
                                        if(eth.getPort()==por.intValue()){

                                            if(eth.getEthernetType() != null) {
                                                if (eth.getEthernetType().getType().equals("Ethernet")) {
                                                    portnames.add("Et" + por);
                                                } else if (eth.getEthernetType().getType().equals("fastEthernet")) {
                                                    portnames.add("Fa" + por);
                                                } else if (eth.getEthernetType().getType().equals("gigabitEthernet")) {
                                                    portnames.add("Gi" + por);
                                                } else if (eth.getEthernetType().getType().equals("10gigabitEthernet")) {
                                                    portnames.add("10Gi" + por);
                                                }
                                            }else{;
                                                portnames.add("Po" + por);//EthernetTypeが設定されていないとき
                                            }
                                        }
                                    }
                                }
                                //情報の出力
                                if(vlanNum!=-1) {
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
                //ここまで　show vlan brief

                //ここから　show vlan all　すべてのvlan情報
                if(input.matches("^show vlan all")){
                    createTab=true; //tabの作成
                    name = "vlan-all"; // tabの名前
                    ArrayList<Integer> vlanNumbers = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Vlan> vlanInstance = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Config> vlan1Configs = new ArrayList<>();//すべてのVLAN番号

                    for(ClassElement classElement : instances) {//図にあるすべてのVLAN番号の把握
                        if (classElement instanceof Vlan) {
                            if (!vlanNumbers.contains(((Vlan) classElement).getNum())) {
                                vlanNumbers.add(((Vlan) classElement).getNum());
                            }
                            vlanInstance.add((Vlan) classElement);
                        }
                        if(classElement instanceof  Config){
                          vlan1Configs.add((Config) classElement);
                        }
                    }
                    Collections.sort(vlanNumbers);//ポート番号の並び替え
                    //vlanNumberにネットワークに存在するVLAN全てがある状態
                    textarea.append("VLAN Name          Config             Status     Ports                  \n");
                    textarea.append("---- ------------- ----------------   --------   --------------------------\n");

                    //vlna1のコンフィグ出力　ここから
                    for(Config config :vlan1Configs){//すべてConfigについてvlan1はかく
                        textarea.append(String.format("%-4s %-14s", "1", "default"));
                        textarea.append(String.format("%-18s",config.getName()));
                        textarea.append(String.format("%-12s", " active"));

                        ArrayList<Integer> usePort = new ArrayList<>();//VLANが設定されているポートをリストに格納する
                        //VLANが設定されているポートを見つける処理　ここから
                        for(ClassElement cs : instances){
                            if(cs instanceof EthernetSetting){
                                if(((EthernetSetting) cs).getConfig()!=null){
                                    if(((EthernetSetting) cs).getConfig().equals(config)) {
                                        if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                            usePort.add(((EthernetSetting) cs).getPort());
                                        }
                                    }
                                }}
                        }
                        int firstcount = 0;
                        //使用されていないポートをVLAN1に表示する。
                        for(int portNumber = 1; portNumber<13;portNumber++){//ポートの最大数は12と仮定する
                            if(!usePort.contains(portNumber)){
                                if(firstcount!=0) {
                                    textarea.append(",");
                                }
                                textarea.append("Po"+portNumber);
                                firstcount++;
                            }
                        }
                        textarea.append("\n");


                    }

                    textarea.append("\n");
                    for(Integer vlanN : vlanNumbers) {//vlanNが60のときVLAN60に属するコンフィグやポートを出力する
                        if(vlanN != 1){
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
                                textarea.append(String.format("%-4s %-14s", vlanN, vlanI.getNamd()));
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

                if(input.matches("^show vlan .*")){//入力[show vlan <vlanID>(10)] (show vlan allをコピーしているところがあるため，余分な文があるかも）
                    String numbers = input.substring(10);//vlanの番号
                    name = "vlan " + numbers;//tabの名前
                    String rightblue = "#87cefa";
                    ArrayList<Integer> vlanNumbers = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Vlan> vlanInstance = new ArrayList<>();//すべてのVLAN番号
                    ArrayList<Config> vlan1Configs = new ArrayList<>();//すべてのVLAN番号
                    vlanNumbers.add(1);
                    for(ClassElement classElement : instances) {//図にあるすべてのVLAN番号の把握
                        if (classElement instanceof Vlan) {
                            if (!vlanNumbers.contains(((Vlan) classElement).getNum())) {
                                vlanNumbers.add(((Vlan) classElement).getNum());
                            }
                            vlanInstance.add((Vlan) classElement);
                        }
                        if(classElement instanceof  Config){
                            vlan1Configs.add((Config) classElement);
                        }
                    }
                    try{//intに型変換要
                    int vlanN = Integer.parseInt(numbers);
                    if(vlanNumbers.contains(vlanN)){//指定したvlanがモデルに存在しているとき
                        createTab = true;
                    }
                    textarea.append(("Config        Ports    \n"));
                    textarea.append(("------------- -------------------------\n"));
                    if(vlanN == 1){
                        for(Config config :vlan1Configs){//すべてConfigについてvlan1はかく
                            textarea.append(String.format("%-14s", config.getName()));
                            Check.changeColor(config,rightblue);
                            ArrayList<Integer> usePort = new ArrayList<>();//VLANが設定されているポートをリストに格納する
                            //VLANが設定されているポートを見つける処理　ここから
                            for(ClassElement cs : instances){
                                if(cs instanceof EthernetSetting){
                                    if(((EthernetSetting) cs).getConfig()!=null){
                                        if(((EthernetSetting) cs).getConfig().equals(config)) {
                                            if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                                usePort.add(((EthernetSetting) cs).getPort());
                                            }
                                        }
                                    }}
                            }
                            int firstcount = 0;
                            //使用されていないポートをVLAN1に表示する。
                            for(int portNumber = 1; portNumber<13;portNumber++){//ポートの最大数は12と仮定する
                                if(!usePort.contains(portNumber)){
                                    if(firstcount!=0) {
                                        textarea.append(",");
                                    }
                                    textarea.append("Po"+portNumber);
                                    firstcount++;
                                }
                            }
                            textarea.append("\n");


                        }
                    }else{
                        for(Vlan vlanI : vlanInstance){
                            if(vlanI.getNum()==vlanN) {
                                Config conf = new Config();
                                conf = vlanI.getConfig();
                                ArrayList<EthernetSetting> ethernetSettings = new ArrayList<>();
                                ethernetSettings.addAll(conf.getEthernetSetting());
                                ArrayList<Integer> ports = new ArrayList<>();//VLANが設定されているポートをまとめた物
                                ArrayList<String> portnames = new ArrayList<>();
                                for (EthernetSetting eth : ethernetSettings) {
                                    if (eth.getAccessVlan() == vlanN) {//現在調べているVLANと同じ時
                                        ports.add(eth.getPort());
                                        Check.changeColor(eth,rightblue);
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
                                textarea.append(String.format("%-14s" ,conf.getName()));
                                Check.changeColor(conf,rightblue);
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
        tab.add(pane,BorderLayout.CENTER);//テキストエリアをtabのパネルに追加する

        //ここからタブの削除ボタン追加の処理
        JPanel p1 = new JPanel( new GridLayout(1,1,5,5) );
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

        if(createTab){//createTabがtureのとき(入力が適切なとき)
            tabbedPane.addTab(name,tab);//astahにタブを追加する
            tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1, p1);//タブに削除ボタンを追加する
        }

    }
    public static ArrayList<String> recordBeforeInstanceColar(ArrayList<IPresentation> presentations){//最初のインスタンスの色を記憶しておくための物
        ArrayList<String> firstPresenttationColor = new ArrayList<>();
        for(IPresentation presentation:presentations){
            if (presentation instanceof INodePresentation){//インスタンスの名前、スロットの処理
                IElement model = presentation.getModel();
                if(model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification){
                    com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;
                    firstPresenttationColor.add(presentation.getProperty(PresentationPropertyConstants.Key.FILL_COLOR));
                }
            }
        }
        return firstPresenttationColor;
    }


}
