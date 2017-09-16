package org.wickedsource.docxstamper.api.coordinates;

public abstract class AbstractCoordinates {

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
