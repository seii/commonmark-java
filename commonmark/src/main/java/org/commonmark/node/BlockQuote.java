package org.commonmark.node;

public class BlockQuote extends Block {
    private int firstLinePrecedingSpaces;
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public int getFirstLinePrecedingSpaces() {
        return firstLinePrecedingSpaces;
    }
    
    public void setFirstLinePrecedingSpaces(int firstLinePrecedingSpaces) {
        this.firstLinePrecedingSpaces = firstLinePrecedingSpaces;
    }
}
