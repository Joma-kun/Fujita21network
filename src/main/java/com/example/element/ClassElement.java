package com.example.element;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

public class ClassElement {


    public void setElement(IElement element) {
        this.element = element;
    }

    public IElement getElement() {
        return element;
    }

    public IPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(IPresentation presentation) {
        this.presentation = presentation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id = this.toString();
    @JsonIgnore
    private IPresentation presentation;//astahのpresentation情報と結びつける
    @JsonIgnore
    private IElement element;//astahの図と対応づける
    private String name; //instanceの名前　例：cf_Hn
    private String className;//classの名前　例：AccessList

    public ArrayList<ClassElement> getNodeFalseInstances() {
        return nodeFalseInstances;
    }

    public void setNodeFalseInstances(ClassElement nodeFalseinstance) {
        if (!(nodeFalseInstances.contains(nodeFalseinstance))) {
            this.nodeFalseInstances.add(nodeFalseinstance);
        }
    }
    @JsonIgnore
    private ArrayList<ClassElement> nodeFalseInstances = new ArrayList<>();//関連線が多重度や関連先の情報を満たしていない物を格納してある。

    /*errorStatementsが使われている箇所
    * 「true」「false」のチェック*/
    @JsonIgnore
    private ArrayList<String> multiplicityErrorStatements = new ArrayList<>();//多重度エラー　値を変換じに検出するためこうした
    @JsonIgnore
    private ArrayList<String> attributeErrorStatements = new ArrayList<>();
    @JsonIgnore
    private ArrayList<ClassElement> link = new java.util.ArrayList<>();
    @JsonIgnore
    protected ArrayList<Slots> slots;

    public String getClassName() {
        return className;
//        return "class";
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ClassElement> getlink() {
        return link;
    }

    public void setLink(ClassElement instance) {
        link.add(instance);
    }

    public void setSlots(ArrayList<Slots> slots) {
        this.slots = slots;
    }

    public ArrayList<Slots> getSlots() {
        return slots;
    }


    public void setmultiplicityErrorStatement(String errorStatement) {//エラー文
        if (!multiplicityErrorStatements.contains(errorStatement)) {
            multiplicityErrorStatements.add(errorStatement);
        }
    }


    public ArrayList<String> getmultiplictyErrorStatement() {
        return multiplicityErrorStatements;
    }




    public void setAttributeErrorStatement(String errorStatement) {//turuとfalseの属性チェック（AttributeInterityCheckerで使う）
        if (!attributeErrorStatements.contains(errorStatement)) {
            attributeErrorStatements.add(errorStatement);
        }
    }


    public ArrayList<String> getAttributeErrorStatement() {//turuとfalseの属性チェック（AttributeInterityCheckerで使う）
        return attributeErrorStatements;
    }
}
