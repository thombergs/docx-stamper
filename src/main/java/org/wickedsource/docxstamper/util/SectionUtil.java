package org.wickedsource.docxstamper.util;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.*;

import java.util.List;

public class SectionUtil {
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	public static SectPr getPreviousSectionBreakIfPresent(Object firstObject, ContentAccessor parent) {
		List<Object> parentContent = parent.getContent();
		int pIndex = parentContent.indexOf(firstObject);

		int i = pIndex - 1;
		while (i >= 0) {
			if (parentContent.get(i) instanceof P prevParagraph) {
				// the first P preceding the object is the one potentially carrying a section break
				PPr pPr = prevParagraph.getPPr();
				if (pPr != null && pPr.getSectPr() != null) {
					return pPr.getSectPr();
				} else return null;
			}
			i--;
		}
		System.out.println("No previous section break found from : " + parent + ", first object index=" + pIndex);
		return null;
	}

	public static SectPr getParagraphSectionBreak(P p) {
		return p.getPPr() != null && p.getPPr().getSectPr() != null
				? p.getPPr().getSectPr()
				: null;
	}

	public static boolean isOddNumberOfSectionBreaks(List<Object> objects) {
		long count = objects.stream()
							.filter(obj -> obj instanceof P)
							.map(obj -> (P) obj)
							.filter(p -> p.getPPr() != null && p.getPPr().getSectPr() != null)
							.count();
		return count % 2 != 0;
	}

	public static void applySectionBreakToParagraph(SectPr sectPr, P paragraph) {
		PPr pPpr = paragraph.getPPr();
		PPr nextPPr = pPpr != null ? pPpr : factory.createPPr();
		nextPPr.setSectPr(XmlUtils.deepCopy(sectPr));
		paragraph.setPPr(nextPPr);
	}

}
