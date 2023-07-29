package org.wickedsource.docxstamper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static pro.verron.docxstamper.utils.context.Contexts.*;

class DefaultTests {

    /**
     * <p>source.</p>
     *
     * @return a {@link java.util.stream.Stream} object
     */
    public static Stream<Arguments> source() {
        return Stream.of(
                Arguments.of(
                        "Tabulation should be preserved",
                        name("Homer Simpson"),
                        getResource("TabsIndentationTest.docx"),
                        List.of(
                                "|Tab/lang=en-US|||TAB|/lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}",
                                "|Space/lang=en-US|| /lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}"
                        )
                ),
                Arguments.of(
                        "White spaces should be preserved",
                        name("Homer Simpson"),
                        getResource("TabsIndentationTest.docx"),
                        List.of(
                                "|Tab/lang=en-US|||TAB|/lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}",
                                "|Space/lang=en-US|| /lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}"
                        )
                ),
                Arguments.of(
                        "Ternary operators should function",
                        name("Homer"),
                        getResource("TernaryOperatorTest.docx"),
                        List.of(
                                "Expression Replacement with ternary operator",
                                "This paragraph is untouched.//rPr={}",
                                "Some replacement before the ternary operator: Homer.//rPr={}",
                                "Homer <-- this should read \"Homer\".//rPr={}",
                                " <-- this should be empty.//rPr={}"
                        )
                ),
                Arguments.of("Repeating table rows should be possible",
                        roles(
                                role("Homer Simpson", "Dan Castellaneta"),
                                role("Marge Simpson", "Julie Kavner"),
                                role("Bart Simpson", "Nancy Cartwright"),
                                role("Kent Brockman", "Harry Shearer"),
                                role("Disco Stu", "Hank Azaria"),
                                role("Krusty the Clown", "Dan Castellaneta")
                        ),
                        getResource("RepeatTableRowTest.docx"),
                        List.of(
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
                        )
                )
        );
    }

    private static InputStream getResource(String s) {
        return DefaultTests.class.getResourceAsStream(s);
    }

    @MethodSource("source")
    @ParameterizedTest
    void features(String ignoredName, Object context, InputStream template, List<String> expected) {
        var stamper = new TestDocxStamper<>();
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertIterableEquals(expected, actual);
    }
}
