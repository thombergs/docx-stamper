package org.wickedsource.docxstamper.expression;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Objekte dieser Klasse dienen als Root-Objekt f�r die Auswertung von Spring-Expression-Language Expressions.
 */
public class ExpressionContext {

    private final XWPFParagraph paragraph;

    private final Object daten;

    private boolean deleteParagraph = false;

    private boolean makeBold = false;

    public ExpressionContext(XWPFParagraph paragraph, Object daten) {
        this.paragraph = paragraph;
        this.daten = daten;
    }

    /**
     * Kann in einer Expression aufgerufen werden, um zu definieren, dass der kommentierte Paragraph gel�scht werden soll.
     */
    public void displayIf(Boolean expression){
        if(expression == null || !expression){
            deleteParagraph = true;
        }else{
            deleteParagraph = false;
        }
    }

    /**
     * Kann in einer Expression aufgerufen werden, um zu definieren, dass der kommentierte Paragraph fett geschrieben werden soll.
     */
    public void boldIf(Boolean expression){
        if(expression == null || !expression){
            makeBold = false;
        }else{
            makeBold = true;
        }
    }

    public boolean isDeleteParagraph() {
        return deleteParagraph;
    }

    public boolean isMakeBold() {
        return makeBold;
    }

    public Object getDaten() {
        return daten;
    }

    public XWPFParagraph getParagraph() {
        return paragraph;
    }
}
