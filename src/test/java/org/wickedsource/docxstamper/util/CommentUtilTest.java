package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Utilities - Comments")
class CommentUtilTest {
	@Test
	void onlyParagraphsWithCommentRangeStartAreCommented() throws Docx4JException {
		var in = getClass().getResourceAsStream("CommentUtilTest.docx");
		var document = WordprocessingMLPackage.load(in);

		P p1 = (P) document.getMainDocumentPart().getContent().get(0);
		P p2 = (P) document.getMainDocumentPart().getContent().get(1);
		P p3 = (P) document.getMainDocumentPart().getContent().get(3);
		P p4 = (P) document.getMainDocumentPart().getContent().get(4);
		P p5 = (P) document.getMainDocumentPart().getContent().get(5);

		assertTrue(CommentUtil.getCommentFor(p1, document).isEmpty());
		assertEquals("Comment for paragraph 2.", CommentUtil.getCommentStringFor(p2, document));
		assertEquals("Comment for paragraph 3.", CommentUtil.getCommentStringFor(p3, document));
		assertTrue(CommentUtil.getCommentFor(p4, document).isEmpty());
		assertTrue(CommentUtil.getCommentFor(p5, document).isEmpty());
	}
}
