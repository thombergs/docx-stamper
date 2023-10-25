package pro.verron.docxstamper.utils.context;

import java.util.List;
import java.util.Objects;

public final class SubContext {
    private String value;
    private List<String> li;

    public SubContext() {
    }

    public SubContext(
            String value,
            List<String> li
    ) {
        this.value = value;
        this.li = li;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getLi() {
        return li;
    }

    public void setLi(List<String> li) {
        this.li = li;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubContext) obj;
        return Objects.equals(this.value, that.value) &&
               Objects.equals(this.li, that.li);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, li);
    }

    @Override
    public String toString() {
        return "SubContext[" +
               "value=" + value + ", " +
               "li=" + li + ']';
    }

}
