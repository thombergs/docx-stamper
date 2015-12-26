package org.wickedsource.docxstamper.docx4j.walk.coordinates;

public abstract class ObjectCoordinates {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        return o.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public abstract String toString();

}
