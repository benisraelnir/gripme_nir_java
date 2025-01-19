package com.grip.util;

import java.util.regex.Pattern;

/**
 * Utility class that patches HTML content rendered by the GitHub API to match GitHub's main site rendering.
 * It specifically handles task list checkboxes and header link icons, applying CSS classes and HTML attributes
 * to make them visually consistent with GitHub's style.
 */
public class HtmlPatcher {

    private static final Pattern INCOMPLETE_TASK_RE = Pattern.compile(
            "<li>\\[ \\] (.*?)(<ul.*?>|</li>)",
            Pattern.DOTALL
    );
    private static final String INCOMPLETE_TASK_SUB =
            "<li class=\"task-list-item\">" +
            "<input type=\"checkbox\" " +
            "class=\"task-list-item-checkbox\" disabled=\"\"> $1$2";

    private static final Pattern COMPLETE_TASK_RE = Pattern.compile(
            "<li>\\[x\\] (.*?)(<ul.*?>|</li>)",
            Pattern.DOTALL
    );
    private static final String COMPLETE_TASK_SUB =
            "<li class=\"task-list-item\">" +
            "<input type=\"checkbox\" class=\"task-list-item-checkbox\" " +
            "checked=\"\" disabled=\"\"> $1$2";

    private static final Pattern HEADER_PATCH_RE = Pattern.compile(
            "<span>\\{:\"aria-hidden\"=&gt;\"true\", :class=&gt;" +
            "\"octicon octicon-link\"\\}</span>",
            Pattern.DOTALL
    );
    private static final String HEADER_PATCH_SUB =
            "<span class=\"octicon octicon-link\"></span>";

    /**
     * Processes the HTML rendered by the GitHub API, patching
     * any inconsistencies from the main site.
     *
     * @param html The HTML content to process
     * @param userContent Whether the content is user-generated
     * @return The processed HTML content
     */
    public static String patch(String html, boolean userContent) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }

        // FUTURE: Remove this once GitHub API renders task lists
        // https://github.com/isaacs/github/issues/309
        if (!userContent) {
            html = INCOMPLETE_TASK_RE.matcher(html).replaceAll(INCOMPLETE_TASK_SUB);
            html = COMPLETE_TASK_RE.matcher(html).replaceAll(COMPLETE_TASK_SUB);
        }

        // FUTURE: Remove this once GitHub API fixes the header bug
        // https://github.com/joeyespo/grip/issues/244
        html = HEADER_PATCH_RE.matcher(html).replaceAll(HEADER_PATCH_SUB);

        return html;
    }

    /**
     * Processes the HTML rendered by the GitHub API with default userContent value (false).
     *
     * @param html The HTML content to process
     * @return The processed HTML content
     */
    public static String patch(String html) {
        return patch(html, false);
    }
}