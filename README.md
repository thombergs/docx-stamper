# docx-stamper

[![Build Status](https://travis-ci.org/thombergs/docx-stamper.png?branch=master)](https://travis-ci.org/thombergs/docx-stamper)

docx-stamper is a Java template engine for docx documents. You create a template .docx document with your favorite word processor
and feed it to a DocxStamper instance to create a document based on the template at runtime. Example code:
```java
MyContext context = ...;                 // your own POJO against which expressions found in the template
                                         // will be resolved
InputStream template = ...;              // InputStream to your .docx template file
OutputStream out = ...;                  // OutputStream in which to write the resulting .docx document
DocxStamper stamper = new DocxStamperConfiguration()
  .build();
stamper.stamp(template, context, out);
out.close();
```

## Replacing Expressions in a .docx Template
The main feature of docx-stamper is **replacement of expressions** within the text of the template document. Simply add expressions like `${person.name}` or `${person.name.equals("Homer") ? "Duff" : "Budweiser"}` in the text of your .docx template and provide a context object against which the expression can be resolved. docx-stamper will try to keep the original formatting of the text in the template intact. You can use the full feature set of [Spring Expression Language](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html) (SpEL).

The value an expression resolves to may be of the following types:

| Type of expression value | Effect  |
| ---|---|
| java.lang.Object | The expression is replaced by the String representation of the object (`String.valueOf()`).
| java.lang.String | The expression is replaced with the String value.|
| java.util.Date   | The expression is replaced by a formatted Date string (by default "dd.MM.yyyy"). You can change the format string by registering your own [DateResolver](src/main/java/org/wickedsource/docxstamper/replace/typeresolver/DateResolver.java).|
| [org.wickedsource.docxstamper...Image](src/main/java/org/wickedsource/docxstamper/replace/typeresolver/image/Image.java) | The expression is replaced with an inline image.|

If an expression cannot be resolved successfully, it will be skipped (meaning the expression stays in the document as it was in the template). To support more than the above types you can implement your own [TypeResolver](src/main/java/org/wickedsource/docxstamper/api/typeresolver/ITypeResolver.java). To register your own TypeResolver with docx-stamper, use the following code:

```java
ITypeResolver typeResolver = ...;              // instance of your own ITypeResolver implementation
Class<?> type ...;                             // class of expression values your resolver handles
DocxStamper stamper = new DocxStamperConfiguration()
  .addTypeResolver(type, typeResolver)
  .build();
```

## Customizing the SpEL Evaluation Context

If you want to take more control over the evaluation of expressions, you can implement a [EvaluationContextConfigurer](src/main/java/org/wickedsource/docxstamper/api/EvaluationContextConfigurer.java)
and customize Springs `StandardEvaluationContext` to your needs. You can register an `EvaluationContextConfigurer` like this:

```java 
EvaluationContextConfigurer configurer = ...;
DocxStamper stamper = new DocxStamperConfiguration()
  .setEvaluationContextConfigurer(configurer)
  .build();
```

## Adding custom functions to the Expression Language

If you want to create custom functions (for different number formats or different date formats, for example), you can register functions
which can then be used in the expression language. The following code for example adds a function `toUppercase(String)`
which can be used within the .docx document to uppercase a String:

```java
DocxStamper stamper = new DocxStamperConfiguration()
  .exposeInterfaceToExpressionLanguage(UppercaseFunction.class, new UppercaseFunctionImpl());
  .build();

public interface UppercaseFunction {
  String toUppercase(String string);
}

public static class UppercaseFunctionImpl implements UppercaseFunction {
  @Override
  public String toUppercase(String string) {
    return string.toUpperCase();
  }
}
```


## Conditional Display and Repeating of Elements
Besides replacing expressions, docx-stamper can **process comments on paragraphs of text** in the template .docx document and do manipulations on the template based on these comments. By default, you can use the following expressions in comments:

| Expression in .docx comment       | Effect  |
| --------------------------------- |---------|
| `displayParagraphIf(boolean)`     | The commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.|
| `displayTableRowIf(boolean)`      | The table row surrounding the commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.|
| `displayTableIf(boolean)`      | The whole table surrounding the commented paragraph is only displayed in the resulting .docx document if the boolean condition resolves to `true`.|
| `repeatTableRow(List<Object>)`      | The table row surrounding the commented paragraph is copied once for each object in the passed-in list. Expressions found in the cells of the table row are evaluated against the object from the list.
| `replaceWordWith(expression)`         | Replaces the commented word (must be a single word!) with the value of the given expression. |

If a comment cannot be processed, by default an exception will be thrown. Successfully processed comments are removed from the document. You can add support to more expressions in comments by implementing your own [ICommentProcessor](src/main/java/org/wickedsource/docxstamper/api/commentprocessor/ICommentProcessor.java). To register you comment processor to docx-stamper, use the following code:

```java
ICommentProcessor commentProcessor = ...;      // instance of your own ICommentProcessor implementation
Class<?> interfaceClass = ...;                 // class of the interface that defines the methods that are
                                               // exposed into the expression language
DocxStamper stamper = new DocxStamperConfiguration()
  .addCommentProcessor(interfaceClass, commentProcessor)
  .build();
```
For an in-depth description of how to create a comment processor, see the javadoc of [ICommentProcessor](src/main/java/org/wickedsource/docxstamper/api/commentprocessor/ICommentProcessor.java).

## Conditional Display and Repeating of Elements in Headers or Footers
The docx file format does not allow comments in Headers or Footers of a document. To be able to conditionally display content in a header or footer, simply surround the expression you would put in a comment with "#{}" and put it at the beginning of the paragraph you want to manipulate. The expression will be evaluated as it would be in a comment.

## Error Handling
By default DocxStamper fails with an UnresolvedExpressionException if an expression within the document or within the comments cannot be resolved successfully. If you want to change this behavior, you can do the following:

```java
DocxStamper stamper = new DocxStamperConfiguration()
  .setFailOnUnresolvedExpression(false)
  .build();
```

## Sample Code
The source code contains a set of tests show how to use the features. If you want to run them yourself, clone the repository and run [the tests in the main package](src/test/java/org/wickedsource/docxstamper) with the system property `-DkeepOutputFile=true` so that the resulting .docx documents will not be cleaned up so you can view them. The resulting files will be stored in your local temp folder (watch the logging output for the exact location of the files).

If you want to have a look at the .docx templates used in the tests, have a look at the [resources subfolder](src/test/resources/org/wickedsource/docxstamper) in the test folder.

## Maven coordinates
To include docx-stamper in your project, you can use the following maven coordinates in your dependency management system:

```xml
<dependency>
    <groupId>org.wickedsource</groupId>
    <artifactId>docx-stamper</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Changelog
* 1.2.1 (2017-10-18) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.2.1+is%3Aclosed)
* 1.2.0 (2017-09-26) - [feature release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.2.0+is%3Aclosed)
* 1.1.0 (2017-09-18) - [feature release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.1.0+is%3Aclosed)
  * *API Break:* All methods that configure `DocxStamper` have been moved into `DocxStamperConfiguration`.
  * *API Break:* Methods `getCommentProcessorRegistry()` and `getTypeResolverRegistry()` have been removed from `DocxStamper`. You can
    configure CommentProcessors and TypeResolvers via `DocxStamperConfiguration` now.
  * `DocxStamperConfiguration` can now be used as a Builder for `DocxStamper` objects.
* 1.0.12 (2017-09-08) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.12+is%3Aclosed)
* 1.0.11 (2017-06-09) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.11+is%3Aclosed)
* 1.0.10 (2017-04-03) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.10+is%3Aclosed)
* 1.0.9 (2017-03-18) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.9+is%3Aclosed)
* 1.0.8 (2017-02-24) - [minor feature release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.8+is%3Aclosed)
* 1.0.7 (2017-01-30) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.7+is%3Aclosed)
* 1.0.6 (2017-01-20) - [minor feature release](/issues?q=is%3Aissue+milestone%3A1.0.6+is%3Aclosed)
* 1.0.5 (2017-01-09) - bugfix release
* 1.0.4 (2016-11-20) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.4+is%3Aclosed)
* 1.0.3 (2016-11-05) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.3+is%3Aclosed)
* 1.0.2 (2016-10-02) - [bugfix release](https://github.com/thombergs/docx-stamper/issues?q=is%3Aissue+milestone%3A1.0.2+is%3Aclosed)

## Contribute
If you have an issue or created a comment processor or type resolver that you think deserves to be part of the default distribution, feel free to open an issue or - even better - a pull request with your contribution.

## Frequently Asked Questions
See the [Frequently Asked Questions](/wiki/Frequently-Asked-Questions) wiki page for some answers to recurring questions.


