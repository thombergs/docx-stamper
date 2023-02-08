# docx-stamper

[![Build Status](https://github.com/verronpro/docx-stamper/actions/workflows/maven.yml/badge.svg)](https://github.com/verronpro/docx-stamper/actions/workflows/maven.yml)

docx-stamper is a Java template engine for docx documents. You create a template .docx document with your favorite word
processor
and feed it to a DocxStamper instance to create a document based on the template at runtime. Example code:

```java
class Example {
    public static void main(String[] args) {
        MyContext context = ...// your own POJO against which expressions found in the template will be resolved
        InputStream template = ...// InputStream to your .docx template file
        OutputStream out = ...// OutputStream in which to write the resulting .docx document
        DocxStamper stamper = new DocxStamperConfiguration().build();
        stamper.stamp(template, context, out);
        out.close();
    }
}
```

## Replacing Expressions in a .docx Template

The main feature of docx-stamper is **replacement of expressions** within the text of the template document. Simply add
expressions like `${person.name}` or `${person.name.equals("Homer") ? "Duff" : "Budweiser"}` in the text of your .docx
template and provide a context object against which the expression can be resolved. docx-stamper will try to keep the
original formatting of the text in the template intact. You can use the full feature set
of [Spring Expression Language](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html) (
SpEL).

The value an expression resolves to may be of the following types:

| Type of expression value                                                                                                 | Effect                                                                                                                                                                                                                                       |
|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.lang.Object                                                                                                         | The expression is replaced by the String representation of the object (`String.valueOf()`).                                                                                                                                                  |
| java.lang.String                                                                                                         | The expression is replaced with the String value.                                                                                                                                                                                            |
| java.util.Date                                                                                                           | The expression is replaced by a formatted Date string (by default "dd.MM.yyyy"). You can change the format string by registering your own [DateResolver](src/main/java/org/wickedsource/docxstamper/replace/typeresolver/DateResolver.java). |
| [org.wickedsource.docxstamper...Image](src/main/java/org/wickedsource/docxstamper/replace/typeresolver/image/Image.java) | The expression is replaced with an inline image.                                                                                                                                                                                             |

If an expression cannot be resolved successfully, it will be skipped (meaning the expression stays in the document as it
was in the template). To support more than the above types you can implement your
own [TypeResolver](src/main/java/org/wickedsource/docxstamper/api/typeresolver/ITypeResolver.java). To register your own
TypeResolver with docx-stamper, use the following code:

```java
class Main {
    public static void main(String... args) {
        ITypeResolver typeResolver = ...// instance of your own ITypeResolver implementation
        Class<?> type ...// class of expression values your resolver handles
        DocxStamper stamper=new DocxStamperConfiguration()
        .addTypeResolver(type,typeResolver)
        .build();
    }
}
```

## Customizing the SpEL Evaluation Context

If you want to take more control over the evaluation of expressions, you can implement
a [EvaluationContextConfigurer](src/main/java/org/wickedsource/docxstamper/api/EvaluationContextConfigurer.java)
and customize Springs `StandardEvaluationContext` to your needs. You can register an `EvaluationContextConfigurer` like
this:

```java 
class Main {
    public static void main(String... args) {
        EvaluationContextConfigurer configurer = ...
        DocxStamper stamper = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(configurer)
                .build();
    }
}
```

## Adding custom functions to the Expression Language

If you want to create custom functions (for different number formats or different date formats, for example), you can
register functions
which can then be used in the expression language. The following code for example adds a function `toUppercase(String)`
which can be used within the .docx document to uppercase a String:

```java
class Main {
    public static void main(String... args) {
        interface UppercaseFunction {
            String toUppercase(String string);
        }
        
        DocxStamper stamper = new DocxStamperConfiguration()
                .exposeInterfaceToExpressionLanguage(UppercaseFunction.class, String::toUppercase)
                .build();
    }
}
```

## Conditional Display and Repeating of Elements

Besides replacing expressions, docx-stamper can **process comments on paragraphs of text** in the template .docx
document and do manipulations on the template based on these comments. By default, you can use the following expressions
in comments:

| Expression in .docx comment    | Effect                                                                                                                                                                                                                                                                                                                                                       |
|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `displayParagraphIf(boolean)`  | The commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.                                                                                                                                                                                                                                       |
| `displayTableRowIf(boolean)`   | The table row surrounding the commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.                                                                                                                                                                                                             |
| `displayTableIf(boolean)`      | The whole table surrounding the commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.                                                                                                                                                                                                           |
| `repeatTableRow(List<Object>)` | The table row surrounding the commented paragraph is copied once for each object in the passed-in list. Expressions found in the cells of the table row are evaluated against the object from the list.                                                                                                                                                      |
| `repeatDocPart(List<Object>)`  | Repeats the part of the document surrounded by the comment. The document part is copied once for each object in the passed-in list. Expressions found in the elements of the document part are evaluated against the object from the list. Can be used instead repeatTableRow and repeatParagraph if you want to repeat more than table rows and paragraphs. |
| `replaceWordWith(expression)`  | Replaces the commented word (must be a single word!) with the value of the given expression.                                                                                                                                                                                                                                                                 |
| `resolveTable(StampTable)`     | Replaces a table (must have 1 column and 2 rows) with the values given by the StampTable. The StampTable contains a list of headers for columns, and a 2 level list of rows containing values for each column.                                                                                                                                               |

If a comment cannot be processed, by default an exception will be thrown. Successfully processed comments are removed
from the document. You can add support to more expressions in comments by implementing your
own [ICommentProcessor](src/main/java/org/wickedsource/docxstamper/api/commentprocessor/ICommentProcessor.java). To
register you comment processor to docx-stamper, use the following code:

```java
class Main {
    public static void main(String... args) {
        ICommentProcessor commentProcessor = ...// instance of your own ICommentProcessor implementation
        Class<?> interfaceClass = ...
        // class of the interface that defines the methods that are exposed into the expression language
        DocxStamper stamper = new DocxStamperConfiguration()
                .addCommentProcessor(interfaceClass, commentProcessor)
                .build();
    }
}
```

For an in-depth description of how to create a comment processor, see the javadoc
of [ICommentProcessor](src/main/java/org/wickedsource/docxstamper/api/commentprocessor/ICommentProcessor.java).

## Conditional Display and Repeating of Elements in Headers or Footers

The docx file format does not allow comments in Headers or Footers of a document. To be able to conditionally display
content in a header or footer, simply surround the expression you would put in a comment with "#{}" and put it at the
beginning of the paragraph you want to manipulate. The expression will be evaluated as it would be in a comment.

## Error Handling

By default, DocxStamper fails with an UnresolvedExpressionException if an expression within the document or within the
comments cannot be resolved successfully. If you want to change this behavior, you can do the following:

```java
class Main {
    public static void main(String... args) {
        DocxStamper stamper = new DocxStamperConfiguration()
                .setFailOnUnresolvedExpression(false)
                .build();
    }
}
```

## Sample Code

The source code contains a set of tests show how to use the features. If you want to run them yourself, clone the
repository and run [the tests in the main package](src/test/java/org/wickedsource/docxstamper) with the system
property `-DkeepOutputFile=true` so that the resulting .docx documents will not be cleaned up so you can view them. The
resulting files will be stored in your local temp folder (watch the logging output for the exact location of the files).

If you want to have a look at the .docx templates used in the tests, have a look at
the [resources subfolder](src/test/resources/org/wickedsource/docxstamper) in the test folder.

## Maven coordinates
To include docx-stamper in your project, you can use the following maven coordinates in your dependency management system:
[go to last documented version](https://verronpro.github.io/docx-stamper/dependency-info.html)

Note that as of version 1.4.0 you have to provide the dependency to your version of Docx4J yourself:

```xml
<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j</artifactId>
    <version>6.1.2</version>
</dependency>
```

This way, you can choose which version of Docx4J you want to use instead having it dictated by docx-stamper.

## Contribute

If you have an issue or created a comment processor or type resolver that you think deserves to be part of the default
distribution, feel free to open an issue or - even better - a pull request with your contribution.
