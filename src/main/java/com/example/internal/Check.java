package com.example.internal;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import com.example.classes.*;
import com.example.element.ClassElement;
import com.example.element.LinkElement;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;


import java.awt.*;
import java.util.*;
import java.util.List;

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
                ;
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
    public static void ipAddressDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances) {
        ArrayList<String> ipAddressList = new ArrayList<>();//ipAddressのリスト
        ArrayList<ClassElement> ipAddresslistinstance = new ArrayList<>();//ipAddressノリスとの対応したinstance
        ArrayList<ClassElement> ipAddressDuplicationinstances = new ArrayList<>();
        ;//重複したipaddressのインスタンス
        ArrayList<String> ipWarningStatements = new ArrayList<>();//エラー文
        for (ClassElement instance : instances) {//すべてのIPアドレスを取得してipAddressListに格納する。他のIpRouteやOspfinterfacesettingは重複して良いため保留
            if (instance instanceof Clients) {
                ipAddressList.add(((Clients) instance).getIpAddress());
            } else if (instance instanceof EthernetSetting) {
                ipAddressList.add(((EthernetSetting) instance).getIpAddress());
                ipAddresslistinstance.add(instance);
            } else if (instance instanceof VlanSetting) {
                ipAddressList.add(((VlanSetting) instance).getIpAddress());
                ipAddresslistinstance.add(instance);
            }//ここまででipAddressをまとめたリストが完成している
            //重複と重複している箇所とを句呈する
        }

        for (int i = 0; i < ipAddressList.size(); i++) {
            String ipAddress = ipAddressList.get(i);
            for (int j = i + 1; j < ipAddressList.size(); j++) {
                if (!(ipAddress.equals(""))) {
                    String ipAddress2 = ipAddressList.get(j);
                    if (ipAddress.equals(ipAddress2)) {
                        ipWarningStatements.add(ipAddresslistinstance.get(i).getName() + "と" + ipAddresslistinstance.get(j).getName() + "のipAddressが重複しています");//astahにエラー文を表示するためのメソッド
                        ipAddressDuplicationinstances.add(ipAddresslistinstance.get(i));//重複しているインスタンスをまとめたリストに加える
                        ipAddressDuplicationinstances.add(ipAddresslistinstance.get(j));
                    }
                }
            }
        }//ipaddressduplicationinstanceには重複したインスタンスが入っている
        //ここからはastahに文章を出力したり、色を変更したりするプログラム
        for (ClassElement ipaddressduplicationinstance : ipAddressDuplicationinstances) {
            try {
                changeColor(ipaddressduplicationinstance, orangered);
            } catch (InvalidEditingException e) {
                throw new RuntimeException(e);
            }
        }
        for (String iperrorstatement : ipWarningStatements) {
            textarea.append(iperrorstatement + "\n");
        }
    }

    /*vlanの重複をチェックするメソッド*/
    public static void vlanDuplicationCheck(TextArea textarea, ArrayList<ClassElement> instances) {//コンフィグが直接つながっていないのに同じVlan番号が振られている。
        ArrayList<Vlan> vlanlists = new ArrayList<>();
        ArrayList<Integer> vlannumbers = new ArrayList<>();
        for (ClassElement instance : instances) {
            if (instance instanceof Vlan) {
                vlanlists.add((Vlan) instance);
                vlannumbers.add(((Vlan) instance).getNum());
                textarea.append(((Vlan) instance).getNum() + ",");
            }
        }
        ArrayList<Integer> duplicationVlanNumber = new ArrayList<>();
        ArrayList<Vlan> duplicationVlan = new ArrayList<>();

//        for(int i = 0; i<vlannumbers.size();i++){
        while (vlannumbers.size() != 0) {
//            System.out.println("vlannumbers" + vlannumbers);
            duplicationVlan.clear();
            duplicationVlanNumber.clear();
            int number = vlannumbers.get(0);
//            System.out.println("number" + number);
            for (int j = 0; j < vlannumbers.size(); j++) {
                if (number == vlannumbers.get(j)) {
                    duplicationVlanNumber.add(vlannumbers.get(j));
                    duplicationVlan.add(vlanlists.get(j));
                    vlannumbers.remove(j);
                    vlanlists.remove(j);
                    j--;
                }
            }
            if (duplicationVlanNumber.size() != 0) {
                //duplicationvlannumberとduplicationvlanに重複したvlanが格納されている
                for (Vlan checkVlan : duplicationVlan) {
                    Config config = checkVlan.getConfig();
                    ArrayList<EthernetSetting> ethernetSettings = config.getEthernetSetting();
                    for (EthernetSetting ethernetSetting : ethernetSettings) {
                        Link link = ethernetSetting.getLink();
                        for (LinkableElement linked : link.getLinkableElement()) {
                            EthernetSetting ethernetSettingTarget;
                            if (linked != ethernetSetting) {
                                ethernetSettingTarget = (EthernetSetting) linked;
                            }
                            if (duplicationVlan.contains(((EthernetSetting) linked).getConfig().getVlan())) {
                                //これは重複して良い

                            }
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
    public static Integer  rupeDfs(ArrayList<ArrayList<Integer>> graph , ArrayList<Integer> seen,ArrayList<Integer> finished, Stack<Integer> hist, int v, int p,ArrayList<Integer> s , ArrayList<Integer> q){
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
                    System.out.println(rupes.getName());
                    changeColor((ClassElement) rupes, orangered);
                } catch (InvalidEditingException e) {
                    throw new RuntimeException(e);
                }
            }
        }



        return rupeconfigs;
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



}
