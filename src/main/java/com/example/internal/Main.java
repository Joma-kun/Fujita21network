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
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
//import org.neo4j.driver.*;

import com.example.internal.converter.ChangeClassInformation;
import com.example.internal.converter.SetOthersInformation;
//import static org.neo4j.driver.Values.parameters;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Main extends JPanel
        implements IPluginExtraTabView, ProjectEventListener, ActionListener {
    private JButton button;//拡張タブにあるrunボタン
    public JTextField text;
    public JCheckBox check1, check2, check3, check4, check5;
    public Map<String, Boolean> checkBoxStatus;
    public JPanel mainPanel = new JPanel(new BorderLayout());//エラー文などを出力するパネル（なかにinputpanelもある）
    boolean colorCount = true;//outputInfomationの色戻し処理用
    public JTabbedPane tabbedPane = new JTabbedPane();//タブを追加するためのもの

    public ArrayList<String> beforeInstanceColor = new ArrayList<>();//9-14お試し処理
    public ArrayList<IPresentation> beforeInstance = new ArrayList<>();//9-14お試し処理
    private java.awt.event.KeyEvent KeyEvent;



    public Main() throws Exception {
        initComponents();//最初に行われる処理
    }



    private void initComponents() throws Exception {//最初に行われるメソッド（ボタンの挙動）
        mainPanel.setPreferredSize(new Dimension(800, 500));
        setLayout(new BorderLayout());//拡張タブのボタンのレイアウト
        text = new JTextField(50);


        add(selectPane(tabbedPane, mainPanel), BorderLayout.NORTH);//拡張タブに貼るコンポーネント（ボタン）（selectPane)，とその配置
        addProjectEventListener();
    }


    public Container selectPane(JTabbedPane tabbedPane, JPanel mainPanel) {//コンポーネント（ボタン）についてのメソッド
        JPanel inputPanel = new JPanel();//入力エリアとボタンを配置する
        button = new JButton("run");//ボタンを作る

        JScrollPane panes = new JScrollPane(button);//スクロールできるようにする
        button.addActionListener(this);//ボタンにアクションリスナー（ボタンが押されたときにactionpeformedを実行する）を追加する
        text.addActionListener(this);
        inputPanel.add(text, BorderLayout.NORTH);//テキストエリア配置
        inputPanel.add(panes, BorderLayout.SOUTH);//ボタン配置

        mainPanel.add(inputPanel, BorderLayout.NORTH);//テキストエリアとボタンをエラー文出力画面の上に追加する

        //チェックボックスのための処理　ここから
        JPanel checkBoxPanel = new JPanel();//チェックボックスをまとめるパネル
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));//縦に並べるレイアウト

//         check1 = new JCheckBox("IP");
//         check2 = new JCheckBox("VLAN");
//         check3 = new JCheckBox("OSPF");
//         check4 = new JCheckBox("STP");
//         check5 = new JCheckBox("ACL");

//        // チェックボックスをパネルに追加
//        checkBoxPanel.add(check1);
//        checkBoxPanel.add(check2);
//        checkBoxPanel.add(check3);
//        checkBoxPanel.add(check4);
//        checkBoxPanel.add(check5);

        mainPanel.add(checkBoxPanel, BorderLayout.WEST); // チェックボックスを中央に配置

        //ここまで
        tabbedPane.addTab("MAIN", mainPanel);//タブの一つ目にmainPnaelを追加する

        return tabbedPane;//タブを含むパネルを返す


    }

    private void addProjectEventListener() {//ボタンが押されたときに，プロジェクトを実行するためのメソッド
        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = api.getProjectAccessor();
            projectAccessor.addProjectEventListener(this);//プロジェクトリスナを追加する
        } catch (ClassNotFoundException e) {
            e.getMessage();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {//アクションが発生（ボタンが押される）と呼び出される
//        System.out.println("ボタン完成");
        String input = text.getText();
        //チェックボックスのための処理　ここから
        //チェックボックスの情報の取得とMAPでの格納
//        checkBoxStatus = new HashMap<>();
//        checkBoxStatus.put("IP", check1.isSelected());
//        checkBoxStatus.put("VLAN", check2.isSelected());
//        checkBoxStatus.put("OSPF", check3.isSelected());
//        checkBoxStatus.put("STP", check4.isSelected());
//        checkBoxStatus.put("ACL", check5.isSelected());
//

        //ここまで
        if (input.isEmpty() || input.equals("no")) {
            mainPanel.add(createLabelPane(input,checkBoxStatus), BorderLayout.CENTER);//拡張タブの下側のパネルを作る
        } else {
            if (colorCount) {//色戻し処理のための作業　beforeInstanceとbeforeInstanceColor
                mainPanel.add(createLabelPane(input,checkBoxStatus), BorderLayout.CENTER);//拡張タブの下側のパネルを作る
                mainPanel.remove(createLabelPane(input,checkBoxStatus));
                colorCount = false;
            }
            OutputInformation outputInformation = new OutputInformation(tabbedPane);
            outputInformation.createLabelPaneJoho(input, beforeInstance, beforeInstanceColor);//新しいタブを作成して情報を出力する
        }



    }


    public Container createLabelPane(String input,Map<String, Boolean> checkBoxStatus) throws RuntimeException {//Panelの中身
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
            ArrayList<ClassElement> instances = null;
            ArrayList<String> formatErrorStatements = new ArrayList<>();
            ArrayList<ClassElement> errorInstances = new ArrayList<>();

            instances = ChangeClassInformation.changeAllElement(presentations,projectAccessor);



            //linkの情報を変換する
            ArrayList<LinkElement> links = SetOthersInformation.changeLinkInformation(presentations, instances);
            //JSON形式に検証結果を反映するための処理
            ArrayList<ErrorInfo> errorInfos = new ArrayList<>();//error情報をまとめたリスト



//
//            //ファイルが存在するか確認する
//            if(file.exists()) {
//
//                //FileReaderクラスのオブジェクトを生成する
//                FileReader filereader = new FileReader(file);
//
//                //filereaderクラスのreadメソッドでファイルを1文字ずつ読み込む
//                int data;
//                while ((data = filereader.read()) != -1) {
//                    System.out.print((char) data);
//                }
//
//                //ファイルクローズ
//                filereader.close();
//            }else {
//                System.out.print("ファイルは存在しません");
//            }
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
                        if (model instanceof IInstanceSpecification) {
                            IInstanceSpecification instanceSpecification = (IInstanceSpecification) model;
                            beforeInstance.add(presentation);//インスタンスの時のみリストに追加する
                        }
                    }
                    if (presentation instanceof ILinkPresentation) {//線の時
                        presentation.setProperty(PresentationPropertyConstants.Key.LINE_COLOR, "#000000");
                    }
                }
                Check check = new Check(errorInfos);//コンストラクタ
                beforeInstanceColor = recordBeforeInstanceColar(presentations);
                //ここまで（astahの色戻し処理）
                //AttributeInntegrityChecker属性の値の解析
                AttributeIntegrityChecker attributeIntegrityChecker = new AttributeIntegrityChecker(instances, textarea,errorInfos);
                attributeIntegrityChecker.AllAttributeIntegrityCheck();

                for(ClassElement eInstance : errorInstances){
                    check.changeColor(eInstance,"#ff0000");
                }

                //データベース接続
//                String uri = "bolt://localhost:7687";
//                String user = "neo4j";
//                String password = "password";
//
//                // Neo4jExampleクラスをインスタンス化して実行
//                try (HelloWorldExample greeter = new HelloWorldExample(uri, user, password)) {
//                    greeter.printGreeting("hello, world");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                if(formatErrorStatements.size()==0 || input.equals("no") ) {
                    ;

                    System.out.println("check開始");
                    CreateFile.Createfile(instances);
                    CreateFile.CreateLinkfile(instances);
                    //ここからチェック開始
                    ArrayList<String> errorStatement = new ArrayList<>();
                    ArrayList<String> warningStatement = new ArrayList<>();

//                    checkBoxStatus.forEach((key, value) -> {
//                        System.out.println(key + ": " + (value ? "Checked" : "Unchecked"));
//                    });
                    check.oposingVlancheck(instances, textarea, errorStatement, warningStatement);
                   System.out.println("oposingVlancheck");

                   check.conectedConfigCheck(instances, textarea, errorStatement, warningStatement);
                    //astahの必須項目のチェック　EthernetSettingにLinkとConcigがあるべきなど
//                   Check.asta(instances, textarea, errorStatement, warningStatement);
                    //関連の多重度のチェック
                    check.nodeCheck( instances, links, errorStatement);
                    System.out.println("nodeCheck");
                    //関連がないもののチェック
                    check.notLinkCheck(textarea, instances, links, errorStatement);
                    //欠如などのチェック
                    check.nodeKetujoCheck(instances,textarea,errorStatement,warningStatement);
                    System.out.println("notLinkCheck");
                    if (errorStatement.size() == 0) {

                        //ipアドレス重複チェック
                        check.ipAddressDuplicationCheck(textarea, instances, errorStatement, warningStatement);
                    System.out.println("ipAddressDuplicationCheck");
                        //Vlan重複チェック
//                        Check.vlanDuplicationCheck(textarea, instances, warningStatement);
                    System.out.println("vlanDuplicationCheck");
                        //nativeVLANが一致するかどうかのチェック
                        check.nativeVlanCheck(instances, textarea, errorStatement, warningStatement);
                    System.out.println("nativeVlanCheck");


                    Check.osfpCheck(instances, textarea, errorStatement, warningStatement);
                    System.out.println("osfpCheck");





                    // DFSここから
                    //ループ
//              Check.dfsCheck(instances,1);

                    //この下を戻す
//              ArrayList<ArrayList<Config>> rupeConfigs=Check.rupeChecks(instances,textarea,warningStatement);
//              Check.stpCheck(instances,rupeConfigs,textarea,errorStatement,warningStatement);
//System.out.println("stpCheck終了");

                }
              //エラー文の出力
                    for(String errorState : errorStatement) {
                        textarea.append("[error]: " + errorState + "\n");
                    }
                    //警告文の出力
                    for(String warningState : warningStatement){
                        textarea.append("[warning]: " + warningState +"\n");
                    }

//                    ここまで
                }else{
                    for (String formatErrorstatement : formatErrorStatements) {
                        textarea.append(formatErrorstatement + "\n");
                    }

                }
                    textarea.append("check終了");
                System.out.println("createfile");
                System.out.println(errorInfos);
                CreateFile.CreateCheckFile(errorInfos);



                transactionManager.endTransaction();//トランザクションの終了
            } catch (Exception e) {

                transactionManager.abortTransaction();//トランザクションの例外処理

            }


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ProjectNotFoundException e) {
            String message = "Please open a project";

        } catch (InvalidUsingException | InvalidEditingException e) {
            throw new RuntimeException(e);
        }
        textarea.setEditable(false);//テキストエリアを編集不可能にする
        JScrollPane pane = new JScrollPane(textarea);//スクロールできるようにして型変換も行う

        return pane;
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
        return "result";
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
