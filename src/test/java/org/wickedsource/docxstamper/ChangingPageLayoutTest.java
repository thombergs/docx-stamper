package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.expression.MapAccessor;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangingPageLayoutTest extends AbstractDocx4jTest {
    @Test
    public void shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment() throws IOException, Docx4JException {
        Map<String, Object> context = new HashMap<>();

        NameContext name1 = new NameContext();
        name1.setName("Homer");

        NameContext name2 = new NameContext();
        name2.setName("Marge");

        List repeatValues = new ArrayList();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);

        InputStream template = getClass().getResourceAsStream("ChangingPageLayoutOutsideRepeatParagraphTest.docx");
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        WordprocessingMLPackage result = stampAndLoad(template, context, config);

        List<Object> content = result.getMainDocumentPart().getContent();

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertTrue(((R) ((P) content.get(5)).getContent().get(23)).getContent().get(0) instanceof Br);
        Assert.assertTrue(((R) ((P) content.get(8)).getContent().get(23)).getContent().get(0) instanceof Br);

        Assert.assertNull(((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient());

        assertThatNoCommentOrReferenceRemains(result);
    }

    @Test
    public void shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment() throws IOException, Docx4JException {
        Map<String, Object> context = new HashMap<>();

        NameContext name1 = new NameContext();
        name1.setName("Homer");

        NameContext name2 = new NameContext();
        name2.setName("Marge");

        List repeatValues = new ArrayList();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);

        InputStream template = getClass().getResourceAsStream("ChangingPageLayoutInRepeatParagraphTest.docx");
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        WordprocessingMLPackage result = stampAndLoad(template, context, config);

        List<Object> content = result.getMainDocumentPart().getContent();
        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertNull(((P) content.get(5)).getPPr().getSectPr().getPgSz().getOrient());

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(6)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertNull(((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient());

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(11)).getPPr().getSectPr().getPgSz().getOrient()
        );

        assertThatNoCommentOrReferenceRemains(result);
    }

    @Test
    public void shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment() throws IOException, Docx4JException {
        Map<String, Object> context = new HashMap<>();

        NameContext name1 = new NameContext();
        name1.setName("Homer");

        NameContext name2 = new NameContext();
        name2.setName("Marge");

        List repeatValues = new ArrayList();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);

        InputStream template = getClass().getResourceAsStream("ChangingPageLayoutInRepeatDocPartTest.docx");
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        WordprocessingMLPackage result = stampAndLoad(template, context, config);

        List<Object> content = result.getMainDocumentPart().getContent();
        Assert.assertNull(((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient());

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(5)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertNull(((P) content.get(6)).getPPr().getSectPr().getPgSz().getOrient());

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertNull(((P) content.get(11)).getPPr().getSectPr().getPgSz().getOrient());

        assertThatNoCommentOrReferenceRemains(result);
    }

    @Test
    public void shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement() throws IOException, Docx4JException {
        Map<String, Object> context = new HashMap<>();

        NameContext name1 = new NameContext();
        name1.setName("Homer");

        NameContext name2 = new NameContext();
        name2.setName("Marge");

        List repeatValues = new ArrayList();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);

        InputStream template = getClass().getResourceAsStream("ChangingPageLayoutInRepeatDocPartWithTableLastElementTest.docx");
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        WordprocessingMLPackage result = stampAndLoad(template, context, config);

        List<Object> content = result.getMainDocumentPart().getContent();
        Assert.assertNull(((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient());

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(5)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertNull(((P) content.get(8)).getPPr().getSectPr().getPgSz().getOrient());

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(11)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertNull(((P) content.get(14)).getPPr().getSectPr().getPgSz().getOrient());

        assertThatNoCommentOrReferenceRemains(result);
    }

    @Test
    public void shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment() throws IOException, Docx4JException {
        Map<String, Object> context = new HashMap<>();

        NameContext name1 = new NameContext();
        name1.setName("Homer");

        NameContext name2 = new NameContext();
        name2.setName("Marge");

        List repeatValues = new ArrayList();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);

        InputStream template = getClass().getResourceAsStream("ChangingPageLayoutOutsideRepeatDocPartTest.docx");
        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        WordprocessingMLPackage result = stampAndLoad(template, context, config);

        List<Object> content = result.getMainDocumentPart().getContent();

        Assert.assertEquals(
                STPageOrientation.LANDSCAPE,
                ((P) content.get(2)).getPPr().getSectPr().getPgSz().getOrient()
        );
        Assert.assertTrue(((R) ((P) content.get(4)).getContent().get(0)).getContent().get(0) instanceof Br);

        Assert.assertNull(((P) content.get(5)).getPPr());
        Assert.assertNull(((P) content.get(6)).getPPr());

        Assert.assertTrue(((R) ((P) content.get(7)).getContent().get(0)).getContent().get(0) instanceof Br);

        Assert.assertNull(((P) content.get(9)).getPPr().getSectPr().getPgSz().getOrient());

        assertThatNoCommentOrReferenceRemains(result);
    }

    public void assertThatNoCommentOrReferenceRemains(WordprocessingMLPackage document) {
        new BaseDocumentWalker(document.getMainDocumentPart()) {
            @Override
            protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
                Assert.fail("Found a remaining comment range start !");
            }

            @Override
            protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
                Assert.fail("Found a remaining comment range end !");
            }

            @Override
            protected void onCommentReference(R.CommentReference commentReference) {
                Assert.fail("Found a remaining comment reference !");
            }
        }.walk();
    }
}
