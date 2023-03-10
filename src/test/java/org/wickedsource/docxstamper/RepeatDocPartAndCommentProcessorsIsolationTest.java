package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatDocPartAndCommentProcessorsIsolationTest {

	@Test
	public void repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate() throws Docx4JException, IOException {
		var context = new HashMap<String, Object>();

		var firstTable = new ArrayList<TableValue>();
		firstTable.add(new TableValue("firstTable value1"));
		firstTable.add(new TableValue("firstTable value2"));

		var secondTable = new ArrayList<TableValue>();
		secondTable.add(new TableValue("repeatDocPart value1"));
		secondTable.add(new TableValue("repeatDocPart value2"));
		secondTable.add(new TableValue("repeatDocPart value3"));

		List<TableValue> thirdTable = new ArrayList<>();
		thirdTable.add(new TableValue("secondTable value1"));
		thirdTable.add(new TableValue("secondTable value2"));
		thirdTable.add(new TableValue("secondTable value3"));
		thirdTable.add(new TableValue("secondTable value4"));

		context.put("firstTable", firstTable);
		context.put("secondTable", secondTable);
		context.put("thirdTable", thirdTable);

		var template = getClass().getResourceAsStream("RepeatDocPartAndCommentProcessorsIsolationTest.docx");
		var config = new DocxStamperConfiguration();
		config.setEvaluationContextConfigurer((ctx) -> ctx.addPropertyAccessor(new MapAccessor()));

		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var document = stamper.stampAndLoad(template, context);

		var documentContent = document.getMainDocumentPart().getContent();

		assertEquals(19, documentContent.size());

		assertEquals("This will stay untouched.", documentContent.get(0).toString());
		assertEquals("This will also stay untouched.", documentContent.get(4).toString());
		assertEquals("This will stay untouched too.", documentContent.get(18).toString());

		// checking table before repeating paragraph
		Tbl table1 = (Tbl) XmlUtils.unwrap(documentContent.get(2));
		checkTableAgainstContextValues(firstTable, table1);

		// checking repeating paragraph
		assertEquals("Repeating paragraph :", documentContent.get(6).toString());
		assertEquals("Repeating paragraph :", documentContent.get(9).toString());
		assertEquals("Repeating paragraph :", documentContent.get(12).toString());

		assertEquals("repeatDocPart value1", documentContent.get(8).toString());
		assertEquals("repeatDocPart value2", documentContent.get(11).toString());
		assertEquals("repeatDocPart value3", documentContent.get(14).toString());

		// checking table after repeating paragraph
		Tbl table2 = (Tbl) XmlUtils.unwrap(documentContent.get(16));
		checkTableAgainstContextValues(thirdTable, table2);
	}

	private static void checkTableAgainstContextValues(List<TableValue> tableValues, Tbl docxTable) {
		assertEquals(tableValues.size(), docxTable.getContent().size());
		for (int i = 0; i < tableValues.size(); i++) {
			Tr row = (Tr) docxTable.getContent().get(i);
			assertEquals(1, row.getContent().size());

			Tc cell = (Tc) XmlUtils.unwrap(row.getContent().get(0));
			String expected = tableValues.get(i).value;
			assertEquals(1, cell.getContent().size());
			assertEquals(expected, cell.getContent().get(0).toString());
		}
	}

	static class TableValue {
		public String value;

		TableValue(String value) {
			this.value = value;
		}
	}
}
