package pro.verron.docxstamper.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.util.CommentUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

/**
 * <p>Stringifier class.</p>
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public class Stringifier {

    private final Supplier<WordprocessingMLPackage> documentSupplier;

    /**
     * <p>Constructor for Stringifier.</p>
     *
     * @param documentSupplier a {@link java.util.function.Supplier} object
     */
    public Stringifier(Supplier<WordprocessingMLPackage> documentSupplier) {
        this.documentSupplier = documentSupplier;
    }

    private static MessageDigest findDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new DocxStamperException(e);
        }
    }

    private WordprocessingMLPackage document() {
        return documentSupplier.get();
    }

    /**
     * <p>stringify.</p>
     *
     * @param blip a {@link org.docx4j.dml.CTBlip} object
     * @return a {@link java.lang.String} object
     */
    public String stringify(CTBlip blip) {
        var image = document()
                .getParts()
                .getParts()
                .entrySet()
                .stream()
                .filter(e -> e.getKey()
                        .getName()
                        .contains(blip.getEmbed()))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(BinaryPartAbstractImage.class::cast)
                .orElseThrow();
        byte[] imageBytes = image.getBytes();
        return "%s:%s:%s:sha1=%s:cy=$d".formatted(
                blip.getEmbed(),
                image.getContentType(),
                humanReadableByteCountSI(imageBytes.length),
                sha1b64(imageBytes));
    }

    /**
     * <p>humanReadableByteCountSI.</p>
     *
     * @param bytes a long
     * @return a {@link java.lang.String} object
     */
    public String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) return bytes + "B";

        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format(Locale.US, "%.1f%cB", bytes / 1000.0,
                             ci.current());
    }

    private String sha1b64(byte[] imageBytes) {
        MessageDigest messageDigest = findDigest();
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] digest = messageDigest.digest(imageBytes);
        return encoder.encodeToString(digest);
    }

    /**
     * <p>stringify.</p>
     *
     * @param o a {@link java.lang.Object} object
     * @return a {@link java.lang.String} object
     */
    public String stringify(Object o) {
        if (o instanceof JAXBElement<?> jaxbElement)
            return stringify(jaxbElement.getValue());
        if (o instanceof List<?> list)
            return list.stream()
                    .map(this::stringify)
                    .collect(joining());
        if (o instanceof Text text)
            return TextUtils.getText(text);
        if (o instanceof P p)
            return stringify(p);
        if (o instanceof Drawing drawing)
            return stringify(drawing.getAnchorOrInline());
        if (o instanceof Inline inline)
            return stringify(inline.getGraphic()) + ":" +inline.getExtent().getCx();
        if (o instanceof Graphic graphic)
            return stringify(graphic.getGraphicData());
        if (o instanceof GraphicData graphicData)
            return stringify(graphicData.getPic());
        if (o instanceof Pic pic)
            return stringify(pic.getBlipFill());
        if (o instanceof CTBlipFillProperties blipFillProperties)
            return stringify(blipFillProperties.getBlip());
        if (o instanceof CTBlip blip)
            return stringify(blip);
        if (o instanceof R.LastRenderedPageBreak)
            return ""; // do not serialize
        if (o instanceof Br)
            return "|BR|";
        if (o instanceof R.Tab)
            return "|TAB|";
        if (o instanceof R.CommentReference commentReference) {
            try {
                return CommentUtil.findComment(document(),
                                               commentReference.getId()).map(c->stringify(c.getContent())).orElseThrow();
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
        }
        if (o == null)
            throw new RuntimeException("Unsupported run content: NULL");
        throw new RuntimeException("Unsupported run content: " + o.getClass());
    }

    public Optional<String> stringify(PPrBase.Spacing spacing) {
        if (spacing == null) return Optional.empty();
        SortedMap<String, Object> map = new TreeMap<>();
        ofNullable(spacing.getAfter())
                .ifPresent(after -> map.put("after", after));
        ofNullable(spacing.getAfter())
                .ifPresent(after -> map.put("before", after));
        ofNullable(spacing.getAfter())
                .ifPresent(after -> map.put("beforeLines", after));
        ofNullable(spacing.getAfter())
                .ifPresent(after -> map.put("afterLines", after));
        ofNullable(spacing.getAfter())
                .ifPresent(after -> map.put("line", after));
        ofNullable(spacing.getAfter())
                .ifPresent(after -> map.put("lineRule", after));
        return map.isEmpty()
                ? Optional.empty()
                : Optional.of(map.entrySet()
                                      .stream()
                                      .map(entry -> entry.getKey() + "=" + entry.getValue())
                                      .collect(joining(",", "{", "}")));
    }

    /**
     * <p>stringify.</p>
     *
     * @param p a {@link org.docx4j.wml.P} object
     * @return a {@link java.lang.String} object
     */
    public String stringify(P p) {
        String runs = extractDocumentRuns(p);
        return ofNullable(p.getPPr())
                .map(ppr -> "%s//%s".formatted(runs, stringify(ppr)))
                .orElse(runs);
    }

    /**
     * <p>extractDocumentRuns.</p>
     *
     * @param p a {@link java.lang.Object} object
     * @return a {@link java.lang.String} object
     */
    public String extractDocumentRuns(Object p) {
        var runCollector = new RunCollector();
        TraversalUtil.visit(p, runCollector);
        return runCollector
                .runs()
                .filter(r -> !r.getContent()
                        .isEmpty())
                .map(this::stringify)
                .collect(joining());
    }

    private String stringify(PPr pPr) {
        var set = new TreeSet<String>();
        if (pPr.getJc() != null) set.add("jc=" + pPr.getJc()
                .getVal()
                .value());
        if (pPr.getInd() != null) set.add("ind=" + pPr.getInd()
                .getLeft()
                .intValue());
        if (pPr.getKeepLines() != null)
            set.add("keepLines=" + pPr.getKeepLines()
                    .isVal());
        if (pPr.getKeepNext() != null) set.add("keepNext=" + pPr.getKeepNext()
                .isVal());
        if (pPr.getOutlineLvl() != null)
            set.add("outlineLvl=" + pPr.getOutlineLvl()
                    .getVal()
                    .intValue());
        if (pPr.getPageBreakBefore() != null)
            set.add("pageBreakBefore=" + pPr.getPageBreakBefore()
                    .isVal());
        if (pPr.getPBdr() != null) set.add("pBdr=xxx");
        if (pPr.getPPrChange() != null) set.add("pPrChange=xxx");
        if (pPr.getRPr() != null)
            set.add("rPr={" + stringify(pPr.getRPr()) + "}");
        if (pPr.getSectPr() != null)
            set.add("sectPr={" + stringify(pPr.getSectPr()) + "}");
        if (pPr.getShd() != null) set.add("shd=xxx");
        stringify(pPr.getSpacing()).ifPresent(spacing -> set.add(
                "spacing=" + spacing));
        if (pPr.getSuppressAutoHyphens() != null)
            set.add("suppressAutoHyphens=xxx");
        if (pPr.getSuppressLineNumbers() != null)
            set.add("suppressLineNumbers=xxx");
        if (pPr.getSuppressOverlap() != null) set.add("suppressOverlap=xxx");
        if (pPr.getTabs() != null) set.add("tabs=xxx");
        if (pPr.getTextAlignment() != null) set.add("textAlignment=xxx");
        if (pPr.getTextDirection() != null) set.add("textDirection=xxx");
        if (pPr.getTopLinePunct() != null) set.add("topLinePunct=xxx");
        if (pPr.getWidowControl() != null) set.add("widowControl=xxx");
        if (pPr.getWordWrap() != null) set.add("wordWrap=xxx");
        if (pPr.getFramePr() != null) set.add("framePr=xxx");
        if (pPr.getDivId() != null) set.add("divId=xxx");
        if (pPr.getCnfStyle() != null) set.add("cnfStyle=xxx");
        return String.join(",", set);
    }

    /**
     * <p>stringify.</p>
     *
     * @param run a {@link org.docx4j.wml.R} object
     * @return a {@link java.lang.String} object
     */
    public String stringify(R run) {
        var runPresentation = ofNullable(run.getRPr())
                .map(this::stringify);

        String serialized = stringify(run.getContent());
        if (serialized.isEmpty())
            return "";
        if (runPresentation.isEmpty())
            return serialized;
        return "|%s/%s|".formatted(serialized, runPresentation.get());
    }

    /**
     * <p>stringify.</p>
     *
     * @param rPr a {@link org.docx4j.wml.RPrAbstract} object
     * @return a {@link java.lang.String} object
     */
    public String stringify(RPrAbstract rPr) {
        var set = new TreeSet<String>();
        if (rPr.getB() != null) set.add("b=" + rPr.getB()
                .isVal());
        if (rPr.getBdr() != null) set.add("bdr=xxx");
        if (rPr.getCaps() != null) set.add("caps=" + rPr.getCaps()
                .isVal());
        if (rPr.getColor() != null) set.add("color=" + rPr.getColor()
                .getVal());
        if (rPr.getDstrike() != null) set.add("dstrike=" + rPr.getDstrike()
                .isVal());
        if (rPr.getI() != null) set.add("i=" + rPr.getI()
                .isVal());
        if (rPr.getKern() != null) set.add("kern=" + rPr.getKern()
                .getVal()
                .intValue());
        if (rPr.getLang() != null) set.add("lang=" + rPr.getLang()
                .getVal());
        //if (rPr.getRFonts() != null) set.add("rFonts=xxx:" + rPr.getRFonts().getHint().value());
        if (rPr.getRPrChange() != null) set.add("rPrChange=xxx");
        if (rPr.getRStyle() != null) set.add("rStyle=" + rPr.getRStyle()
                .getVal());
        if (rPr.getRtl() != null) set.add("rtl=" + rPr.getRtl()
                .isVal());
        if (rPr.getShadow() != null) set.add("shadow=" + rPr.getShadow()
                .isVal());
        if (rPr.getShd() != null) set.add("shd=" + rPr.getShd()
                .getColor());
        if (rPr.getSmallCaps() != null)
            set.add("smallCaps=" + rPr.getSmallCaps()
                    .isVal());
        if (rPr.getVertAlign() != null)
            set.add("vertAlign=" + rPr.getVertAlign()
                    .getVal()
                    .value());
        if (rPr.getSpacing() != null) set.add("spacing=" + rPr.getSpacing()
                .getVal()
                .intValue());
        if (rPr.getStrike() != null) set.add("strike=" + rPr.getStrike()
                .isVal());
        if (rPr.getOutline() != null) set.add("outline=" + rPr.getOutline()
                .isVal());
        if (rPr.getEmboss() != null) set.add("emboss=" + rPr.getEmboss()
                .isVal());
        if (rPr.getImprint() != null) set.add("imprint=" + rPr.getImprint()
                .isVal());
        if (rPr.getNoProof() != null) set.add("noProof=" + rPr.getNoProof()
                .isVal());
        if (rPr.getSpecVanish() != null)
            set.add("specVanish=" + rPr.getSpecVanish()
                    .isVal());
        if (rPr.getU() != null) set.add("u=" + rPr.getU()
                .getVal()
                .value());
        if (rPr.getVanish() != null) set.add("vanish=" + rPr.getVanish()
                .isVal());
        if (rPr.getW() != null) set.add("w=" + rPr.getW()
                .getVal());
        if (rPr.getWebHidden() != null)
            set.add("webHidden=" + rPr.getWebHidden()
                    .isVal());
        if (rPr.getHighlight() != null)
            set.add("highlight=" + rPr.getHighlight()
                    .getVal());
        if (rPr.getEffect() != null) set.add("effect=" + rPr.getEffect()
                .getVal()
                .value());
        return String.join(",", set);
    }

    private String stringify(SectPr sectPr) {
        var set = new TreeSet<String>();
        if (sectPr.getEGHdrFtrReferences() != null)
            set.add("eGHdrFtrReferences=xxx");
        if (sectPr.getPgSz() != null)
            set.add("pgSz={" + stringify(sectPr.getPgSz()) + "}");
        if (sectPr.getPgMar() != null) set.add("pgMar=xxx");
        if (sectPr.getPaperSrc() != null) set.add("paperSrc=xxx");
        if (sectPr.getBidi() != null) set.add("bidi=xxx");
        if (sectPr.getRtlGutter() != null) set.add("rtlGutter=xxx");
        if (sectPr.getDocGrid() != null) set.add("docGrid=xxx");
        if (sectPr.getFormProt() != null) set.add("formProt=xxx");
        if (sectPr.getVAlign() != null) set.add("vAlign=xxx");
        if (sectPr.getNoEndnote() != null) set.add("noEndnote=xxx");
        if (sectPr.getTitlePg() != null) set.add("titlePg=xxx");
        if (sectPr.getTextDirection() != null) set.add("textDirection=xxx");
        if (sectPr.getRtlGutter() != null) set.add("rtlGutter=xxx");
        return String.join(",", set);
    }

    private String stringify(SectPr.PgSz pgSz) {
        var set = new TreeSet<String>();
        if (pgSz.getOrient() != null) set.add("orient=" + pgSz.getOrient()
                .value());
        if (pgSz.getW() != null) set.add("w=" + pgSz.getW()
                .intValue());
        if (pgSz.getH() != null) set.add("h=" + pgSz.getH()
                .intValue());
        if (pgSz.getCode() != null) set.add("code=" + pgSz.getCode()
                .intValue());
        return String.join(",", set);
    }
}
