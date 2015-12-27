package org.wickedsource.docxstamper.docx4j.util;


import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.docx4j.RunAggregator;

import java.math.BigInteger;

public class CommentUtil {

    private static Logger logger = LoggerFactory.getLogger(CommentUtil.class);

    /**
     * Returns the text of a comment for the given docx object. Note that an object is only considered commented if
     * the comment STARTS within the object. Comments spanning several objects are not supported by this method.
     *
     * @param object   the object whose comment to load.
     * @param document the document in which the object is embedded (needed to load the comment from the comments.xml part).
     * @return the concatenated string of all paragraphs of text within the comment or null if the specified object is not
     * commented.
     * @throws Docx4JException in case of a Docx4J processing error.
     */
    public static String getCommentFor(ContentAccessor object, WordprocessingMLPackage document) throws Docx4JException {
        for (Object contentObject : object.getContent()) {
            if (contentObject instanceof CommentRangeStart) {
                try {
                    BigInteger id = ((CommentRangeStart) contentObject).getId();
                    CommentsPart commentsPart = (CommentsPart) document.getParts().get(new PartName("/word/comments.xml"));
                    Comments comments = commentsPart.getContents();
                    for (Comments.Comment comment : comments.getComment()) {
                        if (comment.getId().equals(id)) {
                            String commentString = "";
                            for (Object commentChildObject : comment.getContent()) {
                                if (commentChildObject instanceof P) {
                                    commentString += new RunAggregator((P) commentChildObject).getText();
                                }
                            }
                            return commentString;
                        }
                    }
                } catch (InvalidFormatException e) {
                    logger.warn(String.format("Error while searching comment. Skipping object %s.", object), e);
                }
            }
        }
        return null;
    }

}
