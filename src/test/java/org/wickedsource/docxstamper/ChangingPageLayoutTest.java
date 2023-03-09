package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ChangingPageLayoutTest {
	@Test
	public void shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment() throws IOException, Docx4JException {
		var context = new HashMap<String, Object>();

		var name1 = new Name("Homer");
		var name2 = new Name("Marge");

		var repeatValues = new ArrayList<>();
		repeatValues.add(name1);
		repeatValues.add(name2);

		context.put("repeatValues", repeatValues);

		var template = getClass().getResourceAsStream("ChangingPageLayoutOutsideRepeatParagraphTest.docx");
		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));
		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var result = stamper.stampAndLoad(template, context);

		var content = result.getMainDocumentPart().getContent();

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertTrue(((R) ((P) content.get(5)).getContent().get(23)).getContent().get(0) instanceof Br);
		assertTrue(((R) ((P) content.get(8)).getContent().get(23)).getContent().get(0) instanceof Br);

		assertNull(((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient());

		assertThatNoCommentOrReferenceRemains(result);
	}

	public void assertThatNoCommentOrReferenceRemains(WordprocessingMLPackage document) {
		new BaseDocumentWalker(document.getMainDocumentPart()) {
			@Override
			protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
				fail("Found a remaining comment range start !");
			}

			@Override
			protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
				fail("Found a remaining comment range end !");
			}

			@Override
			protected void onCommentReference(R.CommentReference commentReference) {
				fail("Found a remaining comment reference !");
			}
		}.walk();
	}

	@Test
	public void shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment() throws IOException, Docx4JException {
		Map<String, Object> context = new HashMap<>();

		Name name1 = new Name("Homer");
		Name name2 = new Name("Marge");

		List<Name> repeatValues = new ArrayList<>();
		repeatValues.add(name1);
		repeatValues.add(name2);

		context.put("repeatValues", repeatValues);

		var template = getClass().getResourceAsStream("ChangingPageLayoutInRepeatParagraphTest.docx");
		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));
		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var result = stamper.stampAndLoad(template, context);

		var content = result.getMainDocumentPart().getContent();
		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertNull(((P) content.get(5)).getPPr().getSectPr().getPgSz().getOrient());

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(6)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertNull(((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient());

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(11)).getPPr().getSectPr().getPgSz().getOrient()
		);

		assertThatNoCommentOrReferenceRemains(result);
	}

	@Test
	public void shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment() throws IOException, Docx4JException {
		Map<String, Object> context = new HashMap<>();

		Name name1 = new Name("Homer");
		Name name2 = new Name("Marge");

		List<Name> repeatValues = new ArrayList<>();
		repeatValues.add(name1);
		repeatValues.add(name2);

		context.put("repeatValues", repeatValues);

		var template = getClass().getResourceAsStream("ChangingPageLayoutInRepeatDocPartTest.docx");
		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));
		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var result = stamper.stampAndLoad(template, context);

		var content = result.getMainDocumentPart().getContent();
		assertNull(((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient());

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(5)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertNull(((P) content.get(6)).getPPr().getSectPr().getPgSz().getOrient());

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertNull(((P) content.get(11)).getPPr().getSectPr().getPgSz().getOrient());

		assertThatNoCommentOrReferenceRemains(result);
	}

	@Test
	public void shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement() throws IOException, Docx4JException {
		Map<String, Object> context = new HashMap<>();

		Name name1 = new Name("Homer");
		Name name2 = new Name("Marge");

		List<Name> repeatValues = new ArrayList<>();
		repeatValues.add(name1);
		repeatValues.add(name2);

		context.put("repeatValues", repeatValues);

		InputStream template = getClass().getResourceAsStream(
				"ChangingPageLayoutInRepeatDocPartWithTableLastElementTest.docx");
		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var result = stamper.stampAndLoad(template, context);

		var content = result.getMainDocumentPart().getContent();
		assertNull(((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient());

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(5)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertNull(((P) content.get(8)).getPPr().getSectPr().getPgSz().getOrient());

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(11)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertNull(((P) content.get(14)).getPPr().getSectPr().getPgSz().getOrient());

		assertThatNoCommentOrReferenceRemains(result);
	}

	@Test
	public void shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment() throws IOException, Docx4JException {
		Map<String, Object> context = new HashMap<>();

		Name name1 = new Name("Homer");
		Name name2 = new Name("Marge");

		List<Name> repeatValues = new ArrayList<>();
		repeatValues.add(name1);
		repeatValues.add(name2);

		context.put("repeatValues", repeatValues);

		var template = getClass().getResourceAsStream("ChangingPageLayoutOutsideRepeatDocPartTest.docx");
		var config = new DocxStamperConfiguration()
				.setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

		var stamper = new TestDocxStamper<Map<String, Object>>(config);
		var result = stamper.stampAndLoad(template, context);

		var content = result.getMainDocumentPart().getContent();

		assertEquals(
				STPageOrientation.LANDSCAPE,
				((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient()
		);
		assertTrue(((R) ((P) content.get(4)).getContent().get(0)).getContent().get(0) instanceof Br);

		assertNull(((P) content.get(5)).getPPr());
		assertNull(((P) content.get(6)).getPPr());

		assertTrue(((R) ((P) content.get(7)).getContent().get(0)).getContent().get(0) instanceof Br);

		assertNull(((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient());

		assertThatNoCommentOrReferenceRemains(result);
	}

	public record Name(String name) {
	}
}
