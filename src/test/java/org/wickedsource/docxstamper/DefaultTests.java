package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.docxstamper.utils.context.Contexts.*;

@DisplayName("Core Features")
class DefaultTests {

    /**
     * <p>source.</p>
     *
     * @return a {@link java.util.stream.Stream} object
     */
    public static Stream<Arguments> source() {
        return Stream.of(Arguments.of("Tabulation should be preserved",
                                      name("Homer Simpson"),
                                      getResource("TabsIndentationTest.docx"),
                                      """
                                              |Tab/lang=en-US|||TAB|/lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}
                                              |Space/lang=en-US|| /lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}"""),
                         Arguments.of("White spaces should be preserved",
                                      name("Homer Simpson"),
                                      getResource("TabsIndentationTest.docx"),
                                      """
                                              |Tab/lang=en-US|||TAB|/lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}
                                              |Space/lang=en-US|| /lang=en-US||Homer Simpson/lang=en-US|//rPr={lang=en-US}"""),
                         Arguments.of("Ternary operators should function",
                                      name("Homer"),
                                      getResource("TernaryOperatorTest.docx"),
                                      """
                                              Expression Replacement with ternary operator
                                              This paragraph is untouched.//rPr={}
                                              Some replacement before the ternary operator: Homer.//rPr={}
                                              Homer <-- this should read \"Homer\".//rPr={}
                                               <-- this should be empty.//rPr={}"""),
                         Arguments.of("Repeating table rows should be possible",
                                      roles(role("Homer Simpson",
                                                 "Dan Castellaneta"),
                                            role("Marge Simpson",
                                                 "Julie Kavner"),
                                            role("Bart Simpson",
                                                 "Nancy Cartwright"),
                                            role("Kent Brockman",
                                                 "Harry Shearer"),
                                            role("Disco Stu", "Hank Azaria"),
                                            role("Krusty the Clown",
                                                 "Dan Castellaneta")),
                                      getResource("RepeatTableRowTest.docx"),
                                      """
                                              |Repeating Table Rows/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                                              |List of Simpsons characters/b=true|//rPr={b=true},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                                              |Character name/b=true|//rPr={b=true}
                                              |Voice /b=true||Actor/b=true|//rPr={b=true}
                                              |Homer Simpson/|//rPr={}
                                              |Dan Castellaneta/|//rPr={}
                                              |Marge Simpson/|//rPr={}
                                              |Julie Kavner/|//rPr={}
                                              |Bart Simpson/|//rPr={}
                                              |Nancy Cartwright/|//rPr={}
                                              |Kent Brockman/|//rPr={}
                                              |Harry Shearer/|//rPr={}
                                              |Disco Stu/|//rPr={}
                                              |Hank Azaria/|//rPr={}
                                              |Krusty the Clown/|//rPr={}
                                              |Dan Castellaneta/|//rPr={}
                                              //rPr={}
                                              |There are /||6/lang=de-DE|| characters in the above table./|//rPr={lang=de-DE},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}"""));
    }

    private static InputStream getResource(String s) {
        return DefaultTests.class.getResourceAsStream(s);
    }

    @MethodSource("source")
    @ParameterizedTest
    void features(String ignoredName, Object context, InputStream template, String expected) {
        var stamper = new TestDocxStamper<>();
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }

    @Test
    void replaceWordWithIntegrationTest() {
        var context = name("Simpsons");
        var template = getClass().getResourceAsStream(
                "integration\\ReplaceWordWithIntegrationTest.docx");
        var stamper = new TestDocxStamper<>();
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                ReplaceWordWith Integration//rPr={}
                This variable |name/b=true|| /b=true|should be resolved to the value Simpsons.//rPr={b=true}
                This variable |name/b=true| should be resolved to the value Simpsons.//rPr={}
                //rPr={}""";
        assertEquals(expected, actual);
    }

    @Test
    void replaceNullExpressionTest() {
        var context = name(null);
        var template = getClass().getResourceAsStream(
                "ReplaceNullExpressionTest.docx");
        var config = new DocxStamperConfiguration().replaceNullValues(true);
        var stamper = new TestDocxStamper<>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                I am .//rPr={}
                //rPr={u=single}""";
        assertEquals(expected, document);
    }

    @Test
    void repeatTableRowKeepsFormatTest() {
        Show context = new Show(
                List.of(new CharacterRecord(1, "st", "Homer Simpson",
                                            "Dan Castellaneta"),
                        new CharacterRecord(2, "nd", "Marge Simpson",
                                            "Julie Kavner"),
                        new CharacterRecord(3, "rd", "Bart Simpson",
                                            "Nancy Cartwright"),
                        new CharacterRecord(4, "th", "Lisa Simpson",
                                            "Yeardley Smith"),
                        new CharacterRecord(5, "th", "Maggie Simpson",
                                            "Julie Kavner")));
        InputStream template = getClass().getResourceAsStream(
                "integration\\RepeatTableRowKeepsFormatTest.docx");
        var stamper = new TestDocxStamper<Show>();
        var document = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                1|st/vertAlign=superscript| Homer Simpson-|Dan Castellaneta/b=true|
                2|nd/vertAlign=superscript| Marge Simpson-|Julie Kavner/b=true|
                3|rd/vertAlign=superscript| Bart Simpson-|Nancy Cartwright/b=true|
                4|th/vertAlign=superscript| Lisa Simpson-|Yeardley Smith/b=true|
                5|th/vertAlign=superscript| Maggie Simpson-|Julie Kavner/b=true|
                """;
        assertEquals(expected, document);
    }

    @Test
    void repeatParagraphTest() {
        var context = new Characters(
                List.of(new Character("Homer Simpson", "Dan Castellaneta"),
                        new Character("Marge Simpson", "Julie Kavner"),
                        new Character("Bart Simpson", "Nancy Cartwright"),
                        new Character("Kent Brockman", "Harry Shearer"),
                        new Character("Disco Stu", "Hank Azaria"),
                        new Character("Krusty the Clown", "Dan Castellaneta")));
        var template = getClass().getResourceAsStream(
                "RepeatParagraphTest.docx");
        var stamper = new TestDocxStamper<Characters>();
        var document = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                |Repeating /||Paragraphs/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |List of Simpsons characters/b=true|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |Homer Simpson/|//rPr={}
                |Dan Castellaneta/|//rPr={}
                |Marge Simpson/|//rPr={}
                |Julie Kavner/|//rPr={}
                |Bart Simpson/|//rPr={}
                |Nancy Cartwright/|//rPr={}
                |Kent Brockman/|//rPr={}
                |Harry Shearer/|//rPr={}
                |Disco Stu/|//rPr={}
                |Hank Azaria/|//rPr={}
                |Krusty the Clown/|//rPr={}
                |Dan Castellaneta/|//rPr={}
                //rPr={}
                |There are /||6/lang=de-DE|| characters./|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument() throws IOException {
        var context = Map.of("units", Stream.of(
                        new Image(getClass().getResourceAsStream("butterfly.png")),
                        new Image(getClass().getResourceAsStream("map.jpg")))
                .map(image -> Map.of("coverImage", image))
                .map(map -> Map.of("productionFacility", map))
                .toList());

        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        var stamper = new TestDocxStamper<Map<String, ?>>(config);
        var template = getClass().getResourceAsStream(
                "RepeatDocPartWithImageTest.docx");

        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                //
                rId11:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130//
                rId12:image/jpeg:407.5kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=:cy=$d:6120130//
                //
                //
                //
                Always rendered://
                rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130//
                //""";
        assertEquals(expected, document);
    }

    @Test
    void repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate() throws Docx4JException, IOException {
        var context = new HashMap<String, Object>();
        var subDocParts = new ArrayList<Map<String, Object>>();

        var firstPart = new HashMap<String, Object>();
        firstPart.put("name", "first doc part");
        subDocParts.add(firstPart);

        var secondPart = new HashMap<String, Object>();
        secondPart.put("name", "second doc part");
        subDocParts.add(secondPart);

        context.put("subDocParts", subDocParts);

        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                (StandardEvaluationContext ctx) -> ctx.addPropertyAccessor(
                        new MapAccessor()));

        var template = getClass().getResourceAsStream(
                "RepeatDocPartWithImagesInSourceTest.docx");
        var stamper = new TestDocxStamper<Map<String, Object>>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                This is not repeated
                This should be repeated : first doc part
                rId12:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720
                This should be repeated too
                This should be repeated : second doc part
                rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720
                This should be repeated too
                This is not repeated""";
        assertEquals(expected, document);
    }

    @Test
    void repeatDocPartTest() {
        var context = new Characters(
                List.of(new Character("Homer Simpson", "Dan Castellaneta"),
                        new Character("Marge Simpson", "Julie Kavner"),
                        new Character("Bart Simpson", "Nancy Cartwright"),
                        new Character("Kent Brockman", "Harry Shearer"),
                        new Character("Disco Stu", "Hank Azaria"),
                        new Character("Krusty the Clown", "Dan Castellaneta")));

        var template = getClass().getResourceAsStream("RepeatDocPartTest.docx");
        var stamper = new TestDocxStamper<Characters>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                Repeating Doc Part//rPr={}
                |List /b=true||of/b=true|| Simpsons /b=true||characters/b=true|//rPr={b=true},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Paragraph for test: Homer Simpson - Dan Castellaneta//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Homer Simpson//jc=center
                Dan Castellaneta//jc=center,rPr={}
                 ||BR|/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                Paragraph for test: Marge Simpson - Julie Kavner//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Marge Simpson//jc=center
                Julie Kavner//jc=center,rPr={}
                 ||BR|/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                Paragraph for test: Bart Simpson - Nancy Cartwright//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Bart Simpson//jc=center
                Nancy Cartwright//jc=center,rPr={}
                 ||BR|/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                Paragraph for test: Kent Brockman - Harry Shearer//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Kent Brockman//jc=center
                Harry Shearer//jc=center,rPr={}
                 ||BR|/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                Paragraph for test: Disco Stu - Hank Azaria//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Disco Stu//jc=center
                Hank Azaria//jc=center,rPr={}
                 ||BR|/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                Paragraph for test: Krusty the Clown - Dan Castellaneta//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                Krusty the Clown//jc=center
                Dan Castellaneta//jc=center,rPr={}
                 ||BR|/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                There are 6 characters.//rPr={}""";
        assertEquals(expected, document);

    }

    @Test
    void repeatDocPartNestingTest() {
        List<Grade> grades = new ArrayList<>();
        for (int grade1 = 0; grade1 < 3; grade1++) {
            var classes = new ArrayList<AClass>();
            for (int classroom1 = 0; classroom1 < 3; classroom1++) {
                var students = new ArrayList<Student>();
                for (int i = 0; i < 5; i++) {
                    students.add(new Student(i, "Bruce·No" + i, 1 + i));
                }
                classes.add(new AClass(classroom1, students));
            }
            grades.add(new Grade(grade1, classes));
        }
        var context = new SchoolContext("South Park Primary School", grades);
        var template = getClass().getResourceAsStream(
                "RepeatDocPartNestingTest.docx");
        var stamper = new TestDocxStamper<SchoolContext>();
        var result = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Repeating /||N/lang=en-US||ested/|| /||Doc Part /|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |List of All the s/||tu/lang=en-US||dent’s of all grades/lang=null|//rPr={lang=null},suppressAutoHyphens=xxx,widowControl=xxx
                |South Park Primary School/lang=null|//rPr={lang=null},suppressAutoHyphens=xxx,widowControl=xxx
                |Grade No./b=true,lang=null||0/b=true,lang=null|| /b=true,lang=null||t/lang=null||here are /||3/|| /||classes/|//rPr={b=true,lang=null},suppressAutoHyphens=xxx,widowControl=xxx
                |Class No./||0/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Class No./||1/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Class No./||2/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Grade No./b=true,lang=null||1/b=true,lang=null|| /b=true,lang=null||t/lang=null||here are /||3/|| /||classes/|//rPr={b=true,lang=null},suppressAutoHyphens=xxx,widowControl=xxx
                |Class No./||0/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Class No./||1/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Class No./||2/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Grade No./b=true,lang=null||2/b=true,lang=null|| /b=true,lang=null||t/lang=null||here are /||3/|| /||classes/|//rPr={b=true,lang=null},suppressAutoHyphens=xxx,widowControl=xxx
                |Class No./||0/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Class No./||1/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Class No./||2/|| /||t/lang=null||here are /||5/|| students/|//rPr={},suppressAutoHyphens=xxx,widowControl=xxx
                |0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No0/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No1/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No2/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No3/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |Bruce·No4/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |5/|//ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx
                |There are /|3| /|grades.//rPr={}""";
        assertEquals(expected, result);
    }

    @Test
    void RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate() {
        var context = new HashMap<String, Object>();

        var firstTable = new ArrayList<TableValue>();
        firstTable.add(new TableValue("firstTable value1"));
        firstTable.add(new TableValue("firstTable value2"));

        var secondTable = new ArrayList<TableValue>();
        secondTable.add(new TableValue("repeatDocPart value1"));
        secondTable.add(new TableValue("repeatDocPart value2"));
        secondTable.add(new TableValue("repeatDocPart value3"));

        List<TableValue> thirdTable = new ArrayList<>();
        thirdTable.add(new TableValue("secondTable value1"));
        thirdTable.add(new TableValue("secondTable value2"));
        thirdTable.add(new TableValue("secondTable value3"));
        thirdTable.add(new TableValue("secondTable value4"));

        context.put("firstTable", firstTable);
        context.put("secondTable", secondTable);
        context.put("thirdTable", thirdTable);

        var template = getClass().getResourceAsStream(
                "RepeatDocPartAndCommentProcessorsIsolationTest.docx");
        var config = new DocxStamperConfiguration();
        config.setEvaluationContextConfigurer(
                (ctx) -> ctx.addPropertyAccessor(new MapAccessor()));

        var stamper = new TestDocxStamper<Map<String, Object>>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                This will stay untouched.//rPr={}
                				
                firstTable value1
                firstTable value2
                				
                This will also stay untouched.//rPr={}
                				
                Repeating paragraph :
                				
                repeatDocPart value1
                Repeating paragraph :
                				
                repeatDocPart value2
                Repeating paragraph :
                				
                repeatDocPart value3
                				
                secondTable value1
                secondTable value2
                secondTable value3
                secondTable value4
                				
                This will stay untouched too.//rPr={}""";
        assertEquals(expected, document);
    }

    @Test
    void changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment() {
        var context = Map.of("repeatValues",
                             List.of(new Name("Homer"), new Name("Marge")));

        var template = getClass().getResourceAsStream(
                "ChangingPageLayoutOutsideRepeatParagraphTest.docx");
        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        var stamper = new TestDocxStamper<Map<String, ?>>(config);
        var result = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                First page is landscape.
                				
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                				
                Without a section break changing the layout in between, but a page break instead.|BR|
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                				
                Without a section break changing the layout in between, but a page break instead.|BR|
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Fourth page is set to landscape again.""";
        assertEquals(expected, result);
    }

    @Test
    void changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment() throws IOException, Docx4JException {
        Map<String, Object> context = new HashMap<>();

        Name name1 = new Name("Homer");
        Name name2 = new Name("Marge");

        List<Name> repeatValues = new ArrayList<>();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);

        var template = getClass().getResourceAsStream(
                "ChangingPageLayoutInRepeatParagraphTest.docx");
        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        var stamper = new TestDocxStamper<Map<String, Object>>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                First page is landscape.
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                With a page break changing the layout in between.//sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                With a page break changing the layout in between.
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                Fourth page is set to portrait again.""";
        assertEquals(expected, document);
    }

    @Test
    void changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment() throws IOException, Docx4JException {
        var context = Map.of("repeatValues",
                             List.of(new Name("Homer"), new Name("Marge")));

        var template = getClass().getResourceAsStream(
                "ChangingPageLayoutInRepeatDocPartTest.docx");
        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        var stamper = new TestDocxStamper<Map<String, ?>>(config);

        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                First page is portrait.
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Second page is landscape, layout change should survive to repeatDocPart (Homer).
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                With a break setting the layout to portrait in between.//sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Second page is landscape, layout change should survive to repeatDocPart (Marge).
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                With a break setting the layout to portrait in between.//sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Fourth page is set to landscape again.""";
        assertEquals(expected, document);
    }

    @Test
    void changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement() throws IOException, Docx4JException {
        var context = Map.of("repeatValues",
                             List.of(new Name("Homer"), new Name("Marge")));

        InputStream template = getClass().getResourceAsStream(
                "ChangingPageLayoutInRepeatDocPartWithTableLastElementTest.docx");
        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        var stamper = new TestDocxStamper<Map<String, ?>>(config);

        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                First page is portrait.
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Second page is landscape, layout change should survive to repeatDocPart (Homer).
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                With a break setting the layout to portrait in between.
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Second page is landscape, layout change should survive to repeatDocPart (Marge).
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                With a break setting the layout to portrait in between.
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Fourth page is set to landscape again.""";
        assertEquals(expected, document);
    }

    @Test
    void changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment() throws IOException, Docx4JException {
        var context = Map.of("repeatValues",
                             List.of(new Name("Homer"), new Name("Marge")));

        var template = getClass().getResourceAsStream(
                "ChangingPageLayoutOutsideRepeatDocPartTest.docx");
        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        var stamper = new TestDocxStamper<Map<String, ?>>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                First page is landscape.
                                
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}}
                Second page is portrait, layout change should survive to repeatDocPart (Homer).
                |BR|
                Without a break changing the layout in between (page break should be repeated).
                Second page is portrait, layout change should survive to repeatDocPart (Marge).
                |BR|
                Without a break changing the layout in between (page break should be repeated).
                //sectPr={docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}}
                Fourth page is set to landscape again.""";
        assertEquals(expected, document);
    }

    @Test
    void conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved() {
        var context = new Name("Homer");
        var template = getClass().getResourceAsStream(
                "ConditionalDisplayOfParagraphsTest.docx");

        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Conditional Display of Paragraphs/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph stays untouched./lang=de-DE|//rPr={lang=de-DE}
                |This paragraph stays untouched./lang=de-DE|//rPr={lang=de-DE},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}
                |Conditional Display of paragraphs also works in tables/b=true|//rPr={b=true}
                |This paragraph stays untouched./|//rPr={}
                                
                |Also works in nested tables/b=true|//rPr={b=true}
                |This paragraph stays untouched./|//rPr={}
                //rPr={}
                //rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved() {
        var context = new Name("Homer");
        var template = getClass().getResourceAsStream(
                "ConditionalDisplayOfParagraphsWithoutCommentTest.docx");
        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Conditional Display of Paragraphs/lang=en-US|//rPr={lang=en-US}
                |This paragraph stays untouched./lang=en-US|//rPr={lang=en-US}
                This paragraph stays untouched.//rPr={}
                |Conditional Display of paragraphs also works in tables/b=true,lang=en-US|//rPr={b=true,lang=en-US}
                This paragraph stays untouched.//rPr={}
                                
                |Also works in nested tables/b=true,lang=en-US|//rPr={b=true,lang=en-US}
                |This paragraph stays untouched./lang=en-US|//rPr={lang=en-US}
                //rPr={lang=en-US}
                //rPr={lang=en-US}""";
        assertEquals(expected, document);
    }

    @Test
    void conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved() {
        var context = new Name("Bart");
        var template = getClass().getResourceAsStream(
                "ConditionalDisplayOfParagraphsWithoutCommentTest.docx");
        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Conditional Display of Paragraphs/lang=en-US|//rPr={lang=en-US}
                |This paragraph stays untouched./lang=en-US|//rPr={lang=en-US}
                |This paragraph is only included in the resulting document if the variable „name“ has the value „Bart“./lang=en-US|//rPr={lang=en-US}
                This paragraph stays untouched.//rPr={}
                |Conditional Display of paragraphs also works in tables/b=true,lang=en-US|//rPr={b=true,lang=en-US}
                This paragraph stays untouched.//rPr={}
                |This paragraph is only included if the name is „Bart“./lang=en-US|//rPr={lang=en-US}
                |Also works in nested tables/b=true,lang=en-US|//rPr={b=true,lang=en-US}
                |This paragraph stays untouched./lang=en-US|//rPr={lang=en-US}
                |This paragraph is only included if the name is „Bart“./lang=en-US|//rPr={lang=en-US}
                //rPr={lang=en-US}
                //rPr={lang=en-US}""";
        assertEquals(expected, document);
    }

    @Test
    void conditionalDisplayOfTableRowsTest() {
        var context = new Name("Homer");
        var template = getClass().getResourceAsStream(
                "ConditionalDisplayOfTableRowsTest.docx");
        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Conditional Display of Table /||Rows/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph stays untouched./lang=de-DE|//rPr={lang=de-DE}
                |This row stays untouched./|//rPr={}
                |This row stays untouched./|//rPr={}
                |Also works on nested Tables/b=true|//rPr={b=true}
                |This row stays untouched./|//rPr={}
                //rPr={}
                //rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void conditionalDisplayOfTablesBug32Test() {
        var context = new Name("Homer");
        var template = getClass().getResourceAsStream(
                "ConditionalDisplayOfTablesBug32Test.docx");
        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Conditional Display of Tables/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph stays untouched./lang=de-DE|//rPr={}
                //rPr={}
                |This table stays untouched./|//rPr={},widowControl=xxx
                //rPr={},widowControl=xxx
                //rPr={},widowControl=xxx
                //rPr={},widowControl=xxx
                //rPr={}
                |Also works on nested tables/b=true|//rPr={b=true},widowControl=xxx
                //rPr={b=true},widowControl=xxx
                //rPr={}
                |This paragraph stays untouched./lang=de-DE|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void conditionalDisplayOfTablesTest() {
        var context = new Name("Homer");
        var template = getClass().getResourceAsStream(
                "ConditionalDisplayOfTablesTest.docx");
        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Conditional Display of /||Tables/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph stays untouched./lang=de-DE|//rPr={lang=de-DE}
                //rPr={}
                |This table stays untouched./|//rPr={}
                //rPr={}
                //rPr={}
                //rPr={}
                //rPr={}
                |Also works on nested tables/b=true|//rPr={b=true}
                //rPr={b=true}
                //rPr={}
                |This paragraph stays untouched./lang=de-DE|//rPr={lang=de-DE},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored() {
        var context = new EmptyContext();
        var config = new DocxStamperConfiguration();
        config.setEvaluationContextConfigurer(
                evalContext -> evalContext.addPropertyAccessor(
                        new SimpleGetter("foo", "bar")));

        var template = getClass().getResourceAsStream(
                "CustomEvaluationContextConfigurerTest.docx");
        var stamper = new TestDocxStamper<EmptyContext>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Custom EvaluationContextConfigurer Test/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph stays untouched./lang=de-DE|//rPr={lang=de-DE}
                |The variable foo has the value /||bar/lang=de-DE||./|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void customExpressionFunctionTest() {
        var context = new Name("Homer Simpson");
        var template = getClass().getResourceAsStream(
                "CustomExpressionFunction.docx");
        var config = new DocxStamperConfiguration()
                .exposeInterfaceToExpressionLanguage(UppercaseFunction.class,
                                                     new UppercaseFunctionImpl());
        var stamper = new TestDocxStamper<Name>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Custom Expression Function/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph is untouched./lang=de-DE|//rPr={lang=de-DE}
                |In this paragraph, a custom expression function is used to uppercase a String: |BR|/lang=de-DE||HOMER SIMPSON/b=true,lang=de-DE||./lang=de-DE|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}
                |To test that custom functions work together with comment expressions, we toggle visibility of this paragraph with a comment expression./lang=de-DE|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void customTypeResolverTest() {
        var resolver = new CustomTypeResolver();
        var config = new DocxStamperConfiguration().addTypeResolver(
                CustomType.class, resolver);
        var template = getClass().getResourceAsStream(
                "CustomTypeResolverTest.docx");
        var stamper = new TestDocxStamper<Context>(config);
        var context = new Context(new CustomType());
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Custom /||TypeResolver/|| Test/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph is untouched./lang=de-DE|//rPr={}
                |The name should be resolved to /lang=de-DE||foo/b=true,lang=de-DE||./lang=de-DE|//rPr={}
                |This paragraph is untouched./lang=de-DE|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void dateReplacementTest() {
        var now = new Date();
        var context = new DateContext(now);

        var template = getClass().getResourceAsStream(
                "DateReplacementTest.docx");
        var stamper = new TestDocxStamper<>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var defaultFormat = new SimpleDateFormat(
                "dd.MM.yyyy");
        var expected = """
                |Replacing date expressions/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |Today is: /||%s/|//rPr={}""".formatted(
                defaultFormat.format(now));
        assertEquals(expected, document);
    }

    @Test
    void expressionReplacementInGlobalParagraphsTest() {
        var context = new Name("Homer Simpson");
        var template = getClass().getResourceAsStream(
                "ExpressionReplacementInGlobalParagraphsTest.docx");
        var stamper = new TestDocxStamper<Name>(
                new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                        false));
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Expression Replacement in global paragraphs/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph is untouched./lang=de-DE|//rPr={lang=de-DE}
                |In this paragraph, the variable /lang=de-DE||name/b=true,lang=de-DE|| should be resolved to the value /||Homer Simpson/lang=de-DE||./|//rPr={}
                |In this paragraph, the variable /lang=de-DE||foo/b=true,lang=de-DE|| should not be resolved: ${foo}./lang=de-DE|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void expressionReplacementInTablesTest() {
        var context = new Name("Bart Simpson");
        var template = getClass().getResourceAsStream(
                "ExpressionReplacementInTablesTest.docx");

        var stamper = new TestDocxStamper<Name>(
                new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                        false));
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Expression/|| Replacement in Tables/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This should resolve to a name:/|//rPr={}
                |Bart Simpson/|//rPr={}
                |This should not resolve:/|//rPr={}
                |${foo}/|//rPr={}
                |Nested Table:/b=true|//rPr={b=true}
                |This should resolve to a name:/|//rPr={}
                |Bart Simpson/|//rPr={}
                |This should not resolve:/|//rPr={}
                |${foo}/|//rPr={}
                //rPr={}
                //rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void expressionReplacementWithFormattingTest() {
        var context = new Name("Homer Simpson");
        var template = getClass().getResourceAsStream(
                "ExpressionReplacementWithFormattingTest.docx");
        var stamper = new TestDocxStamper<Name>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                 Expression Replacement with text format//rPr={}
                The text format should be kept intact when an expression is replaced.//rPr={}
                It should be bold: |Homer Simpson/b=true|//rPr={b=true}
                It should be italic: |Homer Simpson/i=true|//rPr={i=true}
                It should be superscript: |Homer Simpson/vertAlign=superscript|//rPr={i=true}
                It should be subscript: |Homer Simpson/vertAlign=subscript|//rPr={vertAlign=subscript}
                It should be striked: |Homer Simpson/strike=true|//rPr={i=true}
                It should be underlined: |Homer Simpson/u=single|//rPr={i=true}
                It should be doubly underlined: |Homer Simpson/u=double|//rPr={i=true}
                It should be thickly underlined: |Homer Simpson/u=thick|//rPr={i=true}
                It should be dot underlined: |Homer Simpson/u=dotted|//rPr={i=true}
                It should be dash underlined: |Homer Simpson/u=dash|//rPr={i=true}
                It should be dot and dash underlined: |Homer Simpson/u=dotDash|//rPr={i=true}
                It should be dot, dot and dash underlined: |Homer Simpson/u=dotDotDash|//rPr={i=true}
                It should be highlighted yellow: |Homer Simpson/highlight=yellow|//rPr={}
                It should be white over darkblue: |Homer Simpson/color=FFFFFF,highlight=darkBlue|//rPr={b=true}
                It should be with header formatting: |Homer Simpson/rStyle=TitreCar|//rPr={b=true}""";
        assertEquals(expected, document);
    }

    @Test
    void expressionWithSurroundingSpacesTest() {
        var spacyContext = new SpacyContext();
        var template = getClass().getResourceAsStream(
                "ExpressionWithSurroundingSpacesTest.docx");
        var stamper = new TestDocxStamper<SpacyContext>();
        var document = stamper.stampAndLoadAndExtract(template, spacyContext);

        var expected = """
                |Expression Replacement /||when expression has leading and/or trailing spaces/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |When an expression within a paragraph is resolved, the spaces between the replacement and the surrounding text should be as expected. The following paragraphs should all look the same./|//rPr={}
                |Before/|| Expression /||After./|//rPr={}
                |Before/|| Expression/|| After./|//rPr={}
                |Before /||Expression /||After./|//rPr={}
                |Before /||Expression/|| After./|//rPr={}
                |Before/|| Expression /||After./|//rPr={}
                |Before /||Expression /||After./|//rPr={}
                |Before/|| Expression/|| After./|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void expressionReplacementWithCommentsTest() {
        var context = new Name("Homer Simpson");
        var template = getClass().getResourceAsStream(
                "ExpressionReplacementWithCommentsTest.docx");
        var stamper = new TestDocxStamper<Name>(
                new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                        false));
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Expression Replacement with comments/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph is untouched./|//rPr={}
                |In this paragraph, the variable /||name/b=true|| should be resolved to the value /||Homer Simpson/||./|//rPr={}
                |In this paragraph, the variable /||foo/b=true|| should not be resolved: /||unresolvedValueWithComment/|||replaceWordWith(foo)/|/||./|//rPr={},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    /**
     * <p>test.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void imageReplacementInGlobalParagraphsTest() throws IOException {
        var context = new ImageContext(
                new Image(getClass().getResourceAsStream("monalisa.jpg")));
        var template = getClass().getResourceAsStream(
                "ImageReplacementInGlobalParagraphsTest.docx");
        var stamper = new TestDocxStamper<ImageContext>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Image/|| Replacement in global paragraphs/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph is untouched./lang=de-DE|//rPr={lang=de-DE}
                |In this paragraph, an image of Mona Lisa is inserted: /||rId4:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350/lang=de-DE||./|//rPr={lang=de-DE}
                |This paragraph has the image /||rId5:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350/lang=de-DE|| in the middle./|//rPr={lang=de-DE},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void imageReplacementInGlobalParagraphsTestWithMaxWidth() throws IOException {
        var context = new ImageContext(
                new Image(getClass().getResourceAsStream("monalisa.jpg"),
                          1000));
        var template = getClass().getResourceAsStream(
                "ImageReplacementInGlobalParagraphsTest.docx");
        var stamper = new TestDocxStamper<ImageContext>();
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Image/|| Replacement in global paragraphs/|//rPr={},spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}
                |This paragraph is untouched./lang=de-DE|//rPr={lang=de-DE}
                |In this paragraph, an image of Mona Lisa is inserted: /||rId4:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000/lang=de-DE||./|//rPr={lang=de-DE}
                |This paragraph has the image /||rId5:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000/lang=de-DE|| in the middle./|//rPr={lang=de-DE},spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}""";
        assertEquals(expected, document);
    }

    @Test
    void leaveEmptyOnExpressionErrorTest() {
        var context = new Name("Homer Simpson");
        var template = getClass().getResourceAsStream(
                "LeaveEmptyOnExpressionErrorTest.docx");
        var config = new DocxStamperConfiguration()
                .setFailOnUnresolvedExpression(false)
                .leaveEmptyOnExpressionError(true);
        var stamper = new TestDocxStamper<Name>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                Leave me empty .//rPr={}
                //rPr={u=single}""";
        assertEquals(expected, document);
    }

    @Test
    void lineBreakReplacementTest() {
        var context = new Name(null);
        var config = new DocxStamperConfiguration();
        config.setLineBreakPlaceholder("#");
        var template = getClass().getResourceAsStream(
                "LineBreakReplacementTest.docx");
        var stamper = new TestDocxStamper<Name>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                |Line Break Replacement/lang=en-US|//rPr={lang=en-US}
                |This paragraph is untouched./lang=en-US|//rPr={lang=en-US}
                |This paragraph should be /|||BR|/lang=en-US|| split in /|||BR|/lang=en-US|| three/lang=en-US|| lines./lang=en-US|//rPr={lang=en-US}
                |This paragraph is untouched./lang=en-US|//rPr={lang=en-US}""";
        assertEquals(expected, document);
    }

    @Test
    void mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders() {
        var listProp = new ArrayList<Container>();
        listProp.add(new Container("first value"));
        listProp.add(new Container("second value"));

        var context = new HashMap<String, Object>();
        context.put("FLAT_STRING", "Flat string has been resolved");
        context.put("OBJECT_LIST_PROP", listProp);

        var config = new DocxStamperConfiguration()
                .setFailOnUnresolvedExpression(false)
                .setLineBreakPlaceholder("\n")
                .replaceNullValues(true)
                .nullValuesDefault("N/C")
                .replaceUnresolvedExpressions(true)
                .unresolvedExpressionsDefaultValue("N/C")
                .setEvaluationContextConfigurer(
                        ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        var template = getClass().getResourceAsStream(
                "MapAccessorAndReflectivePropertyAccessorTest.docx");
        var stamper = new TestDocxStamper<Map<String, Object>>(config);
        var document = stamper.stampAndLoadAndExtract(template, context);

        var expected = """
                Flat string : Flat string has been resolved
                               
                Values
                               
                first value
                               
                               
                second value
                               
                               
                               
                Paragraph start
                first value
                Paragraph end
                Paragraph start
                second value
                Paragraph end
                """;
        assertEquals(expected, document);
    }

    public interface UppercaseFunction {
        String toUppercase(String string);
    }

    public record Show(List<CharacterRecord> characters) {}

    public record CharacterRecord(int index, String indexSuffix, String characterName, String actorName) {}

    public record Character(String name, String actor) {}

    public record Characters(List<Character> characters) {}

    public record AClass(int number, List<Student> students) {}

    record Student(int number, String name, int age) {}

    record SchoolContext(String schoolName, List<Grade> grades) {}

    record Grade(int number, List<AClass> classes) {}

    static class TableValue {
        public String value;

        TableValue(String value) {
            this.value = value;
        }
    }

    public record Name(String name) {}

    static class EmptyContext {
    }

    static class SimpleGetter implements PropertyAccessor {

        private final String fieldName;

        private final Object value;

        public SimpleGetter(String fieldName, Object value) {
            this.fieldName = fieldName;
            this.value = value;
        }

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            return true;
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) {
            if (name.equals(this.fieldName)) {
                return new TypedValue(value);
            } else {
                return null;
            }
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) {
        }
    }

    public static class UppercaseFunctionImpl implements UppercaseFunction {
        @Override
        public String toUppercase(String string) {
            return string.toUpperCase();
        }
    }

    record Context(CustomType name) {}

    public static class CustomType {}

    public static class CustomTypeResolver extends AbstractToTextResolver<CustomType> {
        @Override
        protected String resolveStringForObject(CustomType object) {
            return "foo";
        }
    }

    public record DateContext(Date date) {
    }

    static class SpacyContext {
        private final String expressionWithLeadingAndTrailingSpace = " Expression ";
        private final String expressionWithLeadingSpace = " Expression";
        private final String expressionWithTrailingSpace = "Expression ";
        private final String expressionWithoutSpaces = "Expression";

        public String getExpressionWithLeadingAndTrailingSpace() {
            return " Expression ";
        }

        public String getExpressionWithLeadingSpace() {
            return " Expression";
        }

        public String getExpressionWithTrailingSpace() {
            return "Expression ";
        }

        public String getExpressionWithoutSpaces() {
            return "Expression";
        }
    }

    public record ImageContext(Image monalisa) {
    }

    static class Container {
        public String value;

        public Container(String value) {
            this.value = value;
        }
    }

    @Test
    void nullPointerResolutionTest_testWithDefaultSpel() {
        var subContext = new NullPointerResolutionTest.SubContext("Fullish2",
                                                                  List.of("Fullish3", "Fullish4",
                                                                          "Fullish5"));
        var context = new NullPointerResolutionTest.NullishContext("Fullish1", subContext, null, null);
        var template = getClass().getResourceAsStream(
                "NullPointerResolution.docx");

        var config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                false);
        var actual = new TestDocxStamper<NullPointerResolutionTest.NullishContext>(
                config).stampAndLoadAndExtract(template, context);

        var expected = """
                Deal with null references
                
                Deal with: Fullish1
                Deal with: Fullish2
                Deal with: Fullish3
                Deal with: Fullish5
                
                Deal with: Nullish value!!
                Deal with: ${nullish.value ?: \"Nullish value!!\"}
                Deal with: ${nullish.li[0] ?: \"Nullish value!!\"}
                Deal with: ${nullish.li[2] ?: \"Nullish value!!\"}
                """;
        assertEquals(expected, actual);
    }

    @Test
    void nullPointerResolutionTest_testWithCustomSpel() {
        var subContext = new NullPointerResolutionTest.SubContext("Fullish2",
                                                                  List.of("Fullish3", "Fullish4",
                                                                          "Fullish5"));
        var context = new NullPointerResolutionTest.NullishContext("Fullish1", subContext, null, null);
        var template = getClass().getResourceAsStream(
                "NullPointerResolution.docx");

        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration(
                true, true);

        var config = new DocxStamperConfiguration()
                .setSpelParserConfiguration(spelParserConfiguration)
                .setEvaluationContextConfigurer(
                        new NoOpEvaluationContextConfigurer())
                .nullValuesDefault("Nullish value!!")
                .replaceNullValues(true);

        var actual = new TestDocxStamper<NullPointerResolutionTest.NullishContext>(
                config).stampAndLoadAndExtract(template, context);

        var expected = """
                Deal with null references
                
                Deal with: Fullish1
                Deal with: Fullish2
                Deal with: Fullish3
                Deal with: Fullish5
                
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                """;
        assertEquals(expected, actual);
    }


    public static final class NullishContext {
        private String fullish_value;
        private SubContext fullish;
        private String nullish_value;
        private SubContext nullish;

        public NullishContext() {
        }

        public NullishContext(
                String fullish_value,
                SubContext fullish,
                String nullish_value,
                SubContext nullish
        ) {
            this.fullish_value = fullish_value;
            this.fullish = fullish;
            this.nullish_value = nullish_value;
            this.nullish = nullish;
        }

        public String getFullish_value() {
            return fullish_value;
        }

        public void setFullish_value(String fullish_value) {
            this.fullish_value = fullish_value;
        }

        public SubContext getFullish() {
            return fullish;
        }

        public void setFullish(SubContext fullish) {
            this.fullish = fullish;
        }

        public String getNullish_value() {
            return nullish_value;
        }

        public void setNullish_value(String nullish_value) {
            this.nullish_value = nullish_value;
        }

        public SubContext getNullish() {
            return nullish;
        }

        public void setNullish(SubContext nullish) {
            this.nullish = nullish;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NullishContext) obj;
            return Objects.equals(this.fullish_value, that.fullish_value) &&
                   Objects.equals(this.fullish, that.fullish) &&
                   Objects.equals(this.nullish_value, that.nullish_value) &&
                   Objects.equals(this.nullish, that.nullish);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fullish_value, fullish, nullish_value, nullish);
        }

        @Override
        public String toString() {
            return "NullishContext[" +
                   "fullish_value=" + fullish_value + ", " +
                   "fullish=" + fullish + ", " +
                   "nullish_value=" + nullish_value + ", " +
                   "nullish=" + nullish + ']';
        }

    }

    public static final class SubContext {
        private String value;
        private List<String> li;

        public SubContext() {
        }

        public SubContext(
                String value,
                List<String> li
        ) {
            this.value = value;
            this.li = li;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public List<String> getLi() {
            return li;
        }

        public void setLi(List<String> li) {
            this.li = li;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SubContext) obj;
            return Objects.equals(this.value, that.value) &&
                   Objects.equals(this.li, that.li);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, li);
        }

        @Override
        public String toString() {
            return "SubContext[" +
                   "value=" + value + ", " +
                   "li=" + li + ']';
        }

    }





}
