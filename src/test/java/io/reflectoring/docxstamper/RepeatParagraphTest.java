package io.reflectoring.docxstamper;

import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.context.Character;
import io.reflectoring.docxstamper.util.walk.BaseCoordinatesWalker;
import io.reflectoring.docxstamper.util.walk.CoordinatesWalker;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import io.reflectoring.docxstamper.context.CharactersContext;
import io.reflectoring.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RepeatParagraphTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CharactersContext context = new CharactersContext();
        context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
        context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
        context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
        context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
        context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
        context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));
        InputStream template = getClass().getResourceAsStream("RepeatParagraphTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        final List<ParagraphCoordinates> titleCoords = new ArrayList<>();
        final List<ParagraphCoordinates> quotationCoords = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                if ("Titre2".equals(paragraphCoordinates.getParagraph().getPPr().getPStyle().getVal())) {
                    titleCoords.add(paragraphCoordinates);
                } else if ("Quotations".equals(paragraphCoordinates.getParagraph().getPPr().getPStyle().getVal())) {
                    quotationCoords.add(paragraphCoordinates);
                }
            }
        };
        walker.walk();

        // 6 titles and 6 quotations are expected
        Assert.assertEquals(6, titleCoords.size());
        Assert.assertEquals(6, quotationCoords.size());

        // Check paragraph's content
        Assert.assertEquals("Homer Simpson", new ParagraphWrapper(titleCoords.get(0).getParagraph()).getText());
        Assert.assertEquals("Dan Castellaneta", new ParagraphWrapper(quotationCoords.get(0).getParagraph()).getText());
        Assert.assertEquals("Marge Simpson", new ParagraphWrapper(titleCoords.get(1).getParagraph()).getText());
        Assert.assertEquals("Julie Kavner", new ParagraphWrapper(quotationCoords.get(1).getParagraph()).getText());
        Assert.assertEquals("Bart Simpson", new ParagraphWrapper(titleCoords.get(2).getParagraph()).getText());
        Assert.assertEquals("Nancy Cartwright", new ParagraphWrapper(quotationCoords.get(2).getParagraph()).getText());
        Assert.assertEquals("Kent Brockman", new ParagraphWrapper(titleCoords.get(3).getParagraph()).getText());
        Assert.assertEquals("Harry Shearer", new ParagraphWrapper(quotationCoords.get(3).getParagraph()).getText());
        Assert.assertEquals("Disco Stu", new ParagraphWrapper(titleCoords.get(4).getParagraph()).getText());
        Assert.assertEquals("Hank Azaria", new ParagraphWrapper(quotationCoords.get(4).getParagraph()).getText());
        Assert.assertEquals("Krusty the Clown", new ParagraphWrapper(titleCoords.get(5).getParagraph()).getText());
        Assert.assertEquals("Dan Castellaneta", new ParagraphWrapper(quotationCoords.get(5).getParagraph()).getText());
    }


}
