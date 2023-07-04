package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class RepeatDocPartAndCommentProcessorsIsolationTest {

	@Test
	public void repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate() {
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
		var document = stamper.stampAndLoadAndExtract(template, context);

		List<String> expected = List.of(
				"This will stay untouched.//rPr={}",
				"",
				"firstTable value1",
				"firstTable value2",
				"",
				"This will also stay untouched.//rPr={}",
				"",
				"Repeating paragraph :",
				"",
				"repeatDocPart value1",
				"Repeating paragraph :",
				"",
				"repeatDocPart value2",
				"Repeating paragraph :",
				"",
				"repeatDocPart value3",
				"",
				"secondTable value1",
				"secondTable value2",
				"secondTable value3",
				"secondTable value4",
				"",
				"This will stay untouched too.//rPr={}"
		);
		assertIterableEquals(expected, document);
	}

	static class TableValue {
		public String value;

		TableValue(String value) {
			this.value = value;
		}
	}
}
