package org.wickedsource.docxstamper.util;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class CommentUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommentUtil.class);

    private CommentUtil() {

    }

    /**
     * Returns the comment the given DOCX4J run is commented with.
     *
     * @param run      the DOCX4J run whose comment to retrieve.
     * @param document the document that contains the run.
     * @return the comment, if found, null otherwise.
     */
    public static Comments.Comment getCommentAround(R run,
                                                    WordprocessingMLPackage document) {
        try {
            if (run instanceof Child) {
                Child child = run;
                ContentAccessor parent = (ContentAccessor) child.getParent();
                if (parent == null)
                    return null;
                CommentRangeStart possibleComment = null;
                boolean foundChild = false;
                for (Object contentElement : parent.getContent()) {

                    // so first we look for the start of the comment
                    if (XmlUtils.unwrap(contentElement) instanceof CommentRangeStart) {
                        possibleComment = (CommentRangeStart) contentElement;
                    }
                    // then we check if the child we are looking for is ours
                    else if (possibleComment != null && child.equals(contentElement)) {
                        foundChild = true;
                    }
                    // and then if we have an end of a comment we are good!
                    else if (possibleComment != null && foundChild && XmlUtils
                            .unwrap(contentElement) instanceof CommentRangeEnd) {
                        try {
                            BigInteger id = possibleComment.getId();
                            CommentsPart commentsPart = (CommentsPart) document.getParts()
                                    .get(new PartName("/word/comments.xml"));
                            Comments comments = commentsPart.getContents();
                            for (Comments.Comment comment : comments.getComment()) {
                                if (comment.getId().equals(id)) {
                                    return comment;
                                }
                            }
                        } catch (InvalidFormatException e) {
                            logger.warn(String.format(
                                    "Error while searching comment. Skipping run %s.",
                                    run), e);
                        }
                    }
                    // else restart
                    else {
                        possibleComment = null;
                        foundChild = false;
                    }
                }
            }
            return null;
        } catch (Docx4JException e) {
            throw new DocxStamperException(
                    "error accessing the comments of the document!", e);
        }
    }

    /**
     * Returns the first comment found for the given docx object. Note that an object is
     * only considered commented if the comment STARTS within the object. Comments
     * spanning several objects are not supported by this method.
     *
     * @param object   the object whose comment to load.
     * @param document the document in which the object is embedded (needed to load the
     *                 comment from the comments.xml part).
     * @return the concatenated string of all paragraphs of text within the comment or
     * null if the specified object is not commented.
     * @throws Docx4JException in case of a Docx4J processing error.
     */
    public static Comments.Comment getCommentFor(ContentAccessor object,
                                                 WordprocessingMLPackage document) {
        try {
            for (Object contentObject : object.getContent()) {
                if (contentObject instanceof CommentRangeStart) {
                    try {
                        BigInteger id = ((CommentRangeStart) contentObject).getId();
                        CommentsPart commentsPart = (CommentsPart) document.getParts()
                                .get(new PartName("/word/comments.xml"));
                        Comments comments = commentsPart.getContents();
                        for (Comments.Comment comment : comments.getComment()) {
                            if (comment.getId().equals(id)) {
                                return comment;
                            }
                        }
                    } catch (InvalidFormatException e) {
                        logger.warn(String.format(
                                "Error while searching comment. Skipping object %s.",
                                object), e);
                    }
                }
            }
            return null;
        } catch (Docx4JException e) {
            throw new DocxStamperException(
                    "error accessing the comments of the document!", e);
        }
    }

    public static String getCommentStringFor(ContentAccessor object,
                                             WordprocessingMLPackage document) throws Docx4JException {
        Comments.Comment comment = getCommentFor(object, document);
        return getCommentString(comment);
    }

    /**
     * Returns the string value of the specified comment object.
     */
    public static String getCommentString(Comments.Comment comment) {
        StringBuilder builder = new StringBuilder();
        for (Object commentChildObject : comment.getContent()) {
            if (commentChildObject instanceof P) {
                builder.append(new ParagraphWrapper((P) commentChildObject).getText());
            }
        }
        return builder.toString();
    }

    public static void deleteComment(CommentWrapper comment) {
        if (comment.getCommentRangeEnd() != null) {
            ContentAccessor commentRangeEndParent = (ContentAccessor) comment
                    .getCommentRangeEnd().getParent();
            commentRangeEndParent.getContent().remove(comment.getCommentRangeEnd());
            deleteCommentReference(commentRangeEndParent,
                    comment.getCommentRangeEnd().getId());
        }
        if (comment.getCommentRangeStart() != null) {
            ContentAccessor commentRangeStartParent = (ContentAccessor) comment
                    .getCommentRangeStart().getParent();
            commentRangeStartParent.getContent().remove(comment.getCommentRangeStart());
            deleteCommentReference(commentRangeStartParent,
                    comment.getCommentRangeStart().getId());
        }
        // TODO: also delete comment from comments.xml
    }

    private static boolean deleteCommentReference(ContentAccessor parent,
                                                  BigInteger commentId) {
        for (int i = 0; i < parent.getContent().size(); i++) {
            Object contentObject = XmlUtils.unwrap(parent.getContent().get(i));
            if (contentObject instanceof ContentAccessor) {
                if (deleteCommentReference((ContentAccessor) contentObject, commentId)) {
                    return true;
                }
            }
            if (contentObject instanceof R) {
                for (Object runContentObject : ((R) contentObject).getContent()) {
                    Object unwrapped = XmlUtils.unwrap(runContentObject);
                    if (unwrapped instanceof R.CommentReference) {
                        BigInteger foundCommentId = ((R.CommentReference) unwrapped)
                                .getId();
                        if (foundCommentId.equals(commentId)) {
                            parent.getContent().remove(i);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static Map<BigInteger, CommentWrapper> getComments(
            WordprocessingMLPackage document) {
        Map<BigInteger, CommentWrapper> rootComments = new HashMap<>();
        Map<BigInteger, CommentWrapper> allComments = new HashMap<>();
        collectCommentRanges(rootComments, allComments, document);
        collectComments(rootComments, allComments, document);
        return cleanMalformedComments(rootComments);
    }

    private static Map<BigInteger, CommentWrapper> cleanMalformedComments(Map<BigInteger, CommentWrapper> rootComments) {
        Map<BigInteger, CommentWrapper> filteredCommentEntries = new HashMap<>();

        rootComments.forEach((key, comment) -> {
            if (isCommentMalformed(comment)) {
                logger.error(
                        "Skipping malformed comment, missing range start and/or range end : {}",
                        getCommentContent(comment)
                );
            } else {
                filteredCommentEntries.put(key, comment);
                comment.setChildren(cleanMalformedComments(comment.getChildren()));
            }
        });

        return filteredCommentEntries;
    }

    private static Set<CommentWrapper> cleanMalformedComments(Set<CommentWrapper> children) {
        return children.stream().filter(comment -> {
            if (isCommentMalformed(comment)) {
                logger.error(
                        "Skipping malformed comment, missing range start and/or range end : {}",
                        getCommentContent(comment)
                );
                return false;
            }
            comment.setChildren(cleanMalformedComments(comment.getChildren()));
            return true;
        }).collect(Collectors.toSet());
    }

    private static String getCommentContent(CommentWrapper comment) {
        return comment.getComment() != null
                ? comment.getComment().getContent().stream().map(TextUtils::getText).collect(Collectors.joining(""))
                : "<no content>";
    }

    private static boolean isCommentMalformed(CommentWrapper comment) {
        return comment.getCommentRangeStart() == null || comment.getCommentRangeEnd() == null || comment.getComment() == null;
    }

    private static void collectCommentRanges(
            Map<BigInteger, CommentWrapper> rootComments, final Map<BigInteger, CommentWrapper> allComments,
            WordprocessingMLPackage document) {
        Stack<CommentWrapper> stack = new Stack<>();
        DocumentWalker documentWalker = new BaseDocumentWalker(
                document.getMainDocumentPart()) {
            @Override
            protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
                CommentWrapper commentWrapper = allComments.get(commentRangeStart.getId());
                if (commentWrapper == null) {
                    commentWrapper = new CommentWrapper();
                    allComments.put(commentRangeStart.getId(), commentWrapper);
                    if (stack.isEmpty()) {
                        rootComments.put(commentRangeStart.getId(), commentWrapper);
                    } else {
                        stack.peek().getChildren().add(commentWrapper);
                    }
                }
                commentWrapper.setCommentRangeStart(commentRangeStart);
                stack.push(commentWrapper);
            }

            @Override
            protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
                CommentWrapper commentWrapper = allComments.get(commentRangeEnd.getId());
                if (commentWrapper == null) {
                    throw new RuntimeException("UNEXPECTED !");
                }
                commentWrapper.setCommentRangeEnd(commentRangeEnd);
                if (!stack.isEmpty()) {
                    if (stack.peek().equals(commentWrapper)) {
                        stack.pop();
                    } else {
                        throw new RuntimeException("UNEXPECTED 2 !");
                    }
                }
            }
        };
        documentWalker.walk();
    }

    private static void collectComments(final Map<BigInteger, CommentWrapper> rootComments,
                                        Map<BigInteger, CommentWrapper> allComments, WordprocessingMLPackage document) {
        try {
            CommentsPart commentsPart = (CommentsPart) document.getParts()
                    .get(new PartName("/word/comments.xml"));
            if (commentsPart != null) {
                for (Comments.Comment comment : commentsPart.getContents().getComment()) {
                    CommentWrapper commentWrapper = allComments.get(comment.getId());
                    if (commentWrapper != null) {
                        commentWrapper.setComment(comment);
                    }
                }
            }
        } catch (Docx4JException e) {
            throw new IllegalStateException(e);
        }
    }

}
