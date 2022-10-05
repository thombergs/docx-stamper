package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MultiSectionTest extends AbstractDocx4jTest {

	@Test
	public void expressionsInMultipleSections() throws Docx4JException, IOException {
		DocxStamper<NamesContext> stamper = new DocxStamper<>(new DocxStamperConfiguration());
		NamesContext context = new NamesContext("Homer", "Marge");

		InputStream template = getClass().getResourceAsStream("MultiSectionTest.docx");
		OutputStream out = getOutputStream();
		stamper.stamp(template, context, out);
		InputStream in = getInputStream(out);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
		assertTableRows(document);

	}

	private void assertTableRows(WordprocessingMLPackage document) {
		final List<R> runs = DocumentUtil.extractElements(document, R.class);
		Assert.assertTrue(runs.stream().map(TextUtils::getText).anyMatch(s -> s.contains("Homer")));
		Assert.assertTrue(runs.stream().map(TextUtils::getText).anyMatch(s -> s.contains("Marge")));
	}

	public static class NamesContext {
		private final String firstName;
		private final String secondName;

		public NamesContext(String firstName, String secondName) {
			this.firstName = firstName;
			this.secondName = secondName;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getSecondName() {
			return secondName;
		}
	}

}
