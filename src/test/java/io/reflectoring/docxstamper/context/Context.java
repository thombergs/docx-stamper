package io.reflectoring.docxstamper.context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Context {
    private String string01;

    private String string02;

    private Date date01;

    private Date date02;

    private int intValue;

    private long longValue;

    private BigDecimal bigDecimalValue;

    private boolean showme;

    private List<ListObject> outputListObject = new ArrayList<>();

    public String getString01() {
        return string01;
    }

    public void setString01(String string01) {
        this.string01 = string01;
    }

    public String getString02() {
        return string02;
    }

    public void setString02(String string02) {
        this.string02 = string02;
    }

    public Date getDate01() {
        return date01;
    }

    public void setDate01(Date date01) {
        this.date01 = date01;
    }

    public Date getDate02() {
        return date02;
    }

    public void setDate02(Date date02) {
        this.date02 = date02;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    public boolean getShowme() {
        return showme;
    }

    public void setShowme(boolean showme) {
        this.showme = showme;
    }

    public List<ListObject> getOutputListObject() {
        return outputListObject;
    }

    public void setOutputListObject(List<ListObject> outputListObject) {
        this.outputListObject = outputListObject;
    }
}
