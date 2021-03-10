package org.commonmark.node;

public class ListItem extends Block {
    private String preDelimiterWhitespace;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public String getPreDelimiterWhitespace() {
        return preDelimiterWhitespace;
    }
    
    public void setPreDelimiterWhitespace(String whitespace) {
        preDelimiterWhitespace = whitespace;
    }
}
