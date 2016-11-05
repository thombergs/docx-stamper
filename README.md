# docx-stamper
docx-stamper is a Java template engine for docx documents. You create a template .docx document with your favorite word processor
and feed it to a DocxStamper instance to create a document based on the template at runtime. Example code:
```java
MyContext context = ...;                 // your own POJO against which expressions found in the template
                                         // will be resolved
InputStream template = ...;              // InputStream to your .docx template file
OutputStream out = ...;                  // OutputStream in which to write the resulting .docx document
DocxStamper stamper = new DocxStamper();
stamper.stamp(template, context, out);
out.close();
```

## Replacing Expressions in a .docx Template
The main feature of docx-stamper is **replacement of expressions** within the text of the template document. Simply add expressions like `${person.name}` or `${person.name.equals("Homer") ? "Duff" : "Budweiser"}` in the text of your .docx template and provide a context object against which the expression can be resolved. docx-stamper will try to keep the original formatting of the text in the template intact. You can use the full feature set of [Spring Expression Language](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html).

The value an expression resolves to may be of the following types:

| Type of expression value | Effect  |
| ---|---|
| java.lang.Object | The expression is replaced by the String representation of the object (`String.valueOf()`).
| java.lang.String | The expression is replaced with the String value.|
| java.util.Date   | The expression is replaced by a formatted Date string (by default "dd.MM.yyyy"). You can change the format string by registering your own [DateResolver](http://thombergs.github.io/docx-stamper/apidocs/org/wickedsource/docxstamper/replace/typeresolver/DateResolver.html).|
| [org.wickedsource.docxstamper...Image](http://thombergs.github.io/docx-stamper/apidocs/org/wickedsource/docxstamper/replace/typeresolver/image/Image.html) | The expression is replaced with an inline image.|

If an expression cannot be resolved successfully, it will be skipped (meaning the expression stays in the document as it was in the template). To support more than the above types you can implement your own [TypeResolver](http://thombergs.github.io/docx-stamper/apidocs/org/wickedsource/docxstamper/api/typeresolver/ITypeResolver.html). To register your own TypeResolver with docx-stamper, use the following code:

```java
DocxStamper stamper = ...;              
ITypeResolver typeResolver = ...;              // instance of your own ITypeResolver implementation
Class<?> type ...;                             // class of expression values your resolver handles
stamper.getTypeResolverRegistry()
    .registerTypeResolver(type, typeResolver);
```

## Conditional Display and Repeating of Elements
Besides replacing expressions, docx-stamper can **process comments on paragraphs of text** in the template .docx document and do manipulations on the template based on these comments. By default, you can use the following expressions in comments:

| Expression in .docx comment       | Effect  |
| --------------------------------- |---------|
| `displayParagraphIf(boolean)`     | The commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.|
| `displayTableRowIf(boolean)`      | The table row surrounding the commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.|
| `displayTableIf(boolean)`      | The whole table surrounding the commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.|
| `repeatTableRow(List<Object>)`      | The table row surrounding the commented paragraph is copied once for each object in the passed-in list. Expressions found in the cells of the table row are evaluated against the object from the list.

If a comment cannot be processed, it is simply skipped. Successfully processed comments are removed from the document. You can add support to more expressions in comments by implementing your own [ICommentProcessor](http://thombergs.github.io/docx-stamper/apidocs/org/wickedsource/docxstamper/api/commentprocessor/ICommentProcessor.html). To register you comment processor to docx-stamper, use the following code:

```java
DocxStamper stamper = ...;              
ICommentProcessor commentProcessor = ...;      // instance of your own ICommentProcessor implementation
Class<?> interfaceClass = ...;                 // class of the interface that defines the methods that are
                                               // exposed into the expression language
stamper.getCommentProcessorRegistry()
    .registerCommentProcessor(interfaceClass, commentProcessor);
```
For an in-depth description of how to create a comment processor, see the javadoc of [ICommentProcessor](http://thombergs.github.io/docx-stamper/apidocs/org/wickedsource/docxstamper/api/commentprocessor/ICommentProcessor.html).

## Sample Code
The source code contains a set of tests show how to use the features. If you want to run them yourself, clone the repository and run [the tests in the main package](https://github.com/thombergs/docx-stamper/tree/master/src/test/java/org/wickedsource/docxstamper) with the system property `-DkeepOutputFile=true` so that the resulting .docx documents will not be cleaned up so you can view them. The resulting files will be stored in your local temp folder (watch the logging output for the exact location of the files).

If you want to have a look at the .docx templates used in the tests, have a look at the [resources subfolder](https://github.com/thombergs/docx-stamper/tree/master/src/test/resources/org/wickedsource/docxstamper) in the test folder.

## Maven coordinates
To include docx-stamper in your project, you can use the following maven coordinates in your dependency management system:

```xml
<dependency>
    <groupId>org.wickedsource</groupId>
    <artifactId>docx-stamper</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Changelog
* 1.0.3 (2016-11-05) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.3+is%3Aclosed)
* 1.0.2 (2016-10-02) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.2+is%3Aclosed)

## Contribute
If you have an issue or created a comment processor or type resolver that you think deserves to be part of the default distribution, feel free to open an issue or - even better - a pull request with your contribution.

## Frequently Asked Questions
See the [Frequently Asked Questions](https://github.com/thombergs/docx-stamper/wiki/Frequently-Asked-Questions) wiki page for some answers to recurring questions.


