package com.github.benisraelnir.gripme.cli;

import com.github.benisraelnir.gripme.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Component
@Command(
    name = "gripme",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "Render local markdown files using GitHub API."
)
public class GripCommand implements Callable<Integer> {

    private final ConfigurableApplicationContext context;
    private final GitHubService githubService;

    @Parameters(index = "0", description = "The path to the markdown file or directory", defaultValue = ".")
    private Path path;

    @Option(names = {"-b", "--browser"}, description = "Open a browser window")
    private boolean browser = true;

    @Option(names = {"--user"}, description = "GitHub username for API authentication")
    private String user;

    @Option(names = {"--pass"}, description = "GitHub password/token for API authentication")
    private String pass;

    @Option(names = {"--wide"}, description = "Use wide layout")
    private boolean wide = false;

    @Option(names = {"-p", "--port"}, description = "The port to serve on")
    private int port = 6419;

    @Autowired
    public GripCommand(ConfigurableApplicationContext context, GitHubService githubService) {
        this.context = context;
        this.githubService = githubService;
    }

    @Override
    public Integer call() {
        try {
            if (user != null && pass != null) {
                githubService.setCredentials(user, pass);
            }

            System.setProperty("server.port", String.valueOf(port));
            System.setProperty("grip.wide", String.valueOf(wide));
            System.setProperty("grip.path", path.toString());

            if (browser) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        java.awt.Desktop.getDesktop().browse(
                            new java.net.URI("http://localhost:" + port)
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to open browser: " + e.getMessage());
                    }
                }).start();
            }

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    public Path getPath() { return path; }
    public boolean isBrowser() { return browser; }
    public boolean isWide() { return wide; }
    public int getPort() { return port; }
    public String getUser() { return user; }
    public String getPass() { return pass; }
}
