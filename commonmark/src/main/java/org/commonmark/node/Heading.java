package org.commonmark.node;

public class Heading extends Block {

    private int level;
    private String whitespacePreStart;
    private String whitespacePreContent;
    private String whitespacePostContent;
    private String whitespacePostEnd;
    private char symbolType;
    private int numEndingSymbol;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    
    public String getWhitespacePreStart() {
        return whitespacePreStart;
    }

    public void setWhitespacePreStart(String whitespacePreStart) {
        this.whitespacePreStart = whitespacePreStart;
    }

    public String getWhitespacePreContent() {
        return whitespacePreContent;
    }

    public void setWhitespacePreContent(String whitespacePreContent) {
        this.whitespacePreContent = whitespacePreContent;
    }

    public String getWhitespacePostContent() {
        return whitespacePostContent;
    }

    public void setWhitespacePostContent(String whitespacePostContent) {
        this.whitespacePostContent = whitespacePostContent;
    }

    public String getWhitespacePostEnd() {
        return whitespacePostEnd;
    }

    public void setWhitespacePostEnd(String whitespacePostEnd) {
        this.whitespacePostEnd = whitespacePostEnd;
    }

    public char getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(char symbolType) {
        this.symbolType = symbolType;
    }

    public int getNumEndingSymbol() {
        return numEndingSymbol;
    }

    public void setNumEndingSymbol(int numEndingSymbol) {
        this.numEndingSymbol = numEndingSymbol;
    }
}
