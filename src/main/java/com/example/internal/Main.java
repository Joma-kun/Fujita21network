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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main extends JPanel
        implements IPluginExtraTabView, ProjectEventListener , ActionListener {
    private JButton button;//拡張タブにあるrunボタン


    public JTextField text ;

    public ArrayList<String> beforeInstanceColor = new ArrayList<>();//9-14お試し処理
    public ArrayList<IPresentation> beforeInstance = new ArrayList<>();//9-14お試し処理

    public Main() {
        initComponents();//最初に行われる処理
    }
    private void initComponents() {//最初に行われるメソッド（ボタンの挙動）
        setLayout(new BorderLayout());//拡張タブのボタンのレイアウト
        text = new JTextField(10);
        add(selectPane(),BorderLayout.NORTH);//拡張タブに貼るコンポーネント（ボタン）（selectPane)，とその配置
        addProjectEventListener();
    }


    private Container selectPane() {//コンポーネント（ボタン）についてのメソッド
        JPanel inputPanel = new JPanel(new BorderLayout());

        button = new JButton("run");//ボタンを作る
        JScrollPane panes = new JScrollPane(button);//スクロールできるようにする
        button.addActionListener(this);//ボタンにアクションリスナー（ボタンが押されたときにactionpeformedを実行する）を追加する
        inputPanel.add(panes,BorderLayout.SOUTH);
        inputPanel.add(text,BorderLayout.NORTH);
        return inputPanel;
    }

    private void addProjectEventListener() {//ボタンが押されたときに，プロジェクトを実行するためのメソッド
        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = api.getProjectAccessor();
            projectAccessor.addProjectEventListener(this);//プロジェクトリスナを追加する
        } catch (ClassNotFoundException e) {
            e.getMessage();
        }}
    @Override
    public void actionPerformed(ActionEvent e) {//アクションが発生（ボタンが押される）と呼び出される
//        System.out.println("ボタン完成");
        String input = text.getText();
        if(input.isEmpty()) {
            add(createLabelPane(), BorderLayout.CENTER);//拡張タブの下側のパネルを作る
        }else{
            System.out.println("こっち");
            add(createLabelPaneJoho(input), BorderLayout.CENTER);
        }
        }

    public Container createLabelPane() throws RuntimeException {//Panelの中身
        TextArea textarea = new TextArea();//パネルの文章を入力する部分
        textarea.setText("<実行結果>\n");
        ProjectAccessor projectAccessor = null;
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


            //IInstancespecificationの仕様確認
            // ここから
//            for (ClassElement instance:instances){
//                if(instance instanceof Config)
//                {
//                    System.out.println("名前"+instance.getName());
//                    System.out.println("関連の名前"+((Config) instance).getDeviceModel());
//                }
//            }
//
//
////            //関連確認用
//            for(ClassElement instance : instances){
//                System.out.println("クラス"+instance.getClassName()+"  名前"+ instance.getName());
//                System.out.println("関連");
//                if (instance.getlink()!= null) {
//                    for (ClassElement p : instance.getlink()) {
//                        System.out.println("クラス" + p.getClassName() + "  名前" + p.getName());
//                    }
//                }
//            }
//            //インスタンスの中身を表示する処理
//            for(ClassElement instance : instances){
//                if(instance instanceof ClassElement) {
//                    System.out.println("名前" + instance.getName());
//                    for (Slots s : instance.getSlots()) {
//                        System.out.println("属性" + s.getAttribute() + "  値" + s.getValue());
//                    }
//                }
//            }
            //ここまで

            //ここからチェック処理開始
            try {//モデル編集のためのトランザクション処理
                transactionManager.beginTransaction();//トランザクションの開始
                /*インスタンスのカラーを初期の状態に戻すための処理
                beforeInstance:runを押した時の一個前のインスタンス
                beforeInstanceColor:beforeInstanceのカラー情報
                presentations:runを押した時点でのインスタンス
                runを押したときのインスタンスとそのカラーをリストに格納して、最初にそのリストを元にbeforeinstanceと同じ
                instanceの色を元に戻し、その状態を新しくbeforeInstanceとbeforeInstanceColorに格納して次につなげる。
                 */
                //ここから
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
                //AttributeInntegrityChecker属性の値の解析
                AttributeIntegrityChecker attributeIntegrityChecker = new AttributeIntegrityChecker(instances,textarea);
                attributeIntegrityChecker.AllAttributeIntegrityCheck();


                //ここからチェック開始


                //関連の多重度のチェック
                Check.nodeCheck(textarea, instances,links);

                //関連がないもののチェック

                Check.notLinkCheck(textarea,instances,links);

                //ipアドレス重複チェック
                Check.ipAddressDuplicationCheck(textarea,instances);






                //Vlan重複チェック
//                Check.vlanDuplicationCheck(textarea,instances);

                //DFSここから

//                Check.dfsCheck(instances,1);


//                ArrayList<ArrayList<Config>> rupeConfigs=Check.rupeChecks(instances,textarea);
                //ここまで

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
        return pane;
    }
    public Container createLabelPaneJoho(String input) throws RuntimeException {//Panelの中身
        TextArea textarea = new TextArea();//パネルの文章を入力する部分
        textarea.setText("<実行結果>\n");
        ProjectAccessor projectAccessor = null;
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
                ArrayList<Config> configs = new ArrayList<>();
                for(ClassElement instance : instances){
                    if(instance instanceof Config){
                        configs.add((Config) instance);
                    }
                }
                //ここから違うクラスの処理に書き換えておく
                if(input.matches("^show vlan brief .*")){
                    String name = input.substring(16);
                    Config conf = new Config();//入力したコンフィグ
                    for(Config con : configs){
                        if(con.getName().equals(name)){
                            conf = con;
                            break;
                        }
                    }//入力したコンフィグと名前が一致するコンフィグインスタンスを見つけて格納する

                    ArrayList<Integer> et = new ArrayList<>();
                    for(ClassElement cs : instances){
                        if(cs instanceof EthernetSetting){
                            if(((EthernetSetting) cs).getConfig()!=null){
                            if(((EthernetSetting) cs).getConfig().equals(conf)) {
                                if (((EthernetSetting) cs).getAccessVlan() != -1 || ((EthernetSetting) cs).getAccessVlan() != 0) {
                                    et.add(((EthernetSetting) cs).getPort());
                                }
                            }
                        }}
                    }
                    System.out.println(et);

                    textarea.append("VLAN Name          Status     Ports                  \n");
                    textarea.append("---- ------------- ---------- --------------------------\n");
                    textarea.append(String.format("%-4s %-13s", "1","default"));
                    textarea.append(String.format("%-13s", " active"));
                    int sa = 0;
                    for(int n = 1; n<13;n++){
                        if(!et.contains(n)){
                            if(sa!=0) {
                                textarea.append(",");
                            }
                            textarea.append("Po"+n);
                            sa++;
                        }
                    }
                    textarea.append("\n");



                    for(ClassElement ins : instances){
                        if(ins instanceof Vlan){
                            if(((Vlan) ins).getConfig().equals(conf)){
                                ArrayList<EthernetSetting> ethList = new ArrayList<>();
                                ArrayList<Integer> ports = new ArrayList<>();
                                ArrayList<String> portnames = new ArrayList<>();
                                int vlanNum = ((Vlan) ins).getNum();
                                String vlanName = ((Vlan) ins).getNamd();
                                for(ClassElement ethins : instances){
                                    if(ethins instanceof EthernetSetting) {
                                        if (((EthernetSetting) ethins).getConfig() != null) {
                                            if (((EthernetSetting) ethins).getConfig().equals(conf)) {
                                                ethList.add((EthernetSetting) ethins);
                                                if (((EthernetSetting) ethins).getAccessVlan() == vlanNum) {
                                                    Integer port = ((EthernetSetting) ethins).getPort();
                                                    ports.add(port);
                                                }
                                            }
                                        }
                                    }
                                }//portsの追加終わり
                                Collections.sort(ports);

                                for(Integer por : ports){
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
                                                portnames.add("Po" + por);
                                            }
                                        }
                                    }
                                }



                               if(vlanNum!=-1) {
                                   textarea.append(String.format("%-4s %-13s", vlanNum, vlanName));
                                   textarea.append(String.format("%-13s", " active"));
                                   int count = 0;
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
        return pane;
    }
    private static ArrayList<String> recordBeforeInstanceColar(ArrayList<IPresentation> presentations){//最初のインスタンスの色を記憶しておくための物
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



    @Override
    public void projectChanged(ProjectEvent e) {
    }

    @Override
    public void projectClosed(ProjectEvent e) {
    }

    @Override
    public void projectOpened(ProjectEvent e) {

    }

    @Override
    public void addSelectionListener(ISelectionListener listener) {
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public String getDescription() {
        return "Show Hello World here";
    }

    @Override
    public String getTitle() {
        return "result";
    }

    public void activated() {
    }

    public void deactivated() {
    }




}
