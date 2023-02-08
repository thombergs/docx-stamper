package org.wickedsource.docxstamper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.AClass;
import org.wickedsource.docxstamper.context.Grade;
import org.wickedsource.docxstamper.context.SchoolContext;
import org.wickedsource.docxstamper.context.Student;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RepeatDocPartNestingTest extends AbstractDocx4jTest {

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
	public void test() throws Docx4JException, IOException {
		SchoolContext schoolContext = new SchoolContext("South Park Primary School");
		for (int i = 0; i < numberOfGrades; i++) {
			schoolContext.getGrades().add(createOneGrade(i));
		}
		InputStream template = getClass().getResourceAsStream("RepeatDocPartNestingTest.docx");
		WordprocessingMLPackage document = stampAndLoad(template, schoolContext);

		documentContent = document.getMainDocumentPart().getContent();
		// check object's num
		int expectObjects = initParagraphsNumber + schoolNameTitle + numberOfGrades * (gradeTitle + numberOfClasses * (classTitle + numberOfStudents)) + lastParagraphTitle;
		Assert.assertEquals(expectObjects, documentContent.size());

		int index = 2; // skip init paragraphs
		// check school name
		checkParagraphContent(schoolContext.getSchoolName(), index++);
		for (Grade grade : schoolContext.getGrades()) {
			// check grade name
			String expected = String.format("Grade No.%d there are %d classes",grade.getNumber(),grade.getClasses().size());
			checkParagraphContent(expected, index++);
			for (AClass aClass : grade.getClasses()) {
				// check class name
				expected = String.format("Class No.%d there are %d students", aClass.getNumber(), aClass.getStudents()
						.size());
				checkParagraphContent(expected, index++);
				// check the student's list
				for (Student s : aClass.getStudents()) {
					Object object = XmlUtils.unwrap(documentContent.get(index++));
					final List<Tc> cells = new ArrayList<>();
					DocumentWalker cellWalker = new BaseDocumentWalker((ContentAccessor) object) {
						@Override
						protected void onTableCell(Tc tableCell) {
							cells.add(tableCell);
						}
					};
					cellWalker.walk();
					Assert.assertEquals(3, cells.size());
					Assert.assertEquals(String.valueOf(s.getNumber()), new ParagraphWrapper((P) cells.get(0)
							.getContent()
							.get(0)).getText());
					Assert.assertEquals(s.getName(), new ParagraphWrapper((P) cells.get(1).getContent()
							.get(0)).getText());
					Assert.assertEquals(String.valueOf(s.getAge()), new ParagraphWrapper((P) cells.get(2)
							.getContent()
							.get(0)).getText());

				}
			}
		}
		checkParagraphContent(String.format("There are %d grades.", numberOfGrades), index);

	}

	private void checkParagraphContent(String expected, int index) {
		Object object = XmlUtils.unwrap(documentContent.get(index));
		P paragraph = (P) object;
		Assert.assertEquals(expected, new ParagraphWrapper(paragraph).getText());
	}

	private Grade createOneGrade(int number) {

		Grade grade = new Grade(number);
		for (int i = 0; i < numberOfClasses; i++) {
			grade.getClasses().add(createOneClass(i));
		}
		return grade;
	}

	private AClass createOneClass(int classNumber) {
		AClass aClass = new AClass(classNumber);
		for (int i = 0; i < 5; i++) {
			aClass.getStudents().add(findOneStudent(i));
		}
		return aClass;
	}

	private Student findOneStudent(int i) {
		return new Student(i, "Bruce·No" + i, 1 + i);
	}

}

