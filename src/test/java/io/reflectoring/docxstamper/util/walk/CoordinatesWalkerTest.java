package io.reflectoring.docxstamper.util.walk;

import io.reflectoring.docxstamper.api.coordinates.ParagraphCoordinates;
import io.reflectoring.docxstamper.api.coordinates.RunCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableCellCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableCoordinates;
import io.reflectoring.docxstamper.api.coordinates.TableRowCoordinates;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import io.reflectoring.docxstamper.AbstractDocx4jTest;
import org.wickedsource.docxstamper.api.coordinates.*;
import io.reflectoring.docxstamper.util.ParagraphWrapper;

import java.io.IOException;

public class CoordinatesWalkerTest extends AbstractDocx4jTest {

    @Test
    public void calculatesCorrectNestedCoordinates() throws IOException, Docx4JException {
        WordprocessingMLPackage document = loadDocument("DocumentWalkerTest.docx");

        final Counter paragraphCount = new Counter();
        final Counter tableCount = new Counter();
        final Counter rowCount = new Counter();
        final Counter cellCount = new Counter();

        CoordinatesWalker walker = new CoordinatesWalker(document) {

        	@Override
			protected void onRun(RunCoordinates runCoordinates, ParagraphCoordinates paragraphCoordinates) {
				// TODO Auto-generated method stub
			}
        	
            @Override
            protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                paragraphCount.increment();
                switch (paragraphCount.getCount()) {
                    case 1:     // header
                    case 9:     // main content
                    case 17:    // footer
                        Assert.assertEquals(0, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are: index 0.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates());
                        break;
                    case 2:
                    case 10:
                    case 18:
                        Assert.assertEquals(1, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are: index 1.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates());
                        break;
                    case 3:
                    case 11:
                    case 19:
                        Assert.assertEquals(0, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are: table 2, row 0, cell 0, index 0.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 4:
                    case 12:
                    case 20:
                        Assert.assertEquals(1, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are: table 2, row 0, cell 0, index 1.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 5:
                    case 13:
                    case 21:
                        Assert.assertEquals(0, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are: table 2, row 1, cell 0, table 0, row 0, cell 0, index 0.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 6:
                    case 14:
                    case 22:
                        Assert.assertEquals(0, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are: table 2, row 1, cell 0, table 0, row 1, cell 0, index 0.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 7:
                    case 15:
                    case 23:
                        Assert.assertEquals(1, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are table 2, row 1, cell 0, index 1.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertEquals(0, paragraphCoordinates.getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 8:
                    case 16:
                    case 24:
                        Assert.assertEquals(3, paragraphCoordinates.getIndex());
                        Assert.assertEquals("The coordinates of this paragraph are index 3.", new ParagraphWrapper(paragraphCoordinates.getParagraph()).getText());
                        Assert.assertNull(paragraphCoordinates.getParentTableCellCoordinates());
                        break;
                }
            }

            @Override
            protected void onTable(TableCoordinates tableCoordinates) {
                tableCount.increment();
                switch (tableCount.getCount()) {
                    case 1: // header
                    case 3: // main content
                    case 5: // footer
                        Assert.assertEquals(2, tableCoordinates.getIndex());
                        Assert.assertNull(tableCoordinates.getParentTableCellCoordinates());
                        break;
                    case 2:
                    case 4:
                    case 6:
                        Assert.assertEquals(0, tableCoordinates.getIndex());
                        Assert.assertEquals(0, tableCoordinates.getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, tableCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableCoordinates.getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                }
            }

            @Override
            protected void onTableRow(TableRowCoordinates tableRowCoordinates) {
                rowCount.increment();
                switch (rowCount.getCount()) {
                    case 1: // header
                    case 5: // main content
                    case 9: // footer
                        Assert.assertEquals(0, tableRowCoordinates.getIndex());
                        Assert.assertEquals(2, tableRowCoordinates.getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 2:
                    case 6:
                    case 10:
                        Assert.assertEquals(1, tableRowCoordinates.getIndex());
                        Assert.assertEquals(2, tableRowCoordinates.getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 3:
                    case 7:
                    case 11:
                        Assert.assertEquals(0, tableRowCoordinates.getIndex());
                        Assert.assertEquals(0, tableRowCoordinates.getParentTableCoordinates().getIndex());
                        Assert.assertEquals(0, tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 4:
                    case 8:
                    case 12:
                        Assert.assertEquals(1, tableRowCoordinates.getIndex());
                        Assert.assertEquals(0, tableRowCoordinates.getParentTableCoordinates().getIndex());
                        Assert.assertEquals(0, tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableRowCoordinates.getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;

                }
            }

            @Override
            protected void onTableCell(TableCellCoordinates tableCellCoordinates) {
                cellCount.increment();
                switch (cellCount.getCount()) {
                    case 1: // header
                    case 5: // main content
                    case 9: // footer
                        Assert.assertEquals(0, tableCellCoordinates.getIndex());
                        Assert.assertEquals(0, tableCellCoordinates.getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 2:
                    case 6:
                    case 10:
                        Assert.assertEquals(0, tableCellCoordinates.getIndex());
                        Assert.assertEquals(1, tableCellCoordinates.getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 3:
                    case 7:
                    case 11:
                        Assert.assertEquals(0, tableCellCoordinates.getIndex());
                        Assert.assertEquals(0, tableCellCoordinates.getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(0, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertEquals(0, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;
                    case 4:
                    case 8:
                    case 12:
                        Assert.assertEquals(0, tableCellCoordinates.getIndex());
                        Assert.assertEquals(1, tableCellCoordinates.getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(0, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertEquals(0, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getIndex());
                        Assert.assertEquals(1, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getIndex());
                        Assert.assertEquals(2, tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getIndex());
                        Assert.assertNull(tableCellCoordinates.getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates().getParentTableRowCoordinates().getParentTableCoordinates().getParentTableCellCoordinates());
                        break;

                }
            }
        };

        walker.walk();
        Assert.assertEquals(24, paragraphCount.getCount());
        Assert.assertEquals(6, tableCount.getCount());
        Assert.assertEquals(12, rowCount.getCount());
        Assert.assertEquals(12, cellCount.getCount());


    }

}
