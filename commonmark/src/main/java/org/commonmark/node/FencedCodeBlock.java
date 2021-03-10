package org.commonmark.node;

public class FencedCodeBlock extends Block {

    private char fenceChar;
    private int startFenceLength;
    private int startFenceIndent;
    private int endFenceLength;
    private int endFenceIndent;
    private String whitespacePostFence;

    private String info;
    private String literal;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public char getFenceChar() {
        return fenceChar;
    }

    public void setFenceChar(char fenceChar) {
        this.fenceChar = fenceChar;
    }

    public int getStartFenceLength() {
        return startFenceLength;
    }

    public void setStartFenceLength(int fenceLength) {
        this.startFenceLength = fenceLength;
    }

    public int getStartFenceIndent() {
        return startFenceIndent;
    }

    public void setStartFenceIndent(int fenceIndent) {
        this.startFenceIndent = fenceIndent;
    }
    
    public int getEndFenceLength() {
        return endFenceLength;
    }

    public void setEndFenceLength(int fenceLength) {
        this.endFenceLength = fenceLength;
    }
    
    public int getEndFenceIndent() {
        return endFenceIndent;
    }

    public void setEndFenceIndent(int fenceIndent) {
        this.endFenceIndent = fenceIndent;
    }

    /**
     * @see <a href="http://spec.commonmark.org/0.18/#info-string">CommonMark spec</a>
     */
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public String getWhitespacePostFence() {
        return whitespacePostFence;
    }

    public void setWhitespacePostFence(String whitespacePostFence) {
        this.whitespacePostFence = whitespacePostFence;
    }
}
