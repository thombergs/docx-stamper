package org.wickedsource.docxstamper.context;

import java.util.ArrayList;
import java.util.List;

public class SchoolContext {
    public SchoolContext() {
    }

    String schoolName;
    List<Grade> grades = new ArrayList<>();

    public SchoolContext(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public List<Grade> getGrades() {
        return grades;
    }

}
