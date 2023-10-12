package com.example.element;

import com.change_vision.jude.api.inf.presentation.ILinkPresentation;

public class LinkElement{//線の情報
    private ClassElement sourceEnd;//つながっているインスタンス
    private ClassElement targetEnd;//つながっているインスタンス

    public ILinkPresentation getLinkPresentation() {
        return linkPresentation;
    }

    public void setLinkPresentation(ILinkPresentation linkPresentation) {
        this.linkPresentation = linkPresentation;
    }

    private ILinkPresentation linkPresentation;//対応するastahAPIの線情報

    public ClassElement getSourceEnd() {
        return sourceEnd;
    }

    public void setSourceEnd(ClassElement sourceEnd) {
        this.sourceEnd = sourceEnd;
    }

    public ClassElement getTargetEnd() {
        return targetEnd;
    }

    public void setTargetEnd(ClassElement targetEnd) {
        this.targetEnd = targetEnd;
    }
}
