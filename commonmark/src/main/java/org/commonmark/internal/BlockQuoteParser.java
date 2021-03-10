package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.parser.block.*;

public class BlockQuoteParser extends AbstractBlockParser {

    private final BlockQuote block = new BlockQuote();

    public BlockQuoteParser(int firstLinePrecedingSpaces) {
        block.setFirstLinePrecedingSpaces(firstLinePrecedingSpaces);
    }
    
    public BlockQuoteParser() {
        block.setFirstLinePrecedingSpaces(0);
    }
    
    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public BlockQuote getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        if (isMarker(state, nextNonSpace)) {
            int newColumn = state.getColumn() + state.getIndent() + 1;
            // optional following space or tab
            if (Parsing.isSpaceOrTab(state.getLine().getContent(), nextNonSpace + 1)) {
                newColumn++;
            }
            return BlockContinue.atColumn(newColumn);
        } else {
            return BlockContinue.none();
        }
    }

    private static boolean isMarker(ParserState state, int index) {
        CharSequence line = state.getLine().getContent();
        return state.getIndent() < Parsing.CODE_BLOCK_INDENT && index < line.length() && line.charAt(index) == '>';
    }

    public static class Factory extends AbstractBlockParserFactory {
//        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
//            int nextNonSpace = state.getNextNonSpaceIndex();
//            if (isMarker(state, nextNonSpace)) {
//                int newColumn = state.getColumn() + state.getIndent() + 1;
//                // optional following space or tab
//                if (Parsing.isSpaceOrTab(state.getLine().getContent(), nextNonSpace + 1)) {
//                    newColumn++;
//                }
//                return BlockStart.of(new BlockQuoteParser()).atColumn(newColumn);
//            } else {
//                return BlockStart.none();
//            }
//        }
//    }
        
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            int previousWhitespace = Parsing.skipSpaceTabBackwards(state.getLine().getContent(), nextNonSpace - 1, 0) + 1;
            if (isMarker(state, nextNonSpace)) {
                int newColumn = state.getColumn() + state.getIndent() + 1;
                // optional following space or tab
                if (Parsing.isSpaceOrTab(state.getLine().getContent(), nextNonSpace + 1)) {
                    newColumn++;
                }
//                return BlockStart.of(new BlockQuoteParser(nextNonSpace)).atColumn(newColumn);
//                return BlockStart.of(new BlockQuoteParser(nextNonSpace)).atColumn(0);
                return BlockStart.of(new BlockQuoteParser(nextNonSpace - previousWhitespace)).atColumn(newColumn);
            } else {
                return BlockStart.none();
            }
        }
    }
}
