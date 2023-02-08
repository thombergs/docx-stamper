package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.context.NameContext;
import org.wickedsource.docxstamper.util.DocumentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiStampTest extends AbstractDocx4jTest {

    @Test
    public void expressionsAreResolvedOnMultiStamp() throws Docx4JException, IOException {
        DocxStamper<NamesContext> stamper = new DocxStamper<>(new DocxStamperConfiguration().setFailOnUnresolvedExpression(false));
        NamesContext context = new NamesContext();

        InputStream template = getClass().getResourceAsStream("MultiStampTest.docx");
        OutputStream out = getOutputStream();
        stamper.stamp(template, context, out);
        InputStream in = getInputStream(out);
        WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
        assertTableRows(document);

        template = getClass().getResourceAsStream("MultiStampTest.docx");
        out = getOutputStream();
        stamper.stamp(template, context, out);
        in = getInputStream(out);
        document = WordprocessingMLPackage.load(in);
        assertTableRows(document);
    }

	private static void assertRowContainsText(Tr row, String text) {
		final List<Tc> cell0 = DocumentUtil.extractElements(row, Tc.class);
		String cellContent = TextUtils.getText(cell0.get(0));
		String message = String.format("'%s' is not contained in '%s'", text, cellContent);
		Assert.assertTrue(message, cellContent.contains(text));
	}

    private void assertTableRows(WordprocessingMLPackage document) {
		final List<Tbl> tablesFromObject = DocumentUtil.extractElements(document, Tbl.class);
		Assert.assertEquals(1, tablesFromObject.size());

		final List<Tr> tableRows = DocumentUtil.extractElements(tablesFromObject.get(0), Tr.class);
		Assert.assertEquals(5, tableRows.size());

		assertRowContainsText(tableRows.get(0), "Homer");
		assertRowContainsText(tableRows.get(1), "Marge");
		assertRowContainsText(tableRows.get(2), "Bart");
		assertRowContainsText(tableRows.get(3), "Lisa");
		assertRowContainsText(tableRows.get(4), "Maggie");
    }

    public static class NamesContext {
		private List<NameContext> names = new ArrayList<>();

        public NamesContext() {
			this.names.add(new NameContext("Homer"));
			this.names.add(new NameContext("Marge"));
			this.names.add(new NameContext("Bart"));
			this.names.add(new NameContext("Lisa"));
			this.names.add(new NameContext("Maggie"));
        }

		public List<NameContext> getNames() {
            return names;
        }

		public void setNames(List<NameContext> names) {
            this.names = names;
        }
    }

}
