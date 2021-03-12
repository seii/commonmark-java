package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.*;

import static org.commonmark.internal.util.Escaping.unescapeString;

public class FencedCodeBlockParser extends AbstractBlockParser {

    private final FencedCodeBlock block = new FencedCodeBlock();

    private String firstLine;
    private StringBuilder otherLines = new StringBuilder();
    
    public FencedCodeBlockParser(char fenceChar, int fenceLength, int fenceIndent) {
        block.setFenceChar(fenceChar);
        block.setStartFenceLength(fenceLength);
        block.setStartFenceIndent(fenceIndent);
    }
    
    public FencedCodeBlockParser(char fenceChar, int startFenceLength, int startFenceIndent, int endFenceLength, int endFenceIndent, String whitespacePostFence) {
        this(fenceChar, startFenceLength, startFenceIndent);
        block.setEndFenceIndent(endFenceIndent);
        block.setWhitespacePostFence(whitespacePostFence);
    }

    @Override
    public Block getBlock() {
        return block;
    }
    
    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        int newIndex = state.getIndex();
        CharSequence line = state.getLine().getContent();
        if (state.getIndent() < Parsing.CODE_BLOCK_INDENT && nextNonSpace < line.length() && line.charAt(nextNonSpace) == block.getFenceChar() && isClosing(line, nextNonSpace)) {
            // closing fence - we're at end of line, so we can finalize now
            return BlockContinue.finished();
        } else {
            // skip optional spaces of fence indent
            int i = block.getStartFenceIndent();
            int length = line.length();
            
            if(Parsing.IS_ROUNDTRIP) {
                // spec: The closing code fence may be indented up to three spaces, and may be followed only by spaces, which are ignored.
                // 
                // (Note: The example in the spec makes it clear that these closing spaces are
                //    valid when part of the AST, just not the rendered HTML.)
                if(line.toString().matches(".*\\n$")) {
                    int lastNonNewlineChar = Parsing.skipBackwards('\n', line, length - 1, i);
                    block.setWhitespacePostFence(line.subSequence(lastNonNewlineChar + 1, line.length()).toString());
                }
            }
            
            while (i > 0 && newIndex < length && line.charAt(newIndex) == ' ') {
                newIndex++;
                i--;
            }
        }
        return BlockContinue.atIndex(newIndex);
    }
    
    public void addLine(SourceLine line) {      
      if (firstLine == null) {
          if(!Parsing.IS_ROUNDTRIP) {
              firstLine = line.getContent().toString();
          }else {
              // Capture only the content for the AST, not its delimiters
              int lineEndPos = line.getContent().toString().length();
              int startPos = Parsing.skipSpaceTab(line.getContent(), 0, lineEndPos);
              if(line.getContent().charAt(startPos) == '`') {
                  startPos = Parsing.skip('`', line.getContent(), startPos, lineEndPos);
              }
              
              if(line.getContent().charAt(startPos) == '~') {
                  startPos = Parsing.skip('~', line.getContent(), 0, lineEndPos);
              }
              
              firstLine = line.getContent().subSequence(startPos, lineEndPos).toString();
          }
      } else {
          otherLines.append(line.getContent());
          if(!Parsing.IS_ROUNDTRIP) {
              otherLines.append('\n');
          }
      }
  }
    
    @Override
    public void closeBlock() {
        // first line becomes info string
        if(!Parsing.IS_ROUNDTRIP) {
            block.setInfo(unescapeString(firstLine.trim()));
        }else {
            // AST is literal and does not escape characters
            block.setInfo(firstLine);
        }
        block.setLiteral(otherLines.toString());
    }

    public static class Factory extends AbstractBlockParserFactory {
        
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            FencedCodeBlockParser blockParser = null;
            int nextNonSpace = 0;
            
            if(!Parsing.IS_ROUNDTRIP || ((DocumentRoundtripParser)state).getContainerString() == null) {
                int indent = state.getIndent();
                if (indent >= Parsing.CODE_BLOCK_INDENT) {
                    return BlockStart.none();
                }
            
                nextNonSpace = state.getNextNonSpaceIndex();
                blockParser = checkOpener(state.getLine().getContent(), nextNonSpace, indent);
            }else {
                // Get text without getting the delimiters too
                String containerString = ((DocumentRoundtripParser)state).getContainerString();
                nextNonSpace = Parsing.skipSpaceTab(containerString, 0, containerString.length());
                  
                if(nextNonSpace + 1 >= Parsing.CODE_BLOCK_INDENT) {
                    return BlockStart.none();
                }
                  
                blockParser = checkOpener(containerString, nextNonSpace, nextNonSpace);
            }
              
            if (blockParser != null) {
                blockParser.block.setStartFenceIndent(nextNonSpace);
                return BlockStart.of(blockParser).atIndex(nextNonSpace + blockParser.block.getStartFenceLength());
            } else {
                return BlockStart.none();
            }
        }
    }

    // spec: A code fence is a sequence of at least three consecutive backtick characters (`) or tildes (~). (Tildes and
    // backticks cannot be mixed.)
    private static FencedCodeBlockParser checkOpener(CharSequence line, int index, int indent) {
        int backticks = 0;
        int tildes = 0;
        int length = line.length();
        loop:
        for (int i = index; i < length; i++) {
            switch (line.charAt(i)) {
                case '`':
                    backticks++;
                    break;
                case '~':
                    tildes++;
                    break;
                default:
                    break loop;
            }
        }
        if (backticks >= 3 && tildes == 0) {
            // spec: If the info string comes after a backtick fence, it may not contain any backtick characters.
            if (Parsing.find('`', line, index + backticks) != -1) {
                return null;
            }
            return new FencedCodeBlockParser('`', backticks, indent);
        } else if (tildes >= 3 && backticks == 0) {
            // spec: Info strings for tilde code blocks can contain backticks and tildes
            return new FencedCodeBlockParser('~', tildes, indent);
        } else {
            return null;
        }
    }
    
    // spec: The content of the code block consists of all subsequent lines, until a closing code fence of the same type
    // as the code block began with (backticks or tildes), and with at least as many backticks or tildes as the opening
    // code fence.
    private boolean isClosing(CharSequence line, int index) {
        char fenceChar = block.getFenceChar();
        int fenceLength = block.getStartFenceLength();
        
        if(Parsing.IS_ROUNDTRIP) {
            // AST: Capture how far ending fence is indented
            block.setEndFenceIndent(Parsing.skipSpaceTab(line, 0, line.length()));
        }
        
        int fences = Parsing.skip(fenceChar, line, index, line.length()) - index;
        if (fences < fenceLength) {
            return false;
        }
        
        if(Parsing.IS_ROUNDTRIP) {
            // AST: Capture how long the ending fence is
            block.setEndFenceLength(fences);
        }
        
        // spec: The closing code fence [...] may be followed only by spaces, which are ignored.
        int after = Parsing.skipSpaceTab(line, index + fences, line.length());
        
        if(!Parsing.IS_ROUNDTRIP) {
            return after == line.length();
        }else {
            // AST: Capture any whitespace (including newlines) after the closing
            //      fence
            block.setWhitespacePostFence(
                    line.subSequence(after, line.length()).toString());
            
            return after == line.length() - 1;
        }
    }
}
