package org.mulinlab.varnote.config.param.postDB;

public abstract class PostDBParam {
    protected String label;
    protected String headerPath;
    protected boolean hasHeader;
    protected String commentIndicator;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHeaderPath() {
        return headerPath;
    }

    public void setHeaderPath(String headerPath) {
        this.headerPath = headerPath;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public String getCommentIndicator() {
        return commentIndicator;
    }

    public void setCommentIndicator(String commentIndicator) {
        this.commentIndicator = commentIndicator;
    }
}
