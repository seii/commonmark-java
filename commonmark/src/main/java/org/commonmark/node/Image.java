package org.commonmark.node;

public class Image extends Node {

    private String destination;
    private String title;
    private boolean isAutolink;

    public Image() {
    }

    public Image(String destination, String title) {
        this.destination = destination;
        this.title = title;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isAutolink() {
        return isAutolink;
    }

    public void setAutolink(boolean isAutolink) {
        this.isAutolink = isAutolink;
    }

    @Override
    protected String toStringAttributes() {
        return "destination=" + destination + ", title=" + title;
    }
}
