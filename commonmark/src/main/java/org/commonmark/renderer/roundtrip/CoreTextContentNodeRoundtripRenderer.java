package org.commonmark.renderer.roundtrip;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonmark.internal.renderer.text.BulletListHolder;
import org.commonmark.internal.renderer.text.ListHolder;
import org.commonmark.internal.renderer.text.OrderedListHolder;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.NodeRenderer;

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreTextContentNodeRoundtripRenderer extends AbstractVisitor implements NodeRenderer {

    protected final TextContentNodeRendererRoundtripContext context;
    private final TextContentRoundtripWriter textContent;

    private ListHolder listHolder;

    public CoreTextContentNodeRoundtripRenderer(TextContentNodeRendererRoundtripContext context) {
        this.context = context;
        this.textContent = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
                Document.class,
                Heading.class,
                Paragraph.class,
                BlockQuote.class,
                BulletList.class,
                FencedCodeBlock.class,
                HtmlBlock.class,
                ThematicBreak.class,
                IndentedCodeBlock.class,
                Link.class,
                LinkReferenceDefinition.class,
                ListItem.class,
                OrderedList.class,
                Image.class,
                Emphasis.class,
                StrongEmphasis.class,
                Text.class,
                Code.class,
                HtmlInline.class,
                SoftLineBreak.class,
                HardLineBreak.class
        ));
    }

    @Override
    public void render(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(Document document) {
        // No rendering itself
        visitChildren(document);
    }

    public void visit(BlockQuote blockQuote) {
        for(int i = 0; i < blockQuote.getFirstLinePrecedingSpaces(); i++) {
            textContent.write(" ");
        }
        textContent.write(">");
        visitChildren(blockQuote);

//        writeEndOfLineIfNeeded(blockQuote, null);
//        writeEndOfLine();
    }

    @Override
    public void visit(BulletList bulletList) {
//        if (listHolder != null) {
//            writeEndOfLine();
//        }
        listHolder = new BulletListHolder(listHolder, bulletList);
        visitChildren(bulletList);
//        writeEndOfLineIfNeeded(bulletList, null);
//        writeEndOfLine();
        if (listHolder.getParent() != null) {
            listHolder = listHolder.getParent();
        } else {
            listHolder = null;
        }
    }

    public void visit(Code code) {
        for(int i = 0; i < code.getNumBackticks(); i++) {
            textContent.write('`');
        }
        
        textContent.write(code.getLiteral());
        
        for(int i = 0; i < code.getNumBackticks(); i++) {
            textContent.write('`');
        }
    }
    
    @Override
    public void visit(Emphasis emphasis) {
        writeText(emphasis.getOpeningDelimiter());
        visitChildren(emphasis);
        writeText(emphasis.getClosingDelimiter());
    }

    public void visit(FencedCodeBlock fencedCodeBlock) {
        for(int i = 0; i < fencedCodeBlock.getStartFenceIndent(); i++) {
            textContent.write(" ");
        }
        
        for(int i = 0; i < fencedCodeBlock.getStartFenceLength(); i++) {
            textContent.write(fencedCodeBlock.getFenceChar());
        }
        
        if(fencedCodeBlock.getInfo() != null && !fencedCodeBlock.getInfo().isEmpty()) {
            textContent.write(fencedCodeBlock.getInfo());
        }
        
//        textContent.line();
        textContent.write(fencedCodeBlock.getLiteral());
        
        for(int i = 0; i < fencedCodeBlock.getEndFenceIndent(); i++) {
            textContent.write(" ");
        }
        
        for(int i = 0; i < fencedCodeBlock.getEndFenceLength(); i++) {
            textContent.write(fencedCodeBlock.getFenceChar());
        }
        
        if(fencedCodeBlock.getWhitespacePostFence() != null &&
                !fencedCodeBlock.getWhitespacePostFence().isEmpty() &&
                fencedCodeBlock.getEndFenceLength() > 0) {
            textContent.write(fencedCodeBlock.getWhitespacePostFence());
        }
        
        //writeEndOfLine();
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
//        writeEndOfLineIfNeeded(hardLineBreak, null);
        writeEndOfLine();
    }

    public void visit(Heading heading) {
        //visitChildren(heading);
        //writeEndOfLineIfNeeded(heading, ':');
        
        if(heading.getSymbolType() == '#') {
            if(heading.getWhitespacePreStart() != null && !heading.getWhitespacePreStart().isEmpty()) {
                textContent.write(heading.getWhitespacePreStart());
            }
            
            for(int i = 0; i < heading.getLevel(); i++) {
                textContent.write(heading.getSymbolType());
            }
            
//            if(heading.getWhitespacePreContent() != null && !heading.getWhitespacePreContent().isEmpty()) {
//                textContent.write(heading.getWhitespacePreContent());
//            }
            
            visitChildren(heading);
            
//            if(heading.getWhitespacePostContent() != null && !heading.getWhitespacePostContent().isEmpty()) {
//                textContent.write(heading.getWhitespacePostContent());
//            }
            
            if(heading.getNumEndingSymbol() > 0) {
                for(int i = 0; i < heading.getNumEndingSymbol(); i++) {
                    textContent.write(heading.getSymbolType());
                }
                
                if(heading.getWhitespacePostEnd() != null && !heading.getWhitespacePostEnd().isEmpty()) {
                    textContent.write(heading.getWhitespacePostEnd());
                }
            }
        }else {
            if(heading.getWhitespacePreContent() != null && !heading.getWhitespacePreContent().isEmpty()) {
                textContent.write(heading.getWhitespacePreContent());
            }
            
            visitChildren(heading);
            
            if(heading.getWhitespacePostContent() != null && !heading.getWhitespacePostContent().isEmpty()) {
                textContent.write(heading.getWhitespacePostContent());
            }
            
//            writeEndOfLine();
            
            if(heading.getWhitespacePreStart() != null && !heading.getWhitespacePreStart().isEmpty()) {
                textContent.write(heading.getWhitespacePreStart());
            }
            
            for(int i = 0; i < heading.getNumEndingSymbol(); i++) {
                textContent.write(heading.getSymbolType());
            }
            
            if(heading.getWhitespacePostEnd() != null && !heading.getWhitespacePostEnd().isEmpty()) {
                textContent.write(heading.getWhitespacePostEnd());
            }
        }
//        writeEndOfLineIfNeeded(heading, ':');
//        writeEndOfLine();
//        writeEndOfLineIfNeeded(heading, null);
    }

    public void visit(ThematicBreak thematicBreak) {
        textContent.write(thematicBreak.getContent().toString());
//        writeEndOfLineIfNeeded(thematicBreak, null);
//        writeEndOfLine();
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        writeText(htmlInline.getLiteral());
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        writeText(htmlBlock.getLiteral());
//        writeEndOfLine();
    }

    @Override
    public void visit(Image image) {
        writeLink(image, image.getTitle(), image.getDestination());
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        textContent.write(indentedCodeBlock.getLiteral());
    }

    public void visit(Link link) {
        writeLink(link, link.getTitle(), link.getDestination());
    }
    
    @Override
    public void visit(LinkReferenceDefinition linkDef) {
        writeLinkReference(linkDef);
    }

    @Override
    public void visit(ListItem listItem) {
        if (listHolder != null && listHolder instanceof OrderedListHolder) {
            OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
            String indent = context.stripNewlines() ? "" : orderedListHolder.getIndent();
//            textContent.write(indent + orderedListHolder.getCounter() + orderedListHolder.getDelimiter() + " ");
            textContent.write(listItem.getPreDelimiterWhitespace() + orderedListHolder.getRawNumber() + orderedListHolder.getDelimiter());
            visitChildren(listItem);
//            writeEndOfLineIfNeeded(listItem, null);
            orderedListHolder.increaseCounter();
        } else if (listHolder != null && listHolder instanceof BulletListHolder) {
            BulletListHolder bulletListHolder = (BulletListHolder) listHolder;
//                textContent.write(bulletListHolder.getIndent() + bulletListHolder.getMarker() + " ");
            textContent.write(listItem.getPreDelimiterWhitespace() + bulletListHolder.getMarker());
            visitChildren(listItem);
//            writeEndOfLineIfNeeded(listItem, null);
        }
    }

    @Override
    public void visit(OrderedList orderedList) {
//        if (listHolder != null) {
//            writeEndOfLine();
//        }
        listHolder = new OrderedListHolder(listHolder, orderedList);
        visitChildren(orderedList);
//        writeEndOfLineIfNeeded(orderedList, null);
//        writeEndOfLine();
        if (listHolder.getParent() != null) {
            listHolder = listHolder.getParent();
        } else {
            listHolder = null;
        }
    }

    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        // Add "end of line" only if its "root paragraph.
//        if (paragraph.getParent() == null
//                || paragraph.getParent() instanceof Document
//                || paragraph.getParent() instanceof ListItem) {
//            writeEndOfLineIfNeeded(paragraph, null);
//            writeEndOfLine();
//        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
//        writeEndOfLineIfNeeded(softLineBreak, null);
        writeEndOfLine();
    }
    
    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        writeText(strongEmphasis.getOpeningDelimiter());
        visitChildren(strongEmphasis);
        writeText(strongEmphasis.getClosingDelimiter());
    }

    @Override
    public void visit(Text text) {
        writeText(text.getLiteral());
    }

    @Override
    protected void visitChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private void writeText(String text) {
        textContent.write(text);
    }

    private void writeLink(Node node, String title, String destination) {
        if(node instanceof Link) {
            writeLink((Link)node);
        }
        
        if(node instanceof Image) {
            writeImage((Image)node);
        }
    }
    
    private void writeLink(Link node) {
        boolean hasChild = node.getFirstChild() != null;
//        boolean hasTitle = title != null && !title.equals(destination);
//        boolean hasDestination = destination != null && !destination.equals("");

        // Autolink
        if(node.isAutolink()) {
            textContent.write("<");
            visitChildren(node);
            textContent.write(">");
        }else {
            textContent.write("[");
            
            if(node.getTitle() != null) {
                textContent.write(node.getTitle());
            }else {
                visitChildren(node);
            }
            
            textContent.write("]");
            textContent.write("(");
            
            if(node.getDestination() != null) {
                textContent.write(node.getDestination());
            }else {
                visitChildren(node);
            }
            
            textContent.write(")");
        }
    }
    
    private void writeLinkReference(LinkReferenceDefinition node) {
        //TODO: Implement
        textContent.write("[");
        textContent.write(node.getLabel());
        textContent.write("]:");
        
        if(node.getDestination() != null) {
            textContent.write(node.getDestination());
            
            if(node.getTitle() != null) {
                textContent.write(node.getTitle());
            }
        }
    }
    
    private void writeImage(Image node) {
        boolean hasChild = node.getFirstChild() != null;
//        boolean hasTitle = title != null && !title.equals(destination);
//        boolean hasDestination = destination != null && !destination.equals("");

        // Autolink
        if(node.isAutolink()) {
            textContent.write("<");
            visitChildren(node);
            textContent.write(">");
        }else {
            textContent.write("![");
            visitChildren(node);
            
            textContent.write("]");
            
            if(node.getDestination() != null) {
                textContent.write("(");
                textContent.write(node.getDestination());
                
                if(node.getTitle() != null) {
                    textContent.write(node.getTitle());
                }
                
                textContent.write(")");
            }
        }
    }

    private void writeEndOfLineIfNeeded(Node node, Character c) {
        if (node.getNext() != null || node instanceof Paragraph && node.getNext() == null) {
//          if (node.getNext() != null) {
              textContent.line();
        }
    }

    private void writeEndOfLine() {
        textContent.line();
    }
}
