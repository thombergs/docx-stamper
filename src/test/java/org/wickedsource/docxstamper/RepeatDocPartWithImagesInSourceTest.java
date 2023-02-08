package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RepeatDocPartWithImagesInSourceTest extends AbstractDocx4jTest {
    @Test
    public void shouldReplicateImageFromTheMainDocumentInTheSubTemplate() throws Docx4JException, IOException {
        Map<String, Object> context = new HashMap<>();
        ArrayList<Map<String, Object>> subDocParts = new ArrayList<>();

        Map<String, Object> firstPart = new HashMap<>();
        firstPart.put("name", "first doc part");
        subDocParts.add(firstPart);

        Map<String, Object> secondPart = new HashMap<>();
        secondPart.put("name", "second doc part");
        subDocParts.add(secondPart);

        context.put("subDocParts", subDocParts);

        DocxStamperConfiguration config = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer((StandardEvaluationContext ctx) -> ctx.addPropertyAccessor(new MapAccessor()));

        InputStream template = getClass().getResourceAsStream("RepeatDocPartWithImagesInSourceTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context, config);

        Assert.assertEquals(document.getMainDocumentPart().getContent().size(), 11);

        Assert.assertEquals(document.getMainDocumentPart().getContent().get(0).toString(), "This is not repeated");
        Assert.assertEquals(document.getMainDocumentPart().getContent().get(2).toString(), "This should be repeated : first doc part");
        Assert.assertEquals(document.getMainDocumentPart().getContent().get(4).toString(), "This should be repeated too");
        Assert.assertEquals(document.getMainDocumentPart().getContent().get(5).toString(), "This should be repeated : second doc part");
        Assert.assertEquals(document.getMainDocumentPart().getContent().get(7).toString(), "This should be repeated too");
        Assert.assertEquals(document.getMainDocumentPart().getContent().get(9).toString(), "Not this");

        Drawing image1 = (Drawing) XmlUtils.unwrap(((ContentAccessor) ((ContentAccessor) document.getMainDocumentPart().getContent().get(3)).getContent().get(0)).getContent().get(0));
        Drawing image2 = (Drawing) XmlUtils.unwrap(((ContentAccessor) ((ContentAccessor) document.getMainDocumentPart().getContent().get(6)).getContent().get(0)).getContent().get(0));

        String image1RelId = DocumentUtil.getImageRelationshipId(image1);
        String image2RelId = DocumentUtil.getImageRelationshipId(image2);

        Part image1RelPart = document.getMainDocumentPart().getRelationshipsPart().getPart(image1RelId);
        Part image2RelPart = document.getMainDocumentPart().getRelationshipsPart().getPart(image2RelId);

        Assert.assertNotNull(image1RelPart);
        Assert.assertNotNull(image2RelPart);

        Assert.assertNotEquals(document.getSourcePartStore().getPartSize(image1RelPart.getPartName().getName().substring(1)), 0);
        Assert.assertNotEquals(document.getSourcePartStore().getPartSize(image2RelPart.getPartName().getName().substring(1)), 0);
    }
}
