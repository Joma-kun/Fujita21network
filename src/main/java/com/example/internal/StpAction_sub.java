//package com.example.internal;
//
//import com.change_vision.jude.api.inf.AstahAPI;
//import com.change_vision.jude.api.inf.exception.InvalidUsingException;
//import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
//import com.change_vision.jude.api.inf.model.*;
//import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
//import com.change_vision.jude.api.inf.presentation.INodePresentation;
//import com.change_vision.jude.api.inf.presentation.IPresentation;
//import com.change_vision.jude.api.inf.project.ProjectAccessor;
//import com.change_vision.jude.api.inf.project.ProjectEvent;
//import com.change_vision.jude.api.inf.project.ProjectEventListener;
//import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
//import com.change_vision.jude.api.inf.ui.ISelectionListener;
//import com.example.element.*;
//import com.example.element.ClassElement;
//import com.example.classes.*;
//
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.Arrays;
//
//
////jarファイルの扱い方
////test(モデルの変換元)を編集したら、ビルドからあーティファクトの再構築を行い、outに出てくるjarファイルの中身が変わっていることを確認する
////次にローカルリポジトリの中身を変えるためにmvn install:install-file -Dfile="C:\Users\fujita tomoya\IdeaProjects\test\out\artifacts\testmodel_jar\testmodel.jar" -DgroupId=org.exampl -DartifactId=tes -Dversion=1.0-SNAPSHOT -Dpackaging=jar
////を実行する。最後にこのプロジェクト内でnetworkフォルダを右クリック→mavenをおしてリロードする
//public class StpAction_sub extends JPanel
//        implements IPluginExtraTabView, ProjectEventListener , ActionListener {
//    private JButton button;//拡張タブにあるrunボタン
//    public static ArrayList<String> instancenameList = new ArrayList<String>();//取得したインスタンス仕様の名前を格納しておくリスト
//    public StpAction_sub() {
//        initComponents();//最初に行われる処理
//    }
//    private void initComponents() {//最初に行われるメソッド（ボタンの挙動）
//        setLayout(new BorderLayout());//拡張タブのボタンのレイアウト
//        add(selectPane(),BorderLayout.NORTH);//拡張タブに貼るコンポーネント（ボタン）（selectPane)，とその配置
//        addProjectEventListener();
//
//    }
//
//    private Container selectPane() {//コンポーネント（ボタン）についてのメソッド
//        button = new JButton("run");//ボタンを作る
//        JScrollPane panes = new JScrollPane(button);//スクロールできるようにする
//        button.addActionListener(this);//ボタンにアクションリスナー（ボタンが押されたときにactionpeformedを実行する）を追加する
//        return panes;
//    }
//
//    private void addProjectEventListener() {//ボタンが押されたときに，プロジェクトを実行するためのメソッド
//        try {
//            AstahAPI api = AstahAPI.getAstahAPI();
//            ProjectAccessor projectAccessor = api.getProjectAccessor();
//            projectAccessor.addProjectEventListener(this);//プロジェクトリスナを追加する
//        } catch (ClassNotFoundException e) {
//            e.getMessage();
//        }}
//
//    public Container createLabelPane() throws RuntimeException {//Panelの中身
//        TextArea testlavel = new TextArea();//パネルの文章を入力する部分
//        JScrollPane pane = new JScrollPane(testlavel);//スクロールできるようにして型変換も行う
//
//        try {
//            AstahAPI api = AstahAPI.getAstahAPI();
//            ProjectAccessor projectAccessor = api.getProjectAccessor();//Astahに必要なもの
//            IModel iCurrentProject = projectAccessor.getProject();//Imodelは最初のパッケージを表す
//            ArrayList<IPresentation> presentations = new ArrayList<>();//表示されている図や関連の線についての情報を格納するリスト
//            ArrayList<com.change_vision.jude.api.inf.model.IInstanceSpecification> association_list = new ArrayList<>();//関連を軸としてインスタンス仕様の情報を持つ(for文で情報のgetNameなどを使える）
//            ArrayList<String> assnamelist = new ArrayList<>();//どことどこがつながっているかの情報を「クラスの名前」だけで表す．例：(0,1)ConfigーVlan
//            IDiagram[] diagrams = iCurrentProject.getDiagrams();//図を得る// stpmodel,tobemode
//
//
//            for (IDiagram i: diagrams){//クラス図の数だけ繰り返す（インスタンスの数ではない）
//                presentations.addAll(Arrays.asList(i.getPresentations()));
//            }//取得した情報のIPresentation(関連船やグラフの情報）を得る．
//
//            //自分たちのinstance情報に変えるための処理
//            //ここから
//            ArrayList<ClassElement> instances = new ArrayList<>();//生成したインスタンスのためのリスト
//            for( IPresentation j : presentations){//astahの図の情報
//                if(changeinstanceinfomation(j)!=null) {
//                    instances.add(changeinstanceinfomation(j));//自分たちのinstance仕様に変換しリストに追加する
//                }
//            }//instancesに情報を変換して入れていある状態（関連の情報は入っていない）
////
//            for (IPresentation j :presentations){
//                changenodeinfomation(j,instances);//関連の情報をインスタンスの情報に追加する
//            }
//
//            //ここまで
//
//            //IInstancespecificationの仕様確認
//            // ここから
////            for (IInstancespecification_model l:instances){
////                if(l instanceof Config)
////                {
////                    System.out.println("名前"+l.getName());
////                    System.out.println("関連の名前"+((Config) l).getDeviceModel());
////                }
////            }
//
//
////            //関連確認用
////            for(IInstancespecification_model s : instances){
////                System.out.println("クラス"+s.getClassName()+"  名前"+ s.getName());
////                System.out.println("関連");
////                if (s.getlink()!= null) {
////                    for (IInstancespecification_model p : s.getlink()) {
////                        System.out.println("クラス" + p.getClassName() + "  名前" + p.getName());
////                    }
////                }
////            }
////            //インスタンスの中身を表示する処理
////            for(IInstancespecification_model i : instances){
////                if(i instanceof IInstancespecification_model) {
////                    System.out.println("名前" + i.getName());
////                    for (Slots s : i.getSlots()) {
////                        System.out.println("属性" + s.getAttribute() + "  値" + s.getValue());
////                    }
////                }
////            }
//            //ここまで
//            //astah依存の情報出力（いらない）
//            //ここから
////            getAllinfomation(presentations);
////            for( IPresentation j : presentations){
//////                changeinfomation(j);
////            }
////            getAllinfomation(presentations);
//
////            for (IPresentation j: presentations) {//すべてのグラフや線についての情報にfor文でアクセする（今回は線）
////                ArrayList<IInstanceSpecification> _list = getasoociation(j);//関連で結ばれたインスタンスの情報（両端）をならべてリストで表す
////                if(_list!=null) {
////                    association_list.addAll(_list);//association_listには関連で結ばれたインスタンスのIInstanceSpecificationが入っている．
////                }
////                //_listは０番目と１番目，２番目と３番目が関連で結ばれた情報である
////            }
////
////            for (IInstanceSpecification a : association_list){//関連がついているインスタンスについての名前を得る（クラスのほうのなまえ）
////                assnamelist.add(a.getClassifier().getName());//assnamelistに名前を追加する
////            }
//            //ここまで
//
//
//
////                printPresentationInfo(j);//図要素（インスタンスの情報をprintするための処理）
//
//            //ループのチェックするための処理
//            //ここから
////            ArrayList<Integer> rupenumber =Stp.getlinknumber(assnamelist);//ループを形成しているインスタンスの要素の添え字のリスト
////            ArrayList<String>  Linkedname = new ArrayList<>();//リンクでつながれた情報（Config,Eathernet,Link）の関連情報
////             for(Integer p:rupenumber){
////                Linkedname.add(association_list.get(p).getName());//リンクでつながれた情報について名前を所得する
////
////            }
////            rupecheck(Linkedname);//インスタンスの名前をもとにループをチェックする
//            //ここまで
//
//
//
//
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (ProjectNotFoundException e) {
//            String message = "Please open a project";
//
//        } catch (InvalidUsingException e) {
//            throw new RuntimeException(e);
//        }
//
//        return pane;
//    }
//
//    //    private void changeinstanceinfomation(IPresentation j) {
////        if (j instanceof INodePresentation){//インスタンスの名前、スロットの処理
////            IElement model = j.getModel();
////            if(model instanceof IInstanceSpecification){
////                INodePresentation node =(INodePresentation) j;
////                IInstanceSpecification instanceSpecification = (IInstanceSpecification) model;
////                //modelでためす
////                if(instanceSpecification.getClassifier().getName().equals("Hostname")){
////                    Hostname hostname = new Hostname();
////                    hostname.setName(instanceSpecification.getName());
////                    ISlot[] slot = instanceSpecification.getAllSlots();
////                    ArrayList<Slots> slots = new ArrayList<>();
////                    for(ISlot s :slot) {
////                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
////                        slots.add(sl);
////                    }
////                    hostname.setSlots(slots);
////                    for(Slots w : slots){
////                        System.out.println(w.getAttribute());
////                        System.out.println(w.getValue());
////                    }
////                }
////            }
////        }
////    }
//    private ClassElement changeinstanceinfomation(IPresentation j) {//自分たちのinstance情報に変換するためのメソッド
//        String classs;
//        if (j instanceof INodePresentation){//インスタンスの名前、スロットの処理
//            IElement model = j.getModel();
//            if(model instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification){
//                INodePresentation node =(INodePresentation) j;
//                com.change_vision.jude.api.inf.model.IInstanceSpecification instanceSpecification = (com.change_vision.jude.api.inf.model.IInstanceSpecification) model;
//                //modelでためす
//                ClassElement instance = null;
//
//
//                if(instanceSpecification.getClassifier().getName().equals("AccessList")){
//                    instance = new AccessList();
//                    instance = (AccessList)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    int count=0;
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue());
//                        ((AccessList) instance).setAccessListNumber(number);
//                    }
//                    ((AccessList) instance).setPermitOrDeny(slots.get(1).getValue());
//                    ((AccessList) instance).setAccessListInfo(slots.get(2).getValue());
//
//
//                } else if (instanceSpecification.getClassifier().getName().equals("Client")){
//                    instance = new Clients();
//                    instance = (Clients)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    ((Clients) instance).setNames(slots.get(0).getValue());
//                    ((Clients) instance).setIpAddress(slots.get(1).getValue());
//                    ((Clients) instance).setSubnetMask(slots.get(1).getValue());
//                    ((Clients) instance).setDefaultGateway(slots.get(1).getValue());
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("Config")){
//
//                    instance = new Config();
//                    instance = (Config)instance;
//                    instance.setName(instanceSpecification.getName());
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    ((Config) instance).setDeviceModel(slots.get(0).getValue());
//                }else if (instanceSpecification.getClassifier().getName().equals("EthernetSetting")){
//                    instance = new EthernetSetting();
//                    instance = (EthernetSetting)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue().trim());
//                        ((EthernetSetting) instance).setSlot(number);
//                    }
//                    if(isInt(slots.get(1).getValue())){
//                        int number = Integer.parseInt(slots.get(1).getValue().trim());
//                        ((EthernetSetting) instance).setPort(number);
//                    }
//                    ((EthernetSetting) instance).setIpAddress(slots.get(2).getValue());
//                    ((EthernetSetting) instance).setSubnetMask(slots.get(3).getValue());
//                    if(isInt(slots.get(4).getValue())){
//                        int number = Integer.parseInt(slots.get(4).getValue().trim());
//                        ((EthernetSetting) instance).setAccessVlan(number);
//                    }
//                    if(isInt(slots.get(5).getValue())){
//                        int number = Integer.parseInt(slots.get(5).getValue().trim());
//                        ((EthernetSetting) instance).setNativeVlan(number);
//                    }
//                    ((EthernetSetting) instance).setMode(slots.get(6).getValue());
//                    if(isInt(slots.get(7).getValue())){
//                        int number = Integer.parseInt(slots.get(7).getValue().trim());
//                        ((EthernetSetting) instance).setAccessListNumber(number);
//                    }
//                    ((EthernetSetting) instance).setAccessListName(slots.get(8).getValue());
//                    ((EthernetSetting) instance).setAccessListInOrOut(slots.get(9).getValue());
//                    ((EthernetSetting) instance).setSpeed(slots.get(10).getValue());
//                    ((EthernetSetting) instance).setDuplex(slots.get(11).getValue());
//                    ((EthernetSetting) instance).setDuplex(slots.get(11).getValue());
//                    if(slots.get(12).getValue().equals("true")){
//                        ((EthernetSetting) instance).setIpVirtualReassembly(true);
//                    }else{
//                        ((EthernetSetting) instance).setIpVirtualReassembly(false);
//                    }
//                    ((EthernetSetting) instance).setIpAccessGroup(slots.get(12).getValue());
//                    if(slots.get(13).getValue().equals("true")){
//                        ((EthernetSetting) instance).setSwitchportTrunkEncapsulation(true);
//                    }else{
//                        ((EthernetSetting) instance).setSwitchportTrunkEncapsulation(false);
//                    }
//                    if(slots.get(14).getValue().equals("true")){
//                        ((EthernetSetting) instance).setShutdown(true);
//                    }else{
//                        ((EthernetSetting) instance).setShutdown(false);
//                    }
//
//                }else if (instanceSpecification.getClassifier().getName().equals("Hostname")){
//                    instance = new Hostname();
//                    instance = (Hostname)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    ((Hostname) instance).setHostName(slots.get(0).getValue());
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("IpRoute")){
//                    instance = new IpRoute();
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//
//                    ((IpRoute) instance).setNetwork(slots.get(0).getValue());
//                    ((IpRoute) instance).setAddressPrefix(slots.get(1).getValue());
//                    ((IpRoute) instance).setIpAddress(slots.get(2).getValue());
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("Link")){
//                    instance = new Link();
//                    instance = (Link)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    ((Link) instance).setDescription(slots.get(0).getValue());
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("LinkableElement")){
//                    instance = new LinkableElement();
//                    instance = (LinkableElement)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//
//                }else if (instanceSpecification.getClassifier().getName().equals("OspfInterfaceSetting")){
//                    instance = new OspfInterfaceSetting();
//                    instance = (OspfInterfaceSetting)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    ((OspfInterfaceSetting) instance).setIpAddress(slots.get(0).getValue());
//                    ((OspfInterfaceSetting) instance).setWildcardMask(slots.get(1).getValue());
//                    if(isInt(slots.get(2).getValue())){
//                        int number = Integer.parseInt(slots.get(2).getValue().trim());
//                        ((OspfInterfaceSetting) instance).setAreaId(number);
//                    }
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("OspfSetting")){
//                    instance = new OspfSetting();
//                    instance = (OspfSetting)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue().trim());
//                        ((OspfSetting) instance).setProcessId(number);
//                    }
//                    ((OspfSetting) instance).setRouterId(slots.get(1).getValue());
//
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("OspfVirtualLink")){
//                    instance = new OspfVirtualLink();
//                    instance = (OspfVirtualLink)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue().trim());
//                        ((OspfVirtualLink) instance).setAreaId(number);
//                    }
//                    ((OspfVirtualLink) instance).setRouterId(slots.get(1).getValue());
//
//                }else if (instanceSpecification.getClassifier().getName().equals("StpSetting")){
//                    instance = new StpSetting();
//                    instance = (StpSetting)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue().trim());
//                        ((StpSetting) instance).setBridgePriority(number);
//                    }
//                    if(isInt(slots.get(1).getValue())){
//                        int number = Integer.parseInt(slots.get(1).getValue().trim());
//                        ((StpSetting) instance).setVlan(number);
//                    }
//                    if(isInt(slots.get(2).getValue())){
//                        int number = Integer.parseInt(slots.get(2).getValue().trim());
//                        ((StpSetting) instance).setMode(number);
//                    }
//                    if(isInt(slots.get(3).getValue())){
//                        int number = Integer.parseInt(slots.get(3).getValue().trim());
//                        ((StpSetting) instance).setMacAddress(number);
//                    }
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("Vlan")){
//                    instance = new Vlan();
//                    instance = (Vlan)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue().trim());
//                        ((Vlan) instance).setNum(number);
//                    }
//                    ((Vlan) instance).setNamed(slots.get(1).getValue());
//
//
//
//                }else if (instanceSpecification.getClassifier().getName().equals("VlanSetting")) {
//                    instance = new VlanSetting();
//                    instance = (VlanSetting)instance;
//                    instance.setClassName(instanceSpecification.getClassifier().getName());
//                    instance.setName(instanceSpecification.getName());
//                    ISlot[] slot = instanceSpecification.getAllSlots();
//                    ArrayList<Slots> slots = new ArrayList<>();
//                    for(ISlot s :slot) {
//                        Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                        slots.add(sl);
//                    }
//                    if(isInt(slots.get(0).getValue())){
//                        int number = Integer.parseInt(slots.get(0).getValue().trim());
//                        ((VlanSetting) instance).setVlanNum(number);
//                    }
//                    ((VlanSetting) instance).setIpAddress(slots.get(1).getValue());
//                    ((VlanSetting) instance).setSubnetMask(slots.get(2).getValue());
//                    if(isInt(slots.get(3).getValue())){
//                        int number = Integer.parseInt(slots.get(3).getValue().trim());
//                        ((VlanSetting) instance).setAccessListNumber(number);
//                    }
//                    ((VlanSetting) instance).setAccessListName(slots.get(4).getValue());
//                    ((VlanSetting) instance).setAccessListInOrOut(slots.get(5).getValue());
//                    if(slots.get(6).getValue().equals("true")){
//                        ((VlanSetting) instance).setInNatInside(true);
//                    }else{
//                        ((VlanSetting) instance).setInNatInside(false);
//                    }
//
//                    if(isInt(slots.get(7).getValue())){
//                        int number = Integer.parseInt(slots.get(7).getValue().trim());
//                        ((VlanSetting) instance).setIpTcpAdjustMss(number);
//                    }
//                    if(slots.get(8).getValue().equals("true")){
//                        ((VlanSetting) instance).setIpVirtualReassembly(true);
//                    }else{
//                        ((VlanSetting) instance).setIpVirtualReassembly(false);
//                    }
//                    ((VlanSetting) instance).setIpAccessGroup(slots.get(9).getValue());
//                    if(slots.get(10).getValue().equals("true")){
//                        ((VlanSetting) instance).setShutdown(true);
//                    }else{
//                        ((VlanSetting) instance).setShutdown(false);
//                    }
//
//
//                }else {
//
//                }
////                instance.setName(instanceSpecification.getName());
//                ISlot[] slot = instanceSpecification.getAllSlots();
//                ArrayList<Slots> slots = new ArrayList<>();
//                for(ISlot s :slot) {
//                    Slots sl = new Slots(s.getDefiningAttribute().getName(),s.getValue());
//                    slots.add(sl);
//                }
//                instance.setSlots(slots);
//                slots= instance.getSlots();
//
//
//                return instance;
//            }
//        }
//        return null;
//    }
//    public static boolean isInt(String str){//int型かどうかチェックするメソッド
//        boolean b = true;
//        try{
//            Integer.parseInt(str);
//        }catch(Exception ex){
//            b = false;
//
//        }
//        return b;
//    }
//
//    public void changenodeinfomation(IPresentation j,ArrayList<ClassElement> instances){//関連の情報をinstanceの情報に追加するメソッド
//        ClassElement instance1 = null;
//        ClassElement instance2 = null;
//        if(j instanceof ILinkPresentation){
//            ILinkPresentation _link = (ILinkPresentation) j;
//            IPresentation target = _link.getTargetEnd();
//            IPresentation sorce = _link.getSourceEnd();
//            IElement target_element =target.getModel();//先ほど得られた図の情報をモデルの情報に変える
//            IElement sorce_element =sorce.getModel();
//            //リンクにつながったインスタンスと対応するインスタンスを自分のinstanceから取り出して情報を追加できるようにする
//            if(target_element instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
//                com.change_vision.jude.api.inf.model.IInstanceSpecification target_instance = (com.change_vision.jude.api.inf.model.IInstanceSpecification) target_element;//インスタンスン情報に変える
//
////                if(((IInstanceSpecification) target_instance).getName().equals("Cf1_Hn")){
////                    for(IInstancespecification_model a : instances){
////                    }
////                }
//                for(ClassElement i :instances){
//                    if(i.getName().equals(target_instance.getName())){
//                        instance1 =i;
//                    }
//                }
//            }
//            if(sorce_element instanceof com.change_vision.jude.api.inf.model.IInstanceSpecification) {
//                com.change_vision.jude.api.inf.model.IInstanceSpecification sorce_instance = (com.change_vision.jude.api.inf.model.IInstanceSpecification) sorce_element;
////                System.out.println(sorce_instance.getName());
//                for(ClassElement i :instances){
//                    if(i.getName().equals(sorce_instance.getName())){
//                        instance2 = i;
//                    }
//                }
//            }
//
////            if(instance1 !=null && instance2 !=null) {
//////                System.out.println(e1.getName() + "--" + e2.getName());
////            }
//
//            if(instance1!=null && instance2 != null){
//                instance1.addLink(instance2);
//                instance2.addLink(instance1);
//            }
//
//            if(instance1 instanceof AccessList){
//                if(instance2 instanceof Config){
//                    ((AccessList) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setAccessList((AccessList) instance1);
//                }
//                if(instance2 instanceof  AccessList){
//                    ((AccessList) instance1).setAccessList((AccessList) instance2);
//                    ((AccessList) instance2).setAccessList((AccessList) instance1);
//                }
//            }
//            if(instance1 instanceof Clients){
//                if(instance2 instanceof Link){
//                    ((Clients) instance1).setLink((Link) instance2);
//                    ((Link) instance2).setClient((Clients) instance1);
//                }
//            }
//            if(instance1 instanceof Config){
//                if(instance2 instanceof Vlan){
//                    ((Config) instance1).setVlan((Vlan) instance2);
//                    ((Vlan) instance2).setConfig((Config) instance1);
//                }
//                if(instance2 instanceof EthernetSetting){
//                    ((Config) instance1).setEthernetSetting((EthernetSetting) instance2);
//                    ((EthernetSetting) instance2).setConfig((Config) instance1);
//                }
//                if(instance2 instanceof Hostname){
////                    System.out.println("Hostnameについての処理中");
//                    ((Config) instance1).setHostname((Hostname) instance2);
//                    ((Hostname) instance2).setConfig((Config) instance1);
////                    System.out.println("e1"+((Config) e1).getName());
//
//
//
//                }
//                if(instance2 instanceof VlanSetting){
//                    ((Config) instance1).setVlanSetting((VlanSetting) instance2);
//                    ((VlanSetting) instance2).setConfig((Config) instance1);
//                }
//                if(instance2 instanceof OspfSetting) {
//                    ((Config) instance1).setOspfSetting((OspfSetting) instance2);
//                    ((OspfSetting) instance2).setConfig((Config) instance1);
//                }
//                if (instance2 instanceof StpSetting){
//                    ((Config) instance1).setStpSetting((StpSetting) instance2);
//                    ((StpSetting) instance2).setConfig((Config) instance1);
//                }
//                if(instance2 instanceof AccessList){
//                    ((Config) instance1).setAccessList((AccessList) instance2);
//                    ((AccessList) instance2).setConfig((Config) instance1);
//                }
//                if(instance2 instanceof IpRoute){
//                    ((Config) instance1).setIpRoute((IpRoute) instance2);
//                    ((IpRoute) instance2).setConfig((Config) instance1);
//                }
//                if(instance2 instanceof OspfSetting) {
//                    ((Config) instance1).setOspfSetting((OspfSetting) instance2);
//                    ((OspfSetting) instance2).setConfig((Config) instance1);
//                }
//            }
//            if(instance1 instanceof EthernetSetting){
//                if(instance2 instanceof Config){
//                    ((EthernetSetting) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setEthernetSetting((EthernetSetting) instance1);
//                }
//                if(instance2 instanceof Link){
//                    ((EthernetSetting) instance1).setLink((Link) instance2);
//                    ((Link) instance2).setEthernetSetting((EthernetSetting) instance1);
//                }
//            }
//            if(instance1 instanceof Hostname){
//
//                if(instance2 instanceof Config){
////                    System.out.println("Hostnameについての処理中");
//                    ((Hostname) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setHostname((Hostname) instance1);
////                    System.out.println("e1"+((Hostname) e1).getName());
//
//                }
//            }
//            if(instance1 instanceof IpRoute){
//                if(instance2 instanceof Config){
//                    ((IpRoute) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setIpRoute((IpRoute) instance2);
//                }
//            }
//            if(instance1 instanceof Link){
//                if(instance2 instanceof Clients){
//                    ((Link) instance1).setClient((Clients) instance2);
//                    ((Clients) instance2).setLink((Link) instance1);
//                }
//                if(instance2 instanceof EthernetSetting){
//                    ((Link) instance1).setEthernetSetting((EthernetSetting) instance2);
//                    ((EthernetSetting) instance2).setLink((Link) instance1);
//                }
//            }
//            if(instance1 instanceof OspfInterfaceSetting){
//                if(instance2 instanceof OspfSetting){
//                    ((OspfInterfaceSetting) instance1).setOspfSetting((OspfSetting) instance2);
//                    ((OspfSetting) instance2).setOspfInterfaceSettings((OspfInterfaceSetting) instance1);
//                }
//            }
//            if(instance1 instanceof OspfSetting){
//                if(instance2 instanceof Config){
//                    ((OspfSetting) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setOspfSetting((OspfSetting) instance1);
//                }
//                if(instance2 instanceof OspfInterfaceSetting){
//                    ((OspfSetting) instance1).setOspfInterfaceSettings((OspfInterfaceSetting) instance2);
//                    ((OspfInterfaceSetting) instance2).setOspfSetting((OspfSetting) instance1);
//                }
//                if(instance2 instanceof OspfVirtualLink){
//                    ((OspfSetting) instance1).setOspfVirtualLink((OspfVirtualLink) instance2);
//                    ((OspfVirtualLink) instance2).setOspfSetting((OspfSetting) instance2);
//                }
//            }
//            if(instance1 instanceof OspfVirtualLink){
//                if(instance2 instanceof OspfSetting){
//                    ((OspfVirtualLink) instance1).setOspfSetting((OspfSetting) instance2);
//                    ((OspfSetting) instance2).setOspfVirtualLink((OspfVirtualLink) instance1);
//                }
//            }
//            if(instance1 instanceof StpSetting){
//                if(instance2 instanceof Config){
//                    ((StpSetting) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setStpSetting((StpSetting) instance1);
//                }
//            }
//            if(instance1 instanceof Vlan){
//                if(instance2 instanceof Config){
//                    ((Vlan) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setVlan((Vlan) instance1);
//                }
//            }
//            if(instance1 instanceof VlanSetting){
//                if(instance2 instanceof Config){
//                    ((VlanSetting) instance1).setConfig((Config) instance2);
//                    ((Config) instance2).setVlanSetting((VlanSetting) instance1);
//                }
//            }
//        }
//    }
//
////    public static void getinfomation(IPresentation p){//図のインスタンスについての情報とその関連情報を得るためのメソッド（astahに依存しないコードにはいらない）
////        //Ipresentationはインスタンスや関連線のうちいずれか一つを表す
////        if(p instanceof  INodePresentation){//得られた情報が図の情報（インスタンスやクラス）の時）
////            IElement model = p.getModel();//図の情報についてモデルを得る
////            if(model instanceof IInstanceSpecification){//モデルがインスタンスだった時
////                INodePresentation node = (INodePresentation) p;//関連についての情報を得るためにcastする
////                IInstanceSpecification instanceSpecification = (IInstanceSpecification) model;//得られてた図のインスタンス情報を得られるようになった．
////
//////                System.out.print("名前" + instanceSpecification.getClassifier().getName());//インスタンスのもとになっているクラスの名前
//////                System.out.println("  " + instanceSpecification.getName());//インスタンスの名前
////                ILinkPresentation[] _link = node.getLinks();//得られたインスタンスの関連となっているインスタンスの情報
////                for(ILinkPresentation l:_link){//すべての関連について調べるためのfor文
////                    IElement element= l.getTargetEnd().getModel();//両端の図のモデルを入手する
////                    IElement elementstart= l.getSourceEnd().getModel();
////                    if(element instanceof  IInstanceSpecification){//インスタンスの時に情報をとりたい，片側
////                        IInstanceSpecification link_instance = (IInstanceSpecification) element;//インスタンスにcast
////                        if(!(link_instance.getName().equals(((IInstanceSpecification) model).getName()) )) {//自分のことじゃないとき
//////                            System.out.println("関連" +link_instance.getClassifier().getName() +"  "+link_instance.getName());
////                        }
////                    }
////                    if(elementstart instanceof  IInstanceSpecification){//反対側も
////                        IInstanceSpecification link_instance = (IInstanceSpecification) elementstart;
////                        if(!(link_instance.getName().equals(((IInstanceSpecification) model).getName()) )) {
//////                            System.out.println("関連" +link_instance.getClassifier().getName() +"  "+link_instance.getName());
////                        }
////                    }
////                }
////                ISlot[] slot = instanceSpecification.getAllSlots();//インスタンスのスロットの情報を得る
////                for (ISlot s : slot) {//スロットすべてにおいて情報を出力する
//////                    System.out.print("属性" + s.getDefiningAttribute());
//////                    System.out.print("属性test" + s.getDefiningAttribute().getName());
////
//////                    System.out.println(" " + s.getValue());
////                }
//////                System.out.println("-----------");//次のインスタンスへ
////            }}
////    }
////    private static void getAllinfomation(ArrayList<IPresentation> presentations){//(astahに依存しないコードにはいらない)
////        for (IPresentation p : presentations){
////            getinfomation(p);
//////            printPresentationInfo(p);//図要素（インスタンスの情報をprintするための処理）
////        }
////    }
//
////    public static void rupecheck(ArrayList<String> linkedname){//引数はLinkでつながっているインスタンスの名前（Config,Ethernet,Link)　ループ検出のためのメソッド
////        for(int i=0;i<linkedname.size();i+=2){
//////            System.out.println(linkedname.get(i)+"-"+linkedname.get(i+1));
////        }
////
////    }
//
//
//
//    //    private void getAllClasses(INamedElement element, List<IClass> classList)//いらない
////            throws ClassNotFoundException, ProjectNotFoundException {
////        if (element instanceof IPackage) {
////            for (INamedElement ownedNamedElement :
////                    ((IPackage) element).getOwnedElements()) {
////                getAllClasses(ownedNamedElement, classList);
////            }
////        } else if (element instanceof IClass) {
////            classList.add((IClass) element);
////            for (IClass nestedClasses : ((IClass) element).getNestedClasses()) {
////                getAllClasses(nestedClasses, classList);
////            }
////        }
////    }
////    private static ArrayList<IInstanceSpecification> getasoociation(IPresentation j){//関連についての両端のインスタンス仕様の情報を得る(astahに依存しないコードにはいらない)
////        if(j instanceof  ILinkPresentation){//IpresentationはILinkPresetationだけでなくIInstancepecificationの情報なども持つ）
////
////            ILinkPresentation _link = (ILinkPresentation) j;//関連船についての情報に限定されているためcastできる
////            IPresentation target = _link.getTargetEnd();//関連で結ばれた先のインスタンスの図の情報を得る
////            IPresentation sorce = _link.getSourceEnd();
////            IElement el =target.getModel();//先ほど得られた図の情報をモデルの情報に帰る
////            IElement el2 =sorce.getModel();
////            ArrayList<IInstanceSpecification> association = new ArrayList<>();//関連のリスト startとendで添え字が一致しているものが関連
////            if(el instanceof IInstanceSpecification) {
////                IInstanceSpecification ek = (IInstanceSpecification) el;//インスタンスン情報に帰る
////                association.add(ek);//関連でつながれたものが隣同士になるようにリストに追加する
////            }
////            if(el2 instanceof  IInstanceSpecification) {
////                IInstanceSpecification ek2 = (IInstanceSpecification) el2;
////                association.add(ek2);
////            }
//////           System.out.println("関連"+ ((IInstanceSpecification) ek).getName()+"-"+(((IInstanceSpecification) ek2).getName()));
////            return  association;
//////
////        }
////
////
////        return  null;
////    }
//
//
//
////    public static void getinfomation(IPresentation p){//図のインスタンスについての情報とその関連情報を得るためのメソッド　（いらない）
////        //Ipresentationはインスタンスや関連線のうちいずれか一つを表す
////        if(p instanceof  INodePresentation){//得られた情報が図の情報（インスタンスやクラス）の時）
////            IElement model = p.getModel();//図の情報についてモデルを得る
////            if(model instanceof IInstanceSpecification){//モデルがインスタンスだった時
////            INodePresentation node = (INodePresentation) p;//関連についての情報を得るためにcastする
////            IInstanceSpecification instanceSpecification = (IInstanceSpecification) model;//得られてた図のインスタンス情報を得られるようになった．
////
////            .out.print("名前" + instanceSpecification.getClassifier().getName());//インスタンスのもとになっているクラスの名前
////            System.out.println("  " + instanceSpecification.getName());//インスタンスの名前
////            ILinkPresentation[] _link = node.getLinks();//得られたインスタンスの関連となっているインスタンスの情報
////                for(ILinkPresentation l:_link){//すべての関連について調べるためのfor文
////                    IElement element= l.getTargetEnd().getModel();//両端の図のモデルを入手する
////                    IElement elementstart= l.getSourceEnd().getModel();
////                    if(element instanceof  IInstanceSpecification){//インスタンスの時に情報をとりたい，片側
////                        IInstanceSpecification link_instance = (IInstanceSpecification) element;//インスタンスにcast
////                        if(!(link_instance.getName().equals(((IInstanceSpecification) model).getName()) )) {//自分のことじゃないとき
////                            System.out.println("関連" +link_instance.getClassifier().getName() +"  "+link_instance.getName());
////                        }
////                    }
////                    if(elementstart instanceof  IInstanceSpecification){//反対側も
////                        IInstanceSpecification link_instance = (IInstanceSpecification) elementstart;
////                        if(!(link_instance.getName().equals(((IInstanceSpecification) model).getName()) )) {
////                            System.out.println("関連" +link_instance.getClassifier().getName() +"  "+link_instance.getName());
////                        }
////                    }
////                }
////            ISlot[] slot = instanceSpecification.getAllSlots();//インスタンスのスロットの情報を得る
////            for (ISlot s : slot) {//スロットすべてにおいて情報を出力する
////            System.out.print("属性" + s.getDefiningAttribute());
////            System.out.print("属性test" + s.getDefiningAttribute().getName());
////
////            System.out.println(" " + s.getValue());
////            }
////            System.out.println("-----------");//次のインスタンスへ
////        }}
////    }
////     private static void getAllinfomation(ArrayList<IPresentation> presentations){
////        for (IPresentation p : presentations){
////            getinfomation(p);
//////            printPresentationInfo(p);//図要素（インスタンスの情報をprintするための処理）
////        }
////     }
//
//
//
//
////    private static void printInstanceSpecification(IInstanceSpecification instanceSpecification) {
////        ISlot[] slots = instanceSpecification.getAllSlots();
////        instancenameList.add(instanceSpecification.getName());
//////        System.out.println("!!!!!!!!!!!!!!!!!!!!");
//////        System.out.println(instanceSpecification.getName());
////        for (ISlot s :slots){
////            IAttribute attribute = s.getDefiningAttribute();
////            String value = s.getValue();
//////            System.out.println(value+"これはvalue");
//////            System.out.println(attribute+"これはattribute");
//////            System.out.println("-----------------------------");
//
//
////        }}
//
//
//
//
//    @Override
//    public void projectChanged(ProjectEvent e) {
//    }
//
//    @Override
//    public void projectClosed(ProjectEvent e) {
//    }
//
//    @Override
//    public void projectOpened(ProjectEvent e) {
//
//    }
//
//    @Override
//    public void addSelectionListener(ISelectionListener listener) {
//    }
//
//    @Override
//    public Component getComponent() {
//        return this;
//    }
//
//    @Override
//    public String getDescription() {
//        return "Show Hello World here";
//    }
//
//    @Override
//    public String getTitle() {
//        return "Stp View";
//    }
//
//    public void activated() {
//    }
//
//    public void deactivated() {
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {//アクションが発生（ボタンが押される）と呼び出される
////        System.out.println("ボタン完成");
//        add(createLabelPane(), BorderLayout.CENTER);//拡張タブの下側のパネルを作る
//    }
//
//
//}
