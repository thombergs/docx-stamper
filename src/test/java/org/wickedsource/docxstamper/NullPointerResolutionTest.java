package org.wickedsource.docxstamper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NullPointerResolutionTest {
	@Test
	public void testThrowingCase() {
		var subContext = new SubContext("Fullish2", List.of("Fullish3", "Fullish4", "Fullish5"));
		var context = new NullishContext("Fullish1", subContext, null, null);
		var template = getClass().getResourceAsStream("NullPointerResolution.docx");

		var stamper = new TestDocxStamper<NullishContext>();

		assertThrows(
				UnresolvedExpressionException.class,
				() -> stamper.stampAndLoadAndExtract(template, context)
		);
	}

	@Test
	public void testWithDefaultSpel() {
		var subContext = new SubContext("Fullish2", List.of("Fullish3", "Fullish4", "Fullish5"));
		var context = new NullishContext("Fullish1", subContext, null, null);
		var template = getClass().getResourceAsStream("NullPointerResolution.docx");

		var config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(false);
		var actual = new TestDocxStamper<NullishContext>(config).stampAndLoadAndExtract(template, context);

		var expected = List.of(
				"Deal with null references",
				"",
				"Deal with: Fullish1",
				"Deal with: Fullish2",
				"Deal with: Fullish3",
				"Deal with: Fullish5",
				"",
				"Deal with: Nullish value!!",
				"Deal with: ${nullish.value ?: \"Nullish value!!\"}",
				"Deal with: ${nullish.li[0] ?: \"Nullish value!!\"}",
				"Deal with: ${nullish.li[2] ?: \"Nullish value!!\"}",
				""
		);
		assertIterableEquals(expected, actual);
	}

	@Test
	public void testWithCustomSpel() {
		var subContext = new SubContext("Fullish2", List.of("Fullish3", "Fullish4", "Fullish5"));
		var context = new NullishContext("Fullish1", subContext, null, null);
		var template = getClass().getResourceAsStream("NullPointerResolution.docx");

		// Beware, this configuration only autogrows pojos and java beans,
		// so it will not work if your type has no default constructor and no setters.
		SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration(true, true);

		var config = new DocxStamperConfiguration()
				.setSpelParserConfiguration(spelParserConfiguration);

		var actual = new TestDocxStamper<NullishContext>(config).stampAndLoadAndExtract(template, context);

		var expected = List.of(
				"Deal with null references",
				"",
				"Deal with: Fullish1",
				"Deal with: Fullish2",
				"Deal with: Fullish3",
				"Deal with: Fullish5",
				"",
				"Deal with: Nullish value!!",
				"Deal with: Nullish value!!",
				"Deal with: Nullish value!!",
				"Deal with: Nullish value!!",
				""
		);
		assertIterableEquals(expected, actual);
	}


	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static final class NullishContext {
		private String fullish_value;
		private SubContext fullish;
		private String nullish_value;
		private SubContext nullish;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static final class SubContext {
		private String value;
		private List<String> li;
	}
}