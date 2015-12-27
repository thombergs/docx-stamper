package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.Character;
import org.wickedsource.docxstamper.context.CharactersContext;
import org.wickedsource.docxstamper.docx4j.AbstractDocx4jTest;
import org.wickedsource.docxstamper.docx4j.RunAggregator;
import org.wickedsource.docxstamper.docx4j.walk.TableCellWalker;
import org.wickedsource.docxstamper.docx4j.walk.TableRowWalker;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.docx4j.walk.coordinates.TableRowCoordinates;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RepeatTableRowTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        CharactersContext context = new CharactersContext();
        context.getCharacters().add(new Character("Homer Simpson", "Dan Castellaneta"));
        context.getCharacters().add(new Character("Marge Simpson", "Julie Kavner"));
        context.getCharacters().add(new Character("Bart Simpson", "Nancy Cartwright"));
        context.getCharacters().add(new Character("Kent Brockman", "Harry Shearer"));
        context.getCharacters().add(new Character("Disco Stu", "Hank Azaria"));
        context.getCharacters().add(new Character("Krusty the Clown", "Dan Castellaneta"));
        InputStream template = getClass().getResourceAsStream("RepeatTableRowTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        final List<TableRowCoordinates> rowCoords = new ArrayList<>();
        TableRowWalker walker = new TableRowWalker(document.getMainDocumentPart()) {
            @Override
            protected void onTableRow(TableRowCoordinates tableRowCoordinates) {
                rowCoords.add(tableRowCoordinates);
            }
        };
        walker.walk();

        // 1 header row + 1 row per character in list
        Assert.assertEquals(7, rowCoords.size());


        final List<TableCellCoordinates> cells = new ArrayList<>();
        TableCellWalker cellWalker = new TableCellWalker(document.getMainDocumentPart()) {
            @Override
            protected void onTableCell(TableCellCoordinates tableCellCoordinates) {
                cells.add(tableCellCoordinates);
            }
        };
        cellWalker.walk();

        Assert.assertEquals("Homer Simpson", new RunAggregator((P) cells.get(2).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Dan Castellaneta", new RunAggregator((P) cells.get(3).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Marge Simpson", new RunAggregator((P) cells.get(4).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Julie Kavner", new RunAggregator((P) cells.get(5).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Bart Simpson", new RunAggregator((P) cells.get(6).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Nancy Cartwright", new RunAggregator((P) cells.get(7).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Kent Brockman", new RunAggregator((P) cells.get(8).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Harry Shearer", new RunAggregator((P) cells.get(9).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Disco Stu", new RunAggregator((P) cells.get(10).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Hank Azaria", new RunAggregator((P) cells.get(11).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Krusty the Clown", new RunAggregator((P) cells.get(12).getCell().getContent().get(0)).getText());
        Assert.assertEquals("Dan Castellaneta", new RunAggregator((P) cells.get(13).getCell().getContent().get(0)).getText());
    }


}
