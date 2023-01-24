package org.wickedsource.docxstamper.context;

import java.util.ArrayList;
import java.util.List;


public class AClass {
    public AClass() {
    }

    int number;
    List<Student> students = new ArrayList<>();

    public int getNumber() {
        return number;
    }

    public List<Student> getStudents() {
        return students;
    }

    public AClass(int number) {
        this.number = number;
    }
}