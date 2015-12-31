# docx-stamper
docx-stamper is a template engine for docx documents. You create a template .docx document with your favorite word processor
and feed it to a DocxStamper instance to create a document based on the template at runtime. Features of docx-stamper include:
* replacement of expressions like `${person.name}`
* repeating of table rows, thus displaying lists of items in a table
* conditional display of paragraphs
* conditional display of table rows
* conditional display of tables

To manipulate the template document like repeating a table row or conditionally displaying a paragraph of text, you add comments 
like `${repeatTableRow(items)}` or `displayParagraphIf(booleanExpression)` to certain paragraphs in the template document. These comments are resolved 
by DocxStamper against a context object provided by you.

# Extend docx-stamper
docx-stamper is extensible insofar that you can implement your own [CommentProcessor](https://github.com/thombergs/docx-stamper/blob/master/src/main/java/org/wickedsource/docxstamper/docx4j/processor/ICommentProcessor.java) to extend the default expression language available 
in comments. Also, you can implement your own [TypeResolver](https://github.com/thombergs/docx-stamper/blob/master/src/main/java/org/wickedsource/docxstamper/docx4j/replace/TypeResolver.java) to handle the replacement of expressions by custom object types that are 
not supported by default.

If you created a CommentProcessor or TypeResolver that you think should be available by default, feel free to create a pull request or 
issue so I can review it.

# Maven coordinates
to be done



