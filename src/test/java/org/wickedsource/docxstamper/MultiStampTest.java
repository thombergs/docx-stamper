package org.wickedsource.docxstamper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.api.coordinates.TableCellCoordinates;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import org.wickedsource.docxstamper.util.walk.CoordinatesWalker;

public class MultiStampTest extends AbstractDocx4jTest {

	@Test
	public void expressionsAreResolvedOnMultiStamp() throws Docx4JException, IOException {
		DocxStamper stamper = new DocxStamper(new DocxStamperConfiguration());
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

	private void assertTableRows(WordprocessingMLPackage document) {
		final List<TableCellCoordinates> cells = new ArrayList<>();
		CoordinatesWalker cellWalker = new BaseCoordinatesWalker(document) {
			@Override
			protected void onTableCell(TableCellCoordinates tableCellCoordinates) {
				cells.add(tableCellCoordinates);
			}
		};
		cellWalker.walk();

		Assert.assertEquals(5, cells.size());
	}

	public static class NamesContext {
		private List<String> names = new ArrayList<>();

		public NamesContext() {
			this.names.add("Homer");
			this.names.add("Marge");
			this.names.add("Bart");
			this.names.add("Lisa");
			this.names.add("Maggie");
		}

		public List<String> getNames() {
			return names;
		}

		public void setNames(List<String> names) {
			this.names = names;
		}
	}

}
