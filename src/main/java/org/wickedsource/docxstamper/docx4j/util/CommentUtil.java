package org.wickedsource.docxstamper.docx4j.util;


import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.docx4j.replace.ParagraphWrapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CommentUtil {

    private static Logger logger = LoggerFactory.getLogger(CommentUtil.class);

    /**
     * Returns the first comment found for the given docx object. Note that an object is only considered commented if
     * the comment STARTS within the object. Comments spanning several objects are not supported by this method.
     *
     * @param object   the object whose comment to load.
     * @param document the document in which the object is embedded (needed to load the comment from the comments.xml part).
     * @return the concatenated string of all paragraphs of text within the comment or null if the specified object is not
     * commented.
     * @throws Docx4JException in case of a Docx4J processing error.
     */
    public static Comments.Comment getCommentFor(ContentAccessor object, WordprocessingMLPackage document) throws Docx4JException {
        for (Object contentObject : object.getContent()) {
            if (contentObject instanceof CommentRangeStart) {
                try {
                    BigInteger id = ((CommentRangeStart) contentObject).getId();
                    CommentsPart commentsPart = (CommentsPart) document.getParts().get(new PartName("/word/comments.xml"));
                    Comments comments = commentsPart.getContents();
                    for (Comments.Comment comment : comments.getComment()) {
                        if (comment.getId().equals(id)) {
                            return comment;
                        }
                    }
                } catch (InvalidFormatException e) {
                    logger.warn(String.format("Error while searching comment. Skipping object %s.", object), e);
                }
            }
        }
        return null;
    }

    /**
     * Returns the string value of the specified comment object.
     */
    public static String getCommentString(Comments.Comment comment) {
        String commentString = "";
        for (Object commentChildObject : comment.getContent()) {
            if (commentChildObject instanceof P) {
                commentString += new ParagraphWrapper((P) commentChildObject).getText();
            }
        }
        return commentString;
    }

    public static void deleteCommentFromParagraph(WordprocessingMLPackage document, P paragraph, Comments.Comment comment) {
        List<Integer> indicesToRemove = new ArrayList<>();
        int index = 0;
        for (Object contentObject : paragraph.getContent()) {
            BigInteger commentId = null;
            if (contentObject instanceof CommentRangeStart) {
                commentId = ((CommentRangeStart) contentObject).getId();
            }
            if (contentObject instanceof CommentRangeEnd) {
                commentId = ((CommentRangeEnd) contentObject).getId();
            }
            if (contentObject instanceof R) {
                for (Object runContentObject : ((R) contentObject).getContent()) {
                    Object unwrapped = XmlUtils.unwrap(runContentObject);
                    if (unwrapped instanceof R.CommentReference) {
                        commentId = ((R.CommentReference) unwrapped).getId();
                    }
                }
            }
            if (comment.getId().equals(commentId)) {
                indicesToRemove.add(index);
            }
            index++;
        }
        int indexCorrection = 0;
        for (Integer indexToRemove : indicesToRemove) {
            paragraph.getContent().remove(indexToRemove - indexCorrection);
            indexCorrection++;
        }

        // TODO: also delete comment from comments.xml within the word document
    }

}
