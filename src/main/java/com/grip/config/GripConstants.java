package com.grip.config;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Constants used throughout the Grip application.
 * Contains configuration values for markdown file extensions, default filenames,
 * API URLs, and style settings.
 */
public final class GripConstants {
    /**
     * The current version of Grip
     */
    public static final String VERSION = "4.6.2";

    private GripConstants() {
        // Prevent instantiation
    }

    /**
     * The common titles and supported extensions,
     * as defined by https://github.com/github/markup
     */
    public static final List<String> SUPPORTED_TITLES = List.of("README", "Readme", "readme", "Home");
    public static final List<String> SUPPORTED_EXTENSIONS = List.of(".md", ".markdown");

    /**
     * The default filenames when no file is provided
     */
    public static final List<String> DEFAULT_FILENAMES = SUPPORTED_TITLES.stream()
            .flatMap(title -> SUPPORTED_EXTENSIONS.stream()
                    .map(ext -> title + ext))
            .collect(Collectors.toList());
    public static final String DEFAULT_FILENAME = DEFAULT_FILENAMES.get(0);

    /**
     * The default directory to load Grip settings from
     */
    public static final String DEFAULT_GRIPHOME = "~/.grip";

    /**
     * The default URL of the Grip server
     */
    public static final String DEFAULT_GRIPURL = "/__/grip";

    /**
     * The public GitHub API
     */
    public static final String DEFAULT_API_URL = "https://api.github.com";

    /**
     * Style parsing constants
     */
    public static final String STYLE_URLS_SOURCE = "https://github.com/joeyespo/grip";

    /**
     * Regular expressions for style URL parsing.
     * Note: Using a list in case the implementation limitation is a problem
     * https://docs.python.org/3/library/re.html#re.findall
     */
    public static final List<String> STYLE_URLS_RES = List.of(
            "<link\\b[^>]+\\bhref=['\"']?([^'\"' >]+)['\"']?\\brel=['\"']?stylesheet['\"']?[^>]+[^>]*(?=>)",
            "<link\\b[^>]+\\brel=['\"']?stylesheet['\"']?[^>]+\\bhref=['\"']?([^'\"' >]+)['\"']?[^>]*(?=>)"
    );

    public static final String STYLE_ASSET_URLS_RE = 
            "url\\(['\"']?(/static/fonts/octicons/[^'\"' \\)]+)['\"']?\\)";

    public static final String STYLE_ASSET_URLS_SUB_FORMAT = "url(\"{0}\\1\")";

    public static final String STYLE_ASSET_URLS_INLINE_FORMAT = 
            "url\\(['\"']?((?:/static|{0})/[^'\"' \\)]+)['\"']?\\)";
}