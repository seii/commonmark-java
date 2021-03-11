package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;

class BlockContent {

    private final StringBuilder sb;

    private int lineCount = 0;

    public BlockContent() {
        sb = new StringBuilder();
    }

    public BlockContent(String content) {
        sb = new StringBuilder(content);
    }

    public void add(CharSequence line) {
        if(!Parsing.IS_ROUNDTRIP) {
            if (lineCount != 0) {
                sb.append('\n');
            }
        }
        sb.append(line);
        lineCount++;
    }

    public String getString() {
        return sb.toString();
    }

}
