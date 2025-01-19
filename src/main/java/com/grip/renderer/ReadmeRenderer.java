package com.grip.renderer;

/**
 * Abstract base class for rendering README files.
 */
public abstract class ReadmeRenderer {
    protected final boolean userContent;
    protected final String context;

    /**
     * Returns whether the content is user-generated.
     */
    public boolean isUserContent() {
        return userContent;
    }

    /**
     * Constructs a ReadmeRenderer with specified user content and context settings.
     *
     * @param userContent whether the content is user-generated
     * @param context the context for rendering
     */
    public ReadmeRenderer(Boolean userContent, String context) {
        this.userContent = userContent != null ? userContent : false;
        this.context = context;
    }

    /**
     * Constructs a ReadmeRenderer with default settings.
     */
    public ReadmeRenderer() {
        this(false, null);
    }

    /**
     * Renders the specified markdown content and embedded styles.
     *
     * @param text the markdown text to render
     * @param auth authentication credentials (optional)
     * @return the rendered content
     * @throws Exception if rendering fails
     */
    public abstract String render(String text, Object auth) throws Exception;

    /**
     * Renders the specified markdown content without authentication.
     *
     * @param text the markdown text to render
     * @return the rendered content
     * @throws Exception if rendering fails
     */
    public String render(String text) throws Exception {
        return render(text, null);
    }
}