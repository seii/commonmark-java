package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.block.*;

public class ThematicBreakParser extends AbstractBlockParser {

    private final ThematicBreak block = new ThematicBreak();

    public ThematicBreakParser() {
        block.setContent(null);
    }
    
    public ThematicBreakParser(CharSequence content) {
        block.setContent(content);
    }
    
    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // a horizontal rule can never container > 1 line, so fail to match
        return BlockContinue.none();
    }

    public static class Factory extends AbstractBlockParserFactory {

//        @Override
//        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
//            if (state.getIndent() >= 4) {
//                return BlockStart.none();
//            }
//            int nextNonSpace = state.getNextNonSpaceIndex();
//            CharSequence line = state.getLine().getContent();
//            if (isThematicBreak(line, nextNonSpace)) {
//                return BlockStart.of(new ThematicBreakParser()).atIndex(line.length());
//            } else {
//                return BlockStart.none();
//            }
//        }
        
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }
            int nextNonSpace = state.getNextNonSpaceIndex();
            
            CharSequence line;
            if(!(state instanceof DocumentRoundtripParser) || ((DocumentRoundtripParser)state).getContainerString() == null) {
                line = state.getLine().getContent();
            }else {
                line = ((DocumentRoundtripParser)state).getContainerString();
            }
            
//            if(line.toString().endsWith("\n")) {
//                line = line.subSequence(0, line.length() - 1);
//            }
            if (isThematicBreak(line, nextNonSpace)) {
                return BlockStart.of(new ThematicBreakParser(line)).atIndex(line.length());
            } else {
                return BlockStart.none();
            }
        }
    }

    // spec: A line consisting of 0-3 spaces of indentation, followed by a sequence of three or more matching -, _, or *
    // characters, each followed optionally by any number of spaces, forms a thematic break.
    private static boolean isThematicBreak(CharSequence line, int index) {
        int dashes = 0;
        int underscores = 0;
        int asterisks = 0;
        int length = line.length();
        for (int i = index; i < length; i++) {
            switch (line.charAt(i)) {
                case '-':
                    dashes++;
                    break;
                case '_':
                    underscores++;
                    break;
                case '*':
                    asterisks++;
                    break;
                case ' ':
                case '\t':
                    // Allowed, even between markers
                    break;
                default:
                    return false;
            }
        }

        return ((dashes >= 3 && underscores == 0 && asterisks == 0) ||
                (underscores >= 3 && dashes == 0 && asterisks == 0) ||
                (asterisks >= 3 && dashes == 0 && underscores == 0));
    }
}
