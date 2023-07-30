package io.reflectoring.docxstamper.context;

import java.util.Date;

public class ListObject{
    private String vorname;
    private String nachname;
    private Date date11;

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public Date getDate11() {
        return date11;
    }

    public void setDate11(Date date11) {
        this.date11 = date11;
    }
}