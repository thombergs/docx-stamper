package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class RepeatTableRowTest {
    @Test
    public void test() throws Docx4JException, IOException {
        var context = new Characters(List.of(
                new Character("Homer Simpson", "Dan Castellaneta"),
                new Character("Marge Simpson", "Julie Kavner"),
                new Character("Bart Simpson", "Nancy Cartwright"),
                new Character("Kent Brockman", "Harry Shearer"),
                new Character("Disco Stu", "Hank Azaria"),
                new Character("Krusty the Clown", "Dan Castellaneta")
        ));
        var template = getClass().getResourceAsStream("RepeatTableRowTest.docx");

        var actual = new TestDocxStamper<>().stampAndLoadAndExtract(template, context);

        var expected = List.of(
                "|Repeating Table Rows/|//rPr={},spacing=xxx",
                "|List of Simpsons characters/b=true|//rPr={b=true},spacing=xxx",
                "|Character name/b=true|//rPr={b=true}",
                "|Voice /b=true||Actor/b=true|//rPr={b=true}",
                "|Homer Simpson/|//rPr={}",
                "|Dan Castellaneta/|//rPr={}",
                "|Marge Simpson/|//rPr={}",
                "|Julie Kavner/|//rPr={}",
                "|Bart Simpson/|//rPr={}",
                "|Nancy Cartwright/|//rPr={}",
                "|Kent Brockman/|//rPr={}",
                "|Harry Shearer/|//rPr={}",
                "|Disco Stu/|//rPr={}",
                "|Hank Azaria/|//rPr={}",
                "|Krusty the Clown/|//rPr={}",
                "|Dan Castellaneta/|//rPr={}",
                "//rPr={}",
                "|There are /||6/lang=de-DE|| characters in the above table./|//rPr={lang=de-DE},spacing=xxx"
        );
        assertIterableEquals(expected, actual);
    }


    public record Character(String name, String actor) {
    }

    public record Characters(List<Character> characters) {
    }
}