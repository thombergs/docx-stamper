package io.reflectoring.docxstamper.util;

import io.reflectoring.docxstamper.AbstractDocx4jTest;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;

public class CommentUtilTest extends AbstractDocx4jTest {

    @Test
    public void onlyParagraphsWithCommentRangeStartAreCommented() throws Docx4JException {
        WordprocessingMLPackage document = loadDocument("CommentUtilTest.docx");

        P p1 = (P) document.getMainDocumentPart().getContent().get(0);
        P p2 = (P) document.getMainDocumentPart().getContent().get(1);
        P p3 = (P) document.getMainDocumentPart().getContent().get(3);
        P p4 = (P) document.getMainDocumentPart().getContent().get(4);
        P p5 = (P) document.getMainDocumentPart().getContent().get(5);

        Assert.assertNull(CommentUtil.getCommentFor(p1, document));
        Assert.assertEquals("Comment for paragraph 2.", CommentUtil.getCommentStringFor(p2, document));
        Assert.assertEquals("Comment for paragraph 3.", CommentUtil.getCommentStringFor(p3, document));
        Assert.assertNull(CommentUtil.getCommentFor(p4, document));
        Assert.assertNull(CommentUtil.getCommentFor(p5, document));
    }

}