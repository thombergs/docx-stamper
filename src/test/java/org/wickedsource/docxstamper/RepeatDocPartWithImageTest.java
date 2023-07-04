package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class RepeatDocPartWithImageTest {
	@Test
	public void shouldImportImageDataInTheMainDocument() throws IOException {
		var context = Map.of(
				"units",
				Stream.of(new Image(getClass().getResourceAsStream("butterfly.png")),
						  new Image(getClass().getResourceAsStream("map.jpg")))
					  .map(image -> Map.of("coverImage", image))
					  .map(map -> Map.of("productionFacility", map))
					  .toList()
		);

		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));
		var stamper = new TestDocxStamper<Map<String, ?>>(config);
		var template = getClass().getResourceAsStream("RepeatDocPartWithImageTest.docx");

		var actual = stamper.stampAndLoadAndExtract(template, context);

		var expected = List.of(
				"//",
				"rId11:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=//",
				"rId12:image/jpeg:407.5kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=//",
				"//",
				"//",
				"//",
				"Always rendered://",
				"rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=//",
				"//"
		);
		assertIterableEquals(expected, actual);
	}
}
