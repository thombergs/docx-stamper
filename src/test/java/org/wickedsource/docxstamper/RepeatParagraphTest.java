package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepeatParagraphTest {
    @Test
    void test() throws Docx4JException, IOException {
        var context = new Characters(List.of(
                new Character("Homer Simpson", "Dan Castellaneta"),
                new Character("Marge Simpson", "Julie Kavner"),
                new Character("Bart Simpson", "Nancy Cartwright"),
                new Character("Kent Brockman", "Harry Shearer"),
                new Character("Disco Stu", "Hank Azaria"),
                new Character("Krusty the Clown", "Dan Castellaneta")
        ));
        var template = getClass().getResourceAsStream("RepeatParagraphTest.docx");
        var stamper = new TestDocxStamper<Characters>();
        var document = stamper.stampAndLoad(template, context);

        var titleCoords = new ArrayList<P>();
        var quotationCoords = new ArrayList<P>();
        new BaseCoordinatesWalker() {
            @Override
            protected void onParagraph(P paragraph) {
                if ("Titre2".equals(paragraph.getPPr().getPStyle().getVal())) {
                    titleCoords.add(paragraph);
                } else if ("Quotations".equals(paragraph.getPPr().getPStyle().getVal())) {
                    quotationCoords.add(paragraph);
                }
            }
        }.walk(document);

        // 6 titles and 6 quotations are expected
        assertEquals(6, titleCoords.size());
        assertEquals(6, quotationCoords.size());

        // Check paragraph's content
        assertEquals("Homer Simpson", new ParagraphWrapper(titleCoords.get(0)).getText());
        assertEquals("Dan Castellaneta", new ParagraphWrapper(quotationCoords.get(0)).getText());
        assertEquals("Marge Simpson", new ParagraphWrapper(titleCoords.get(1)).getText());
        assertEquals("Julie Kavner", new ParagraphWrapper(quotationCoords.get(1)).getText());
        assertEquals("Bart Simpson", new ParagraphWrapper(titleCoords.get(2)).getText());
        assertEquals("Nancy Cartwright", new ParagraphWrapper(quotationCoords.get(2)).getText());
        assertEquals("Kent Brockman", new ParagraphWrapper(titleCoords.get(3)).getText());
        assertEquals("Harry Shearer", new ParagraphWrapper(quotationCoords.get(3)).getText());
        assertEquals("Disco Stu", new ParagraphWrapper(titleCoords.get(4)).getText());
        assertEquals("Hank Azaria", new ParagraphWrapper(quotationCoords.get(4)).getText());
        assertEquals("Krusty the Clown", new ParagraphWrapper(titleCoords.get(5)).getText());
        assertEquals("Dan Castellaneta", new ParagraphWrapper(quotationCoords.get(5)).getText());
    }

    public record Character(String name, String actor) {
    }

    public record Characters(List<Character> characters) {
    }
}
