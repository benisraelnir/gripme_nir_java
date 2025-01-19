package com.grip.core;

import com.grip.assets.GitHubAssetManager;
import com.grip.assets.ReadmeAssetManager;
import com.grip.config.GripConstants;
import com.grip.exceptions.AlreadyRunningException;
import com.grip.exceptions.ReadmeNotFoundException;
import com.grip.reader.DirectoryReader;
import com.grip.reader.ReadmeReader;
import com.grip.renderer.GitHubRenderer;
import com.grip.renderer.ReadmeRenderer;
import com.grip.util.BrowserUtils;
import com.grip.util.PathUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.grip.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.util.MimeTypeUtils;
import java.nio.charset.StandardCharsets;
import org.springframework.ui.ConcurrentModel;
import org.springframework.http.MediaType;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * A Spring Boot application that can serve the specified file or directory
 * containing a README.
 */
@Controller
public class Grip {
    private static final Logger log = LoggerFactory.getLogger(Grip.class);
    private final ReentrantLock runMutex;
    private final CountDownLatch shutdownLatch;
    private final AtomicBoolean shutdownEvent;
    private boolean stylesRetrieved;
    
    @Autowired(required = false)
    private DirectoryReader injectedReader;
    
    private final ReadmeReader reader;
    private final Object auth;
    private final ReadmeRenderer renderer;
    private final ReadmeAssetManager assets;
    private final boolean renderWide;
    private final boolean renderInline;
    private final String title;
    private final boolean autorefresh;
    private final boolean quiet;
    private final String theme;
    private final Properties config;
    @SuppressWarnings("deprecation") // Using RestTemplate for backward compatibility
    private final RestTemplate restTemplate;

    public Grip() throws ReadmeNotFoundException {
        this((ReadmeReader)null);
    }

    public Grip(String source) throws ReadmeNotFoundException {
        this(source != null ? new DirectoryReader(source) : null);
    }

    public Grip(ReadmeReader source) throws ReadmeNotFoundException {
        this(source, null, null, null, null, null, null, null, null, "light");
    }

    public Grip(ReadmeReader source, Object auth) throws ReadmeNotFoundException {
        this(source, auth, null, null, null, null, null, null, null, "light");
    }

    public Grip(ReadmeReader source, Object auth, ReadmeRenderer renderer) throws ReadmeNotFoundException {
        this(source, auth, renderer, null, null, null, null, null, null, "light");
    }

    public Grip(ReadmeReader source, Object auth, ReadmeRenderer renderer,
               ReadmeAssetManager assets) throws ReadmeNotFoundException {
        this(source, auth, renderer, assets, null, null, null, null, null, "light");
    }

    public Grip(ReadmeReader source, Object auth, ReadmeRenderer renderer,
               ReadmeAssetManager assets, Boolean renderWide) throws ReadmeNotFoundException {
        this(source, auth, renderer, assets, renderWide, null, null, null, null, "light");
    }

    public Grip(ReadmeReader source, Object auth, ReadmeRenderer renderer,
            ReadmeAssetManager assets, Boolean renderWide, Boolean renderInline,
            String title, Boolean autorefresh, Boolean quiet, String theme) {
        this(source, auth, renderer, assets, renderWide, renderInline, title, autorefresh, quiet, theme,
             null, null, null, null);
    }

    public Grip(ReadmeReader source, Object auth, ReadmeRenderer renderer,
            ReadmeAssetManager assets, Boolean renderWide, Boolean renderInline,
            String title, Boolean autorefresh, Boolean quiet, String theme,
            String gripUrl, String staticUrlPath, String instancePath,
            Map<String, Object> additionalConfig) {
        // Initialize fields
        this.runMutex = new ReentrantLock();
        this.shutdownLatch = new CountDownLatch(1);
        this.shutdownEvent = new AtomicBoolean(false);
        this.stylesRetrieved = false;
        
        // Initialize config
        this.config = new Properties();
        
        // Set defaults from ENV
        if (gripUrl == null) {
            gripUrl = System.getenv().getOrDefault("GRIPURL", GripConstants.DEFAULT_GRIPURL);
        }
        gripUrl = gripUrl.replaceAll("/$", "");
        
        if (staticUrlPath == null) {
            staticUrlPath = Paths.get(gripUrl, "static").toString();
        }
        
        if (instancePath == null) {
            instancePath = System.getenv().getOrDefault("GRIPHOME", GripConstants.DEFAULT_GRIPHOME);
        }
        instancePath = Paths.get(instancePath).toAbsolutePath().normalize().toString();
        
        // Load config
        loadConfig(instancePath);
        
        // Apply additional config
        if (additionalConfig != null) {
            additionalConfig.forEach((k, v) -> config.put(k, v));
        }
        
        // Initialize source
        try {
            this.reader = source != null ? source : (injectedReader != null ? injectedReader : new DirectoryReader(null));
        } catch (ReadmeNotFoundException e) {
            throw new RuntimeException("Failed to initialize reader", e);
        }
        
        // Initialize other parameters with defaults
        this.auth = auth;
        this.renderer = renderer != null ? renderer : defaultRenderer();
        this.assets = assets != null ? assets : defaultAssetManager();
        this.renderWide = renderWide != null ? renderWide : false;
        this.renderInline = renderInline != null ? renderInline : false;
        this.title = title;
        this.autorefresh = autorefresh != null ? autorefresh : false;
        this.quiet = quiet != null ? quiet : false;
        this.theme = theme != null ? theme : "light";
        
        this.restTemplate = new RestTemplate(); // Keep for backward compatibility
        
        // Add MIME types
        addContentTypes();
    }

    private void loadConfig(String instancePath) {
        try {
            // Load default settings
            config.load(getClass().getResourceAsStream("/application.properties"));
            
            try {
                // Try loading settings_local.py equivalent
                Path localSettings = Paths.get(instancePath, "settings_local.properties");
                if (Files.exists(localSettings)) {
                    Properties localConfig = new Properties();
                    localConfig.load(Files.newInputStream(localSettings));
                    config.putAll(localConfig);
                }
            } catch (IOException ex) {
                // Only log if not a NOTDIR error
                if (!ex.getMessage().contains("Not a directory")) {
                    log.warn("Failed to load local settings: {}", ex.getMessage());
                }
            }
            
            try {
                // Try loading settings.py equivalent
                Path settings = Paths.get(instancePath, "settings.properties");
                if (Files.exists(settings)) {
                    Properties settingsConfig = new Properties();
                    settingsConfig.load(Files.newInputStream(settings));
                    config.putAll(settingsConfig);
                }
            } catch (IOException ex) {
                // Only log if not a NOTDIR error
                if (!ex.getMessage().contains("Not a directory")) {
                    log.warn("Failed to load settings: {}", ex.getMessage());
                }
            }
        } catch (IOException e) {
            if (!quiet) {
                log.warn("Failed to load configuration: {}", e.getMessage());
            }
        }
    }

    private void addContentTypes() {
        try {
            // Add MIME types using Spring's MediaType
            MediaType.valueOf("application/x-font-woff");
            MediaType.valueOf("application/octet-stream");
        } catch (Exception e) {
            if (!quiet) {
                log.warn("Warning: Failed to register MIME types: {}", e.getMessage());
            }
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> renderRoot(Model model) {
        return renderPage(null, model);
    }

    @GetMapping("/{subpath}/**")
    public ResponseEntity<?> renderPage(@PathVariable(required = false) String subpath, Model model) {
        // Normalize the subpath
        String normalizedPath = reader.normalizeSubpath(subpath);
        if (!Objects.equals(normalizedPath, subpath)) {
            return ResponseEntity.status(302)
                    .location(URI.create("/" + (normalizedPath != null ? normalizedPath : "")))
                    .build();
        }

        try {
            // Read the README text or asset
            Object content = reader.read(subpath);

            // Return binary asset
            if (reader.isBinary(subpath)) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(reader.mimetypeFor(subpath)))
                        .body(content);
            }

            // Render the README content
            String renderedContent;
            try {
                renderedContent = renderer.render((String) content, auth);
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode() == 403) {
                    return ResponseEntity.status(403)
                            .contentType(MediaType.TEXT_HTML)
                            .body(renderTemplate("limit", model, Map.of("isAuthenticated", auth != null)));
                }
                throw e;
            } catch (SSLException e) {
                if (e.getMessage() != null && 
                    e.getMessage().contains("TLSV1_ALERT_PROTOCOL_VERSION")) {
                    log.error("Error: GitHub has turned off TLS1.0 support. " +
                              "Please upgrade your Java version or system SSL/TLS support. " +
                              "For more information, see " +
                              "https://github.com/joeyespo/grip/issues/262");
                    return ResponseEntity.status(500).build();
                }
                throw e;
            }

            // Prepare favicon
            String favicon = null;
            if (renderInline) {
                ClassPathResource faviconResource = new ClassPathResource("static/favicon.ico");
                favicon = toDataUrl(faviconResource.getInputStream().readAllBytes(), "image/x-icon");
            }

            // Prepare model attributes
            model.addAttribute("filename", reader.filenameFor(subpath));
            model.addAttribute("title", title);
            model.addAttribute("content", renderedContent);
            model.addAttribute("favicon", favicon);
            model.addAttribute("userContent", renderer.isUserContent());
            model.addAttribute("wideStyle", renderWide);
            model.addAttribute("styleUrls", assets.getStyleUrls());
            model.addAttribute("styles", assets.getStyles());
            model.addAttribute("autorefreshUrl", autorefresh ? 
                    buildUrl(GripConstants.DEFAULT_GRIPURL + "/refresh/" + (subpath != null ? subpath : "")) : null);
            
            // Set theme-related attributes
            model.addAttribute("dataColorMode", theme.equals("dark") ? "dark" : "light");
            model.addAttribute("dataLightTheme", "light");
            model.addAttribute("dataDarkTheme", "dark");

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderTemplate("index", model));

        } catch (ReadmeNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "/";
        }
        // Convert Windows path separators to forward slashes
        path = path.replace('\\', '/');
        // Remove multiple consecutive slashes
        path = path.replaceAll("/+", "/");
        // Remove trailing slashes except root
        path = path.length() > 1 ? path.replaceAll("/$", "") : path;
        return path;
    }

    private ResponseEntity<?> _redirectToSubpath(String subpath) {
        String route = "/" + (subpath != null ? subpath.replaceAll("^/+|/+$", "") : "");
        route = Paths.get(route).normalize().toString();
        return ResponseEntity.status(302)
                .location(URI.create(route))
                .build();
    }

    private ResponseEntity<?> _renderAsset(String subpath) {
        try {
            Path assetPath = Paths.get(assets.getCachePath(), Paths.get(subpath).getFileName().toString());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(Files.readAllBytes(assetPath));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException ex) {
        if (ex.getRawStatusCode() == 403) {
            return renderRateLimitPage(null, new ConcurrentModel());
        }
        throw ex;
    }

    private String buildUrl(String path) {
        return MvcUriComponentsBuilder.fromMethodName(getClass(), "handleRefresh", path).build().toString();
    }

    private ResponseEntity<?> redirectToSubpath(String subpath) {
        String route = normalizePath("/" + (subpath != null ? subpath : ""));
        return ResponseEntity.status(302)
                .location(URI.create(route))
                .build();
    }

    @GetMapping(path = GripConstants.DEFAULT_GRIPURL + "/refresh/**", 
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> handleRefresh(@PathVariable(required = false) String subpath) {
        if (!autorefresh) {
            return ResponseEntity.notFound().build();
        }

        // Normalize subpath
        String normalizedPath = reader.normalizeSubpath(subpath);
        if (!Objects.equals(normalizedPath, subpath)) {
            return redirectToSubpath(normalizedPath);
        }

        SseEmitter emitter = new SseEmitter();
        
        // Check if app is running
        if (shutdownEvent == null || shutdownEvent.get()) {
            emitter.complete();
            return ResponseEntity.ok().body(emitter);
        }

        Thread refreshThread = new Thread(() -> {
            try {
                long lastUpdated = reader.lastUpdated(subpath);
                while (!shutdownEvent.get()) {
                    Thread.sleep(300);

                    // Check for update
                    long updated = reader.lastUpdated(subpath);
                    if (updated == lastUpdated) {
                        continue;
                    }
                    lastUpdated = updated;

                    // Notify user
                    if (!quiet) {
                        log.info("* Change detected in {}, refreshing", reader.filenameFor(subpath));
                    }
                    
                    emitter.send(Map.of("updating", true));

                    // Skip binary assets
                    if (reader.isBinary(subpath)) {
                        continue;
                    }

                    // Read and render content
                    try {
                        String text = (String) reader.read(subpath);
                        String content = renderer.render(text, auth);
                        emitter.send(Map.of("content", content));
                    } catch (ReadmeNotFoundException e) {
                        // Skip if file not found
                        continue;
                    } catch (HttpClientErrorException e) {
                        if (e.getRawStatusCode() == 403) {
                            emitter.completeWithError(e);
                            return;
                        }
                        throw e;
                    }
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();

        return ResponseEntity.ok().body(emitter);
    }

    @GetMapping(GripConstants.DEFAULT_GRIPURL + "/rate-limit-preview")
    public ResponseEntity<String> renderRateLimitPage(
            @RequestParam(required = false) String auth, Model model) {
        boolean isAuth = "1".equals(auth) ? true : this.auth != null;
        model.addAttribute("isAuthenticated", isAuth);
        return ResponseEntity.status(403)
                .contentType(MediaType.TEXT_HTML)
                .body(renderTemplate("limit", model));
    }

    @GetMapping(value = {
        GripConstants.DEFAULT_GRIPURL + "/asset",
        GripConstants.DEFAULT_GRIPURL + "/asset/{subpath}/**"
    })
    public ResponseEntity<byte[]> handleAsset(
            @PathVariable(required = false) String subpath) {
        try {
            Path assetPath = Paths.get(assets.getCachePath(), Paths.get(subpath).getFileName().toString());
            String mimeType = Files.probeContentType(assetPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(Files.readAllBytes(assetPath));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String toDataUrl(byte[] data, String contentType) {
        return String.format("data:%s;base64,%s", 
                contentType, Base64.getEncoder().encodeToString(data));
    }
    
    private String renderTemplate(String template, Model model, Map<String, Object> additionalAttributes) {
        if (additionalAttributes != null) {
            additionalAttributes.forEach(model::addAttribute);
        }
        return renderTemplate(template, model);
    }
    
    private String renderTemplate(String template, Model model) {
        try {
            // Get the template engine from Spring context
            ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
            SpringTemplateEngine templateEngine = applicationContext.getBean(SpringTemplateEngine.class);
            
            // Create context and add model attributes
            Context context = new Context();
            model.asMap().forEach(context::setVariable);
            
            // Render template
            return templateEngine.process(template, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render template: " + template, e);
        }
    }

    private String matchAsset(MatchResult match) {
        String url = match.group(1);
        String ext = url.substring(url.lastIndexOf('.') + 1);
        try {
            byte[] data = downloadBinary(url);
            return String.format("url(%s)", toDataUrl(data, "font/" + ext));
        } catch (Exception e) {
            // Check if it's an SSL error
            Throwable cause = e.getCause();
            if (cause instanceof SSLException) {
                SSLException sslEx = (SSLException) cause;
                if (sslEx.getMessage() != null && 
                    sslEx.getMessage().contains("TLSV1_ALERT_PROTOCOL_VERSION")) {
                    log.error("Error: GitHub has turned off TLS1.0 support. " +
                              "Please upgrade your Java version or system SSL/TLS support. " +
                              "For more information, see " +
                              "https://github.com/joeyespo/grip/issues/262");
                    throw new RuntimeException("SSL Protocol Error", sslEx);
                }
                throw new RuntimeException("SSL Error", sslEx);
            }
            throw new RuntimeException("Failed to download asset: " + url, e);
        }
    }

    private byte[] downloadBinary(String url) {
        try {
            if (url.startsWith("http")) {
                return restTemplate.getForObject(url, byte[].class);
            }
            
            // Use test client for internal URLs
            TestRestTemplate testClient = new TestRestTemplate();
            ResponseEntity<byte[]> response = testClient.getForEntity(
                url, 
                byte[].class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download binary: " + url, e);
        }
    }

    @PostConstruct
    public void init() {
        retrieveStyles();
    }

    public void retrieveStyles() {
        if (stylesRetrieved) {
            return;
        }
        stylesRetrieved = true;

        try {
            assets.retrieveStyles(GripConstants.DEFAULT_GRIPURL + "/asset");
            if (renderInline) {
                inlineStyles();
            }
        } catch (Exception e) {
            if (!quiet) {
                log.error("Error: could not retrieve styles: {}", e.getMessage());
            }
        }
    }

    private void inlineStyles() {
        List<String> styles = getStyles(assets.getStyleUrls(), 
            GripConstants.DEFAULT_GRIPURL + "/asset");
        assets.getStyles().addAll(styles);
        assets.getStyleUrls().clear();
    }

    private String downloadText(String url) {
        try {
            ResponseEntity<String> response;
            if (url.startsWith("http")) {
                response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            } else {
                // Use test client for internal URLs
                TestRestTemplate testClient = new TestRestTemplate();
                response = testClient.exchange(
                    URI.create("http://localhost" + url),
                    HttpMethod.GET, null, String.class);
            }
            
            // Get response encoding from Content-Type header
            Charset charset = Optional.ofNullable(response.getHeaders().getContentType())
                .map(MediaType::getCharset)
                .orElseGet(() -> {
                    // Try to get charset from response body if it's HTML
                    String body = response.getBody();
                    if (body != null && body.contains("<meta charset=")) {
                        Pattern pattern = Pattern.compile("<meta charset=[\"']([^\"']+)[\"']");
                        Matcher matcher = pattern.matcher(body);
                        if (matcher.find()) {
                            try {
                                return Charset.forName(matcher.group(1));
                            } catch (Exception e) {
                                // Ignore charset errors
                            }
                        }
                    }
                    return StandardCharsets.UTF_8;
                });
            
            return new String(response.getBody().getBytes(), charset);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download text: " + url, e);
        }
    }

    private List<String> getStyles(List<String> styleUrls, String assetUrlPath) {
        List<String> styles = new ArrayList<>();
        for (String styleUrl : styleUrls) {
            String urlsInline = String.format(GripConstants.STYLE_ASSET_URLS_INLINE_FORMAT, 
                assetUrlPath.replaceAll("/$", ""));
            String assetContent = downloadText(styleUrl);
            String content = Pattern.compile(urlsInline)
                .matcher(assetContent)
                .replaceAll(this::matchAsset);
            styles.add(content);
        }
        return styles;
    }

    /**
     * Returns the default renderer using the current config.
     * This is only used if renderer is set to None in the constructor.
     */
    private ReadmeRenderer defaultRenderer() {
        return new GitHubRenderer(
            null,                          // userContent
            null,                          // context
            config.getProperty("API_URL"),  // API URL from config
            null                           // raw
        );
    }

    /**
     * Returns the default asset manager using the current config.
     * This is only used if asset_manager is set to None in the constructor.
     */
    protected ReadmeAssetManager defaultAssetManager() {
        String cachePath = null;
        String cacheDirectory = config.getProperty("CACHE_DIRECTORY");
        if (cacheDirectory != null) {
            cacheDirectory = cacheDirectory.replace("{version}", GripConstants.VERSION);
            cachePath = Paths.get(System.getProperty("user.home"), ".grip", cacheDirectory).toString();
        }
        
        // Get style URLs from config
        List<String> styleUrls = new ArrayList<>();
        String configUrls = config.getProperty("STYLE_URLS");
        if (configUrls != null) {
            styleUrls.add(configUrls);  // Add as single URL, not split
        }
        
        return new GitHubAssetManager(cachePath, styleUrls, quiet);
    }

    public void clearCache() {
        assets.clear();
        if (!quiet) {
            log.info("Cache cleared.");
        }
    }

    /**
     * Renders the content at the root path.
     *
     * @return The rendered HTML content
     */
    public String render() {
        return render("/");
    }

    /**
     * Renders the content at the specified route path.
     *
     * @param route The route path to render
     * @return The rendered HTML content
     */
    public String render(String route) {
        if (route == null) {
            route = "/";
        }
        
        @SuppressWarnings("deprecation") // Using TestRestTemplate for test compatibility
        TestRestTemplate testClient = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = testClient.exchange(
            URI.create("http://localhost" + route),
            HttpMethod.GET,
            entity,
            String.class
        );
        
        // Follow redirects (max 10 times)
        int redirectCount = 0;
        while (response.getStatusCode().is3xxRedirection() && 
               response.getHeaders().getLocation() != null &&
               redirectCount < 10) {
            URI redirectUrl = response.getHeaders().getLocation();
            response = testClient.exchange(
                redirectUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            redirectCount++;
        }
        
        // Get response encoding
        Charset charset = Optional.ofNullable(response.getHeaders().getContentType())
            .map(MediaType::getCharset)
            .orElse(StandardCharsets.UTF_8);
        
        return new String(response.getBody().getBytes(), charset);
    }

    /**
     * Test-specific render method that renders content at the specified route.
     * This method is similar to render() but is specifically intended for testing purposes.
     * It uses the configured renderer directly without making HTTP requests.
     *
     * @param route The route path to render for testing
     * @return The rendered HTML content
     * @throws Exception if rendering fails
     */
    public String testRender(String route) throws Exception {
        // Normalize path
        String normalizedPath = reader.normalizeSubpath(route);
        
        // Read content
        Object content = reader.read(normalizedPath);
        
        // Skip binary files
        if (reader.isBinary(normalizedPath)) {
            return null;
        }
        
        // Render content using configured renderer
        return renderer.render((String)content, auth);
    }

    /**
     * Creates a new instance of the specified Grip class.
     * This static factory method is primarily used for testing with mock classes.
     *
     * @param <T> The type of Grip class to create
     * @param gripClass The Class object representing the Grip type to instantiate
     * @return A new instance of the specified Grip class
     * @throws RuntimeException if instantiation fails
     */
    public static <T extends Grip> T createApp(Class<T> gripClass) {
        try {
            return gripClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Grip instance", e);
        }
    }

    public void run() {
        run(null, null, false, null, null);
    }

    public void run(String host, Integer port) {
        run(host, port, false, null, null);
    }

    public void run(String host, Integer port, boolean openBrowser) {
        run(host, port, openBrowser, null, null);
    }

    public void run(Boolean debug, Boolean useReloader) {
        run(null, null, false, debug, useReloader);
    }

    public void run(String host, Integer port, boolean openBrowser, Boolean debug, Boolean useReloader) {
        // Use config values for debug/reloader if null
        boolean effectiveDebug = debug != null ? debug : 
            Boolean.parseBoolean(config.getProperty("DEBUG", "false"));
        boolean effectiveReloader = useReloader != null ? useReloader : 
            Boolean.parseBoolean(config.getProperty("DEBUG_GRIP", "false"));
        
        run(host, port, openBrowser, effectiveDebug, effectiveReloader);
    }

    public boolean isRunning() {
        return !shutdownEvent.get();
    }

    public void shutdown() {
        shutdownEvent.set(true);
        shutdownLatch.countDown();
    }

    private void run(String host, Integer port, boolean openBrowser, 
                   boolean debug, boolean useReloader) {
        // Use config values for host/port if null
        if (host == null) {
            host = config.getProperty("HOST", "localhost");
        }
        if (port == null) {
            port = Integer.parseInt(config.getProperty("PORT", "8080"));
        }

        // Verify server not already running
        runMutex.lock();
        try {
            if (shutdownEvent.get()) {
                throw new AlreadyRunningException();
            }
            shutdownEvent.set(false);
        } finally {
            runMutex.unlock();
        }

        try {
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
            
            // Configure server
            factory.setPort(port);
            factory.setAddress(InetAddress.getByName(host));
            
            // Set debug mode
            if (debug) {
                System.setProperty("debug", "true");
                System.setProperty("logging.level.root", "DEBUG");
                System.setProperty("logging.level.com.grip", "DEBUG");
                if (!quiet) {
                    log.info(" * Debug mode enabled");
                }
            }
            
            // Configure reloader
            if (useReloader) {
                System.setProperty("spring.devtools.restart.enabled", "true");
                System.setProperty("spring.devtools.livereload.enabled", "true");
                if (!quiet) {
                    log.info(" * Reloader active");
                }
            }
            
            // Authentication message
            if (auth != null && !quiet) {
                String authMethod;
                if (auth instanceof Object[]) {
                    Object[] credentials = (Object[]) auth;
                    String username = credentials.length > 0 ? (String)credentials[0] : null;
                    authMethod = username != null ? 
                        "credentials: " + username : 
                        "personal access token";
                } else {
                    authMethod = auth.getClass().getSimpleName();
                }
                log.info("* Using {}", authMethod);
            }
            
            WebServer server = factory.getWebServer();
            
            // Start browser if requested
            Thread browserThread = null;
            if (openBrowser) {
                browserThread = BrowserUtils.startBrowserWhenReady(
                        host != null ? host : "localhost",
                        port != null ? port : 8080,
                        shutdownEvent);
            }

            // Start server
            server.start();

            // Wait for shutdown signal
            shutdownLatch.await();
            
            // Stop server
            server.stop();
            
            // Wait for browser thread
            if (browserThread != null) {
                browserThread.join();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start server", e);
        } finally {
            if (!quiet) {
                log.info(" * Shutting down...");
            }
            shutdownEvent.set(true);
            shutdownLatch.countDown();
        }
    }
}