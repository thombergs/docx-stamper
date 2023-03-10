package org.wickedsource.docxstamper;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.wickedsource.docxstamper.util.ParagraphCollector;
import org.wickedsource.docxstamper.util.RunCollector;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import static java.util.stream.Collectors.joining;

/**
 * Common methods to interact with docx documents.
 */
public final class TestDocxStamper<T> {

	private final DocxStamper<T> stamper;

	public TestDocxStamper() {
		this(new DocxStamperConfiguration()
					 .setFailOnUnresolvedExpression(false));
	}

	public TestDocxStamper(DocxStamperConfiguration config) {
		stamper = new DocxStamper<>(config);
	}

	/**
	 * Stamps the given template resolving the expressions within the template against the specified contextRoot.
	 * Returns the resulting document after it has been saved and loaded again to ensure that changes in the Docx4j
	 * object structure were really transported into the XML of the .docx file.
	 */
	public WordprocessingMLPackage stampAndLoad(InputStream template, T contextRoot) throws IOException, Docx4JException {
		var out = IOStreams.getOutputStream();
		stamper.stamp(template, contextRoot, out);
		var in = IOStreams.getInputStream(out);
		return WordprocessingMLPackage.load(in);
	}

	public List<String> stampAndLoadAndExtract(InputStream template, T context) {
		try {
			var config = new DocxStamperConfiguration()
					.setFailOnUnresolvedExpression(false);
			var out = IOStreams.getOutputStream();
			var stamper = new DocxStamper<T>(config);
			stamper.stamp(template, context, out);
			var in = IOStreams.getInputStream(out);
			var document = WordprocessingMLPackage.load(in);
			var visitor = new ParagraphCollector();
			TraversalUtil.visit(document.getMainDocumentPart().getContent(), visitor);
			return visitor.paragraphs()
						  .map(TestDocxStamper::extractDocumentRuns)
						  .toList();
		} catch (Docx4JException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String extractDocumentRuns(P p) {
		var runCollector = new RunCollector();
		TraversalUtil.visit(p, runCollector);
		return runCollector.runs()
						   .filter(r -> !r.getContent().isEmpty())
						   .filter(r -> !TextUtils.getText(r).isEmpty())
						   .map(TestDocxStamper::serialize)
						   .collect(joining());
	}

	private static String serialize(R run) {
		var runPresentation = Optional.ofNullable(run.getRPr());
		var runText = TextUtils.getText(run);
		return runPresentation
				.map(TestDocxStamper::serialize)
				.map(s -> "|%s/%s|".formatted(runText, s))
				.orElse(runText);
	}

	private static String serialize(RPr rPr) {
		var set = new TreeSet<String>();
		if (rPr.getB() != null) set.add("b=" + rPr.getB().isVal());
		if (rPr.getBdr() != null) set.add("bdr=xxx");
		if (rPr.getCaps() != null) set.add("caps=" + rPr.getCaps().isVal());
		if (rPr.getColor() != null) set.add("color=" + rPr.getColor().getVal());
		if (rPr.getDstrike() != null) set.add("dstrike=" + rPr.getDstrike().isVal());
		if (rPr.getI() != null) set.add("i=" + rPr.getI().isVal());
		if (rPr.getKern() != null) set.add("kern=" + rPr.getKern().getVal().intValue());
		if (rPr.getLang() != null) set.add("lang=" + rPr.getLang().getVal());
		//if (rPr.getRFonts() != null) set.add("rFonts=xxx:" + rPr.getRFonts().getHint().value());
		if (rPr.getRPrChange() != null) set.add("rPrChange=xxx");
		if (rPr.getRStyle() != null) set.add("rStyle=" + rPr.getRStyle().getVal());
		if (rPr.getRtl() != null) set.add("rtl=" + rPr.getRtl().isVal());
		if (rPr.getShadow() != null) set.add("shadow=" + rPr.getShadow().isVal());
		if (rPr.getShd() != null) set.add("shd=" + rPr.getShd().getColor());
		if (rPr.getSmallCaps() != null) set.add("smallCaps=" + rPr.getSmallCaps().isVal());
		if (rPr.getVertAlign() != null) set.add("vertAlign=" + rPr.getVertAlign().getVal().value());
		if (rPr.getSpacing() != null) set.add("spacing=" + rPr.getSpacing().getVal().intValue());
		if (rPr.getStrike() != null) set.add("strike=" + rPr.getStrike().isVal());
		if (rPr.getOutline() != null) set.add("outline=" + rPr.getOutline().isVal());
		if (rPr.getEmboss() != null) set.add("emboss=" + rPr.getEmboss().isVal());
		if (rPr.getImprint() != null) set.add("imprint=" + rPr.getImprint().isVal());
		if (rPr.getNoProof() != null) set.add("noProof=" + rPr.getNoProof().isVal());
		if (rPr.getSpecVanish() != null) set.add("specVanish=" + rPr.getSpecVanish().isVal());
		if (rPr.getU() != null) set.add("u=" + rPr.getU().getVal().value());
		if (rPr.getVanish() != null) set.add("vanish=" + rPr.getVanish().isVal());
		if (rPr.getW() != null) set.add("w=" + rPr.getW().getVal());
		if (rPr.getWebHidden() != null) set.add("webHidden=" + rPr.getWebHidden().isVal());
		if (rPr.getHighlight() != null) set.add("highlight=" + rPr.getHighlight().getVal());
		if (rPr.getEffect() != null) set.add("effect=" + rPr.getEffect().getVal().value());
		return String.join(",", set);
	}
}
