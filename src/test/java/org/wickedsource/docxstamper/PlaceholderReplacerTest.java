package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.expression.PlaceholderReplacer;

import java.io.*;
import java.util.List;

public class PlaceholderReplacerTest {

    @Test
    public void resolvesExpressions() throws IOException {
        InputStream templateStream = getClass().getResourceAsStream("/template.docx");
        PersonContext context = new PersonContext();
        context.setName("Bob");
        context.setAge(30);
        Location newyork = new Location();
        newyork.setName("New York");
        newyork.setPopulation(8491079);
        newyork.setSquareKilometers(1214f);
        context.getLocations().add(newyork);
        Location boston = new Location();
        boston.setName("Boston");
        boston.setPopulation(655884);
        boston.setSquareKilometers(232.14f);
        context.getLocations().add(boston);
        Location washington = new Location();
        washington.setName("Washington");
        washington.setSquareKilometers(177f);
        washington.setPopulation(658893);
        context.getLocations().add(washington);

        PlaceholderReplacer<PersonContext> resolver = new PlaceholderReplacer<>();
        File outputFile = File.createTempFile("ExpressionResolverTest", ".docx");
        outputFile.deleteOnExit();
        XWPFDocument document = new XWPFDocument(templateStream);
        resolver.resolveExpressions(document, context);

        resolvesExpressionsInParagraphs(document);
        resolvesExpressionsInTables(document);
        resolvesExpressionsInNestedTables(document);
    }

    public void resolvesExpressionsInParagraphs(XWPFDocument document) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        RunAggregator nameParagraph = new RunAggregator(paragraphs.get(2));
        RunAggregator ageParagraph = new RunAggregator(paragraphs.get(4));
        RunAggregator locationParagraph = new RunAggregator(paragraphs.get(6));

        Assert.assertEquals("My name is Bob.", nameParagraph.getText());
        Assert.assertEquals("I am 30 years old.", ageParagraph.getText());
        Assert.assertEquals("I live in New York.", locationParagraph.getText());
    }

    public void resolvesExpressionsInTables(XWPFDocument document) {
        XWPFTable table = document.getTables().get(0);
        RunAggregator nameParagraph = new RunAggregator(table.getRow(0).getCell(1).getParagraphs().get(0));
        RunAggregator ageParagraph = new RunAggregator(table.getRow(1).getCell(1).getParagraphs().get(0));
        RunAggregator locationParagraph = new RunAggregator(table.getRow(2).getCell(1).getParagraphs().get(0));

        Assert.assertEquals("Bob", nameParagraph.getText());
        Assert.assertEquals("30", ageParagraph.getText());
        Assert.assertEquals("New York", locationParagraph.getText());
    }

    public void resolvesExpressionsInNestedTables(XWPFDocument document) {
        XWPFTable table = document.getTables().get(1);
        RunAggregator nameParagraph = new RunAggregator(table.getRow(0).getCell(1).getTables().get(0).getRow(0).getCell(1).getParagraphs().get(0));
        RunAggregator ageParagraph = new RunAggregator(table.getRow(0).getCell(1).getTables().get(0).getRow(1).getCell(1).getParagraphs().get(0));
        RunAggregator locationParagraph = new RunAggregator(table.getRow(0).getCell(1).getTables().get(0).getRow(2).getCell(1).getParagraphs().get(0));

        Assert.assertEquals("Bob", nameParagraph.getText());
        Assert.assertEquals("30", ageParagraph.getText());
        Assert.assertEquals("New York", locationParagraph.getText());
    }

}