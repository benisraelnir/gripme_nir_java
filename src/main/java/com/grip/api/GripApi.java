package com.grip.api;

import com.grip.core.Grip;
import com.grip.reader.DirectoryReader;
import com.grip.reader.ReadmeReader;
import com.grip.reader.StdinReader;
import com.grip.reader.TextReader;

import com.grip.renderer.GitHubRenderer;
import com.grip.renderer.OfflineRenderer;
import com.grip.renderer.ReadmeRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Core API for creating and managing Grip applications.
 * This class provides factory methods and utilities for rendering markdown content.
 */
@Service
public class GripApi {
    @Autowired
    private GripService gripService;

    /**
     * Creates a Grip application with the specified overrides.
     *
     * @param path Path to the README file or directory
     * @param userContent Whether to use user content mode
     * @param context GitHub context
     * @param username GitHub username for authentication
     * @param password GitHub password or token for authentication
     * @param renderOffline Whether to render offline
     * @param renderWide Whether to render in wide mode
     * @param renderInline Whether to render inline
     * @param apiUrl Custom GitHub API URL
     * @param title Custom page title
     * @param text Direct text input (optional)
     * @param autorefresh Whether to enable auto-refresh
     * @param quiet Whether to suppress output
     * @param theme Theme to use (default: "light")
     * @param gripClass Custom Grip class (optional)
     * @return Configured Grip instance
     */
    /**
     * Creates a Grip application with the specified overrides.
     * Note: The password array will be cleared after use for security.
     *
     * @param path Path to the README file or directory
     * @param userContent Whether to use user content mode
     * @param context GitHub context
     * @param username GitHub username for authentication
     * @param password GitHub password or token as char array (will be cleared)
     * @param renderOffline Whether to render offline
     * @param renderWide Whether to render in wide mode
     * @param renderInline Whether to render inline
     * @param apiUrl Custom GitHub API URL
     * @param title Custom page title
     * @param text Direct text input (optional)
     * @param autorefresh Whether to enable auto-refresh
     * @param quiet Whether to suppress output
     * @param theme Theme to use (default: "light")
     * @param gripClass Custom Grip class (optional)
     * @return Configured Grip instance
     */
    public Grip createApp(String path, Boolean userContent, String context,
                         String username, char[] password, Boolean renderOffline,
                         Boolean renderWide, Boolean renderInline, String apiUrl,
                         String title, String text, Boolean autorefresh,
                         Boolean quiet, String theme, Class<? extends Grip> gripClass) {
        return createAppInternal(path, userContent, context, username, password,
                              renderOffline, renderWide, renderInline, apiUrl,
                              title, text, autorefresh, quiet, theme, gripClass);
    }

    // Overloaded versions with default values
    public Grip createApp() {
        return createApp(null, false, null, null, null, false,
                        false, false, null, null, null, false,
                        false, "light", null);
    }

    public Grip createApp(String path) {
        return createApp(path, false, null, null, null, false,
                        false, false, null, null, null, false,
                        false, "light", null);
    }

    private Grip createAppInternal(String path, Boolean userContent, String context,
                         String username, char[] password, Boolean renderOffline,
                         Boolean renderWide, Boolean renderInline, String apiUrl,
                         String title, String text, Boolean autorefresh,
                         Boolean quiet, String theme, Class<? extends Grip> gripClass) {
        
        // Set default values matching Python implementation
        userContent = Objects.requireNonNullElse(userContent, false);
        renderOffline = Objects.requireNonNullElse(renderOffline, false);
        renderWide = Objects.requireNonNullElse(renderWide, false);
        renderInline = Objects.requireNonNullElse(renderInline, false);
        autorefresh = Objects.requireNonNullElse(autorefresh, false);
        quiet = Objects.requireNonNullElse(quiet, false);
        theme = Objects.requireNonNullElse(theme, "light");
        gripClass = Objects.requireNonNullElse(gripClass, Grip.class);

        // Configure the reader
        ReadmeReader source;
        if (text != null) {
            String displayFilename;
            try {
                displayFilename = new DirectoryReader(path, true).filenameFor(null);
            } catch (Exception e) {
                displayFilename = path != null ? path : "stdin";
            }
            source = new TextReader(text, displayFilename);
        } else if ("-".equals(path)) {
            source = new StdinReader("");
        } else {
            try {
                source = new DirectoryReader(path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create directory reader: " + e.getMessage(), e);
            }
        }

        // Configure the renderer
        ReadmeRenderer renderer;
        if (renderOffline) {
            renderer = new OfflineRenderer(userContent, context);
        } else if (Boolean.TRUE.equals(userContent) || context != null || apiUrl != null) {
            renderer = new GitHubRenderer(userContent, context, apiUrl, null);
        } else {
            renderer = null;
        }

        // Create authentication credentials if provided
        AuthCredentials auth = AuthCredentials.from(username, password);

        // Create and return the Grip instance
        return gripService.createGripInstance(gripClass, source, auth, renderer, null,
            renderWide, renderInline, title, autorefresh, quiet, theme);
    }

    /**
     * Starts a server to render the specified file or directory containing a README.
     */
    /**
     * Starts a server to render the specified file or directory containing a README.
     * Note: The password array will be cleared after use for security.
     *
     * @param path Path to the README file or directory
     * @param host Host to bind to
     * @param port Port to listen on
     * @param userContent Whether to use user content mode
     * @param context GitHub context
     * @param username GitHub username for authentication
     * @param password GitHub password or token as char array (will be cleared)
     * @param renderOffline Whether to render offline
     * @param renderWide Whether to render in wide mode
     * @param renderInline Whether to render inline
     * @param apiUrl Custom GitHub API URL
     * @param title Custom page title
     * @param autorefresh Whether to enable auto-refresh
     * @param browser Whether to open in browser
     * @param quiet Whether to suppress output
     * @param theme Theme to use (default: "light")
     * @param gripClass Custom Grip class (optional)
     */
    public void serve(String path, String host, Integer port, Boolean userContent,
                     String context, String username, char[] password,
                     Boolean renderOffline, Boolean renderWide, Boolean renderInline,
                     String apiUrl, String title, Boolean autorefresh,
                     Boolean browser, Boolean quiet, String theme,
                     Class<? extends Grip> gripClass) {
        
        Grip app = createApp(path, userContent, context, username, password,
                           renderOffline, renderWide, renderInline, apiUrl,
                           title, null, autorefresh, quiet, theme, gripClass);
        
        app.run(host, port, browser, false, false);
    }

    // Overloaded versions with default values
    public void serve() {
        serve(null, null, null, false, null, null, null, false,
              false, false, null, null, true, false, false, "light", null);
    }

    public void serve(String path) {
        serve(path, null, null, false, null, null, null, false,
              false, false, null, null, true, false, false, "light", null);
    }

    public void serve(String path, String host, Integer port) {
        serve(path, host, port, false, null, null, null, false,
              false, false, null, null, true, false, false, "light", null);
    }

    public void serve(String path, Boolean userContent, String context,
                     String username, char[] password, Boolean renderOffline,
                     Boolean renderWide, Boolean renderInline, String apiUrl,
                     String title, Boolean browser, Boolean quiet, String theme,
                     Class<? extends Grip> gripClass) {
        serve(path, null, null, userContent, context, username, password,
              renderOffline, renderWide, renderInline, apiUrl, title, true,
              browser, quiet, theme, gripClass);
    }

    /**
     * Clears the cached styles and assets.
     */
    public void clearCache(Class<? extends Grip> gripClass) {
        gripClass = Objects.requireNonNullElse(gripClass, Grip.class);
        gripService.createGripInstance(gripClass, new StdinReader(""), null, null,
            null, null, null, null, null, null, null)
            .clearCache();
    }

    // Overloaded version with default value
    public void clearCache() {
        clearCache(null);
    }

    /**
     * Renders the specified markup text to an HTML page and returns it.
     */
    /**
     * Renders the specified markup text to an HTML page and returns it.
     * Note: The password array will be cleared after use for security.
     *
     * @param path Path to the README file or directory
     * @param userContent Whether to use user content mode
     * @param context GitHub context
     * @param username GitHub username for authentication
     * @param password GitHub password or token as char array (will be cleared)
     * @param renderOffline Whether to render offline
     * @param renderWide Whether to render in wide mode
     * @param renderInline Whether to render inline
     * @param apiUrl Custom GitHub API URL
     * @param title Custom page title
     * @param text Direct text input (optional)
     * @param quiet Whether to suppress output
     * @param theme Theme to use (default: "light")
     * @param gripClass Custom Grip class (optional)
     * @return The rendered HTML page
     */
    public String renderPage(String path, Boolean userContent, String context,
                           String username, char[] password, Boolean renderOffline,
                           Boolean renderWide, Boolean renderInline, String apiUrl,
                           String title, String text, Boolean quiet, String theme,
                           Class<? extends Grip> gripClass) {
        
        return createApp(path, userContent, context, username, password,
                        renderOffline, renderWide, renderInline, apiUrl,
                        title, text, false, quiet, theme, gripClass)
                .render("");
    }

    // Overloaded versions with default values
    public String renderPage() {
        return renderPage(null, false, null, null, null, false,
                         false, false, null, null, null, false,
                         "light", null);
    }

    public String renderPage(String path) {
        return renderPage(path, false, null, null, null, false,
                         false, false, null, null, null, false,
                         "light", null);
    }

    public String renderPage(String path, String text) {
        return renderPage(path, false, null, null, null, false,
                         false, false, null, null, text, false,
                         "light", null);
    }

    /**
     * Renders the specified markup and returns the result.
     */
    /**
     * Renders the specified markup and returns the result.
     * Note: The password array will be cleared after use for security.
     *
     * @param text The markup text to render
     * @param userContent Whether to use user content mode
     * @param context GitHub context
     * @param username GitHub username for authentication
     * @param password GitHub password or token as char array (will be cleared)
     * @param renderOffline Whether to render offline
     * @param apiUrl Custom GitHub API URL
     * @return The rendered content
     */
    public String renderContent(String text, Boolean userContent, String context,
                              String username, char[] password,
                              Boolean renderOffline, String apiUrl) {
        
        userContent = Objects.requireNonNullElse(userContent, false);
        renderOffline = Objects.requireNonNullElse(renderOffline, false);

        ReadmeRenderer renderer;
        if (renderOffline) {
            renderer = new OfflineRenderer(userContent, context);
        } else {
            renderer = new GitHubRenderer(userContent, context, apiUrl, null);
        }

        AuthCredentials auth = AuthCredentials.from(username, password);

        try {
            return renderer.render(text, auth);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render content", e);
        }
    }

    // Overloaded versions with default values
    public String renderContent(String text) {
        return renderContent(text, false, null, null, null, false, null);
    }

    public String renderContent(String text, Boolean userContent) {
        return renderContent(text, userContent, null, null, null, false, null);
    }

    /**
     * Exports the rendered HTML to a file.
     */
    /**
     * Exports the rendered HTML to a file.
     * Note: The password array will be cleared after use for security.
     *
     * @param path Path to the README file or directory
     * @param userContent Whether to use user content mode
     * @param context GitHub context
     * @param username GitHub username for authentication
     * @param password GitHub password or token as char array (will be cleared)
     * @param renderOffline Whether to render offline
     * @param renderWide Whether to render in wide mode
     * @param renderInline Whether to render inline
     * @param outFilename Output filename or - for stdout
     * @param apiUrl Custom GitHub API URL
     * @param title Custom page title
     * @param quiet Whether to suppress output
     * @param theme Theme to use (default: "light")
     * @param gripClass Custom Grip class (optional)
     * @throws IOException if there is an error writing the file
     */
    public void export(String path, Boolean userContent, String context,
                      String username, char[] password, Boolean renderOffline,
                      Boolean renderWide, Boolean renderInline, String outFilename,
                      String apiUrl, String title, Boolean quiet, String theme,
                      Class<? extends Grip> gripClass) throws IOException {
        
        // Set default values
        userContent = Objects.requireNonNullElse(userContent, false);
        renderOffline = Objects.requireNonNullElse(renderOffline, false);
        renderWide = Objects.requireNonNullElse(renderWide, false);
        renderInline = Objects.requireNonNullElse(renderInline, true); // Default true for export
        quiet = Objects.requireNonNullElse(quiet, false);
        theme = Objects.requireNonNullElse(theme, "light");

        boolean exportToStdout = "-".equals(outFilename);
        
        if (outFilename == null) {
            if ("-".equals(path)) {
                exportToStdout = true;
            } else {
                try {
                    String filetitle = new DirectoryReader(path).readmeFor(null)
                        .getFileName().toString();
                    filetitle = filetitle.substring(0, filetitle.lastIndexOf('.'));
                    outFilename = filetitle + ".html";
                } catch (Exception e) {
                    outFilename = "README.html";
                }
            }
        }

        if (!exportToStdout && !quiet) {
            System.err.println("Exporting to " + outFilename);
        }

        String page = renderPage(path, userContent, context, username, password,
                               renderOffline, renderWide, renderInline, apiUrl,
                               title, null, quiet, theme, gripClass);

        if (exportToStdout) {
            try {
                System.out.println(page);
            } catch (Exception e) {
                // Match Python's errno checks for broken pipe (errno.EPIPE)
                String message = e.getMessage();
                if (message == null || !message.contains("Broken pipe")) {
                    throw new IOException("Failed to write to stdout", e);
                }
                // Silently ignore broken pipe errors, matching Python behavior
            }
        } else {
            Files.write(Path.of(outFilename), page.getBytes(StandardCharsets.UTF_8),
                       CREATE, WRITE, TRUNCATE_EXISTING);
        }
    }

    // Overloaded versions with default values
    public void export(String path) throws IOException {
        export(path, false, null, null, null, false,
               false, true, null, null, null, false,
               "light", null);
    }

    public void export(String path, String outFilename) throws IOException {
        export(path, false, null, null, null, false,
               false, true, outFilename, null, null, false,
               "light", null);
    }
}