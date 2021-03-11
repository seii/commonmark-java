package org.commonmark.internal;

import org.commonmark.internal.inline.Position;
import org.commonmark.internal.inline.Scanner;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Heading;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.*;

public class HeadingParser extends AbstractBlockParser {

    private final Heading block = new Heading();
    private final SourceLines content;

    public HeadingParser(int level, SourceLines content) {
        block.setLevel(level);
        this.content = content;
    }
    
    public HeadingParser(int level, SourceLines content, String preSymbol, String postSymbol, int numEndingSymbol, char symbolType) {
        this(level, content);
        block.setWhitespacePreStart(preSymbol);
        block.setWhitespacePostEnd(postSymbol);
        block.setNumEndingSymbol(numEndingSymbol);
        block.setSymbolType(symbolType);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        // In both ATX and Setext headings, once we have the heading markup, there's nothing more to parse.
        return BlockContinue.none();
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        inlineParser.parse(content, block);
    }

    public static class Factory extends AbstractBlockParserFactory {

//        @Override
//        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
//            if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
//                return BlockStart.none();
//            }
//
//            SourceLine line = state.getLine();
//            int nextNonSpace = state.getNextNonSpaceIndex();
//            if (line.getContent().charAt(nextNonSpace) == '#') {
//                HeadingParser atxHeading = getAtxHeading(line.substring(nextNonSpace, line.getContent().length()));
//                if (atxHeading != null) {
//                    return BlockStart.of(atxHeading).atIndex(line.getContent().length());
//                }
//            }
//
//            int setextHeadingLevel = getSetextHeadingLevel(line.getContent(), nextNonSpace);
//            if (setextHeadingLevel > 0) {
//                SourceLines paragraph = matchedBlockParser.getParagraphLines();
//                if (!paragraph.isEmpty()) {
//                    return BlockStart.of(new HeadingParser(setextHeadingLevel, paragraph))
//                            .atIndex(line.getContent().length())
//                            .replaceActiveBlockParser();
//                }
//            }
//
//            return BlockStart.none();
//        }
        
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
                return BlockStart.none();
            }

            SourceLine line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            if (line.getContent().charAt(nextNonSpace) == '#') {
                HeadingParser atxHeading = null;
                
                if(!Parsing.IS_ROUNDTRIP) {
                    atxHeading = getAtxHeading(line.substring(nextNonSpace, line.getContent().length()));
                }else {
                    atxHeading = getAtxHeading(line);
                }
                
                if (atxHeading != null) {
                    return BlockStart.of(atxHeading).atIndex(line.getContent().length());
                }
            }

            int setextHeadingLevel = getSetextHeadingLevel(line.getContent(), nextNonSpace);
            if (setextHeadingLevel > 0) {
                SourceLines paragraph = matchedBlockParser.getParagraphLines();
                if (!paragraph.isEmpty()) {
                    if(!Parsing.IS_ROUNDTRIP) {
                        return BlockStart.of(new HeadingParser(setextHeadingLevel, paragraph))
                                .atIndex(line.getContent().length())
                                .replaceActiveBlockParser();
                    }else {
                        return BlockStart.of(getSetextHeading(setextHeadingLevel, paragraph, line.getContent(), nextNonSpace))
                                .atIndex(line.getContent().length())
                                .replaceActiveBlockParser();
                    }
                }
            }

            return BlockStart.none();
        }
    }

//    // spec: An ATX heading consists of a string of characters, parsed as inline content, between an opening sequence of
//    // 1–6 unescaped # characters and an optional closing sequence of any number of unescaped # characters. The opening
//    // sequence of # characters must be followed by a space or by the end of line. The optional closing sequence of #s
//    // must be preceded by a space and may be followed by spaces only.
//    private static HeadingParser getAtxHeading(SourceLine line) {
//        Scanner scanner = Scanner.of(SourceLines.of(line));
//        int level = scanner.matchMultiple('#');
//
//        if (level == 0 || level > 6) {
//            return null;
//        }
//
//        if (!scanner.hasNext()) {
//            // End of line after markers is an empty heading
//            return new HeadingParser(level, SourceLines.empty());
//        }
//
//        char next = scanner.peek();
//        if (!(next == ' ' || next == '\t')) {
//            return null;
//        }
//
//        scanner.whitespace();
//        Position start = scanner.position();
//        Position end = start;
//        boolean hashCanEnd = true;
//
//        while (scanner.hasNext()) {
//            char c = scanner.peek();
//            switch (c) {
//                case '#':
//                    if (hashCanEnd) {
//                        scanner.matchMultiple('#');
//                        int whitespace = scanner.whitespace();
//                        // If there's other characters, the hashes and spaces were part of the heading
//                        if (scanner.hasNext()) {
//                            end = scanner.position();
//                        }
//                        hashCanEnd = whitespace > 0;
//                    } else {
//                        scanner.next();
//                        end = scanner.position();
//                    }
//                    break;
//                case ' ':
//                case '\t':
//                    hashCanEnd = true;
//                    scanner.next();
//                    break;
//                default:
//                    hashCanEnd = false;
//                    scanner.next();
//                    end = scanner.position();
//            }
//        }
//
//        SourceLines source = scanner.getSource(start, end);
//        String content = source.getContent();
//        if (content.isEmpty()) {
//            return new HeadingParser(level, SourceLines.empty());
//        }
//        return new HeadingParser(level, source);
//    }
    
    // spec: An ATX heading consists of a string of characters, parsed as inline content, between an opening sequence of
    // 1–6 unescaped # characters and an optional closing sequence of any number of unescaped # characters. The opening
    // sequence of # characters must be followed by a space or by the end of line. The optional closing sequence of #s
    // must be preceded by a space and may be followed by spaces only.
    private static HeadingParser getAtxHeading(SourceLine line) {
        Scanner scanner = Scanner.of(SourceLines.of(line));
        //The opening # character may be indented 0-3 spaces.
        String whitespacePreStart = "";
        
        if(Parsing.IS_ROUNDTRIP) {
            whitespacePreStart = scanner.whitespaceAsString();
        }
        
        int level = scanner.matchMultiple('#');

        if (level == 0 || level > 6) {
            return null;
        }

        if (!scanner.hasNext()) {
            // End of line after markers is an empty heading
//            return new HeadingParser(level, SourceLines.empty());
            return new HeadingParser(level, SourceLines.empty(), "", "", 0, '#');
        }

        char next = scanner.peek();
        if (!(next == ' ' || next == '\t')) {
            return null;
        }

        if(!Parsing.IS_ROUNDTRIP) {
            scanner.whitespace();
        }
        
//        String whitespacePreContent = scanner.whitespaceAsString();
        Position start = scanner.position();
        Position end = start;
        
        String whitespacePreContent = "";
        if(Parsing.IS_ROUNDTRIP) {
            whitespacePreContent = scanner.whitespaceAsString();
            
            if(scanner.peek() == Scanner.END) {
                end = scanner.position();
            }
        }
        
        boolean hashCanEnd = true;
        String whitespacePostContent = "";
        
        String whitespacePostEnd = "";
        int numEndingSymbol = 0;
        
        while (scanner.hasNext()) {
            char c = scanner.peek();
            switch (c) {
                case '#':
                    if (hashCanEnd) {
                        if(whitespacePostContent.length() < 1) {
                            end = scanner.position();
                        }
                        numEndingSymbol = scanner.matchMultiple('#');
                        whitespacePostEnd = scanner.whitespaceAsString();
                        
                        // If there's other characters, the hashes and spaces were part of the heading
                        if (scanner.hasNext()) {
                            // spec: A sequence of # characters with anything but spaces following it is
                            //    not a closing sequence, but counts as part of the contents of the heading
                            if(Parsing.IS_ROUNDTRIP) {
                                scanner.find(Scanner.END);
                            }
                            end = scanner.position();
                            
                            if(Parsing.IS_ROUNDTRIP) {
                                // Since this isn't a closing sequence, any whitespace will be considered
                                //    part of the "content" and will be passed literally for inline parsing
                                numEndingSymbol = 0;
                                whitespacePostContent = "";
                                whitespacePostEnd = "";
                            }
                        }
                        
                        hashCanEnd = whitespacePostEnd.length() > 0;
                    } else {
                        scanner.next();
                        end = scanner.position();
                    }
                    break;
                case ' ':
                case '\t':
                    hashCanEnd = true;
                    whitespacePostContent = scanner.whitespaceAsString();
                    end = scanner.position();
                    break;
                case '\n':
                    if(Parsing.IS_ROUNDTRIP) {
                        hashCanEnd = true;
                        whitespacePostContent = scanner.whitespaceAsString();
                        end = scanner.position();
                        break;
                    }
                default:
                    hashCanEnd = false;
                    scanner.next();
                    end = scanner.position();
            }
        }

        SourceLines source = scanner.getSource(start, end);
        String content = source.getContent();
        
        if(!Parsing.IS_ROUNDTRIP) {
          if (content.isEmpty()) {
              return new HeadingParser(level, SourceLines.empty());
          }
          return new HeadingParser(level, source);
        }else {
            return new HeadingParser(level, source, whitespacePreStart,
                    whitespacePostEnd, numEndingSymbol, '#');
        }
    }

    // spec: A setext heading underline is a sequence of = characters or a sequence of - characters, with no more than
    // 3 spaces indentation and any number of trailing spaces.
    private static int getSetextHeadingLevel(CharSequence line, int index) {
        switch (line.charAt(index)) {
            case '=':
                if (isSetextHeadingRest(line, index + 1, '=')) {
                    return 1;
                }
            case '-':
                if (isSetextHeadingRest(line, index + 1, '-')) {
                    return 2;
                }
        }
        return 0;
    }

    private static boolean isSetextHeadingRest(CharSequence line, int index, char marker) {
        int afterMarker = Parsing.skip(marker, line, index, line.length());
        int afterSpace = Parsing.skipSpaceTab(line, afterMarker, line.length());
        return afterSpace >= line.length();
    }
    
    private static HeadingParser getSetextHeading(int headingLevel, SourceLines content, CharSequence line, int index) {
        String whitespacePreStart = line.subSequence(0, index).toString();
        
        char lineSymbol = line.charAt(index);
        
        int headingLength = Parsing.skip(lineSymbol, line, index, line.length()) - whitespacePreStart.length();
        
        int numEndWhitespace = Parsing.skipSpaceTabBackwards(line, line.length() - 1, 0);
        
        numEndWhitespace = line.length() - 1 - numEndWhitespace;
        
        StringBuilder whitespacePostEnd = new StringBuilder(0);
        for(int i = 0; i < numEndWhitespace; i++) {
            whitespacePostEnd.append(" ");
        }
        
        return new HeadingParser(headingLevel, content, whitespacePreStart, whitespacePostEnd.toString(), headingLength, lineSymbol);
    }
}
