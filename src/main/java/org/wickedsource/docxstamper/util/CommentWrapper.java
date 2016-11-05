package org.wickedsource.docxstamper.util;

import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;

public class CommentWrapper {

    private Comments.Comment comment;

    private CommentRangeStart commentRangeStart;

    private CommentRangeEnd commentRangeEnd;

    public CommentWrapper(Comments.Comment comment, CommentRangeStart commentRangeStart, CommentRangeEnd commentRangeEnd) {
        this.comment = comment;
        this.commentRangeStart = commentRangeStart;
        this.commentRangeEnd = commentRangeEnd;
    }

    public CommentWrapper() {
    }

    public Comments.Comment getComment() {
        return comment;
    }

    public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    void setComment(Comments.Comment comment) {
        this.comment = comment;
    }

    void setCommentRangeStart(CommentRangeStart commentRangeStart) {
        this.commentRangeStart = commentRangeStart;
    }

    void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
        this.commentRangeEnd = commentRangeEnd;
    }
}
