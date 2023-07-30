package io.reflectoring.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.junit.Assert;
import org.junit.Test;
import io.reflectoring.docxstamper.context.NameContext;
import io.reflectoring.docxstamper.util.ParagraphWrapper;

import java.io.IOException;
import java.io.InputStream;

public class ExpressionReplacementInHeaderAndFooterTest extends AbstractDocx4jTest {

    @Test
    public void test() throws Docx4JException, IOException {
        NameContext context = new NameContext();
        context.setName("Homer Simpson");
        InputStream template = getClass().getResourceAsStream("ExpressionReplacementInHeaderAndFooterTest.docx");
        WordprocessingMLPackage document = stampAndLoad(template, context);
        resolvedExpressionsAreReplacedInHeader(document);
        resolvedExpressionsAreReplacedInFooter(document);
        unresolvedExpressionsAreNotReplacedInHeader(document);
        unresolvedExpressionsAreNotReplacedInFooter(document);
    }

    private void resolvedExpressionsAreReplacedInHeader(WordprocessingMLPackage document) {
        HeaderPart headerPart = getHeaderPart(document);

        P nameParagraph = (P) headerPart.getContent().get(1);
        Assert.assertEquals("In this paragraph, the variable name should be resolved to the value Homer Simpson.", new ParagraphWrapper(nameParagraph).getText());
    }

    private void resolvedExpressionsAreReplacedInFooter(WordprocessingMLPackage document) {
        FooterPart footerPart = getFooterPart(document);

        P nameParagraph = (P) footerPart.getContent().get(1);
        Assert.assertEquals("In this paragraph, the variable name should be resolved to the value Homer Simpson.", new ParagraphWrapper(nameParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplacedInHeader(WordprocessingMLPackage document) {
        HeaderPart headerPart = getHeaderPart(document);

        P fooParagraph = (P) headerPart.getContent().get(2);
        Assert.assertEquals("In this paragraph, the variable foo should not be resolved: ${foo}.", new ParagraphWrapper(fooParagraph).getText());
    }

    private void unresolvedExpressionsAreNotReplacedInFooter(WordprocessingMLPackage document) {
        FooterPart footerPart = getFooterPart(document);

        P fooParagraph = (P) footerPart.getContent().get(2);
        Assert.assertEquals("In this paragraph, the variable foo should not be resolved: ${foo}.", new ParagraphWrapper(fooParagraph).getText());
    }

    private HeaderPart getHeaderPart(WordprocessingMLPackage document) {
        RelationshipsPart relPart = document.getMainDocumentPart().getRelationshipsPart();
        Relationship rel = relPart.getRelationshipByType(Namespaces.HEADER);
        return (HeaderPart) relPart.getPart(rel);
    }

    private FooterPart getFooterPart(WordprocessingMLPackage document) {
        RelationshipsPart relPart = document.getMainDocumentPart().getRelationshipsPart();
        Relationship rel = relPart.getRelationshipByType(Namespaces.FOOTER);
        return (FooterPart) relPart.getPart(rel);
    }


}
