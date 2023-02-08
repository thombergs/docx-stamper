package org.wickedsource.docxstamper;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.Character;
import org.wickedsource.docxstamper.context.CharactersContext;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RepeatDocPartTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CharactersContext context = new CharactersContext();
        context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
        context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
        context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
        context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
        context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
        context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));

        InputStream template = getClass().getResourceAsStream("RepeatDocPartTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        List<Object> documentContent = document.getMainDocumentPart().getContent();

        int index = 2; // skip init paragraphs
        for (Character character : context.getCharacters()) {
            for (int j = 0; j < 3; j++) { // 3 elements should be repeated
                Object object = XmlUtils.unwrap(documentContent.get(index++));
                switch (j) {
                    case 0: {
                        P paragraph = (P) object;
                        String expected = String.format("Paragraph for test: %s - %s", character.getName(), character.getActor());
                        Assert.assertEquals(expected, new ParagraphWrapper(paragraph).getText());
                        break;
                    }
                    case 1: {

                        final List<Tc> cells = new ArrayList<>();
                        DocumentWalker cellWalker = new BaseDocumentWalker((ContentAccessor) object) {
                            @Override
                            protected void onTableCell(Tc tableCell) {
                                cells.add(tableCell);
                            }
                        };
                        cellWalker.walk();

                        Assert.assertEquals(character.getName(), new ParagraphWrapper((P) cells.get(0).getContent().get(0)).getText());
                        Assert.assertEquals(character.getActor(), new ParagraphWrapper((P) cells.get(1).getContent().get(0)).getText());
                        break;
                    }
                    case 2: {
                        P paragraph = (P) object;
                        List<Object> runs = paragraph.getContent();
                        Assert.assertEquals(3, runs.size());

                        List<Object> targetRunContent = ((R) runs.get(1)).getContent();
                        Assert.assertEquals(1, targetRunContent.size());
                        Assert.assertTrue(targetRunContent.get(0) instanceof Br);
                        break;
                    }
                }
            }
        }

    }


}
