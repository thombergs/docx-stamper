package org.wickedsource.docxstamper.util;

import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.R;

import java.util.HashSet;
import java.util.Set;

public class CommentWrapper {

	private Comments.Comment comment;

	private CommentRangeStart commentRangeStart;

	private CommentRangeEnd commentRangeEnd;

	private R.CommentReference commentReference;

	private Set<CommentWrapper> children = new HashSet<>();

	public CommentWrapper() {
	}

	public Comments.Comment getComment() {
		return comment;
	}

	void setComment(Comments.Comment comment) {
		this.comment = comment;
	}

	public CommentRangeStart getCommentRangeStart() {
		return commentRangeStart;
	}

	void setCommentRangeStart(CommentRangeStart commentRangeStart) {
		this.commentRangeStart = commentRangeStart;
	}

	public CommentRangeEnd getCommentRangeEnd() {
		return commentRangeEnd;
	}

	void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
		this.commentRangeEnd = commentRangeEnd;
	}

	public R.CommentReference getCommentReference() {
		return commentReference;
	}

	void setCommentReference(R.CommentReference commentReference) {
		this.commentReference = commentReference;
	}

	public Set<CommentWrapper> getChildren() {
		return children;
	}

	void setChildren(Set<CommentWrapper> children) {
		this.children = children;
	}

}
