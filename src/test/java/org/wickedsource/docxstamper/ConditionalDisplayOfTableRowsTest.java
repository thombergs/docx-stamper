package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.walk.coordinates.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.CoordinatesWalker;
import org.wickedsource.docxstamper.walk.coordinates.TableRowCoordinates;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConditionalDisplayOfTableRowsTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer");
        InputStream template = getClass().getResourceAsStream("ConditionalDisplayOfTableRowsTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);

        final List<TableRowCoordinates> rowCoords = new ArrayList<>();
        CoordinatesWalker walker = new BaseCoordinatesWalker(document) {
            @Override
            protected void onTableRow(TableRowCoordinates tableRowCoordinates) {
                rowCoords.add(tableRowCoordinates);
            }
        };
        walker.walk();

        Assert.assertEquals(5, rowCoords.size());
    }


}
