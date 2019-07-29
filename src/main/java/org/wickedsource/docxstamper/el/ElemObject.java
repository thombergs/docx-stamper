package org.wickedsource.docxstamper.el;

public class ElemObject {

    /*循环index*/
    private Integer count;
    /*是否是循环*/
    private boolean isEach;

    public ElemObject() {
    }

    public ElemObject(Integer count, boolean isEach) {
        this.count = count;
        this.isEach = isEach;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public boolean isEach() {
        return isEach;
    }

    public void setEach(boolean each) {
        isEach = each;
    }
}
