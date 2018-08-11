package org.wickedsource.docxstamper.util.walk;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.api.coordinates.*;
import org.wickedsource.docxstamper.util.CommentUtil;
import org.wickedsource.docxstamper.util.CommentWrapper;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

public abstract class CoordinatesWalker {

    private WordprocessingMLPackage document;

    public CoordinatesWalker(WordprocessingMLPackage document) {
        this.document = document;
    }

    public void walk() {

        RelationshipsPart relationshipsPart = document.getMainDocumentPart().getRelationshipsPart();

        // walk through elements in headers
        List<Relationship> headerRelationships = getRelationshipsOfType(document, Namespaces.HEADER);
        for (Relationship header : headerRelationships) {
            HeaderPart headerPart = (HeaderPart) relationshipsPart.getPart(header.getId());
            walkContent(headerPart.getContent());
        }

        // walk through elements in main document part.
        //
        // walk a copy of the document content list to avoid a comod exception in the event that a
        // custom ITypeResolver wants to insert top-level element(s) like a paragraph or table
        // (e.g. content from another document)
        walkContent(new ArrayList<>(document.getMainDocumentPart().getContent()));

        // walk through elements in headers
        List<Relationship> footerRelationships = getRelationshipsOfType(document, Namespaces.FOOTER);
        for (Relationship footer : footerRelationships) {
            FooterPart footerPart = (FooterPart) relationshipsPart.getPart(footer.getId());
            walkContent(footerPart.getContent());
        }
    }

    private List<Relationship> getRelationshipsOfType(WordprocessingMLPackage document, String type) {
        List<Relationship> allRelationhips = document
                .getMainDocumentPart()
                .getRelationshipsPart()
                .getRelationships()
                .getRelationship();
        List<Relationship> headerRelationships = new ArrayList<>();
        for (Relationship r : allRelationhips) {
            if (r.getType().equals(type)) {
                headerRelationships.add(r);
            }
        }
        return headerRelationships;
    }

    private void walkContent(List<Object> contentElements) {
        int elementIndex = 0;
        for (Object contentElement : contentElements) {
            Object unwrappedObject = XmlUtils.unwrap(contentElement);
            if (unwrappedObject instanceof P) {
                P p = (P) unwrappedObject;
                ParagraphCoordinates coordinates = new ParagraphCoordinates(p, elementIndex);
                walkParagraph(coordinates);
            } else if (unwrappedObject instanceof Tbl) {
                Tbl table = (Tbl) unwrappedObject;
                TableCoordinates tableCoordinates = new TableCoordinates(table, elementIndex);
                walkTable(tableCoordinates);
            }
            elementIndex++;
        }
    }
    
    private void walkParagraph(ParagraphCoordinates paragraphCoordinates){
    	int rowIndex = 0;
    	List<CommentWrapper> commentsToDelete = new ArrayList<>();
    	for (Object contentElement : paragraphCoordinates.getParagraph().getContent()){
    		 if (XmlUtils.unwrap(contentElement) instanceof R) {
    			 R run = (R) contentElement;
    			 RunCoordinates runCooridnates = new RunCoordinates(run, rowIndex);
    			 CommentWrapper commentToDelete = onRun(runCooridnates, paragraphCoordinates);
    			 if (commentToDelete != null)
    				 commentsToDelete.add(commentToDelete);
    		 }
    	}
    	for (CommentWrapper cw : commentsToDelete)
    		CommentUtil.deleteComment(cw);
    	// we run the paragraph afterwards so that the comments inside work before the whole paragraph comments
    	onParagraph(paragraphCoordinates);

    }
    
    private void walkTable(TableCoordinates tableCoordinates) {
        onTable(tableCoordinates);
        int rowIndex = 0;
        for (Object contentElement : tableCoordinates.getTable().getContent()) {
            if (XmlUtils.unwrap(contentElement) instanceof Tr) {
                Tr row = (Tr) contentElement;
                TableRowCoordinates rowCoordinates = new TableRowCoordinates(row, rowIndex, tableCoordinates);
                walkTableRow(rowCoordinates);
            }
            rowIndex++;
        }
    }


    private void walkTableRow(TableRowCoordinates rowCoordinates) {
        onTableRow(rowCoordinates);
        int cellIndex = 0;
        for (Object rowContentElement : rowCoordinates.getRow().getContent()) {
            if (XmlUtils.unwrap(rowContentElement) instanceof Tc) {
                Tc cell = rowContentElement instanceof Tc ? (Tc) rowContentElement : (Tc) ((JAXBElement) rowContentElement).getValue();
                TableCellCoordinates cellCoordinates = new TableCellCoordinates(cell, cellIndex, rowCoordinates);
                walkTableCell(cellCoordinates);
            }
            cellIndex++;
        }
    }

    private void walkTableCell(TableCellCoordinates cellCoordinates) {
        onTableCell(cellCoordinates);
        int elementIndex = 0;
        for (Object cellContentElement : cellCoordinates.getCell().getContent()) {
            if (XmlUtils.unwrap(cellContentElement) instanceof P) {
                P p = (P) cellContentElement;
                ParagraphCoordinates paragraphCoordinates = new ParagraphCoordinates(p, elementIndex, cellCoordinates);
                onParagraph(paragraphCoordinates);
            } else if (XmlUtils.unwrap(cellContentElement) instanceof Tbl) {
                Tbl nestedTable = (Tbl) ((JAXBElement) cellContentElement).getValue();
                TableCoordinates innerTableCoordinates = new TableCoordinates(nestedTable, elementIndex, cellCoordinates);
                walkTable(innerTableCoordinates);
            }
            elementIndex++;
        }
    }

    protected abstract void onParagraph(ParagraphCoordinates paragraphCoordinates);

    protected abstract CommentWrapper onRun(RunCoordinates runCoordinates, ParagraphCoordinates paragraphCoordinates);
    
    protected abstract void onTable(TableCoordinates tableCoordinates);

    protected abstract void onTableCell(TableCellCoordinates tableCellCoordinates);

    protected abstract void onTableRow(TableRowCoordinates tableRowCoordinates);

}
