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
import com.example.classes.Config;
import com.example.element.ClassElement;
import com.example.element.LinkElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends JPanel
        implements IPluginExtraTabView, ProjectEventListener , ActionListener {
    private JButton button;//拡張タブにあるrunボタン

    public ArrayList<String> beforeInstanceColor = new ArrayList<>();//9-14お試し処理
    public ArrayList<IPresentation> beforeInstance = new ArrayList<>();//9-14お試し処理

    public Main() {
        initComponents();//最初に行われる処理
    }
    private void initComponents() {//最初に行われるメソッド（ボタンの挙動）
        setLayout(new BorderLayout());//拡張タブのボタンのレイアウト
        add(selectPane(),BorderLayout.NORTH);//拡張タブに貼るコンポーネント（ボタン）（selectPane)，とその配置
        addProjectEventListener();

    }

    private Container selectPane() {//コンポーネント（ボタン）についてのメソッド
        button = new JButton("run");//ボタンを作る
        JScrollPane panes = new JScrollPane(button);//スクロールできるようにする
        button.addActionListener(this);//ボタンにアクションリスナー（ボタンが押されたときにactionpeformedを実行する）を追加する
        return panes;
    }

    private void addProjectEventListener() {//ボタンが押されたときに，プロジェクトを実行するためのメソッド
        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = api.getProjectAccessor();
            projectAccessor.addProjectEventListener(this);//プロジェクトリスナを追加する
        } catch (ClassNotFoundException e) {
            e.getMessage();
        }}

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
                //ここからチェック開始

                //関連の多重度のチェック
                Check.nodeCheck(textarea, instances,links);

                //関連がないもののチェック

                Check.notLinkCheck(textarea,instances,links);

                //ipアドレス重複チェック
                Check.ipAddressDuplicationCheck(textarea,instances);

                //Vlan重複チェック
                Check.vlanDuplicationCheck(textarea,instances);

                //DFSここから

//                Check.dfsCheck(instances,1);


                ArrayList<ArrayList<Config>> rupeConfigs=Check.rupeChecks(instances,textarea);
                //ここまで

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

    @Override
    public void actionPerformed(ActionEvent e) {//アクションが発生（ボタンが押される）と呼び出される
//        System.out.println("ボタン完成");
        add(createLabelPane(), BorderLayout.CENTER);//拡張タブの下側のパネルを作る
    }


}
