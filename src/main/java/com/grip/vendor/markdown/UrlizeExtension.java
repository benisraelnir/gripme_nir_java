package com.grip.vendor.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.LineAppendable;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A more liberal autolinker for Markdown, inspired by Django's urlize function.
 *
 * This extension provides URL auto-linking functionality, converting plain text URLs
 * and domain names into clickable HTML links.
 *
 * Examples:
 * - http://example.com/ -> <a href="http://example.com/">http://example.com/</a>
 * - example.com -> <a href="http://example.com">example.com</a>
 * - www.example.us -> <a href="http://www.example.us">www.example.us</a>
 */
public class UrlizeExtension implements Parser.ParserExtension {
    private static final Pattern URLIZE_PATTERN = Pattern.compile(
            "(?:<(?:f|ht)tps?://[^>]*>)|" +
            "(?:\\b(?:f|ht)tps?://[^)<>\\s]+[^.,)<>\\s])|" +
            "(?:\\bwww\\.[^)<>\\s]+[^.,)<>\\s])|" +
            "(?:[^(<\\s]+\\.(?:com|net|org)\\b)"
    );

    private UrlizeExtension() {
    }

    public static UrlizeExtension create() {
        return new UrlizeExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customInlineParserExtensionFactory(new UrlizeParserExtensionFactory());
    }

    @Override
    public void parserOptions(MutableDataHolder options) {
    }

    /**
     * Factory for creating the Urlize parser extension
     */
    private static class UrlizeParserExtensionFactory implements InlineParserExtensionFactory {
        @Override
        public InlineParserExtension apply(LightInlineParser inlineParser) {
            return new UrlizeParserExtension(inlineParser);
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public CharSequence getCharacters() {
            return "hwf"; // First characters that might start a URL (http, www, ftp)
        }
    }

    /**
     * The actual parser extension that handles URL detection and link creation
     */
    private static class UrlizeParserExtension implements InlineParserExtension {
        private final LightInlineParser inlineParser;

        UrlizeParserExtension(LightInlineParser inlineParser) {
            this.inlineParser = inlineParser;
        }

        @Override
        public void finalizeDocument(InlineParser inlineParser) {
        }

        @Override
        public void finalizeBlock(InlineParser inlineParser) {
        }

        @Override
        public boolean parse(LightInlineParser inlineParser) {
            BasedSequence line = inlineParser.getInput();
            int startIndex = inlineParser.getIndex();
            String text = line.subSequence(startIndex, line.length()).toString();
            
            Matcher matcher = URLIZE_PATTERN.matcher(text);
            if (!matcher.lookingAt()) {
                return false;
            }

            String url = matcher.group();
            String href = url;

            // Clean up URL if it starts with < and ends with >
            if (url.startsWith("<") && url.endsWith(">")) {
                url = url.substring(1, url.length() - 1);
                href = url;
            }

            // Add http:// prefix if needed
            if (!href.matches("^(?:f|ht)tps?://.*")) {
                if (href.contains("@") && !href.contains("/")) {
                    href = "mailto:" + href;
                } else {
                    href = "http://" + href;
                }
            }

            // Create link node
            Link link = new Link(
                    line.subSequence(startIndex, startIndex + matcher.end()),
                    BasedSequence.of(href),
                    BasedSequence.of(url),
                    BasedSequence.EMPTY,
                    BasedSequence.EMPTY,
                    BasedSequence.EMPTY
            );
            link.setCharsFromContent();
            inlineParser.setIndex(startIndex + matcher.end());
            inlineParser.getBlock().appendChild(link);

            return true;
        }
    }

    /**
     * Creates a new extension with default settings
     *
     * @return A configured Parser.ParserExtension
     */
    public static Parser.ParserExtension create(MutableDataHolder options) {
        return new UrlizeExtension();
    }
}