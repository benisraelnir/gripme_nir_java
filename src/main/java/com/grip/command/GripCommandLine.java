package com.grip.command;

import com.grip.api.GripApi;
import com.grip.exceptions.ReadmeNotFoundException;
import com.grip.GripApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Console;
import java.io.IOException;
import java.net.BindException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of the command-line interface for Grip.
 * Handles command-line argument parsing and execution.
 *
 * Usage:
 *   grip [options] [<path>] [<address>]
 *   grip -V | --version
 *   grip -h | --help
 *
 * Where:
 *   <path> is a file to render or a directory containing README.md (- for stdin)
 *   <address> is what to listen on, of the form <host>[:<port>], or just <port>
 *
 * Options:
 *   --user-content    Render as user-content like comments or issues.
 *   --context=<repo>  The repository context, only taken into account
 *                     when using --user-content.
 *   --user=<username> A GitHub username for API authentication.
 *   --pass=<password> A GitHub password or auth token for API auth.
 *   --wide           Renders wide, i.e. when the side nav is collapsed.
 *   --clear          Clears the cached styles and assets and exits.
 *   --export         Exports to <path>.html or README.md instead of serving.
 *   --no-inline      Link to styles instead inlining when using --export.
 *   -b --browser     Open a tab in the browser after the server starts.
 *   --api-url=<url>  Specify a different base URL for the github API.
 *   --title=<title>  Manually sets the page's title.
 *   --norefresh      Do not automatically refresh the Readme content.
 *   --quiet          Do not print to the terminal.
 *   --theme=<theme>  Theme to view markdown file (light/dark mode).
 */
@Component
public class GripCommandLine implements CommandLineRunner, AutoCloseable {

    private static final String[] VALID_THEME_OPTIONS = {"light", "dark"};
    
    private final boolean forceUtf8;
    private final boolean patchSvg;
    private final ApplicationContext applicationContext;

    @Override
    public void close() {
        if (config != null) {
            config.clearPassword();
        }
    }

    @Autowired
    public GripCommandLine(ApplicationContext applicationContext) {
        this(applicationContext, true, true); // Default values matching Python
    }

    public GripCommandLine(ApplicationContext applicationContext, boolean forceUtf8, boolean patchSvg) {
        this.applicationContext = applicationContext;
        this.forceUtf8 = forceUtf8;
        this.patchSvg = patchSvg;
        
        // Initialize config if not autowired (e.g., in tests)
        if (this.config == null) {
            this.config = new GripCommandLineConfig();
        }
    }

    private static class AddressInfo {
        final String host;
        final Integer port;
        
        AddressInfo(String host, Integer port) {
            this.host = host;
            this.port = port;
        }
    }

    @Autowired
    private GripApi gripApi;

    @Autowired
    private GripCommandLineConfig config;

    private void initMimeTypes() {
        // Patch SVG MIME type for Java 6 and below
        if (System.getProperty("java.version").startsWith("1.6")) {
            URLConnection.setDefaultAllowUserInteraction(false);
            URLConnection.setContentHandlerFactory(null);
            System.setProperty("content.types.user.table", "image/svg+xml svg");
        }
    }

    public static void main(String[] args) {
        // This matches the original Python grip.command.main()
        SpringApplication app = new SpringApplication(GripApplication.class);
        ConfigurableApplicationContext context = app.run(args);
        GripCommandLine commandLine = context.getBean(GripCommandLine.class);
        commandLine.run(args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }

    @Override
    public void run(String[] args) {  // Changed from String... to String[]
        try {
            initializeEnvironment();
            processCommand(args);
            executeCommand();
        } catch (Exception e) {
            handleError(e);
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }
    }

    /**
     * Note: In Python 2, sys.reload() was used to set default encoding to UTF-8.
     * In Java, we use System.setProperty("file.encoding", "UTF-8") which achieves
     * the same goal but in a Java-specific way. This is only needed for Java 8
     * and below, as newer Java versions use UTF-8 by default.
     */
    private void initializeEnvironment() {
        // Force UTF-8 encoding if enabled and running on Java 8 or below
        if (forceUtf8 && System.getProperty("java.version").startsWith("1.")) {
            System.setProperty("file.encoding", "UTF-8");
        }
        
        // Patch SVG MIME type if enabled and running on Java 6
        if (patchSvg && System.getProperty("java.version").startsWith("1.6")) {
            initMimeTypes();
        }
    }

    private void processCommand(String... args) throws IOException {
        // Parse positional arguments first
        String path = null;
        String address = null;
        List<String> positionalArgs = new ArrayList<>();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("-")) {
                positionalArgs.add(arg);
            } else if (arg.startsWith("--context=")) {
                config.setContext(arg.substring("--context=".length()));
            } else if (arg.startsWith("--api-url=")) {
                config.setApiUrl(arg.substring("--api-url=".length()));
            } else if (arg.startsWith("--title=")) {
                config.setTitle(arg.substring("--title=".length()));
            } else if (arg.startsWith("--user=")) {
                config.setUser(arg.substring("--user=".length()));
            } else if (arg.startsWith("--pass=")) {
                config.setPassword(arg.substring("--pass=".length()).toCharArray());
            } else if (arg.startsWith("--theme=")) {
                config.setTheme(arg.substring("--theme=".length()));
            } else if ("--user-content".equals(arg)) {
                config.setUserContent(true);
            } else if ("--wide".equals(arg)) {
                config.setWide(true);
            } else if ("--clear".equals(arg)) {
                config.setClear(true);
            } else if ("--export".equals(arg)) {
                config.setExport(true);
            } else if ("--no-inline".equals(arg)) {
                config.setNoInline(true);
            } else if ("-b".equals(arg) || "--browser".equals(arg)) {
                config.setBrowser(true);
            } else if ("--norefresh".equals(arg)) {
                config.setNorefresh(true);
            } else if ("--quiet".equals(arg)) {
                config.setQuiet(true);
            }
        }
        
        // Map positional arguments to path and address
        if (positionalArgs.size() >= 1) {
            path = positionalArgs.get(0);
        }
        if (positionalArgs.size() >= 2) {
            address = positionalArgs.get(1);
        }
        
        // Validate positional arguments
        validatePositionalArgs(path, address);
        
        // Update config with positional arguments
        if (path != null) {
            config.setPath(path);
        }
        if (address != null) {
            config.setAddress(address);
        }

        // Show specific errors for deprecated flags
        if (Arrays.asList(args).contains("-a") || Arrays.asList(args).contains("--address")) {
            System.err.println("Use grip [options] <path> <address> instead of -a");
            System.err.println("See grip -h for details");
            System.exit(SpringApplication.exit(applicationContext, () -> 2));
        }
        if (Arrays.asList(args).contains("-p") || Arrays.asList(args).contains("--port")) {
            System.err.println("Use grip [options] [<path>] [<hostname>:]<port> instead of -p");
            System.err.println("See grip -h for details");
            System.exit(SpringApplication.exit(applicationContext, () -> 2));
        }

        // Handle version flag
        if (Arrays.asList(args).contains("-V") || Arrays.asList(args).contains("--version")) {
            System.out.println(getVersion());
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        }

        // Handle help flag
        if (Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("--help")) {
            printHelp();
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        }

        // Handle clear cache
        if (config.isClear()) {
            gripApi.clearCache();
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        }

        // Get password from console if user is set but password isn't
        handlePassword();

        // Validate theme
        String theme = config.getTheme();
        if (theme != null && !Arrays.asList(VALID_THEME_OPTIONS).contains(theme)) {
            System.err.println("Error: valid options for theme argument are \"light\", \"dark\"");
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }

        // Remove export mode logic as it's now in executeCommand()

        // Validate address for server mode
        if (!config.isExport()) {
            AddressInfo addressInfo = resolveAddress(config.getPath(), config.getAddress());
            if (config.getAddress() != null && addressInfo.host == null && addressInfo.port == null) {
                System.err.println("Error: Invalid address " + config.getAddress());
                System.exit(SpringApplication.exit(applicationContext, () -> 1));
            }
        }
    }

    /**
     * Execute the command based on the processed configuration.
     * This method handles the actual execution of either export or server mode.
     */
    /**
     * Execute the command based on the processed configuration.
     * This method handles the actual execution of either export or server mode.
     * Password arrays are cleared after use for security.
     *
     * @throws IOException if there is an error executing the command
     */
    private void executeCommand() throws IOException {
        char[] password = null;
        try {
            password = config.getPassword();
            String theme = config.getTheme();

            // Export mode
            if (config.isExport()) {
                try {
                    gripApi.export(
                        config.getPath(),
                        config.isUserContent(),
                        config.getContext(),
                        config.getUser(),
                        password,  // Using char[] directly
                        false, // renderOffline
                        config.isWide(),
                        !config.isNoInline(),
                        config.getAddress(),
                        config.getApiUrl(),
                        config.getTitle(),
                        config.isQuiet(),
                        theme != null ? theme : "light",
                        null // gripClass
                    );
                } catch (ReadmeNotFoundException e) {
                    throw new IOException("README not found: " + e.getMessage());
                }
            }
            // Server mode
            else {
                AddressInfo addressInfo = resolveAddress(config.getPath(), config.getAddress());
                try {
                    gripApi.serve(
                        config.getPath(),
                        addressInfo.host,
                        addressInfo.port,
                        config.isUserContent(),
                        config.getContext(),
                        config.getUser(),
                        password,  // Using char[] directly
                        false, // renderOffline
                        config.isWide(),
                        false, // renderInline
                        config.getApiUrl(),
                        config.getTitle(),
                        !config.isNorefresh(),
                        config.isBrowser(),
                        config.isQuiet(),
                        theme != null ? theme : "light",
                        null // gripClass
                    );
                } catch (Exception e) {
                    if (e.getCause() instanceof BindException) {
                        throw new IOException("Address already in use. Is a grip server already running? " +
                                            "Stop that instance or specify another port.");
                    }
                    throw e;
                }
            }
        } finally {
            // Clear password array for security
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private void handlePassword() {
        char[] password = config.getPassword();
        try {
            if (config.getUser() != null && password == null) {
                Console console = System.console();
                if (console == null) {
                    throw new IllegalStateException("No console available for password input");
                }
                password = console.readPassword("Enter password for %s: ", config.getUser());
                config.setPassword(password.clone());
            }
        } finally {
            if (password != null) {
                Arrays.fill(password, ' ');
            }
        }
    }

    private void handleError(Exception e) {
        if (!config.isQuiet()) {
            System.err.println("Error: " + e.getMessage());
            
            if (e instanceof ReadmeNotFoundException) {
                System.err.println("Could not find a README.md file in the specified path.");
            } else if (e instanceof IOException) {
                if (e.getCause() instanceof BindException) {
                    System.err.println("The specified port is already in use. Please try a different port.");
                } else {
                    System.err.println("An I/O error occurred: " + e.getMessage());
                }
            } else if (e instanceof IllegalArgumentException) {
                System.err.println("Invalid argument: " + e.getMessage());
            }
            
            // Print stack trace for unexpected errors in debug mode
            String debugEnv = System.getenv("GRIP_DEBUG");
            if (debugEnv != null && debugEnv.equals("1")) {
                e.printStackTrace();
            }
        }
    }

    private AddressInfo resolveAddress(String path, String address) {
        // If no address specified, return default
        if (address == null) {
            return new AddressInfo(null, null);
        }
        
        return splitAddress(address);
    }

    private void validatePositionalArgs(String path, String address) {
        if (path != null && path.startsWith("-") && !"-".equals(path)) {
            System.err.println("Error: Invalid path argument");
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }
        
        if (address != null) {
            AddressInfo addressInfo = splitAddress(address);
            if (addressInfo.host == null && addressInfo.port == null) {
                System.err.println("Error: Invalid address " + address);
                System.exit(SpringApplication.exit(applicationContext, () -> 1));
            }
        }
    }

    private AddressInfo splitAddress(String address) {
        if (address == null) {
            return new AddressInfo(null, null);
        }
        
        String host = null;
        Integer port = null;
        
        if (address.contains(":")) {
            String[] parts = address.split(":", 2);
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number: " + parts[1]);
            }
        } else {
            try {
                port = Integer.parseInt(address);
            } catch (NumberFormatException e) {
                host = address;
            }
        }
        
        return new AddressInfo(host, port);
    }

    private String getVersion() {
        return "Grip " + config.getVersion();
    }

    /**
     * Prints the help text to System.out.
     * Protected for testing purposes.
     */
    protected void printHelp() {
        System.out.println(
            "Usage:\n" +
            "  grip [options] [<path>] [<address>]\n" +
            "  grip -V | --version\n" +
            "  grip -h | --help\n\n" +
            "Where:\n" +
            "  <path> is a file to render or a directory containing README.md (- for stdin)\n" +
            "  <address> is what to listen on, of the form <host>[:<port>], or just <port>\n\n" +
            "Options:\n" +
            "  --user-content    Render as user-content like comments or issues.\n" +
            "  --context=<repo>  The repository context, only taken into account\n" +
            "                    when using --user-content.\n" +
            "  --user=<username> A GitHub username for API authentication. If used\n" +
            "                    without the --pass option, an upcoming password\n" +
            "                    input will be necessary.\n" +
            "  --pass=<password> A GitHub password or auth token for API auth.\n" +
            "  --wide           Renders wide, i.e. when the side nav is collapsed.\n" +
            "                    This only takes effect when --user-content is used.\n" +
            "  --clear          Clears the cached styles and assets and exits.\n" +
            "  --export         Exports to <path>.html or README.md instead of\n" +
            "                    serving, optionally using [<address>] as the out\n" +
            "                    file (- for stdout).\n" +
            "  --no-inline      Link to styles instead inlining when using --export.\n" +
            "  -b --browser     Open a tab in the browser after the server starts.\n" +
            "  --api-url=<url>  Specify a different base URL for the github API,\n" +
            "                    for example that of a Github Enterprise instance.\n" +
            "                    Default is the public API: https://api.github.com\n" +
            "  --title=<title>  Manually sets the page's title.\n" +
            "                    The default is the filename.\n" +
            "  --norefresh      Do not automatically refresh the Readme content when\n" +
            "                    the file changes.\n" +
            "  --quiet          Do not print to the terminal.\n" +
            "  --theme=<theme>  Theme to view markdown file (light mode or dark mode).\n" +
            "                    Valid options (\"light\", \"dark\"). Default: \"light\""
        );
    }
}