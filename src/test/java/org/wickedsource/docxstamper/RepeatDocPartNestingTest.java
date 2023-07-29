package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RepeatDocPartNestingTest {

    int initParagraphsNumber = 2;
    int schoolNameTitle = 1;
    int numberOfGrades = 3;
    int gradeTitle = 1;
    int numberOfClasses = 3;
    int classTitle = 1;
    int numberOfStudents = 5;
    int lastParagraphTitle = 1;

    private List<Object> documentContent = null;

    @Test
    void test() throws Docx4JException, IOException {
        var schoolContext = new SchoolContext(
                "South Park Primary School",
                IntStream.range(0, numberOfGrades).mapToObj(this::createOneGrade).toList()
        );
        var template = getClass().getResourceAsStream("RepeatDocPartNestingTest.docx");
        var stamper = new TestDocxStamper<SchoolContext>();
        var document = stamper.stampAndLoad(template, schoolContext);

        documentContent = document.getMainDocumentPart().getContent();
        // check object's num
        int expectObjects = initParagraphsNumber + schoolNameTitle + numberOfGrades * (gradeTitle + numberOfClasses * (classTitle + numberOfStudents)) + lastParagraphTitle;
        assertEquals(expectObjects, documentContent.size());

        int index = 2; // skip init paragraphs
        // check school name
        checkParagraphContent(schoolContext.schoolName(), index++);
        for (Grade grade : schoolContext.grades()) {
            // check grade name
            String expected = String.format("Grade No.%d there are %d classes",
                    grade.number(),
                    grade.classes().size());
            checkParagraphContent(expected, index++);
            for (AClass aClass : grade.classes()) {
                // check class name
                expected = String.format("Class No.%d there are %d students", aClass.number(), aClass.students()
                        .size());
                checkParagraphContent(expected, index++);
                // check the student's list
                for (Student s : aClass.students()) {
                    Object object = XmlUtils.unwrap(documentContent.get(index++));
                    final List<Tc> cells = new ArrayList<>();
                    DocumentWalker cellWalker = new BaseDocumentWalker((ContentAccessor) object) {
                        @Override
                        protected void onTableCell(Tc tableCell) {
                            cells.add(tableCell);
                        }
                    };
                    cellWalker.walk();
                    assertEquals(3, cells.size());
                    assertEquals(String.valueOf(s.number()), new ParagraphWrapper((P) cells.get(0)
                            .getContent()
                            .get(0)).getText());
                    assertEquals(s.name(), new ParagraphWrapper((P) cells.get(1).getContent()
                            .get(0)).getText());
                    assertEquals(String.valueOf(s.age()), new ParagraphWrapper((P) cells.get(2)
                            .getContent()
                            .get(0)).getText());

                }
            }
        }
        checkParagraphContent(String.format("There are %d grades.", numberOfGrades), index);

    }

    private Grade createOneGrade(int number) {
        return new Grade(
                number,
                IntStream.range(0, numberOfClasses).mapToObj(this::createOneClass).toList()
        );
    }

    private void checkParagraphContent(String expected, int index) {
        Object object = XmlUtils.unwrap(documentContent.get(index));
        P paragraph = (P) object;
        assertEquals(expected, new ParagraphWrapper(paragraph).getText());
    }

    private AClass createOneClass(int classNumber) {
        var students = range(0, 5).mapToObj(this::findOneStudent).toList();
        return new AClass(classNumber, students);
    }

    private Student findOneStudent(int i) {
        return new Student(i, "Bruce·No" + i, 1 + i);
    }

    public record AClass(int number, List<Student> students) {
    }

    record Student(int number, String name, int age) {
    }

    record SchoolContext(String schoolName, List<Grade> grades) {
    }

    record Grade(int number, List<AClass> classes) {
    }
}

