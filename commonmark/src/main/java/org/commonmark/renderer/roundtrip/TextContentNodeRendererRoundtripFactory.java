package org.commonmark.renderer.roundtrip;

import org.commonmark.renderer.NodeRenderer;

/**
 * Factory for instantiating new node renderers when rendering is done.
 */
public interface TextContentNodeRendererRoundtripFactory {

    /**
     * Create a new node renderer for the specified rendering context.
     *
     * @param context the context for rendering (normally passed on to the node renderer)
     * @return a node renderer
     */
    NodeRenderer create(TextContentNodeRendererRoundtripContext context);
}
